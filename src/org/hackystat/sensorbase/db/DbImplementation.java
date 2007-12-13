package org.hackystat.sensorbase.db;

import java.util.List;
import java.util.logging.Logger;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.resource.projects.ProjectManager;
import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.sensordata.SensorDataManager;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.sensordatatypes.SdtManager;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
import org.hackystat.sensorbase.resource.users.UserManager;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.Server;
import org.hackystat.sensorbase.uripattern.UriPattern;

/**
 * Provides a specification of the operations that must be implemented by every
 * SensorBase storage system.  Also provides some 'helper' methods, which return
 * the Managers for the various resources. 
 * @author Philip Johnson
 */
public abstract class DbImplementation {
  
  /**
   * To be called as part of the startup process for a storage system. This method should:
   * <ul>
   * <li> Check to see if this storage system has already been created during a previous session.
   * <li> If no storage system exists, it should create one and initialize it appropriately.
   * </ul>
   */
  public abstract void initialize();
  
  /**
   * Returns true if the initialize() method did indeed create a fresh storage system.
   * This is used by the ResourceManagers to determine if they should read in default data or not.
   * @return True if the storage system is freshly created.
   */
  public abstract boolean isFreshlyCreated();
  
  /**
   * Returns the XML SensorDataIndex for all sensor data in this server.
   * @return The XML String containing an index to all Sensor Data.
   */
  public abstract String getSensorDataIndex();
  
  /**
   * Returns the XML SensorDataIndex for all sensor data for this user. 
   * @param user The User whose sensor data is to be returned. 
   * @return The XML String providing an index of all relevent sensor data resources.
   */
  public abstract String getSensorDataIndex(User user);
  
  /**
   * Returns the XML SensorDataIndex for all sensor data for this user and sensor data type.
   * @param user The User whose sensor data is to be returned. 
   * @param sdtName The sensor data type name.
   * @return The XML String providing an index of all relevent sensor data resources.
   */
  public abstract String getSensorDataIndex(User user, String sdtName);
  
  /**
   * Returns an XML SensorDataIndex representing the SensorData for the given user between
   * start and end time whose resource string matches at least one of the UriPatterns.
   * @param users The list of users whose SensorData will be returned.
   * @param startTime The earliest Sensor Data to be returned.
   * @param endTime The latest SensorData to be returned.
   * @param uriPatterns At least one UriPattern must match the SensorData resource field.
   * @param sdt The SDT of interest, or null if data from all SDTs should be retrieved.
   * @return An XML String containing a SensorDataIndex of matching SensorData. 
   */
  public abstract String getSensorDataIndex(List<User> users, XMLGregorianCalendar startTime, 
      XMLGregorianCalendar endTime, List<UriPattern> uriPatterns, String sdt);
  
  /**
   * Returns the XML SensorDataIndex for all sensor data for the given user that arrived
   * at the server since the given start and end timestamps.  
   * This method uses the LastMod timestamp
   * rather than the "regular" timestamp, and is used for real-time monitoring of data
   * arriving at the server. 
   * @param user The user whose data is being monitored.
   * @param lastModStartTime  The beginning lastMod timestamp of interest. 
   * @param lastModEndTime  The ending lastMod timestamp of interest. 
   * @return The XML SensorDataIndex for the recently arrived data based upon the lastMod tstamps.
   */
  public abstract String getSensorDataIndexLastMod(User user, XMLGregorianCalendar lastModStartTime,
      XMLGregorianCalendar lastModEndTime);
  
  /**
   * Returns the SensorData instance as XML string, or null if not found.
   * @param user The user.
   * @param timestamp The timestamp associated with this sensor data.
   * @return The SensorData XML string, or null.
   */
  public abstract String getSensorData(User user, XMLGregorianCalendar timestamp);
  
  /**
   * Returns true if the passed [key, timestamp] has sensor data defined for it.
   * @param user The user.
   * @param timestamp The timestamp
   * @return True if there is any sensor data for this [key, sdtName, timestamp].
   */
  public abstract boolean hasSensorData(User user, XMLGregorianCalendar timestamp);
  
  /**
   * Persists a SensorData instance.  If SensorData with this [email, timestamp] 
   * already exists in the storage system, it should be overwritten.
   * @param data The sensor data. 
   * @param xmlSensorData The SensorData marshalled into an XML String.  
   * @param xmlSensorDataRef The corresponding SensorDataRef marshalled into an XML String
   * @return True if the sensor data was successfully inserted.
   */
  public abstract boolean storeSensorData(SensorData data, String xmlSensorData, 
      String xmlSensorDataRef);
  

  /**
   * Ensures that sensor data with the given user and timestamp is no longer
   * present in this manager.
   * @param user The user.
   * @param timestamp The timestamp associated with this sensor data.
   */
  public abstract void deleteSensorData(User user, XMLGregorianCalendar timestamp);
  
  /**
   * Ensures that sensor data with the given user is no longer present in this manager.
   * @param user The user.
   */
  public abstract void deleteSensorData(User user);
  
