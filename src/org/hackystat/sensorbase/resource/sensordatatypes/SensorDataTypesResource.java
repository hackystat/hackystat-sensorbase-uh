package org.hackystat.sensorbase.resource.sensordatatypes;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

/**
 * Implements a Restlet Resource representing an index of Hackystat Sensor Data Types. 
 * @author Philip Johnson
 */
public class SensorDataTypesResource extends Resource {
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public SensorDataTypesResource(Context context, Request request, Response response) {
    super(context, request, response);
    getVariants().clear(); // copyied from BookmarksResource.java, not sure why needed.
    getVariants().add(new Variant(MediaType.TEXT_XML));
  }
  
  /**
   * Returns the representation of the SensorDataTypes resource. 
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    Representation result = null;
    SdtManager manager = (SdtManager)getContext().getAttributes().get("SdtManager");
    if (manager == null) {
      throw new RuntimeException("Failed to find SdtManager");
    }
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      result = new DomRepresentation(MediaType.TEXT_XML, manager.getSensorDataTypeIndexDocument());
    }
    return result;
  }

  
}