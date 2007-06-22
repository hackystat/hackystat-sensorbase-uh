package org.hackystat.sensorbase.resource.projects;

import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.resource.sensordata.Tstamp;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * The resource for processing GET host/projects/{user}/{projectname}/sensordata.
 * Returns an index to the SensorData resources associated with this User and Project.
 * 
 * @author Philip Johnson
 */
public class UserProjectSensorDataResource extends SensorBaseResource {
  
  /** The user corresponding to email, or null if not found. */
  private User user;
  /** To be retrieved from the URL. */
  private String projectName;
  /** An optional query parameter */
  private String startTime;
  /** An optional query string parameter. */
  private String endTime;

  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UserProjectSensorDataResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.projectName = (String) request.getAttributes().get("projectname"); 
    this.startTime = (String) request.getAttributes().get("startTime");
    this.endTime = (String) request.getAttributes().get("endTime");
    this.user = super.userManager.getUser(super.uriUser);
  }
  
  /**
   * Returns a SensorDataIndex of all SensorData associated with this Project and User.
   * Returns an error condition if:
   * <ul>
   * <li> The user does not exist.
   * <li> The authenticated user is not the uriUser or the Admin. 
   * <li> The Project Resource named by the User and Project does not exist.
   * <li> startTime or endTime is not an XMLGregorianCalendar string.
   * <li> One or the other but not both of startTime and endTime is provided.
   * <li> endTime is greater than startTime.
   * </ul>
   * 
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    if (this.user == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user");
      return null;
    }  
    if (!super.userManager.isAdmin(this.authUser) && !this.uriUser.equals(this.authUser)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, super.badAuth);
      return null;
    }
    // If this User/Project pair does not exist, return an error.
    if (!super.projectManager.hasProject(this.user, this.projectName)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown project");
      return null;
    }
    // If startTime or endTime is provided, but is not an XMLGregorianCalendar, then return error.
    if ((this.startTime != null) && (!Tstamp.isTimestamp(this.startTime))) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad startTime");
      return null;
    }
    if ((this.endTime != null) && (!Tstamp.isTimestamp(this.endTime))) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad endTime");
      return null;
    }
    // If end time is greater than start time, return an error.
    if ((this.endTime != null) && (Tstamp.isTimestamp(this.endTime)) &&
        (this.startTime != null) && (Tstamp.isTimestamp(this.startTime)) &&
        (Tstamp.greaterThan(this.startTime, this.endTime))) {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Start time after end time.");
        return null;
    }
    // If startType is provided but not endTime, or vice versa, return an error. 
    if ((this.endTime == null) && (this.startTime != null) ||
        (this.endTime != null) && (this.startTime == null)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "startTime & endTime required.");
      return null;
    }

    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      try {
        if (startTime == null) {
          // Return all sensor data for this user and project if no query parameters.
          String data = super.projectManager.getProjectSensorDataIndex(this.user, this.projectName);
          return this.getStringRepresentation(data);
        }
        else {
          // Return the sensor data starting at startTime and ending with endTime. 
          String data = super.projectManager.getProjectSensorDataIndex(this.user, this.projectName,
              this.startTime, this.endTime);
          return this.getStringRepresentation(data);
        }
      }
      catch (Exception e) {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Problems marshalling");
        return null;
      }
    }
    return null;
  }
}