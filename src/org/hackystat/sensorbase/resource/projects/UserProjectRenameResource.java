package org.hackystat.sensorbase.resource.projects;

import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.ResponseMessage;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * The resource for processing POST host/projects/{email}/{projectname}/rename/{newprojectname}.
 * 
 * @author Philip Johnson
 */
public class UserProjectRenameResource extends SensorBaseResource {
  /** The user, or null if the uriUser does not name a defined User. */
  private User user; 
  /** To be retrieved from the URL. */
  private String projectName;
  /** To be retrieved from the URL; the new name. */
  private String newProjectName;
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UserProjectRenameResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.projectName = (String) request.getAttributes().get("projectname");
    this.newProjectName = (String) request.getAttributes().get("newprojectname");
    this.user = super.userManager.getUser(uriUser);
  }
  
  /** 
   * Indicate the GET method is not supported.
   * @return False.
   */
  @Override
  public boolean allowGet() {
    return false;
  }
  
  /**
   * Returns nothing since GET is not supported.
   * 
   * @param variant The representational variant requested, or null if conditions are violated.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    return null;
  }
  
  /** 
   * Indicate the POST method is supported. 
   * @return True.
   */
  @Override
  public boolean allowPost() {
    return true;
  }

  /**
   * Implement the POST method that processes a project rename command. 
   * <ul>
   * <li> UriUser must be a defined user, and user/project must be a defined project.
   * <li> The authorized user must be the project owner. 
   * </ul>
   * @param entity ignored.
   */
  @Override
  public void post(Representation entity) {
    // Error if uriUser is not defined.
    if (this.user == null) {
      this.responseMsg = ResponseMessage.undefinedUser(this, this.uriUser);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return;
    }  
    
    // Error if project is not defined.
    Project project = super.projectManager.getProject(this.user, this.projectName);
    if (project == null) {
      this.responseMsg = ResponseMessage.undefinedProject(this, this.user, this.projectName);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return;
    }
    
    // Make sure that authorized user is the project owner. 
    if (!project.getOwner().equals(this.authUser)) {
      this.responseMsg = ResponseMessage.notProjectOwner(this, this.authUser);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return;
    }
    
    // Cannot rename the default project.
    if (ProjectManager.DEFAULT_PROJECT_NAME.equals(this.projectName)) {
      String msg = "Error: cannot rename the default project.";
      this.responseMsg = ResponseMessage.miscError(this, msg);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return;
    }

    // Now we tell the project manager to attempt to rename the project and return.
    // This could fail if the project already exists.  The renameProject method is synchronized
    // for thread safety. 
    try {
      super.projectManager.renameProject(this.user, projectName, newProjectName);
    }
    catch (Exception e) {
      this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return;
    }
    
    // We got here, so everything is cool.
    getResponse().setStatus(Status.SUCCESS_OK);
  
  }
  
}
