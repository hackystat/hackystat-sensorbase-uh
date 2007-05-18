package org.hackystat.sensorbase.resource.sensordata;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

/**
 * Implements a Restlet Resource for Hackystat Sensor Data. 
 * @author Philip Johnson
 */
public class SensorDataResource extends Resource {
 
  /**
   * The standard constructor.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public SensorDataResource(Context context, Request request, Response response) {
    super(context, request, response);
    getVariants().add(new Variant(MediaType.TEXT_PLAIN));
  }
  
  /**
   * Returns the representation of the Sensor Data resource. 
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    Representation result = null;
    return result; 
  }
}