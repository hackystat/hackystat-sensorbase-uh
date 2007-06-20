package org.hackystat.sensorbase.db;

import java.util.Set;
import java.util.logging.Logger;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.resource.projects.ProjectManager;
import org.hackystat.sensorbase.resource.sensordata.SensorDataManager;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.sensordatatypes.SdtManager;
import org.hackystat.sensorbase.resource.users.UserManager;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.Server;
import org.w3c.dom.Document;

/**
 * Provides a specification of the operations that must be implemented by every
 * SensorBase storage system.  Also provides some 'helper' methods, which return
 * the Managers for the various resources. 
 * @author Philip Johnson
 */
public abstract class DbImplementation {
  
  /**
   * To be called as part of the startup process for a DB implementation. This method should:
   * <ul>
   * <li> Check to see if a database has been created.
   * <li> If no database exists, it should create one and initialize the tables.
   * </ul>
   */
  public abstract void initialize();
  
  /**
   * Returns true if the initialize() method created a fresh database with empty tables. 
   * This is used by the ResourceManagers to determine if they should read in default data or not.
   * @return True if the database is freshly created.
   */
  public abstract boolean isFreshlyCreated();
  
  /**
   * Persists a SensorData instance.  If the Owner/Timestamp already exists in the table, it is
   * overwritten.
   * @param data The sensor data. 
   * @param xmlSensorData The sensor data resource as an XML String.  
   * @param xmlSensorDataRef The sensor data resource as an XML resource reference
   * @return True if the sensor data was successfully inserted.
   */
  public abstract boolean saveSensorData(SensorData data, String xmlSensorData, 
      String xmlSensorDataRef);
  
  /**
   * Returns a Document representing a SensorDataIndex with all Sensor Data. 
   * @return The Document containing an index to all Sensor Data.
   */
  public abstract Document getSensorDataIndexDocument();
  
  /**
   * Returns the XML Index for all sensor data for this user. 
   * @param user The User whose sensor data is to be returned. 
   * @return The XML Document instance providing an index of all relevent sensor data resources.
   */
  public abstract Document getSensorDataIndexDocument(User user);
  
  /**
   * Returns the XML Index for all sensor data for this user and sensor data type.
   * @param user The User whose sensor data is to be returned. 
   * @param sdtName The sensor data type name.
   * @return The XML Document instance providing an index of all relevent sensor data resources.
   */
  public abstract Document getSensorDataIndexDocument(User user, String sdtName);
  
  /**
   * Returns true if the passed [key, timestamp] has sensor data defined for it.
   * @param user The user.
   * @param timestamp The timestamp
   * @return True if there is any sensor data for this [key, sdtName, timestamp].
   */
  public abstract boolean hasSensorData(User user, XMLGregorianCalendar timestamp);
  
  /**
   * Returns true if the passed [user, timestamp] has sensor data defined for it.
   * Note that this method returns false if timestamp cannot be converted into XMLGregorianCalendar.
   * @param user The user.
   * @param timestamp The timestamp as a string.
   * @return True if there is any sensor data for this [key, sdtName, timestamp].
   */
  public abstract boolean hasSensorData(User user, String timestamp);

  /**
   * Ensures that sensor data with the given user and timestamp is no longer
   * present in this manager.
   * @param user The user.
   * @param timestamp The timestamp associated with this sensor data.
   */
  public abstract void deleteData(User user, XMLGregorianCalendar timestamp);
 
  /**
   * Returns the SensorData instance, or null if not found.
   * @param user The user.
   * @param timestamp The timestamp associated with this sensor data.
   * @return The SensorData instance, or null.
   */
  public abstract SensorData getSensorData(User user, XMLGregorianCalendar timestamp);
  
  /**
   * Returns a (possibly empty) set of SensorData instances associated with the 
   * given user between the startTime and endTime. 
   * @param user The user
   * @param startTime The start time.
   * @param endTime The end time.
   * @return The set of SensorData instances. 
   */
  public abstract Set<SensorData> getSensorData(User user, XMLGregorianCalendar startTime, 
      XMLGregorianCalendar endTime);
  
  /** Keeps a pointer to this Server for use in accessing the managers. */
  private Server server;
  
  /** Keep a pointer to the Logger. */
  protected Logger logger;
  
  /**
   * Stores the server instance. 
   * @param server The server. 
   */
  public DbImplementation(Server server) {
    this.server = server;
    this.logger = SensorBaseLogger.getLogger();
  }
  
  /**
   * Returns the UserManager.
   * Since the DbManager is initialized before all other managers, we will simply 
   * get these other Managers on demand and not cache them. 
   * @return The User Manager. 
   */
  protected UserManager getUserManager() {
    return (UserManager)server.getContext().getAttributes().get("UserManager");
  }
  
  /**
   * Returns the SensorDataManager.
   * Since the DbManager is initialized before all other managers, we will simply 
   * get these other Managers on demand and not cache them. 
   * @return The Sensor Data Manager. 
   */
  protected SensorDataManager getSensorDataManager() {
    return (SensorDataManager)server.getContext().getAttributes().get("SensorDataManager");
  }
  
  /**
   * Returns the SdtManager.
   * Since the DbManager is initialized before all other managers, we will simply 
   * get these other Managers on demand and not cache them. 
   * @return The SdtManager. 
   */
  protected SdtManager getSdtManager() {
    return (SdtManager)server.getContext().getAttributes().get("SdtManager");
  }
  
  /**
   * Returns the ProjectManager.
   * Since the DbManager is initialized before all other managers, we will simply 
   * get these other Managers on demand and not cache them. 
   * @return The ProjectManager. 
   */
  protected ProjectManager getProjectManager() {
    return (ProjectManager)server.getContext().getAttributes().get("ProjectManager");
  }
  
}
