package org.hackystat.sensorbase.resource.projects;

import java.util.ArrayList;
import java.util.List;

import org.hackystat.utilities.stacktrace.StackTrace;
import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.projects.jaxb.UriPatterns;
import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.ResponseMessage;
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
   * invited to be in the Project, or a spectator.
   * </ul>
   * 
   * @param variant The representational variant requested, or null if conditions are violated.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    // The uriUser must be a defined User.
    if (this.user == null) {
      this.responseMsg = ResponseMessage.undefinedUser(this, this.uriUser);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return null;
    }
    // The named project must be defined.
    if (!super.projectManager.hasProject(this.user, this.projectName)) {
      this.responseMsg = ResponseMessage.undefinedProject(this, this.user, this.projectName);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return null;
    }    
    // The authorized user must be an admin, or the project owner, or a member, or invitee.
    if (!super.userManager.isAdmin(this.authUser) && !this.uriUser.equals(this.authUser) &&
        !super.projectManager.isMember(this.user, this.projectName, this.authUser) &&
        !super.projectManager.isInvited(this.user, this.projectName, this.authUser) &&
        !super.projectManager.isSpectator(this.user, this.projectName, this.authUser)) {
      String msg = String.format("User %s not authorized to view project %s", this.authUser, 
          this.projectName);
      this.responseMsg = ResponseMessage.miscError(this, msg);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return null;
    }
    // It's all good, so return the Project representation.
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      try {
        String xmlData = super.projectManager.getProjectString(this.user, this.projectName);
        return super.getStringRepresentation(xmlData);
      }
      catch (Exception e) {
        this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
        getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
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
   * <li> The project representation must include a name, owner, start time, and end time.
   * <li> The Project name in the URI string must match the Project name in the XML.
   * <li> The authenticated user must be the uriUser or the admin.
   * <li> The project cannot be the Default project.   
   * <li> All members in the new project representation must have been members previously.
   * <li> All Members, Invitees, and Spectators must be defined Users.
   * <li> The project owner cannot be a Member or Invitee or Spectator.
   * <li> No Invitee can be a Member or Spectator.
   * <li> No Spectator can be a Member or Invitee.
   * <li> If no UriPatterns are supplied, then the '*' UriPattern is provided by default.
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
      String msg = "Illegal Project definition. Not all required fields are supplied and/or have" +
      " correct values: ";
      server.getLogger().warning(msg + StackTrace.toString(e));
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg + entityString);
      return;
    }
    // Error if the Project name, owner, start date, or end date is not supplied.
    if ((newProject.getName() == null) || (newProject.getName().trim().equals(""))) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Project name must be supplied.");
      return;
    }
    if ((newProject.getOwner() == null) || (newProject.getOwner().trim().equals(""))) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Project owner must be supplied.");
      return;
    }
    if (newProject.getStartTime() == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
          "Project start time must be supplied.");
      return;
    }
    if (newProject.getEndTime() == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
          "Project end time must be supplied.");
      return;
    }
    // Error if the URI ProjectName is not the same as the XML Project name.
    if (!(this.projectName.equals(newProject.getName()))) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Different URI/XML project names");
      return;
    }
    // Error if the project is the Default project.
    if (this.projectName.equals(ProjectManager.DEFAULT_PROJECT_NAME)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Cannot modify the Default project");
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
    
    // Get a (possibly empty) list of spectators.
    List<String> newSpectators = ((newProject.getSpectators() == null) ? 
        new ArrayList<String>() : newProject.getSpectators().getSpectator());

    // Make sure all newMembers, newInvitees, and newSpecators are defined Users.
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
    for (String spectator : newSpectators) {
      if (!super.userManager.isUser(spectator)) {
        String msg = "Spectator is not a registered user: " + spectator;
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
    // Spectator cannot be a project owner.
    if (newSpectators.contains(this.uriUser)) {
      String msg = "Project owner cannot also be a specator: " + this.uriUser;
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return;
    }
    // No spectator can be a (new) member.
    for (String spectator : newSpectators) {
      if (newMembers.contains(spectator)) {
        String msg = "Spectator cannot also be a member: " + spectator;
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
        return;
      }
    }
    // No spectator can be an invitee.
    for (String spectator : newSpectators) {
      if (newInvitees.contains(spectator)) {
        String msg = "Spectator cannot also be an invitee: " + spectator;
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
        return;
      }
    }
    
    // Make sure there are UriPatterns. If not, provide a default of "*".
    if (newProject.getUriPatterns() == null) {
      newProject.setUriPatterns(new UriPatterns());
    }
    if (newProject.getUriPatterns().getUriPattern().isEmpty()) {
      newProject.getUriPatterns().getUriPattern().add("*");
    }

    // if we're still here, add it to the Manager and return success.
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
   * <li> The project name must not be "Default".
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
    if ("Default".equals(this.projectName)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Cannot delete Default project.");
      return;
    }    
    // Otherwise, delete it and return successs.
    super.projectManager.deleteProject(this.user, this.projectName);      
    getResponse().setStatus(Status.SUCCESS_OK);
  }
}
