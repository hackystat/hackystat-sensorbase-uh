package org.hackystat.sensorbase.resource.sensordatatypes;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * Implements a resource for PUT, GET, DELETE of host/sensordatatype/{sensordatatypename}.
 * @author Philip Johnson
 */
public class SensorDataTypeResource extends SensorBaseResource {
  
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
  }
  
  /**
   * Returns the representation of the specified SensorDataType resource.
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    if (!super.sdtManager.hasSdt(this.sdtName)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown SDT: " + this.sdtName);
      return null;
    } 
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      String xmlData = super.sdtManager.getSensorDataTypeString(this.sdtName);
      return super.getStringRepresentation(xmlData);
    }
    return null;
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
   * <ul>
   * <li> The XML must be marshallable into an SDT instance using the SDT XmlSchema definition.
   * <li> The SDT name in the URI string must match the SDT name in the XML.
   * <li> The authenticated user must be the admin.
   * </ul>
   * @param entity The XML representation of the new SDT. 
   */
  @Override
  public void put(Representation entity) {
    String entityString = null;
    SensorDataType sdt;
    if (!super.userManager.isAdmin(this.authUser)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Only the admin can define SDTs");
      return;
    }

    // Try to make the XML payload into an SDT, return failure if this fails. 
    try { 
      entityString = entity.getText();
      sdt = super.sdtManager.makeSensorDataType(entityString);
    }
    catch (Exception e) {
      SensorBaseLogger.getLogger().warning("Bad Sdt Definition in PUT: " + StackTrace.toString(e));
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad SDT: " + entityString);
      return;
    }
    // Return failure if the URI SdtName is not the same as the XML SdtName.
    if (!(this.sdtName.equals(sdt.getName()))) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "URI/XML name mismatch");
      return;
    }
    // otherwise we add it to the Manager and return success.
    super.sdtManager.putSdt(sdt);      
    getResponse().setStatus(Status.SUCCESS_CREATED);
  }
  
  /** 
   * Indicate the DELETE method is supported. 
   * @return True.
   */
  @Override
  public boolean allowDelete() {
      return true;
  }
  
  /**
   * Implement the DELETE method that deletes an existing SDT given its name.
   */
  @Override
  public void delete() {
    if (!super.userManager.isAdmin(this.authUser)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Only the admin can delete SDTs");
      return;
    }    
    super.sdtManager.deleteSdt(sdtName);      
    getResponse().setStatus(Status.SUCCESS_OK);
  }
}
