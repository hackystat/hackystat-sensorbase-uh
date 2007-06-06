package org.hackystat.sensorbase.resource.projects;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.users.UserManager;
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
 * The resource for processing GET host/projects/{userkey}/{projectname}.
 * Returns a representation of the Project resource associated with this user. 
 * 
 * @author Philip Johnson
 */
public class UserProjectResource extends Resource {
  
  /** To be retrieved from the URL. */
  private String userKey;
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
    this.userKey = (String) request.getAttributes().get("userkey");
    this.projectName = (String) request.getAttributes().get("projectname");
    this.projectManager = (ProjectManager)getContext().getAttributes().get("ProjectManager");
    this.userManager = (UserManager)getContext().getAttributes().get("UserManager");
    getVariants().clear(); // copyied from BookmarksResource.java, not sure why needed.
    getVariants().add(new Variant(MediaType.TEXT_XML));
  }
  
  /**
   * Returns a ProjectIndex of all projects associated with this UserKey.
   * 
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    Representation result = null;
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      // If this User/Project pair does not exist, return an error.
      if (!projectManager.hasProject(this.userKey, this.projectName)) {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No project");
        return null;
      }
      // Otherwise return the Project representation. 
      try {
        result = new DomRepresentation(MediaType.TEXT_XML, 
            projectManager.getProjectDocument(this.userKey, this.projectName));
      }
      catch (Exception e) {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Problems marshalling");
        return null;
      }
      }
    return result;
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
   * </ul>
   * This implementation does not yet require members to agree to Project participation. 
   * @param entity The XML representation of the new Project.
   */
  @Override
  public void put(Representation entity) {
    // If this User does not exist, return an error.
    if (!userManager.hasUser(this.userKey)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user");
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
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "URI/XML name mismatch");
      return;
      
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
   * </ul>
   * If the Project doesn't exist, that's fine, it's still "deleted".
   */
  @Override
  public void delete() {
    //  If this User does not exist, return an error.
    if (!userManager.hasUser(this.userKey)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user");
      return;
    }  
    // Otherwise, delete it and return successs.
    projectManager.deleteProject(this.userKey, this.projectName);      
    getResponse().setStatus(Status.SUCCESS_OK);
  }
  

}
