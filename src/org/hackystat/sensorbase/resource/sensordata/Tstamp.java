package org.hackystat.sensorbase.resource.sensordata;

import java.util.GregorianCalendar;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Utility class that facilitates Timestamp representation and processing. 
 * There are too many classes already named "Timestamp", thus the abbreviated name.
 * @author Philip Johnson
 */
public class Tstamp {
  
  /**
   * Returns true if the passed string can be parsed into an XMLGregorianCalendar object.
   * @param lexicalRepresentation The string representation.
   * @return True if the string is a legal XMLGregorianCalendar. 
   */
  public static boolean isTimestamp(String lexicalRepresentation) {
    try {
      DatatypeFactory factory = DatatypeFactory.newInstance();
      factory.newXMLGregorianCalendar(lexicalRepresentation);
      return true;
      
    }
    catch (Exception e) {
      return false;
    }
  }
  
  /**
   * Returns an XMLGregorianCalendar, given its string representation. 
   * @param lexicalRepresentation The string representation.
   * @return The timestamp. 
   * @throws Exception If the string cannot be parsed into a timestamp. 
   */
  public static XMLGregorianCalendar makeTimestamp(String lexicalRepresentation) throws Exception {
    DatatypeFactory factory = DatatypeFactory.newInstance();
    return factory.newXMLGregorianCalendar(lexicalRepresentation);
  }
  
  /**
   * Converts a javax.sql.Timestamp into a javax.xml.datatype.XMLGregorianCalendar.
   * @param tstamp The javax.sql.Timestamp
   * @return An new instance of a javax.xml.datatype.XmlGregorianCalendar
   */
  public static XMLGregorianCalendar makeTimestamp(java.sql.Timestamp tstamp) {
    DatatypeFactory factory = null;
    try {
      factory = DatatypeFactory.newInstance();
      GregorianCalendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(tstamp.getTime());
      return factory.newXMLGregorianCalendar(calendar);
    }
    catch (DatatypeConfigurationException e) {
      Logger logger = LogManager.getLogManager().getLogger("org.hackystat.sensorbase");
      logger.warning("Failed to create DatatypeFactory in makeTimestamp()");
    }
    return null;
  }
  
  /**
   * Returns a new java.sql.Timestamp created from a javax.xml.datatype.XMLGregorianCalendar.
   * @param calendar The XML timestamp.
   * @return The SQL timestamp.
   */
  public static java.sql.Timestamp makeTimestamp(XMLGregorianCalendar calendar) {
    return new java.sql.Timestamp(calendar.toGregorianCalendar().getTimeInMillis());
  }
  
  /**
   * Returns an XMLGregorianCalendar corresponding to the current time.
   * @return The timestamp. 
   */
  public static XMLGregorianCalendar makeTimestamp() {
    try {
      DatatypeFactory factory = DatatypeFactory.newInstance();
      return factory.newXMLGregorianCalendar(new GregorianCalendar());
    }
    catch (Exception e) {
      throw new RuntimeException("Bad datatypeFactory", e);
    }
  }
  
  /**
   * Returns an XMLGregorianCalendar corresponding to 01-Jan-1000.
   * @return The timestamp. 
   */
  public static XMLGregorianCalendar getDefaultProjectStartTime() {
    try {
      DatatypeFactory factory = DatatypeFactory.newInstance();
      XMLGregorianCalendar startTime = factory.newXMLGregorianCalendar();
      startTime.setDay(1);
      startTime.setMonth(1);
      startTime.setYear(1000);
      startTime.setTime(0, 0, 0);
      startTime.setMillisecond(000); //NOPMD
      return startTime; 
    }
    catch (Exception e) {
      throw new RuntimeException("Bad datatypeFactory", e);
    }
  }

  /**
   * Returns an XMLGregorianCalendar corresponding to 01-Jan-3000.
   * @return The timestamp. 
   */
  public static XMLGregorianCalendar getDefaultProjectEndTime() {
    try {
      DatatypeFactory factory = DatatypeFactory.newInstance();
      XMLGregorianCalendar endTime = factory.newXMLGregorianCalendar();
      endTime.setDay(1);
      endTime.setMonth(1);
      endTime.setYear(3000);
      endTime.setTime(23, 59, 59);
      endTime.setMillisecond(999);      
      return endTime; 
    }
    catch (Exception e) {
      throw new RuntimeException("Bad datatypeFactory", e);
    }
  }  
  
  /**
   * Returns true if tstamp is equal to or between start and end.
   * @param start The start time.
   * @param end The end time.
   * @param tstamp The timestamp to test. 
   * @return True if between this interval.
   */  
  public static boolean inBetween(XMLGregorianCalendar start, XMLGregorianCalendar end, 
      XMLGregorianCalendar tstamp) {
    if ((start.compare(tstamp) == DatatypeConstants.EQUAL) ||
        (end.compare(tstamp) == DatatypeConstants.EQUAL)) {
      return true;
    }
    if ((start.compare(tstamp) == DatatypeConstants.LESSER) &&
        (end.compare(tstamp) == DatatypeConstants.GREATER)) {
      return true;
    }
    return false;
  }
  
  /**
   * Returns true if time1 > time2.
   * @param time1 The first time. 
   * @param time2 The second time. 
   * @return True if time1 > time2
   */
  public static boolean greaterThan(XMLGregorianCalendar time1, XMLGregorianCalendar time2) {
    return time1.compare(time2) == DatatypeConstants.GREATER;
  }
  
  /**
   * Returns true if timeString1 > timeString2.
   * Throws an unchecked IllegalArgument exception if the strings can't be converted to timestamps.
   * @param timeString1 The first time. 
   * @param timeString2 The second time. 
   * @return True if time1 > time2
   */
  public static boolean greaterThan(String timeString1, String timeString2) {
    try {
      DatatypeFactory factory = DatatypeFactory.newInstance();
      XMLGregorianCalendar time1 = factory.newXMLGregorianCalendar(timeString1);
      XMLGregorianCalendar time2 = factory.newXMLGregorianCalendar(timeString2);
      return time1.compare(time2) == DatatypeConstants.GREATER;
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Illegal timestring", e);
    }
  }
  
  /**
   * Returns true if time1 < time2.
   * @param time1 The first time. 
   * @param time2 The second time. 
   * @return True if time1 < time2
   */
  public static boolean lessThan(XMLGregorianCalendar time1, XMLGregorianCalendar time2) {
    return time1.compare(time2) == DatatypeConstants.LESSER;
  }

  /**
   * Returns true if time1 equals time2
   * @param time1 The first time. 
   * @param time2 The second time. 
   * @return True if time1 equals time2
   */
  public static boolean equal(XMLGregorianCalendar time1, XMLGregorianCalendar time2) {
    return time1.compare(time2) == DatatypeConstants.EQUAL;
  }
  
  
}
