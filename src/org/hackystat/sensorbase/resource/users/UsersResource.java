package org.hackystat.sensorbase.resource.users;

import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * Implements a Restlet Resource representing an index of Hackystat Users. 
 * @author Philip Johnson
 */
public class UsersResource extends SensorBaseResource {
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UsersResource(Context context, Request request, Response response) {
    super(context, request, response);
  }
  
  /**
   * Returns the representation of an index of all the users in the system. 
   * Only the administrator can request this resource. 
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation represent(Variant variant) {
    if (!validateAuthUserIsAdmin()) {
      return null;
    }
    try {
      if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
        String xmlData = super.userManager.getUserIndex();
        return super.getStringRepresentation(xmlData);      
      }
    }
    catch (RuntimeException e) {
      setStatusInternalError(e);
    }
    return null;
  }
}
