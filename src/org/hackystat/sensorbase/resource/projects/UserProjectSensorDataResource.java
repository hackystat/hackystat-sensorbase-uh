package org.hackystat.sensorbase.resource.projects;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.utilities.tstamp.Tstamp;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * The resource for processing GET host/projects/{user}/{projectname}/sensordata.
 * Returns an index to the SensorData resources associated with this  Project.
 * This includes all of the SensorData from all members that matches the UriPattern.
 * Note that this could be quite large if you do not also specify a start and end time.
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
  /** An optional query parameter. */
  private String sdt;
  /** An optional query parameter. */
  private String startIndex;
  /** An optional query parameter. */
  private String maxInstances;
  /** An optional tool parameter. */
  private String tool;

  
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
    this.sdt = (String) request.getAttributes().get("sdt");
    this.startIndex = (String) request.getAttributes().get("startIndex");
    this.maxInstances = (String) request.getAttributes().get("maxInstances");
    this.tool = (String) request.getAttributes().get("tool");
    this.user = super.userManager.getUser(super.uriUser);
  }
  
  /**
   * Returns a SensorDataIndex of all SensorData associated with this Project.  This
   * includes all SensorData from all Members in this project over the 
   * (optional) specified time period  for the (optional) SDT and (optional) tool that match 
   * at least one of the UriPatterns in the project definition.
   * 
   * Returns an error condition if:
   * <ul>
   * <li> The user does not exist.
   * <li> The authenticated user is not the uriUser or the Admin or a member of the project.
   * <li> The Project Resource named by the User and Project does not exist.
   * <li> startTime or endTime is not an XMLGregorianCalendar string.
   * <li> One or the other but not both of startTime and endTime is provided.
   * <li> endTime is earlier than startTime.
   * </ul>
   * 
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    // The user (project owner) must be defined.
    if (this.user == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user");
      return null;
    } 
    // The project must be defined.
    Project project = super.projectManager.getProject(this.user, this.projectName);
    if (project == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown project");
      return null;
    }
    // The authorized user must be an admin, or the project owner, or a member, or invitee.
    if (!super.userManager.isAdmin(this.authUser) && !this.uriUser.equals(this.authUser) &&
        !super.projectManager.isMember(this.user, this.projectName, this.authUser) &&
        !super.projectManager.isInvited(this.user, this.projectName, this.authUser)) {
      String msg = "User " + this.authUser + "is not authorized to obtain data from this Project.";
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return null;
    }
    // If startTime is provided, then both startTime and endTime must be XMLGregorianCalendars,
    // and startTime must be <= endTime.
    XMLGregorianCalendar startTimeXml = null;
    XMLGregorianCalendar endTimeXml = null;
    if (this.startTime != null) {
      try {
        startTimeXml = Tstamp.makeTimestamp(this.startTime);
        endTimeXml = Tstamp.makeTimestamp(this.endTime);
      }
      catch (Exception e) {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
        "startTime (or endTime) is not supplied and/or is not a timestamp");
        return null;
      }
      // We have a start and end time. Make sure startTime is not greater than endTime.
      if (Tstamp.greaterThan(startTimeXml, endTimeXml)) {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
        "startTime cannot be greater than endTime.");
        return null;
      }
      // Make sure that startTime is not less than project.startTime.
      if (!ProjectUtils.isValidStartTime(project, startTimeXml)) {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
        startTimeXml + " cannot be less than the project's start time: " + project.getStartTime());
        return null;
      }
      // And that endTime is not past the project endTime (if there is a project endTime).
      if ((project.getEndTime() != null) && 
          (!ProjectUtils.isValidEndTime(project, endTimeXml))) {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
        endTimeXml + " cannot be greater than the project's end time: " + project.getEndTime());
        return null;
      }
    }
    int startIndexInt = 0;
    int maxInstancesInt = 0;
    // Must supply both startIndex and maxInstances if either are supplied, and
    // startIndex and maxInstances must be non-negative integers.
    if (this.startIndex != null) {
      try {
        startIndexInt = Integer.parseInt(this.startIndex);
        maxInstancesInt = Integer.parseInt(this.maxInstances);
        if ((startIndexInt < 0) || (maxInstancesInt <= 0)) {
          getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
          "both startIndex & maxInstances must be non-negative.");
          return null;
        }
      }
      catch (Exception e) {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
        "startIndex (or maxInstances) is not supplied and/or is not an integer.");
        return null;
      }
    }

    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      try {
        if (startTime == null) {
          // Return all sensor data for this project if no query parameters.
          String data = super.projectManager.getProjectSensorDataIndex(this.user, project);
          return SensorBaseResource.getStringRepresentation(data);
        }
        if (tool != null) {
          // Return the tool's sensor data starting at startTime and ending with endTime.
          String data = super.projectManager.getProjectSensorDataIndex(this.user, project,
              startTimeXml, endTimeXml, this.sdt, this.tool);
          return SensorBaseResource.getStringRepresentation(data);
          
        }
        if (startIndex == null) {
          // Return all sensor data starting at startTime and ending with endTime.
          String data = super.projectManager.getProjectSensorDataIndex(this.user, project,
              startTimeXml, endTimeXml, this.sdt);
          return SensorBaseResource.getStringRepresentation(data);
        }
        else {
          // Return the data for startIndex and maxInstances. 
          String data = super.projectManager.getProjectSensorDataIndex(this.user, project,
              startTimeXml, endTimeXml, startIndexInt, maxInstancesInt);
          return SensorBaseResource.getStringRepresentation(data);
          
        }
      }
      catch (Exception e) {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Problem: " + e.getMessage());
        return null;
      }
    }
    return null;
  }
}