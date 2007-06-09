package org.hackystat.sensorbase.resource.projects;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.users.UserManager;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

/**
 * The resource for processing GET host/projects/{email}/{projectname}.
 * Returns a representation of the Project resource associated with this user. 
 * 
 * @author Philip Johnson
 */
public class UserProjectResource extends Resource {
  
  /** To be retrieved from the URL. */
  private String email;
  /** The user, or null if 'email' does not represent a User. */
  private User user; 
  /** To be retrieved from the URL. */
  private String projectName;
  /** The Project Manager. */
  private ProjectManager projectManager;
  /** The User Manager. */
  private UserManager userManager;
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UserProjectResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.email = (String) request.getAttributes().get("email");
    this.projectName = (String) request.getAttributes().get("projectname");
    this.projectManager = (ProjectManager)getContext().getAttributes().get("ProjectManager");
    this.userManager = (UserManager)getContext().getAttributes().get("UserManager");
    this.user = userManager.getUser(email);
    getVariants().clear(); // copied from BookmarksResource.java, not sure why needed.
    getVariants().add(new Variant(MediaType.TEXT_XML));
  }
  
  /**
   * Returns an XML representation of the Project associated with this User.
   * 
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    // Return an error code if we can't find a User for this email.
    if (this.user == null) {
      String msg = "No user corresponding to: " + this.email;
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return null;
    }
    // Return an error code if we can't find the named Project for this User. 
    if (!projectManager.hasProject(this.user, this.projectName)) {
      String msg = "No Project named " + this.projectName + " for user " + this.email;
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return null;
    }    
    // Have a user and a project, so proceed.
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      try {
        return new DomRepresentation(MediaType.TEXT_XML, 
            projectManager.getProjectDocument(this.user, this.projectName));
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
   * <li> The User must be the Owner.
   * <li> All members must be defined as Users. 
   * </ul>
   * This implementation does not yet require members to agree to Project participation. 
   * @param entity The XML representation of the new Project.
   */
  @Override
  public void put(Representation entity) {
    // If this User does not exist, return an error.
    if (this.user == null) {
      String msg = "Bad PUT: No user corresponding to: " + this.email;
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
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
    // Return failure if the User is not the Owner
    if (!this.email.equals(project.getOwner())) {
      String msg = "User " + user.getEmail() + " is not the Project Owner: " + project.getOwner();
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return;
    }
    // Return failure if any Members are not Users.
    if (project.getMembers() != null) {
      for (String memberKey : project.getMembers().getMember()) {
        if (!userManager.hasUser(memberKey)) {
          String msg = "Undefined member: " + memberKey;
          getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg); 
          return;
        }
      }
    }

    // otherwise we add it to the Manager and return success.
    projectManager.putProject(project);      
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
   * <li> The User must be the owner.
   * </ul>
   * If the Project doesn't exist, that's fine, it's still "deleted".
   */
  @Override
  public void delete() {
    //  If this User does not exist, return an error.
    if (this.user == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user: " + this.email);
      return;
    } 
    
    if (!projectManager.isOwner(this.user, this.projectName)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "User is not Owner.");
      return;
    }
    // Otherwise, delete it and return successs.
    projectManager.deleteProject(this.user, this.projectName);      
    getResponse().setStatus(Status.SUCCESS_OK);
  }
  

}
