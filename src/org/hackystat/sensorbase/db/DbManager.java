package org.hackystat.sensorbase.db;

import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.db.derby.DerbyImplementation;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.Server;
import org.hackystat.sensorbase.uripattern.UriPattern;

/**
 * Provides an interface to storage for the four resources managed by the SensorBase.
 * Currently we have two storage mechanisms: a persistent store which is implemented by
 * an embedded Derby database, and a InMemory store which we are using a cache.  In 
 * future, we can enhance this DbManager to enable "swappable" persistent stores and other
 * alternatives to storage. 
 * @author Philip Johnson
 */
public class DbManager {
  
  /** The Server instance this DbManager is attached to. */
  private Server server;
  
  /** The Derby Storage system. */
  private DerbyImplementation derbyImpl;

  /** The InMemory Storage system. */
  //private InMemoryImplementation inMemoryImpl;
  
  /** The SensorDataIndex open tag. */
  public static final String sensorDataIndexOpenTag = "<SensorDataIndex>";
  
  /** The SensorDataIndex close tag. */
  public static final String sensorDataIndexCloseTag = "</SensorDataIndex>";

  /**
   * Creates a new DbManager which manages access to the underlying persistency layer(s).
   * @param server The Restlet server instance. 
   */
  public DbManager(Server server) {
    this.server = server;
    this.derbyImpl = new DerbyImplementation(this.server);
    this.derbyImpl.initialize();
    //this.inMemoryImpl = new InMemoryImplementation(this.server);
    //this.inMemoryImpl.initialize();
  }
  
  /**
   * Returns true if this Db was freshly initialized upon instantiation of the DbManager.
   * This indicates the need for the various Resource Managers to read in the default data
   * to this database.  
   * We only care about the persistent store. We know that the InMemoryImpl will always be
   * "fresh" each time we come up.
   * @return True if this DB was freshly created or not.
   */
  public boolean isFreshDb() {
    return true;  
  }

  /**
   * Persists a SensorData instance.  If the Owner/Timestamp already exists in the table, it is
   * overwritten.
   * @param data The sensor data. 
   * @param xmlSensorData The sensor data resource as an XML String.  
   * @param xmlSensorDataRef The sensor data resource as an XML resource reference
   */
  public void storeSensorData(SensorData data, String xmlSensorData, String xmlSensorDataRef) {
    this.derbyImpl.storeSensorData(data, xmlSensorData, xmlSensorDataRef);
  }
  
  /**
   * Persists a SensorDataType instance.  If the SDT name already exists in the table, it is
   * overwritten.
   * @param sdt The sensor data. 
   * @param xmlSensorDataType The SDT resource as an XML String.  
   * @param xmlSensorDataTypeRef The SDT as an XML resource reference
   */
  public void storeSensorDataType(SensorDataType sdt, String xmlSensorDataType, 
      String xmlSensorDataTypeRef) {
    this.derbyImpl.storeSensorDataType(sdt, xmlSensorDataType, xmlSensorDataTypeRef);
  }
  
  /**
   * Returns the XML SensorDataIndex for all sensor data.
   * @return An XML String providing an index of all sensor data resources.
   */
  public String getSensorDataIndex() {
    return this.derbyImpl.getSensorDataIndex();
  }
  
  /**
   * Returns the XML SensorDataIndex for all sensor data for this user. 
   * @param user The User whose sensor data is to be returned. 
   * @return The XML String providing an index of all relevent sensor data resources.
   */
  public String getSensorDataIndex(User user) {
    return this.derbyImpl.getSensorDataIndex(user);
  }
  
  /**
   * Returns the XML SensorDataIndex for all sensor data for this user and sensor data type.
   * @param user The User whose sensor data is to be returned. 
   * @param sdtName The sensor data type name.
   * @return The XML Document instance providing an index of all relevent sensor data resources.
   */
  public String getSensorDataIndex(User user, String sdtName) {
    return this.derbyImpl.getSensorDataIndex(user, sdtName);
  }
  
  /**
   * Returns the XML SensorDataIndex for all sensor data matching this user, start/end time, and 
   * whose resource string matches at least one in the list of UriPatterns. 
   * @param user The user. 
   * @param startTime The start time. 
   * @param endTime The end time. 
   * @param uriPatterns A list of UriPatterns. 
   * @return The XML SensorDataIndex string corresponding to the matching sensor data. 
   */
  public String getSensorDataIndex(User user, XMLGregorianCalendar startTime, 
      XMLGregorianCalendar endTime, List<UriPattern> uriPatterns) {
    return this.derbyImpl.getSensorDataIndex(user, startTime, endTime, uriPatterns);
  }
  
  /**
   * Returns the XML SensorDataTypeIndex for all sensor data.
   * @return An XML String providing an index of all SDT resources.
   */
  public String getSensorDataTypeIndex() {
    return this.derbyImpl.getSensorDataTypeIndex();
  }
  
  /**
   * Returns true if the passed [user, timestamp] has sensor data defined for it.
   * @param user The user.
   * @param timestamp The timestamp
   * @return True if there is any sensor data for this [user, timestamp].
   */
  public boolean hasSensorData(User user, XMLGregorianCalendar timestamp) {
    return this.derbyImpl.hasSensorData(user, timestamp);
  }  
  
  
  /**
   * Ensures that sensor data with the given user and timestamp no longer exists.
   * @param user The user.
   * @param timestamp The timestamp associated with this sensor data.
   */
  public void deleteSensorData(User user, XMLGregorianCalendar timestamp) {
    this.derbyImpl.deleteSensorData(user, timestamp);
  }
  
  /**
   * Ensures that the SensorDataType with the given name no longer exists.
   * @param sdtName The SDT name.
   */
  public void deleteSensorDataType(String sdtName) {
    this.derbyImpl.deleteSensorDataType(sdtName);
  }
  
  /**
   * Returns the SensorData instance as an XML string, or null.
   * @param user The user.
   * @param timestamp The timestamp associated with this sensor data.
   * @return The SensorData instance as an XML string, or null.
   */
  public String getSensorData(User user, XMLGregorianCalendar timestamp) {
    return this.derbyImpl.getSensorData(user, timestamp);
  }
  
  /**
   * Returns the SensorDataType instance as an XML string, or null.
   * @param sdtName The name of the SDT to retrieve.
   * @return The SensorDataType instance as an XML string, or null.
   */
  public String getSensorDataType(String sdtName) {
    return this.derbyImpl.getSensorDataType(sdtName);
  }
  
}
