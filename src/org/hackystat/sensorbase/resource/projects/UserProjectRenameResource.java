package org.hackystat.sensorbase.resource.projects;

import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
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
    this.newProjectName = (String) request.getAttributes().get("newprojectname");
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
  public Representation represent(Variant variant) {
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
  public void acceptRepresentation(Representation entity) {
   
    if (!validateUriUserIsUser() ||
        !validateUriProjectName() ||
        !validateProjectOwner()) {
      return;
    }
    
    // Cannot rename the default project.
    if (ProjectManager.DEFAULT_PROJECT_NAME.equals(this.projectName)) {
      setStatusMiscError("Cannot rename the default project.");
      return;
    }
    
    // Cannot rename a project to the name of an already existing project.
    if (super.projectManager.getProject(this.user, newProjectName) != null) {
      setStatusMiscError(String.format("Project %s already exists.", newProjectName));
      return;
    }

    // Now we tell the project manager to attempt to rename the project and return.
    // The renameProject method is synchronized for thread safety. 
    try {
      super.projectManager.renameProject(this.user, projectName, newProjectName);
      getResponse().setStatus(Status.SUCCESS_OK);
    }
    catch (Exception e) {
      setStatusInternalError(e);
    }
  }
}
