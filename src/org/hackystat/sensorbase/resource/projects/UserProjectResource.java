package org.hackystat.sensorbase.resource.projects;

import java.util.ArrayList;
import java.util.List;

import org.hackystat.utilities.stacktrace.StackTrace;
import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * The resource for processing GET/PUT/DELETE host/projects/{email}/{projectname}.
 * Returns a representation of the Project resource associated with this user. 
 * 
 * @author Philip Johnson
 */
public class UserProjectResource extends SensorBaseResource {
  
  /** The user, or null if the uriUser does not name a defined User. */
  private User user; 
  /** To be retrieved from the URL. */
  private String projectName;
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UserProjectResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.projectName = (String) request.getAttributes().get("projectname");
    this.user = super.userManager.getUser(uriUser);
  }
  
  /**
   * Returns an XML representation of the Project associated with this User.
   * <ul>
   * <li> The uriUser must be defined as a User.
   * <li> The Project must be defined for this User.
   * <li> The authenticated user must be the admin, or uriUser, or a member of the project, or 
   * invited to be in the Project.
   * </ul>
   * 
   * @param variant The representational variant requested, or null if conditions are violated.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    // The uriUser must be a defined User.
    if (this.user == null) {
      String msg = "No user corresponding to: " + this.uriUser;
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return null;
    }
    // The named project must be defined.
    if (!super.projectManager.hasProject(this.user, this.projectName)) {
      String msg = "No Project named " + this.projectName + " for user " + this.uriUser;
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return null;
    }    
    // The authorized user must be an admin, or the project owner, or a member, or invitee.
    if (!super.userManager.isAdmin(this.authUser) && !this.uriUser.equals(this.authUser) &&
        !super.projectManager.isMember(this.user, this.projectName, this.authUser) &&
        !super.projectManager.isInvited(this.user, this.projectName, this.authUser)) {
      String msg = "User " + this.authUser + "is not authorized to view this Project.";
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return null;
    }
    // It's all good, so return the Project representation.
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      try {
        String xmlData = super.projectManager.getProjectString(this.user, this.projectName);
        return super.getStringRepresentation(xmlData);
      }
      catch (Exception e) {
        String msg = "Couldn't marshall project " + this.projectName + " into XML.";
        getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, msg);
        return null;
      }
    }
    return null;
  }
  
  /** 
   * Indicate the PUT method is supported. 
   * @return True.
   */
  @Override
  public boolean allowPut() {
      return true;
  }

  /**
   * Implement the PUT method that creates a new Project or updates an existing Project.
   * <ul>
   * <li> The XML must be marshallable into a Project instance using the Project XmlSchema.
   * <li> The User must exist.
   * <li> The Project name in the URI string must match the Project name in the XML.
   * <li> The authenticated user must be the uriUser or the admin.
   * <li> The project cannot be the Default project.   
   * <li> All members in the new project representation must have been members previously.
   * <li> All Members and Invitees must be defined Users.
   * <li> No Invitee can be a Member.
   * <li> The project owner cannot be a Member or Invitee.
   * </ul>
   * @param entity The XML representation of the new Project.
   */
  @Override
  public void put(Representation entity) {
    // Error if uriUser is not defined.
    if (this.user == null) {
      String msg = "Bad PUT: No user corresponding to: " + this.uriUser;
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return;
    }  
    // Error if authorized User is not the admin or the uriUser.
    if (!super.userManager.isAdmin(this.authUser) && !this.uriUser.equals(this.authUser)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, super.badAuth);
      return;
    }
    String entityString = null;
    Project newProject;
    // Try to make the XML payload into a Project, return failure if this fails. 
    try { 
      entityString = entity.getText();
      newProject = super.projectManager.makeProject(entityString);
    }
    catch (Exception e) {
      server.getLogger().warning("Bad Project in PUT: " + StackTrace.toString(e));
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad Project: " + entityString);
      return;
    }
    // Error if the URI ProjectName is not the same as the XML Project name.
    if (!(this.projectName.equals(newProject.getName()))) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Different URI/XML project names");
      return;
    }
    // Error if the project is the Default project.
    if (this.projectName.equals(ProjectManager.DEFAULT_PROJECT_NAME)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Cannot PUT the Default project");
      return;
    }
    // Error if the uriUser is not the Admin or the Project Owner
    if (!super.userManager.isAdmin(this.uriUser) && !this.uriUser.equals(newProject.getOwner())) {
      String msg = "User " + user.getEmail() + " is not the admin or Project Owner.";
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return;
    }
    
    // Get or create a list of new members (possibly empty).
    List<String> newMembers = ((newProject.getMembers() == null) ? new ArrayList<String>()
        : newProject.getMembers().getMember());

    // Error if oldProject exists and new one contains a member not in old one.
    for (String newMember : newMembers) {
      if (!super.projectManager.isMember(this.user, this.projectName, newMember)) {
        String msg = "New project contains non-member: " + newMember;
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
        return;
      }
    }
    
    // Get a (possibly empty) list of the invitees.
    List<String> newInvitees = ((newProject.getInvitations() == null) ? 
        new ArrayList<String>() : newProject.getInvitations().getInvitation());

    // Make sure all newMembers and newInvitees are defined Users.
    for (String member : newMembers) {
      if (!super.userManager.isUser(member)) {
        String msg = "Member is not a registered user: " + member;
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
        return;
      }
    }
    for (String invitee : newInvitees) {
      if (!super.userManager.isUser(invitee)) {
        String msg = "Invited member is not a registered user: " + invitee;
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
        return;
      }
    }
    // No invitee can be a (new) member.
    for (String invitee : newInvitees) {
      if (newMembers.contains(invitee)) {
        String msg = "Invited member cannot already be a member: " + invitee;
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
        return;
      }
    }
    // Project owner cannot be a member or invitee.
    if (newMembers.contains(this.uriUser)) {
      String msg = "Project owner cannot also be a member: " + this.uriUser;
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return;
    }
    if (newInvitees.contains(this.uriUser)) {
      String msg = "Project owner cannot also be an invited member: " + this.uriUser;
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return;
    }

    // otherwise we add it to the Manager and return success.
    super.projectManager.putProject(newProject);      
    getResponse().setStatus(Status.SUCCESS_CREATED);
  }
  
  /** 
   * Indicate the DELETE method is supported. 
   * @return True.
   */
  @Override
  public boolean allowDelete() {
      return true;
  }
  
  /**
   * Implement the DELETE method that deletes an existing Project for a given User.
   * <ul> 
   * <li> The User must be currently defined.
   * <li> The authenticated user must be the uriUser or the Admin. 
   * <li> The User must be the admin or the Owner.
   * </ul>
   * If the Project doesn't exist, that's fine, it's still "deleted".
   */
  @Override
  public void delete() {
    //  If this User does not exist, return an error.
    if (this.user == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user: " + this.uriUser);
      return;
    } 
    if (!super.userManager.isAdmin(this.authUser) && !this.uriUser.equals(this.authUser)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, super.badAuth);
      return;
    }    
    if (!super.userManager.isAdmin(this.authUser) && 
        !super.projectManager.hasProject(this.user, this.projectName)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "User is not Owner or Admin.");
      return;
    }
    // Otherwise, delete it and return successs.
    super.projectManager.deleteProject(this.user, this.projectName);      
    getResponse().setStatus(Status.SUCCESS_OK);
  }
}
