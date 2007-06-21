package org.hackystat.sensorbase.resource.sensordata;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
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
    if (!super.userManager.isAdmin(this.uriUser) && !this.uriUser.equals(this.authUser)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, super.badAuth);
      return null;
    }
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      // sensordata/{email}
      if ((this.sdtName == null) && (this.timestamp == null)) {
        return new DomRepresentation(MediaType.TEXT_XML, 
            super.sensorDataManager.getSensorDataIndexDocument(this.user));
      }
      // sensordata/{email}?sdt={sensordatatype}
      else if (this.timestamp == null) {
        return new DomRepresentation(MediaType.TEXT_XML, 
            super.sensorDataManager.getSensorDataIndexDocument(this.user, this.sdtName));
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
        // Now, see if we actually have one.
        if (!super.sensorDataManager.hasSensorData(user, tstamp)) {
          getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unknown Sensor Data");
          return null;
        }
        // We have one, so make its representation and return it.
        try {
          SensorData data = super.sensorDataManager.getSensorData(this.user, tstamp);
          return new DomRepresentation(MediaType.TEXT_XML, 
              SensorDataManager.marshallSensorData(data));
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
    String entityString = null;
    SensorData data;
    // If this User does not exist, return an error.
    if (this.user == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user: " + this.uriUser);
      return;
    } 
    if (!super.userManager.isAdmin(this.uriUser) && !this.uriUser.equals(this.authUser)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, super.badAuth);
      return;
    }
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
    super.sensorDataManager.putSensorData(data);      
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
    super.sensorDataManager.deleteData(this.user, this.timestamp);      
    getResponse().setStatus(Status.SUCCESS_OK);
  }
}
