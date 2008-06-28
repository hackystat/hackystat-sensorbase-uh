package org.hackystat.sensorbase.resource.projects;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.utilities.tstamp.Tstamp;
import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * The resource for processing GET host/project/{email}/{projectname}/summary.
 * Returns a representation of the ProjectSummary resource associated with this project.
 * The user can either specify a start and end time, which returns a ProjectSummary, or 
 * a start time and number of days, which will return a MultDayProjectSummary. 
 * 
 * @author Philip Johnson
 */
public class UserProjectSummaryResource extends SensorBaseResource {
  
  /** The user, or null if the uriUser does not name a defined User. */
  private User user; 
  /** To be retrieved from the URL. */
  private String projectName;
  /** To be retrieved from the URL. */
  private String startTime;
  /** To be retrieved from the URL. */
  private String endTime;
  /** To be retrieved from the URL. */
  private String numDays;
  
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UserProjectSummaryResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.projectName = (String) request.getAttributes().get("projectname");
    this.startTime = (String) request.getAttributes().get("startTime");
    this.endTime = (String) request.getAttributes().get("endTime");
    this.numDays = (String) request.getAttributes().get("numDays");
    this.user = super.userManager.getUser(uriUser);
  }
  
  /**
   * Returns an XML representation of the ProjectSummary associated with this User.
   * <ul>
   * <li> The uriUser must be defined as a User.
   * <li> The Project must be defined for this User.
   * <li> The authenticated user must be the admin, or uriUser, or a member of the project, or 
   * invited to be in the Project.
   * <li> There must be a startTime and endTime parameters which are timestamps. 
   * </ul>
   * 
   * @param variant The representational variant requested, or null if conditions are violated.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    // The uriUser must be a defined User.
    if (this.user == null) {
      String msg = "No user corresponding to: " + this.uriUser;
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return null;
    }
    // The named project must be defined.
    if (!super.projectManager.hasProject(this.user, this.projectName)) {
      String msg = "No Project named " + this.projectName + " for user " + this.uriUser;
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return null;
    }    
    // The authorized user must be an admin, or the project owner, or member, invitee, spectator. 
    if (!super.userManager.isAdmin(this.authUser) && !this.uriUser.equals(this.authUser) &&
        !super.projectManager.isMember(this.user, this.projectName, this.authUser) &&
        !super.projectManager.isSpectator(this.user, this.projectName, this.authUser) &&
        !super.projectManager.isInvited(this.user, this.projectName, this.authUser)) {
      String msg = "User " + this.authUser + "is not authorized to view this Project.";
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return null;
    }
    XMLGregorianCalendar startTimeXml = null;
    XMLGregorianCalendar endTimeXml = null;
    Integer numDays = null;
    // Parse this.startTime, this.endTime, and this.numDays
    try {
      startTimeXml = Tstamp.makeTimestamp(this.startTime);
      if (this.endTime != null) {
        endTimeXml = Tstamp.makeTimestamp(this.endTime);
      }
      if (this.numDays != null) {
        numDays = Integer.valueOf(this.numDays);
      }
    }
    catch (Exception e) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
      "startTime (or endTime) is not a timestamp");
      return null;
    }
    // The project must be defined.
    Project project = super.projectManager.getProject(this.user, this.projectName);
    if (project == null) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown project");
      return null;
    }
    // Make sure that startTime is not less than project.startTime.
    if (!ProjectUtils.isValidStartTime(project, startTimeXml)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
      startTimeXml + " cannot be less than the project's start time: " + project.getStartTime());
      return null;
    }
    try {
      String result = ((this.numDays == null) ? 
          getProjectSummaryString(project, startTimeXml, endTimeXml) :
            super.projectManager.getMultiDayProjectSummaryString(project, startTimeXml, numDays));
      return super.getStringRepresentation(result);
    }
    catch (Exception e) {
      String msg = "Couldn't marshall project summary for" + this.projectName + " into XML.";
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, msg);
      return null;
    }
  }
  
  /**
   * Returns the ProjectSummary string after doing additional argument validation.
   * @param project The project. 
   * @param startTimeXml The start time. 
   * @param endTimeXml The end time. 
   * @return The ProjectSummary as a string. 
   * @throws Exception If problems occur. 
   */
  private String getProjectSummaryString(Project project, XMLGregorianCalendar startTimeXml, 
      XMLGregorianCalendar endTimeXml) throws Exception {
    // If start time is greater than end time, return an error.
    if (Tstamp.greaterThan(startTimeXml, endTimeXml)) {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Start time after end time.");
        return null;
    }
    // And that endTime is not past the project endTime (if there is a project endTime).
    if ((project.getEndTime() != null) && 
        (!ProjectUtils.isValidEndTime(project, endTimeXml))) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, 
      "endTime cannot be greater than the project's end time.");
      return null;
    }
    return super.projectManager.getProjectSummaryString(project, startTimeXml, endTimeXml);
  }
  
}
