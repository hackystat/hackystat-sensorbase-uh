package org.hackystat.sensorbase.resource.projects;

import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * The resource for processing GET host/projects/{user} requests.
 * Returns an index of all Project resource names associated with this user. 
 * 
 * @author Philip Johnson
 */
public class UserProjectsResource extends SensorBaseResource {
  

  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UserProjectsResource(Context context, Request request, Response response) {
    super(context, request, response);
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
  public Representation represent(Variant variant) {
    try {
      if (!validateUriUserIsUser() ||
          !validateAuthUserIsAdminOrUriUser()) {
        return null;
      }  
      if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
        String xmlData = super.projectManager.getProjectIndex(this.user);
        return super.getStringRepresentation(xmlData);      
        }
    }
    catch (RuntimeException e) {
      setStatusInternalError(e);
    }
    return null;
  }
}
