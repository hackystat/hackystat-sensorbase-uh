package org.hackystat.sensorbase.resource.sensordatatypes;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
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
 * Implements a Restlet Resource for obtaining individual SensorDataType resources. 
 * @author Philip Johnson
 */
public class SensorDataTypeResource extends Resource {
  
  /** To be retrieved from the URL. */
  private String sdtName; 
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public SensorDataTypeResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.sdtName = (String) request.getAttributes().get("sensordatatypename");
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
      result = new DomRepresentation(MediaType.TEXT_XML, 
          manager.getSensorDataTypeDocument(this.sdtName));
    }
    return result;
  }
  
  /** 
   * Indicate the PUT method is supported. 
   * @return True.
   */
  @Override
  public boolean allowPut() {
      return true;
  }

  /**
   * Implement the PUT method that creates a new SDT. 
   * @param entity The XML representation of the new SDT. 
   */
  @Override
  public void put(Representation entity) {
    String entityString = null;
    SensorDataType sdt;
    // First, we see if the request payload can be made into an SDT.
    try { 
      entityString = entity.getText();
      sdt = SdtManager.getSensorDataType(entityString);
    }
    catch (Exception e) {
      SensorBaseLogger.getLogger().warning("Bad Sdt Definition in PUT: " + StackTrace.toString(e));
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad SDT: " + entityString);
      return;
    }
    // Now, we see if it already exists. 
    SdtManager manager = (SdtManager)getContext().getAttributes().get("SdtManager");
    if (manager.hasSdt(sdt)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "SDT defined: " + sdt.getName());
      return;
    }
    // otherwise we add it to the Manager and return success.
    manager.putSdt(sdt);      
    getResponse().setStatus(Status.SUCCESS_CREATED);
  }
}
