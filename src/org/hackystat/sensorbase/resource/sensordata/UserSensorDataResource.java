package org.hackystat.sensorbase.resource.sensordata;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
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
 * The resource for all URIs that extend sensordata, including;
 * <ul>
 * <li> host/sensordata/{userkey}
 * <li> host/sensordata/{userkey}/{sensordatatype}
 * <li> host/sensordata/{userkey}/{sensordatatype}/{timestamp}
 * </ul>
 * 
 * @author Philip Johnson
 */
public class UserSensorDataResource extends Resource {
  /** To be retrieved from the URL. */
  private String userKey;
  /** To be retrieved from the URL, or else null if not found. */
  private String sdtName;
  /** To be retrieved from the URL, or else null if not found. */
  private String timestamp;
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UserSensorDataResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.userKey = (String) request.getAttributes().get("userkey");
    this.sdtName = (String) request.getAttributes().get("sensordatatype");
    this.timestamp = (String) request.getAttributes().get("timestamp");
    getVariants().clear(); // copyied from BookmarksResource.java, not sure why needed.
    getVariants().add(new Variant(MediaType.TEXT_XML));
  }
  
  /**
   * Returns a SensorDataIndex when a GET is called with:
   * <ul>
   * <li> sensordata/{userkey}
   * <li> sensordata/{userkey/{sensordatatype}
   * </ul>
   * Returns a SensorData when a GET is called with:
   * <ul>
   * <li> sensordata/{userkey}/{sensordatatype}/{timestamp}
   * </ul>
   * 
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    Representation result = null;
    SensorDataManager manager = 
      (SensorDataManager)getContext().getAttributes().get("SensorDataManager");
    if (manager == null) {
      throw new RuntimeException("Failed to find SensorDataManager");
    }
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      // sensordata/{userkey}
      if ((this.sdtName == null) && (this.timestamp == null)) {
        result = new DomRepresentation(MediaType.TEXT_XML, 
            manager.getSensorDataIndexDocument(this.userKey));
      }
      // sensordata/{userkey}/{sensordatatype}
      else if (this.timestamp == null) {
        result = new DomRepresentation(MediaType.TEXT_XML, 
            manager.getSensorDataIndexDocument(this.userKey, this.sdtName));
      }
      // sensordata/{userkey}/{sensordatatype}/{timestamp}
      else {
        // First, try to parse the timestamp string, and return error if it doesn't parse.
        XMLGregorianCalendar tstamp;
        try {
          tstamp = manager.convertTimestamp(this.timestamp);
        }
        catch (Exception e) {
          getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad Timestamp " + timestamp);
          return null;
        }
        // Now, see if we actually have one.
        if (!manager.hasData(userKey, sdtName, tstamp)) {
          getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unknown Sensor Data");
          return null;
        }
        // We have one, so make its representation and return it.
        result = new DomRepresentation(MediaType.TEXT_XML, 
            manager.marshallSensorData(this.userKey, this.sdtName, tstamp));
      }
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
   * Implement the PUT method that creates a new sensor data instance.
   * <ul>
   * <li> The XML must be marshallable into a sensor data instance.
   * <li> The timestamp in the URL must match the timestamp in the XML.
   * <li> The User and SDT must exist.
   * </ul>
   * Note that we are not validating that this sensor data instance contains all of the 
   * Required Properties specified by the SDT.  This should be done later, on demand, as part of
   * analyses. 
   * <p>
   * We are also not at this point checking to see whether the User and SDT exist. 
   * @param entity The XML representation of the new sensor data instance.. 
   */
  @Override
  public void put(Representation entity) {
    String entityString = null;
    SensorData data;
    // Try to make the XML payload into sensor data, return failure if this fails. 
    try { 
      entityString = entity.getText();
      data = SensorDataManager.unmarshallSensorData(entityString);
    }
    catch (Exception e) {
      SensorBaseLogger.getLogger().warning("Bad Sensor Data in PUT: " + StackTrace.toString(e));
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad Sensor Data: " + entityString);
      return;
    }
    // Return failure if the payload XML timestamp doesn't match the URI timestamp.
    if ((this.timestamp == null) || (!this.timestamp.equals(data.getTimestamp().toString()))) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Inconsistent timestamps");
      return;
    }
    // otherwise we add it to the Manager and return success.
    SensorDataManager manager = 
      (SensorDataManager)getContext().getAttributes().get("SensorDataManager");
    manager.putSensorData(data);      
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
   * Implement the DELETE method that deletes an existing sensor data instance.
   */
  @Override
  public void delete() {
    SensorDataManager manager = 
      (SensorDataManager)getContext().getAttributes().get("SensorDataManager");
    // Return failure if it doesn't exist.
    if (!manager.hasData(this.userKey, this.sdtName, this.timestamp)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Nonexistent Sensor data");
      return;
    }
    // Otherwise, delete it and return successs.
    manager.deleteData(this.userKey, this.sdtName, this.timestamp);      
    getResponse().setStatus(Status.SUCCESS_OK);
  }
  

}
