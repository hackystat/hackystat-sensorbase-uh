package org.hackystat.sensorbase.resource.projects;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.utilities.tstamp.Tstamp;
import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
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
    this.startTime = (String) request.getAttributes().get("startTime");
    this.endTime = (String) request.getAttributes().get("endTime");
    this.numDays = (String) request.getAttributes().get("numDays");
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
  public Representation represent(Variant variant) {
    if (!validateUriUserIsUser() ||
        !validateUriProjectName() ||
        !validateProjectViewer()) {
      return null;
    }  
    
    XMLGregorianCalendar startTimeXml = null;
    XMLGregorianCalendar endTimeXml = null;
    Integer numDaysInt = null;
    // Parse this.startTime, this.endTime, and this.numDays
    try {
      startTimeXml = Tstamp.makeTimestamp(this.startTime);
    }
    catch (Exception e) {
      setStatusBadTimestamp(this.startTime);
      return null;
    }
    try {
      if (this.endTime != null) {
        endTimeXml = Tstamp.makeTimestamp(this.endTime);
      }
    }
    catch (Exception e) {
      setStatusBadTimestamp(this.endTime);
      return null;
    }
    try {
      if (this.numDays != null) {      
        numDaysInt = Integer.valueOf(this.numDays);
      }
    }
    catch (Exception e) {
      setStatusMiscError("numDays parameter not an integer: " + this.numDays);      
    }

    // Make sure that startTime is not less than project.startTime.
    if (!ProjectUtils.isValidStartTime(project, startTimeXml)) {
      setStatusMiscError(String.format("%s cannot be less than project start time of %s", 
          startTimeXml, project.getStartTime()));
      return null;
    }
    try {
      // Result will hold the return value as a string.  
      String result;
      // First, deal with case where we have a start day and end day in URI string. 
      // In this case we have to do additional argument validation. 
      if (this.numDays == null) { 
        if (Tstamp.greaterThan(startTimeXml, endTimeXml)) {
          setStatusMiscError("startTime cannot be greater than endTime.");
          return null;
        }
        // And that endTime is not past the project endTime (if there is a project endTime).
        if ((project.getEndTime() != null) && 
            (!ProjectUtils.isValidEndTime(project, endTimeXml))) {
          setStatusMiscError(String.format("%s cannot be greater than project end time of %s", 
              endTimeXml, project.getEndTime()));
          return null;
        }
        result = super.projectManager.getProjectSummaryString(project, startTimeXml, endTimeXml);
      }
      // Otherwise, deal with the "numDays" URI string. 
      else {
        result = projectManager.getMultiDayProjectSummaryString(project, startTimeXml, numDaysInt);
      }
      return super.getStringRepresentation(result);
    }
    catch (Exception e) {
      setStatusInternalError(e);
    }
    return null;
  }
}
