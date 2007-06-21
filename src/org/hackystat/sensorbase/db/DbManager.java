package org.hackystat.sensorbase.db;

import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.db.derby.DerbyImplementation;
import org.hackystat.sensorbase.db.inmemory.InMemoryImplementation;
import org.hackystat.sensorbase.resource.sensordata.Tstamp;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.Server;
import org.w3c.dom.Document;

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
  private InMemoryImplementation inMemoryImpl;

  /**
   * Creates a new DbManager which manages access to the underlying persistency layer(s).
   * @param server The Restlet server instance. 
   */
  public DbManager(Server server) {
    this.server = server;
    this.derbyImpl = new DerbyImplementation(this.server);
    this.derbyImpl.initialize();
    this.inMemoryImpl = new InMemoryImplementation(this.server);
    this.inMemoryImpl.initialize();
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
    return true;  // only until the persistent storage impl is finished.
    //return this.derbyImpl.isFreshlyCreated();
  }

  /**
   * Persists a SensorData instance.  If the Owner/Timestamp already exists in the table, it is
   * overwritten.
   * @param data The sensor data. 
   * @param xmlSensorData The sensor data resource as an XML String.  
   * @param xmlSensorDataRef The sensor data resource as an XML resource reference
   */
  public void saveSensorData(SensorData data, String xmlSensorData, String xmlSensorDataRef) {
    this.derbyImpl.saveSensorData(data, xmlSensorData, xmlSensorDataRef);
    this.inMemoryImpl.saveSensorData(data, xmlSensorData, xmlSensorDataRef);
  }
  
  /**
   * Returns the XML Index for all sensor data.
   * @return The XML Document instance providing an index of all relevent sensor data resources.
   */
  public Document getSensorDataIndexDocument() {
    return this.inMemoryImpl.getSensorDataIndexDocument();
  }
  
  /**
   * Returns the XML Index for all sensor data for this user. 
   * @param user The User whose sensor data is to be returned. 
   * @return The XML Document instance providing an index of all relevent sensor data resources.
   */
  public Document getSensorDataIndexDocument(User user) {
    return this.inMemoryImpl.getSensorDataIndexDocument(user);
  }
  
  /**
   * Returns the XML Index for all sensor data for this user and sensor data type.
   * @param user The User whose sensor data is to be returned. 
   * @param sdtName The sensor data type name.
   * @return The XML Document instance providing an index of all relevent sensor data resources.
   */
  public Document getSensorDataIndexDocument(User user, String sdtName) {
    return this.inMemoryImpl.getSensorDataIndexDocument(user, sdtName);
  }
  
  /**
   * Returns true if the passed [user, timestamp] has sensor data defined for it.
   * @param user The user.
   * @param timestamp The timestamp
   * @return True if there is any sensor data for this [user, timestamp].
   */
  public boolean hasSensorData(User user, XMLGregorianCalendar timestamp) {
    return this.inMemoryImpl.hasSensorData(user, timestamp);
  }  
  
  /**
   * Returns true if the passed [user, timestamp] has sensor data defined for it.
   * Note that this method returns false if timestamp cannot be converted into XMLGregorianCalendar.
   * @param user The user.
   * @param timestamp The timestamp as a string.
   * @return True if there is any sensor data for this [key, timestamp].
   */
  public boolean hasSensorData(User user, String timestamp) {
    try {
      XMLGregorianCalendar tstamp = Tstamp.makeTimestamp(timestamp);
      return this.inMemoryImpl.hasSensorData(user, tstamp);
    }
    catch (Exception e) {
      return false;
    }
  }  
  
  /**
   * Ensures that sensor data with the given user and timestamp no longer exists.
   * @param user The user.
   * @param timestamp The timestamp associated with this sensor data.
   */
  public void deleteData(User user, XMLGregorianCalendar timestamp) {
    this.inMemoryImpl.deleteData(user, timestamp);
  }
  
  /**
   * Returns the SensorData instance, or null.
   * present in this manager.
   * @param user The user.
   * @param timestamp The timestamp associated with this sensor data.
   * @return The SensorData instance, or null.
   */
  public SensorData getSensorData(User user, XMLGregorianCalendar timestamp) {
    return this.inMemoryImpl.getSensorData(user, timestamp);
  }
  
  /**
   * Returns a (possibly empty) set of SensorData instances associated with the 
   * given user between the startTime and endTime. 
   * @param user The user
   * @param startTime The start time.
   * @param endTime The end time.
   * @return The set of SensorData instances. 
   */
  public Set<SensorData> getSensorData(User user, XMLGregorianCalendar startTime, 
      XMLGregorianCalendar endTime) {
    return this.inMemoryImpl.getSensorData(user, startTime, endTime);
  }

}
