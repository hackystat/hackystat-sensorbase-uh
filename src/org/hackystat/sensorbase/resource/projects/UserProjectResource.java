package org.hackystat.sensorbase.resource.projects;

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
    ProjectManager manager = 
      (ProjectManager)getContext().getAttributes().get("ProjectManager");
    if (manager == null) {
      throw new RuntimeException("Failed to find ProjectManager");
    }
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      // If this User/Project pair does not exist, return an error.
      if (!manager.hasProject(this.userKey, this.projectName)) {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No project");
        return null;
      }
      // Otherwise return the Project representation. 
      try {
        result = new DomRepresentation(MediaType.TEXT_XML, 
            manager.getProjectDocument(this.userKey, this.projectName));
      }
      catch (Exception e) {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Problems marshalling");
        return null;
      }
      }
    return result;
  }
  

}
