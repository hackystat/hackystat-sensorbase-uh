package org.hackystat.sensorbase.resource.sensordata;

import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Utility class that facilitates Timestamp representation and processing. 
 * @author Philip Johnson
 */
public class Timestamp {
  
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
