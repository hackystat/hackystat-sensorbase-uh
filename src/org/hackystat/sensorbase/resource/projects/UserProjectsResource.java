package org.hackystat.sensorbase.resource.projects;

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
 * The resource for processing GET host/projects/{user} requests.
 * Returns an index of all Project resource names associated with this user. 
 * 
 * @author Philip Johnson
 */
public class UserProjectsResource extends SensorBaseResource {
  
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
    this.user = super.userManager.getUser(this.uriUser);
  }
  
  /**
   * Returns a ProjectIndex of all projects associated with this User.
   * <ul>
   * <li> The user must be defined.
   * <li> The authenticated user must be the uriUser or the Admin. 
   * </ul>
   * 
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    // If this User does not exist, return an error.
    if (this.user == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user: " + super.uriUser);
      return null;
    }  
    if (!super.userManager.isAdmin(this.authUser) && !this.uriUser.equals(this.authUser)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, super.badAuth);
      return null;
    }
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      return new DomRepresentation(MediaType.TEXT_XML, 
          super.projectManager.getProjectIndexDocument(this.user));
      }
    return null;
  }
}
