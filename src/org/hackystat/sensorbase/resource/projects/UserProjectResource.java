package org.hackystat.sensorbase.resource.projects;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * The resource for processing GET host/projects/{email}/{projectname}.
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
   * <li> The authenticated user must be the admin or uriUser.
   * <li> The Project must be defined for this User.
   * </ul>
   * 
   * @param variant The representational variant requested, or null if conditions are violated.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    if (this.user == null) {
      String msg = "No user corresponding to: " + this.uriUser;
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return null;
    }
    if (!super.userManager.isAdmin(this.uriUser) && !this.uriUser.equals(this.authUser)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, super.badAuth);
      return null;
    }
    if (!super.projectManager.hasProject(this.user, this.projectName)) {
      String msg = "No Project named " + this.projectName + " for user " + this.uriUser;
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return null;
    }    
    // Have a user and a project, so proceed.
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      try {
        return new DomRepresentation(MediaType.TEXT_XML, 
            super.projectManager.getProjectDocument(this.user, this.projectName));
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
   * <li> All members must be defined as Users. 
   * </ul>
   * This implementation does not yet require members to agree to Project participation. 
   * @param entity The XML representation of the new Project.
   */
  @Override
  public void put(Representation entity) {
    if (this.user == null) {
      String msg = "Bad PUT: No user corresponding to: " + this.uriUser;
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return;
    }  
    if (!super.userManager.isAdmin(this.uriUser) && !this.uriUser.equals(this.authUser)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, super.badAuth);
      return;
    }
    String entityString = null;
    Project project;
    // Try to make the XML payload into an SDT, return failure if this fails. 
    try { 
      entityString = entity.getText();
      project = ProjectManager.unmarshallProject(entityString);
    }
    catch (Exception e) {
      SensorBaseLogger.getLogger().warning("Bad Project in PUT: " + StackTrace.toString(e));
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad Project: " + entityString);
      return;
    }
    // Return failure if the URI ProjectName is not the same as the XML SdtName.
    if (!(this.projectName.equals(project.getName()))) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Different URI/XML project names");
      return;
      
    }
    // Return failure if the User is not the Admin or the Project Owner
    if (!super.userManager.isAdmin(this.uriUser) && !this.uriUser.equals(project.getOwner())) {
      String msg = "User " + user.getEmail() + " is not the admin or Project Owner.";
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return;
    }
    // Return failure if any Members are not Users.
    if (project.getMembers() != null) {
      for (String memberKey : project.getMembers().getMember()) {
        if (!super.userManager.hasUser(memberKey)) {
          String msg = "Undefined member: " + memberKey;
          getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg); 
          return;
        }
      }
    }

    // otherwise we add it to the Manager and return success.
    super.projectManager.putProject(project);      
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
    if (!super.userManager.isAdmin(this.uriUser) && !this.uriUser.equals(this.authUser)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, super.badAuth);
      return;
    }    
    if (!super.userManager.isAdmin(this.uriUser) && 
        !super.projectManager.isOwner(this.user, this.projectName)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "User is not Owner or Admin.");
      return;
    }
    // Otherwise, delete it and return successs.
    super.projectManager.deleteProject(this.user, this.projectName);      
    getResponse().setStatus(Status.SUCCESS_OK);
  }
}
