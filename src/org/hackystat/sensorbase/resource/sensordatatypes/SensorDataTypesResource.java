package org.hackystat.sensorbase.resource.sensordatatypes;

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
 * Implements a resource for GET host/sensordatatypes that returns an index of all defined SDTs. 
 * @author Philip Johnson
 */
public class SensorDataTypesResource extends SensorBaseResource {
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public SensorDataTypesResource(Context context, Request request, Response response) {
    super(context, request, response);
  }
  
  /**
   * Returns the representation of the SensorDataTypes resource. 
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    try {
      if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
        String xmlData = super.sdtManager.getSensorDataTypeIndex();
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
