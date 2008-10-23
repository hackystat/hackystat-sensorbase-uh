package org.hackystat.sensorbase.resource.sensordata;

import java.io.IOException;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.utilities.tstamp.Tstamp;
import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDatas;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.ResponseMessage;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * The resource for all URIs that extend sensordata. This includes:
 * <ul>
 * <li> host/sensordata/{user}
 * <li> host/sensordata/{user}?sdt={sensordatatype}
 * <li> host/sensordata/{user}?lastModStartTime={lastModTimestamp}&lastModEndTime={lastModTimestamp}
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
  /** To be retrieved from the URL, or else null if not found. */
  private String lastModStartTime;
  /** To be retrieved from the URL, or else null if not found. */
  private String lastModEndTime;

  /**
   * Provides the following representational variants: TEXT_XML.
   * 
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UserSensorDataResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.user = super.userManager.getUser(uriUser);
    this.sdtName = (String) request.getAttributes().get("sensordatatype");
    this.timestamp = (String) request.getAttributes().get("timestamp");
    this.lastModStartTime = (String) request.getAttributes().get("lastModStartTime");
    this.lastModEndTime = (String) request.getAttributes().get("lastModEndTime");
  }

  /**
   * Returns a SensorDataIndex when a GET is called with:
   * <ul>
   * <li> sensordata/{email}
   * <li> sensordata/{email}?sdt={sensordatatype}
   * <li> sensordata/{user}?lastModStartTime={timestamp}&lastModEndTime={timestamp}
   * </ul>
   * Returns a SensorData when a GET is called with:
   * <ul>
   * <li> sensordata/{email}/{timestamp}
   * </ul>
   * <p>
   * The user must be defined, and the authenticated user must be the uriUser or the Admin or in a
   * project with the uriUser on that day.
   * 
   * @param variant The representational variant requested.
   * @return The representation.
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    // Return error if unknown user.
    if (this.user == null) {
      this.responseMsg = ResponseMessage.undefinedUser(this, this.uriUser);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return null;
    }
    // Return error if authUser is not UriUser, not admin, and not in a Project with UriUser.
    if (!super.userManager.isAdmin(this.authUser) && !this.uriUser.equals(this.authUser)
        && !super.projectManager.inProject(this.authUser, this.uriUser, this.timestamp)) {
      String msg = String.format("Request requires authorized user %s to be in at least one "
          + "project with the URI user %s on the specified day", this.authUser, this.uriUser);
      this.responseMsg = ResponseMessage.miscError(this, msg);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return null;
    }
    // Now check to make sure they want XML.
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      // Return index of all data for URI: sensordata/{email}
      if ((this.sdtName == null) && (this.timestamp == null) && (this.lastModStartTime == null)) {
        String xmlData = super.sensorDataManager.getSensorDataIndex(this.user);
        return super.getStringRepresentation(xmlData);
      }
      // Return index of data for a given SDT for URI: sensordata/{email}?sdt={sensordatatype}
      if ((this.sdtName != null) && (this.timestamp == null) && (this.lastModStartTime == null)) {
        String xmlData = super.sensorDataManager.getSensorDataIndex(this.user, this.sdtName);
        return super.getStringRepresentation(xmlData);
      }
      // Return index of data since the tstamp for URI:
      // sensordata/{user}?lastModStartTime={timestamp}&lastModEndTime={timestamp}
      if ((this.sdtName == null) && (this.timestamp == null) && (this.lastModStartTime != null)) {
        // First, check to see that we can convert the lastModStartTime and EndTime into a tstamp.
        XMLGregorianCalendar lastModStart;
        XMLGregorianCalendar lastModEnd;
        try {
          lastModStart = Tstamp.makeTimestamp(this.lastModStartTime);
        }
        catch (Exception e) {
          this.responseMsg = ResponseMessage.badTimestamp(this, lastModStartTime);
          getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
          return null;
        }
        try {
          lastModEnd = Tstamp.makeTimestamp(this.lastModEndTime);
        }
        catch (Exception e) {
          this.responseMsg = ResponseMessage.badTimestamp(this, lastModEndTime);
          getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
          return null;
        }
        // Now, get the data and return.
        String xmlData = super.sensorDataManager.getSensorDataIndexLastMod(this.user, lastModStart,
            lastModEnd);
        return super.getStringRepresentation(xmlData);
      }
      // Return sensordata representation for URI: sensordata/{email}/{timestamp}
      if ((this.sdtName == null) && (this.timestamp != null) && (this.lastModStartTime == null)) {
        // First, try to parse the timestamp string, and return error if it doesn't parse.
        XMLGregorianCalendar tstamp;
        try {
          tstamp = Tstamp.makeTimestamp(this.timestamp);
        }
        catch (Exception e) {
          this.responseMsg = ResponseMessage.badTimestamp(this, timestamp);
          getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
          return null;
        }
        String xmlData = super.sensorDataManager.getSensorData(this.user, tstamp);
        // Now, see if we actually have the SensorData.
        if (xmlData == null) { // NOPMD (deeply nested if-then-else)
          String msg = "Unknown sensor data";
          this.responseMsg = ResponseMessage.miscError(this, msg);
          getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
          return null;
        }
        // We have the SensorData, so retrieve its xml string representation and return it.
        try {
          return getStringRepresentation(xmlData);
        }
        catch (Exception e) {
          this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
          getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
          return null;
        }
      }
    }
    // Otherwise we don't understand the URI that they invoked us with.
    String msg = "Request could not be understood.";
    this.responseMsg = ResponseMessage.miscError(this, msg);
    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
    return null;
  }

  /**
   * Indicate the PUT method is supported.
   * 
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
   * Note that we are not validating that this sensor data instance contains all of the Required
   * Properties specified by the SDT. This should be done later, on demand, as part of analyses.
   * <p>
   * We are also not at this point checking to see whether the User and SDT exist.
   * 
   * @param entity The XML representation of the new sensor data instance..
   */
  @Override
  public void put(Representation entity) {
    try {
      // If this User does not exist, return an error.
      if (this.user == null) {
        this.responseMsg = ResponseMessage.undefinedUser(this, this.uriUser);
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
        return;
      }
      if (!super.userManager.isAdmin(this.uriUser) && !this.uriUser.equals(this.authUser)) {
        this.responseMsg = ResponseMessage.adminOrAuthUserOnly(this, this.authUser, this.uriUser);
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
        return;
      }

      // Get the payload.
      String entityString = null;
      try {
        entityString = entity.getText();
      }
      catch (IOException e) {
        String msg = "Bad or missing content";
        this.responseMsg = ResponseMessage.miscError(this, msg);
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
        return;
      }

      // if the "timestamp" is "batch", then our payload is <SensorDatas>, otherwise <SensorData>
      if (this.timestamp.equals("batch")) {
        putSensorDatas(entityString);
      }
      else {
        putSensorData(entityString);
      }
    }
    catch (RuntimeException e) {
      this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
      return;
    }
  }

  /**
   * Put a SensorData XML payload.
   * 
   * @param entityString An entity string that should represent a SensorData instance.
   */
  private void putSensorData(String entityString) {
    SensorData data;
    // Try to make the XML payload into sensor data, return failure if this fails.
    try {
      data = this.sensorDataManager.makeSensorData(entityString);
    }
    catch (Exception e) {
      String msg = "Invalid SensorData representation: " + entityString;
      this.responseMsg = ResponseMessage.miscError(this, msg);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return;
    }

    try {
      // Return failure if the payload XML timestamp doesn't match the URI timestamp.
      if ((this.timestamp == null) || (!this.timestamp.equals(data.getTimestamp().toString()))) {
        String msg = "Timestamp in URI does not match timestamp in sensor data instance.";
        this.responseMsg = ResponseMessage.miscError(this, msg);
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
        return;
      }
      // Return failure if the SensorData Owner doesn't match the UriUser
      if (!this.uriUser.equals(super.sensorDataManager.convertOwnerToEmail(data.getOwner()))) {
        String msg = "SensorData payload owner field does not match user field in URI";
        this.responseMsg = ResponseMessage.miscError(this, msg);
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
        return;
      }
      // otherwise we add it to the Manager and return success.
      super.sensorDataManager.putSensorData(data);
      getResponse().setStatus(Status.SUCCESS_CREATED);
    }
    catch (RuntimeException e) {
      this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
      return;
    }
  }

  /**
   * Put a SensorDatas payload.
   * 
   * @param entityString An entity string that should represent a SensorDatas instance.
   */
  private void putSensorDatas(String entityString) {
    SensorDatas datas;
    // Try to make the XML payload into a collection of SensorData, return failure if this fails.
    try {
      datas = this.sensorDataManager.makeSensorDatas(entityString);
    }
    catch (Exception e) {
      String msg = "Invalid SensorDatas representation: " + entityString;
      this.responseMsg = ResponseMessage.miscError(this, msg);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return;
    }

    try {
      // Makes sure all SensorData matches the UriUser, otherwise fail and don't add anything.
      for (SensorData data : datas.getSensorData()) {
        if (!this.uriUser.equals(super.sensorDataManager.convertOwnerToEmail(data.getOwner()))) {
          String msg = "At least one SensorData owner field does not match user field in URI";
          this.responseMsg = ResponseMessage.miscError(this, msg);
          getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
          return;
        }
      }
      // Otherwise we should be OK. Add all of them and return success.
      for (SensorData data : datas.getSensorData()) {
        super.sensorDataManager.putSensorData(data);
      }
      getResponse().setStatus(Status.SUCCESS_CREATED);
    }
    catch (RuntimeException e) {
      this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
      return;
    }
  }

  /**
   * Indicate the DELETE method is supported.
   * 
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
   * If not timestamp is supplied, then all sensor data will be deleted if the user is the test
   * user.
   */
  @Override
  public void delete() {
    // If this User does not exist, return an error.
    if (this.user == null) {
      this.responseMsg = ResponseMessage.undefinedUser(this, this.uriUser);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return;
    }
    if (!super.userManager.isAdmin(this.uriUser) && !this.uriUser.equals(this.authUser)) {
      this.responseMsg = ResponseMessage.adminOrAuthUserOnly(this, this.authUser, this.uriUser);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return;
    }
    // If timestamp is null, then delete all data if a test user is deleting its own data.
    if (this.timestamp == null) {
      if ((this.authUser != null) && this.authUser.equals(this.uriUser)
          && userManager.isTestUser(this.user)) {
        super.sensorDataManager.deleteData(this.user);
        getResponse().setStatus(Status.SUCCESS_OK);
        return;
      }
      else {
        String msg = "Can't delete all sensor data from a non-test user.";
        this.responseMsg = ResponseMessage.miscError(this, msg);
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
        return;
      }
    }

    XMLGregorianCalendar tstamp;
    try {
      tstamp = Tstamp.makeTimestamp(this.timestamp);
      super.sensorDataManager.deleteData(this.user, tstamp);
    }
    catch (Exception e) {
      String msg = "Bad timestamp: " + this.timestamp;
      this.responseMsg = ResponseMessage.miscError(this, msg);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return;
    }
    getResponse().setStatus(Status.SUCCESS_OK);
  }
}