  /**
   * Returns the XML SensorDataTypeIndex for all SDTs in this server.
   * @return The XML String containing an index to all SensorDataTypes.
   */
  public abstract String getSensorDataTypeIndex();
  
  /**
   * Returns the SensorDataType instance as XML string, or null if not found.
   * @param sdtName The SDT name.
   * @return The SensorDataType XML string, or null.
   */
  public abstract String getSensorDataType(String sdtName);
  
  
  /**
   * Persists a SensorDataType instance.  If a SensorDataType with this name
   * already exists in the storage system, it will be overwritten.
   * @param sdt The sensor data type 
   * @param xmlSensorDataType The SensorDataType marshalled into an XML String.  
   * @param xmlSensorDataTypeRef The corresponding SensorDataTypeRef marshalled into an XML String
   * @return True if the SDT was successfully inserted.
   */
  public abstract boolean storeSensorDataType(SensorDataType sdt, String xmlSensorDataType, 
      String xmlSensorDataTypeRef);
  
  /**
   * Ensures that the SensorDataType with the given name is no longer present in this manager.
   * @param sdtName The SDT name.
   */
  public abstract void deleteSensorDataType(String sdtName);
  
  /**
   * Returns the XML UserIndex for all Users in this server.
   * @return The XML String containing an index to all Users.
   */
  public abstract String getUserIndex();
  
  /**
   * Returns the User instance as XML string, or null if not found.
   * @param email The user's email.
   * @return The User XML string, or null.
   */
  public abstract String getUser(String email);
  
  /**
   * Persists a User instance.  If a User with this name
   * already exists in the storage system, it will be overwritten.
   * @param user The user
   * @param xmlUser The User marshalled into an XML String.  
   * @param xmlUserRef The corresponding UserRef marshalled into an XML String
   * @return True if the user was successfully inserted.
   */
  public abstract boolean storeUser(User user, String xmlUser, String xmlUserRef);
  
  /**
   * Ensures that the User with the given email is no longer present in this manager.
   * @param email The user's email address.
   */
  public abstract void deleteUser(String email);
  
  /**
   * Returns the XML ProjectIndex for all Projects in this server.
   * @return The XML String containing an index to all Projects.
   */
  public abstract String getProjectIndex();
  
  /**
   * Returns the Project instance as XML string, or null if not found.
   * @param owner The user who owns the project.
   * @param projectName The name of the Project.
   * @return The Project XML string, or null.
   */
  public abstract String getProject(User owner, String projectName);
  
  /**
   * Persists a Project instance.  If a Project with this owner and name
   * already exists in the storage system, it will be overwritten.
   * @param project The Project.
   * @param xmlProject The Project marshalled into an XML String.  
   * @param xmlProjectRef The corresponding ProjectRef marshalled into an XML String
   * @return True if the user was successfully inserted.
   */
  public abstract boolean storeProject(Project project, String xmlProject, String xmlProjectRef);
  
  /**
   * Ensures that the Project with the given owner and projectName is no longer present in the db.
   * @param owner The User who owns this project.
   * @param projectName The name of the Project.
   */
  public abstract void deleteProject(User owner, String projectName);
  
  
  /** Keeps a pointer to this Server for use in accessing the managers. */
  protected Server server;
  
  /** Keep a pointer to the Logger. */
  protected Logger logger;
  
  /**
   * Constructs a new DbImplementation.
   * @param server The server. 
   */
  public DbImplementation(Server server) {
    this.server = server;
    this.logger = server.getLogger();
  }
  
  /**
   * Returns the UserManager associated with this server.
   * Since the DbManager is initialized before all other managers, we will simply 
   * get these other Managers on demand and not cache them. 
   * @return The User Manager. 
   */
  protected UserManager getUserManager() {
    return (UserManager)server.getContext().getAttributes().get("UserManager");
  }
  
  /**
   * Returns the SensorDataManager associated with this server. 
   * Since the DbManager is initialized before all other managers, we will simply 
   * get these other Managers on demand and not cache them. 
   * @return The Sensor Data Manager. 
   */
  protected SensorDataManager getSensorDataManager() {
    return (SensorDataManager)server.getContext().getAttributes().get("SensorDataManager");
  }
  
  /**
   * Returns the SdtManager associated with this server. 
   * Since the DbManager is initialized before all other managers, we will simply 
   * get these other Managers on demand and not cache them. 
   * @return The SdtManager. 
   */
  protected SdtManager getSdtManager() {
    return (SdtManager)server.getContext().getAttributes().get("SdtManager");
  }
  
  /**
   * Returns the ProjectManager associated with this server. 
   * Since the DbManager is initialized before all other managers, we will simply 
   * get these other Managers on demand and not cache them. 
   * @return The ProjectManager. 
   */
  protected ProjectManager getProjectManager() {
    return (ProjectManager)server.getContext().getAttributes().get("ProjectManager");
  }
  
}
