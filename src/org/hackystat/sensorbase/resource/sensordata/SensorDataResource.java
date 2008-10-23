package org.hackystat.sensorbase.resource.sensordata;

import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.server.ResponseMessage;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * Implements a Resource for processing host/sensordata requests and returning an index to 
 * all sensor data for all users in the SensorBase.
 * This is an admin-only operation that could return quite a large amount of data. 
 * @author Philip Johnson
 */
public class SensorDataResource extends SensorBaseResource {
 
  /**
   * The standard constructor.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public SensorDataResource(Context context, Request request, Response response) {
    super(context, request, response);
  }
  
  /**
   * Returns an index to all sensor data defined for all users in this system. 
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    try {
      if (!super.userManager.isAdmin(this.authUser)) {
        this.responseMsg = ResponseMessage.adminOnly(this);
        getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, this.responseMsg);
        return null;
      }    
      if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
        String xmlData = super.sensorDataManager.getSensorDataIndex();
        return super.getStringRepresentation(xmlData);
      }
    }
    catch (RuntimeException e) {
      this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
    }
    return null;
  }
}
