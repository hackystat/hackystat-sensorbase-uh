package org.hackystat.sensorbase.resource.sensordata;

import java.io.IOException;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.utilities.stacktrace.StackTrace;
import org.hackystat.utilities.tstamp.Tstamp;
import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDatas;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * The resource for all URIs that extend sensordata, including;
 * <ul>
 * <li> host/sensordata/{user}
 * <li> host/sensordata/{user}?sdt={sensordatatype}
 * <li> host/sensordata/{user}/{timestamp}
 * </ul>
 * 
 * @author Philip Johnson
 */
public class UserSensorDataResource extends SensorBaseResource {

  /** The user, or null if not found. */
  private User user; 
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
    this.user = super.userManager.getUser(uriUser);
    this.sdtName = (String) request.getAttributes().get("sensordatatype");
    this.timestamp = (String) request.getAttributes().get("timestamp");
  }
  
  /**
   * Returns a SensorDataIndex when a GET is called with:
   * <ul>
   * <li> sensordata/{email}
   * <li> sensordata/{email}?sdt={sensordatatype}
   * </ul>
   * Returns a SensorData when a GET is called with:
   * <ul>
   * <li> sensordata/{email}/{timestamp}
   * </ul>
   * <p>
   * The user must be defined, and the authenticated user must be the uriUser or the Admin.
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    if (this.user == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user: " + this.uriUser);
      return null;
    } 
    if (!super.userManager.isAdmin(this.authUser) && !this.uriUser.equals(this.authUser)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, super.badAuth);
      return null;
    }
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      // sensordata/{email}
      if ((this.sdtName == null) && (this.timestamp == null)) {
        String xmlData = super.sensorDataManager.getSensorDataIndex(this.user);
        return super.getStringRepresentation(xmlData);
      }
      // sensordata/{email}?sdt={sensordatatype}
      else if (this.timestamp == null) {
        String xmlData = super.sensorDataManager.getSensorDataIndex(this.user);
        return super.getStringRepresentation(xmlData);
      }
      // sensordata/{email}/{timestamp}
      else {
        // First, try to parse the timestamp string, and return error if it doesn't parse.
        XMLGregorianCalendar tstamp;
        try {
          tstamp = Tstamp.makeTimestamp(this.timestamp);
        }
        catch (Exception e) {
          getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad Timestamp " + timestamp);
          return null;
        }
        // Now, see if we actually have the SensorData.
        if (!super.sensorDataManager.hasSensorData(user, tstamp)) {
          getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unknown Sensor Data");
          return null;
        }
        // We have the SensorData, so retrieve its xml string representation and return it.
        try {
          String xmlData = super.sensorDataManager.getSensorData(this.user, tstamp);
          return getStringRepresentation(xmlData);
        }
        catch (Exception e) {
          // The marshallSensorData threw an exception, which is unrecoverable.
          return null;
        }
      }
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
    // If this User does not exist, return an error.
    if (this.user == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user: " + this.uriUser);
      return;
    } 
    if (!super.userManager.isAdmin(this.uriUser) && !this.uriUser.equals(this.authUser)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, super.badAuth);
      return;
    }
    
    // Get the payload.
    String entityString = null;
    try {
      entityString = entity.getText();
    }
    catch (IOException e) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad payload.");
      return;
    }
    
    //if the "timestamp" is "batch", then our payload is <SensorDatas>, otherwise <SensorData>
    if (this.timestamp.equals("batch")) {
      putSensorDatas(entityString);
    }
    else {
      putSensorData(entityString);
    }
  }
  
  /**
   * Put a SensorData XML payload.
   * @param entityString An entity string that should represent a SensorData instance.
   */
  private void putSensorData(String entityString) {
    SensorData data;
    // Try to make the XML payload into sensor data, return failure if this fails. 
    try { 
      data = this.sensorDataManager.makeSensorData(entityString);
    }
    catch (Exception e) {
      server.getLogger().warning("Bad Sensor Data in PUT: " + StackTrace.toString(e));
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad Sensor Data: " + entityString);
      return;
    }
    // Return failure if the payload XML timestamp doesn't match the URI timestamp.
    if ((this.timestamp == null) || (!this.timestamp.equals(data.getTimestamp().toString()))) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Inconsistent timestamps");
      return;
    }
    // Return failure if the SensorData Owner doesn't match the UriUser
    if (!this.uriUser.equals(super.sensorDataManager.convertOwnerToEmail(data.getOwner()))) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
          "SensorData payload owner field does not match user field in URI");
      return;
    }
    // otherwise we add it to the Manager and return success.
    super.sensorDataManager.putSensorData(data);      
    getResponse().setStatus(Status.SUCCESS_CREATED);
  }
  
  /**
   * Put a SensorDatas payload.
   * @param entityString An entity string that should represent a SensorDatas instance. 
   */
  private void putSensorDatas(String entityString) {
    SensorDatas datas;
    // Try to make the XML payload into a collection of SensorData, return failure if this fails. 
    try { 
      datas = this.sensorDataManager.makeSensorDatas(entityString);
    }
    catch (Exception e) {
      server.getLogger().warning("Bad Sensor Data in PUT: " + StackTrace.toString(e));
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
          "Bad Batch SensorData: " + entityString);
      return;
    }
    // Makes sure all SensorData matches the UriUser, otherwise fail and don't add anything.
    for (SensorData data : datas.getSensorData()) {
      if (!this.uriUser.equals(super.sensorDataManager.convertOwnerToEmail(data.getOwner()))) {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
            "At least one SensorData owner field does not match user field in URI");
        return;
      }
    }
    // Otherwise we should be OK.  Add all of them and return success.
    for (SensorData data : datas.getSensorData()) {
      super.sensorDataManager.putSensorData(data);      
    }
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
   * Implement the DELETE method that ensures the specified sensor data instance no longer exists.
   * <ul>
   * <li> The user must exist.
   * <li> The timestamp must be well-formed.
   * <li> The authenticated user must be the uriUser or the admin. 
   * </ul>
   */
  @Override
  public void delete() {
    // If this User does not exist, return an error.
    if (this.user == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user: " + this.uriUser);
      return;
    } 
    if (!super.userManager.isAdmin(this.uriUser) && !this.uriUser.equals(this.authUser)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, super.badAuth);
      return;
    }
    XMLGregorianCalendar tstamp;
    try {
      tstamp = Tstamp.makeTimestamp(this.timestamp);
    }
    catch (Exception e) { 
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad timestamp " + this.timestamp);
      return;
    }
    super.sensorDataManager.deleteData(this.user, tstamp);      
    getResponse().setStatus(Status.SUCCESS_OK);
  }
}
