package org.hackystat.sensorbase.resource.projects;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.utilities.tstamp.Tstamp;

/**
 * Methods that really should be part of the Project JAXB class, but I don't know
 * how to extend that class with additional methods.
 *  
 * @author Philip Johnson
 */
public class ProjectUtils {
  
  /**
   * Returns true if the passed date falls within the project start date. Due to time zones
   * and other issues, we are going to be lenient with the definition.  Basically, we will 
   * accept the day before the project start date, and any day afterwards. 
   * @param project The project.
   * @param date The date of interest. 
   * @return True if date is after the day before the project start date.
   */
  public static boolean isValidStartTime(Project project, XMLGregorianCalendar date) {
    XMLGregorianCalendar lenientStart = Tstamp.incrementDays(project.getStartTime(), -2);
    return Tstamp.lessThan(lenientStart, date);
  }
  
  /**
   * Returns true if the passed date falls within the project end date.  Due to time zones
   * and other issues, we will accept the day after the official project end date as well.
   * @param project The project.
   * @param date The date of interest. 
   * @return True if date is less than to equal to the project start date. 
   */
  public static boolean isValidEndTime(Project project, XMLGregorianCalendar date) {
    XMLGregorianCalendar lenientEnd = Tstamp.incrementDays(project.getEndTime(), 2);
    return Tstamp.lessThan(date, lenientEnd); 
  }

  /**
   * Returns true if the start and end date constitute a valid project interval. This means
   * that isValidStartDate() and isValidEndDate() return true, and that start is less than end.
   * @param project The project. 
   * @param start The proposed start date.
   * @param end The proposed end date. 
   * @return True if start and end are acceptable according to the project definition.
   */
  public static boolean isValidInterval(Project project, XMLGregorianCalendar start, 
      XMLGregorianCalendar end) {
    return (isValidStartTime(project, start) && isValidEndTime(project, end) &&
    Tstamp.lessThan(start, end));
  }
}
