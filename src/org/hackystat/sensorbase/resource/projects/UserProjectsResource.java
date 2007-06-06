package org.hackystat.sensorbase.resource.projects;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

/**
 * The resource for processing GET host/projects/{userkey}.
 * Returns an index of all Project resource names associated with this user. 
 * 
 * @author Philip Johnson
 */
public class UserProjectsResource extends Resource {
  
  /** To be retrieved from the URL. */
  private String userKey;
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UserProjectsResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.userKey = (String) request.getAttributes().get("userkey");
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
      result = new DomRepresentation(MediaType.TEXT_XML, 
          manager.getProjectIndexDocument(this.userKey));
      }
    return result;
  }
  

}