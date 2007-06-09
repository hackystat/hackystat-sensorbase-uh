package org.hackystat.sensorbase.resource.projects;

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
 * The resource for processing GET host/projects/{email}.
 * Returns an index of all Project resource names associated with this user. 
 * 
 * @author Philip Johnson
 */
public class UserProjectsResource extends Resource {
  
  /** To be retrieved from the URL. */
  private String email;
  /** The user, or null if not found. */
  private User user;
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UserProjectsResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.email = (String) request.getAttributes().get("email");
    UserManager userManager = (UserManager)getContext().getAttributes().get("UserManager");
    this.user = userManager.getUser(this.email);
    getVariants().clear(); // copied from BookmarksResource.java, not sure why needed.
    getVariants().add(new Variant(MediaType.TEXT_XML));
  }
  
  /**
   * Returns a ProjectIndex of all projects associated with this User.
   * 
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    // If this User does not exist, return an error.
    if (this.user == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user: " + this.email);
      return null;
    }  
    ProjectManager manager = 
      (ProjectManager)getContext().getAttributes().get("ProjectManager");
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      return new DomRepresentation(MediaType.TEXT_XML, manager.getProjectIndexDocument(this.user));
      }
    return null;
  }
  

}
