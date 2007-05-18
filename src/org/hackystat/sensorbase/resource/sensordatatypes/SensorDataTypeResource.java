package org.hackystat.sensorbase.resource.sensordatatypes;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.w3c.dom.Document;

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
    try { 
      SdtManager manager = (SdtManager)getContext().getAttributes().get("SdtManager");
      //System.out.println("In put: " + entity.getMediaType());
      //System.out.println("In put: " + entity.getText());
      //System.out.println("In put: " + entity.getMediaType());
      //System.out.println(entity.getMediaType());
      if (entity.getMediaType().equals(MediaType.TEXT_XML)) {
        Document sdt = new DomRepresentation(entity).getDocument();
        Status status = manager.putSdt(sdt)
        ? Status.SUCCESS_CREATED
            : Status.CLIENT_ERROR_BAD_REQUEST;
        getResponse().setStatus(status);
      } 
      else {
        getResponse().setStatus(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
      }
    }
    catch (Exception e) {
      SensorBaseLogger.getLogger().warning("Error in SDT PUT: " + StackTrace.toString(e));
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
    }
  }
}
