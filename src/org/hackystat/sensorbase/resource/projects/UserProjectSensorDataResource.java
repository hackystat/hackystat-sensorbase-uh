package org.hackystat.sensorbase.resource.projects;

import org.hackystat.sensorbase.resource.sensordata.Timestamp;
import org.hackystat.sensorbase.resource.users.UserManager;
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
 * The resource for processing GET host/projects/{userkey}/{projectname}/sensordata.
 * Returns an index to the SensorData resources associated with this User and Project.
 * 
 * @author Philip Johnson
 */
public class UserProjectSensorDataResource extends Resource {
  
  /** To be retrieved from the URL. */
  private String userKey;
  /** To be retrieved from the URL. */
  private String projectName;
  /** An optional query parameter */
  private String startTime;
  /** An optional query string parameter. */
  private String endTime;
  /** The Project Manager. */
  private ProjectManager projectManager;
  /** The User Manager. */
  private UserManager userManager;
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UserProjectSensorDataResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.userKey = (String) request.getAttributes().get("userkey");
    this.projectName = (String) request.getAttributes().get("projectname"); 
    this.startTime = (String) request.getAttributes().get("startTime");
    this.endTime = (String) request.getAttributes().get("endTime");
    this.projectManager = (ProjectManager)getContext().getAttributes().get("ProjectManager");
    this.userManager = (UserManager)getContext().getAttributes().get("UserManager");
    getVariants().clear(); // copyied from BookmarksResource.java, not sure why needed.
    getVariants().add(new Variant(MediaType.TEXT_XML));
  }
  
  /**
   * Returns a SensorDataIndex of all SensorData associated with this Project and User.
   * Returns an error condition if:
   * <ul>
   * <li> The user does not exist.
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

    // If this User does not exist, return an error.
    if (!userManager.hasUser(this.userKey)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user");
      return null;
    }   
    // If this User/Project pair does not exist, return an error.
    if (!projectManager.hasProject(this.userKey, this.projectName)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown project");
      return null;
    }
    // If startTime or endTime is provided, but is not an XMLGregorianCalendar, then return error.
    if ((this.startTime != null) && (!Timestamp.isTimestamp(this.startTime))) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad startTime");
      return null;
    }
    if ((this.endTime != null) && (!Timestamp.isTimestamp(this.endTime))) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad endTime");
      return null;
    }
    // If end time is greater than start time, return an error.
    if ((this.endTime != null) && (Timestamp.isTimestamp(this.endTime)) &&
        (this.startTime != null) && (Timestamp.isTimestamp(this.startTime)) &&
        (Timestamp.greaterThan(this.startTime, this.endTime))) {
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
          return new DomRepresentation(MediaType.TEXT_XML, 
              projectManager.getProjectSensorDataIndexDocument(this.userKey, this.projectName));
        }
        else {
          // Return the sensor data starting at startTime and for the following duration.  
          return new DomRepresentation(MediaType.TEXT_XML, 
              projectManager.getProjectSensorDataIndexDocument(this.userKey, this.projectName,
                  this.startTime, this.endTime));
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