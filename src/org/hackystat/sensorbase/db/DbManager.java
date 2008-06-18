package org.hackystat.sensorbase.db;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;
import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.projects.jaxb.ProjectSummary;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.Server;
import static org.hackystat.sensorbase.server.ServerProperties.DB_IMPL_KEY;
import org.hackystat.utilities.stacktrace.StackTrace;

/**
 * Provides an interface to storage for the resources managed by the SensorBase.
 * Currently we have one storage mechanisms: a persistent store which is implemented by
 * an embedded Derby database.
 * @author Philip Johnson
 */
public class DbManager {
  
  /** The chosen Storage system. */
  private DbImplementation dbImpl;

  /** The SensorDataIndex open tag. */
  public static final String sensorDataIndexOpenTag = "<SensorDataIndex>";
  
  /** The SensorDataIndex close tag. */
  public static final String sensorDataIndexCloseTag = "</SensorDataIndex>";

  /**
   * Creates a new DbManager which manages access to the underlying persistency layer(s).
   * Instantiates the underlying storage system to use. 
   * @param server The Restlet server instance. 
   */
  public DbManager(Server server) {
    //Defaults to: "org.hackystat.sensorbase.db.derby.DerbyImplementation"
    String dbClassName = server.getServerProperties().get(DB_IMPL_KEY); 
    Class<?> dbClass = null;
    //First, try to find the class specified in the sensorbase.properties file (or the default) 
    try {
      dbClass = Class.forName(dbClassName);
    }
    catch (ClassNotFoundException e) {
      String msg = "DB error instantiating " + dbClassName + ". Could not find this class.";
      server.getLogger().warning(msg + "\n" + StackTrace.toString(e));
      throw new IllegalArgumentException(e);
    }
    // Next, try to find a constructor that accepts a Server as its parameter. 
    Class<?>[] constructorParam = {org.hackystat.sensorbase.server.Server.class};
    Constructor<?> dbConstructor = null;
    try {
      dbConstructor = dbClass.getConstructor(constructorParam);
    }
    catch (Exception e) {
      String msg = "DB error instantiating " + dbClassName + ". Could not find Constructor(server)";
      server.getLogger().warning(msg + "\n" + StackTrace.toString(e));
      throw new IllegalArgumentException(e);
    }
    // Next, try to create an instance of DbImplementation from the Constructor.
    Object[] serverArg = {server};
    try {
      this.dbImpl = (DbImplementation) dbConstructor.newInstance(serverArg);
    }
    catch (Exception e) {
      String msg = "DB error instantiating " + dbClassName + ". Could not create instance.";
      server.getLogger().warning(msg + "\n" + StackTrace.toString(e));
      throw new IllegalArgumentException(e);
    }
    this.dbImpl.initialize();
  }
  
  /**
   * Persists a SensorData instance.  If the Owner/Timestamp already exists in the table, it is
   * overwritten.
   * @param data The sensor data. 
   * @param xmlSensorData The sensor data resource as an XML String.  
   * @param xmlSensorDataRef The sensor data resource as an XML resource reference
   */
  public void storeSensorData(SensorData data, String xmlSensorData, String xmlSensorDataRef) {
    this.dbImpl.storeSensorData(data, xmlSensorData, xmlSensorDataRef);
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
    this.dbImpl.storeSensorDataType(sdt, xmlSensorDataType, xmlSensorDataTypeRef);
  }
  
  /**
   * Persists a User instance.  If the User email already exists in the table, it is
   * overwritten.
   * @param user The user instance.
   * @param xmlUser The User resource as an XML String.  
   * @param xmlUserRef The User as an XML resource reference
   */
  public void storeUser(User user, String xmlUser, String xmlUserRef) {
    this.dbImpl.storeUser(user, xmlUser, xmlUserRef);
  }
  
  /**
   * Persists a Project instance.  If the Project already exists in the db, it is
   * overwritten.
   * @param project The project instance.
   * @param xmlProject The Project resource as an XML String.  
   * @param xmlProjectRef The Project as an XML resource reference
   */
  public void storeProject(Project project, String xmlProject, String xmlProjectRef) {
    this.dbImpl.storeProject(project, xmlProject, xmlProjectRef);
  }
  
  /**
   * Returns the XML SensorDataIndex for all sensor data.
   * @return An XML String providing an index of all sensor data resources.
   */
  public String getSensorDataIndex() {
    return this.dbImpl.getSensorDataIndex();
  }
  
 
  /**
   * Returns the XML SensorDataIndex for all sensor data for this user. 
   * @param user The User whose sensor data is to be returned. 
   * @return The XML String providing an index of all relevent sensor data resources.
   */
  public String getSensorDataIndex(User user) {
    return this.dbImpl.getSensorDataIndex(user);
  }
  
  /**
   * Returns the XML SensorDataIndex for all sensor data for this user and sensor data type.
   * @param user The User whose sensor data is to be returned. 
   * @param sdtName The sensor data type name.
   * @return The XML Document instance providing an index of all relevent sensor data resources.
   */
  public String getSensorDataIndex(User user, String sdtName) {
    return this.dbImpl.getSensorDataIndex(user, sdtName);
  }
  
  /**
   * Returns the XML SensorDataIndex for all sensor data matching this user, start/end time, and 
   * whose resource string matches at least one in the list of UriPatterns. 
   * @param users The list of users. 
   * @param startTime The start time. 
   * @param endTime The end time. 
   * @param uriPatterns A list of UriPatterns. 
   * @param sdt The SensorDataType of interest, or null if all data of all SDTs should be retrieved.
   * @return The XML SensorDataIndex string corresponding to the matching sensor data. 
   */
  public String getSensorDataIndex(List<User> users, XMLGregorianCalendar startTime, 
      XMLGregorianCalendar endTime, List<String> uriPatterns, String sdt) {
    return this.dbImpl.getSensorDataIndex(users, startTime, endTime, uriPatterns, sdt);
  }
  
  /**
   * Returns the XML SensorDataIndex for all sensor data matching this user, start/end time, and 
   * whose resource string matches at least one in the list of UriPatterns. 
   * @param users The list of users. 
   * @param startTime The start time. 
   * @param endTime The end time. 
   * @param uriPatterns A list of UriPatterns. 
   * @param sdt The SensorDataType of interest, or null if all data of all SDTs should be retrieved.
   * @param tool The tool of interest.
   * @return The XML SensorDataIndex string corresponding to the matching sensor data. 
   */
  public String getSensorDataIndex(List<User> users, XMLGregorianCalendar startTime, 
      XMLGregorianCalendar endTime, List<String> uriPatterns, String sdt, String tool) {
    return this.dbImpl.getSensorDataIndex(users, startTime, endTime, uriPatterns, sdt, tool);
  }
  
  /**
   * Returns the XML SensorDataIndex for all sensor data matching these users, start/end time, and 
   * whose resource string matches at least one in the list of UriPatterns. 
   * Client must guarantee that startTime and endTime are within Project dates, and that 
   * startIndex and maxInstances are non-negative.
   * @param users The users. 
   * @param startTime The start time. 
   * @param endTime The end time. 
   * @param uriPatterns A list of UriPatterns. 
   * @param startIndex The starting index.
   * @param maxInstances The maximum number of instances to return.
   * @return The XML SensorDataIndex string corresponding to the matching sensor data. 
   */
  public String getSensorDataIndex(List<User> users, XMLGregorianCalendar startTime, 
      XMLGregorianCalendar endTime, List<String> uriPatterns, int startIndex, int maxInstances) {
    return this.dbImpl.getSensorDataIndex(users, startTime, endTime, uriPatterns, startIndex,
        maxInstances);
  }  
  
  /**
   * Returns the XML SensorDataIndex for all sensor data for the given user that arrived
   * at the server between the two timestamps.  This method uses the LastMod timestamp
   * rather than the "regular" timestamp, and is used for real-time monitoring of data
   * arriving at the server. 
   * @param user The user whose data is being monitored.
   * @param lastModStartTime  The lastMod startTime of interest. 
   * @param lastModEndTime  The lastMod endTime of interest. 
   * @return The XML SensorDataIndex for the data that arrived between the two timestamps.
   */
  public String getSensorDataIndexLastMod(User user, XMLGregorianCalendar lastModStartTime,
      XMLGregorianCalendar lastModEndTime) {
    return this.dbImpl.getSensorDataIndexLastMod(user, lastModStartTime, lastModEndTime);
  }
  
  /**
   * Returns the XML SensorDataTypeIndex for all sensor data.
   * @return An XML String providing an index of all SDT resources.
   */
  public String getSensorDataTypeIndex() {
    return this.dbImpl.getSensorDataTypeIndex();
  }
  
  /**
   * Returns the XML UserIndex for all Users..
   * @return An XML String providing an index of all User resources.
   */
  public String getUserIndex() {
    return this.dbImpl.getUserIndex();
  }
  
  /**
   * Returns the XML Project Index for all Projects.
   * @return An XML String providing an index of all Project resources.
   */
  public String getProjectIndex() {
    return this.dbImpl.getProjectIndex();
  }
  
  
  /**
   * Returns a ProjectSummary instance constructed for the given Project between the startTime
   * and endTime. This summary provides a breakdown of the number of sensor data instances found
   * of the given type during the given time period.
   * @param users The list of users in this project.
   * @param startTime The startTime
   * @param endTime The endTime.
   * @param uriPatterns The uriPatterns for this project.
   * @param href The URL naming this resource.
   * @return The ProjectSummary instance. 
   */
  public ProjectSummary getProjectSummary(List<User> users, XMLGregorianCalendar startTime, 
      XMLGregorianCalendar endTime, List<String> uriPatterns, String href) {
    return this.dbImpl.getProjectSummary(users, startTime, endTime, uriPatterns, href);
  }
  
  /**
   * Returns a SensorDataIndex representing the "snapshot" of sensor data in the given time 
   * interval for the given sdt and tool (if tool is not null).  The "snapshot" is the set of
   * sensor data with the most recent runtime value during the interval.
   * @param users The list of users in this project.
   * @param startTime The startTime
   * @param endTime The endTime.
   * @param uriPatterns The uriPatterns for this project.
   * @param sdt The sensor data type of the sensor data of interest.
   * @param tool The tool associated with this snapshot, or null if any tool will do.
   * @return The SensorDataIndex with the latest runtime. 
   */
  public String getProjectSensorDataSnapshot(List<User> users, XMLGregorianCalendar startTime, 
      XMLGregorianCalendar endTime, List<String> uriPatterns, String sdt, String tool) {
    return this.dbImpl.getProjectSensorDataSnapshot(users, startTime, endTime, uriPatterns, sdt,
        tool);
  }
  
  /**
   * Returns the SensorData instance as an XML string, or null.
   * @param user The user.
   * @param timestamp The timestamp associated with this sensor data.
   * @return The SensorData instance as an XML string, or null.
   */
  public String getSensorData(User user, XMLGregorianCalendar timestamp) {
    return this.dbImpl.getSensorData(user, timestamp);
  }
  
  /**
   * Returns the SensorDataType instance as an XML string, or null.
   * @param sdtName The name of the SDT to retrieve.
   * @return The SensorDataType instance as an XML string, or null.
   */
  public String getSensorDataType(String sdtName) {
    return this.dbImpl.getSensorDataType(sdtName);
  }
  
  /**
   * Returns the User instance as an XML string, or null.
   * @param email The email address of the User to retrieve.
   * @return The User instance as an XML string, or null.
   */
  public String getUser(String email) {
    return this.dbImpl.getUser(email);
  }
  
  /**
   * Returns the Project instance as an XML string, or null.
   * @param user The User that owns the Project to retrieve.
   * @param projectName The name of the Project to retrieve.
   * @return The Project instance as an XML string, or null.
   */
  public String getProject(User user, String projectName) {
    return this.dbImpl.getProject(user, projectName);
  }
  
  /**
   * Returns true if the passed [user, timestamp] has sensor data defined for it.
   * @param user The user.
   * @param timestamp The timestamp
   * @return True if there is any sensor data for this [user, timestamp].
   */
  public boolean hasSensorData(User user, XMLGregorianCalendar timestamp) {
    return this.dbImpl.hasSensorData(user, timestamp);
  }  
  
  
  /**
   * Ensures that sensor data with the given user and timestamp no longer exists.
   * @param user The user.
   * @param timestamp The timestamp associated with this sensor data.
   */
  public void deleteSensorData(User user, XMLGregorianCalendar timestamp) {
    this.dbImpl.deleteSensorData(user, timestamp);
  }
  
  /**
   * Ensures that sensor data with the given user no longer exists.
   * @param user The user.
   */
  public void deleteSensorData(User user) {
    this.dbImpl.deleteSensorData(user);
  }
  
  /**
   * Ensures that the SensorDataType with the given name no longer exists.
   * @param sdtName The SDT name.
   */
  public void deleteSensorDataType(String sdtName) {
    this.dbImpl.deleteSensorDataType(sdtName);
  }
  
  /**
   * Ensures that the User with the given email address is no longer present in this db.
   * @param email The user email.
   */
  public void deleteUser(String email) {
    this.dbImpl.deleteUser(email);
  }
  
  /**
   * Ensures that the Project with the given user and name is no longer present in this db.
   * @param user  The User who owns this Project.
   * @param projectName The name of the Project to delete.
   */
  public void deleteProject(User user, String projectName) {
    this.dbImpl.deleteProject(user, projectName);
  }
  
  /**
   * Databases like Derby require an explicit compress command for releasing disk space 
   * after a large number of rows have been deleted.  This operation invokes the compress
   * command on all tables in the database.  If a database implementation does not support
   * explicit compression, then this command should do nothing but return true.   
   * @return True if the compress command succeeded or if the database does not support
   * compression. 
   */
  public boolean compressTables() {
    return this.dbImpl.compressTables();
  }
  
  /**
   * The most appropriate set of indexes for the database has been evolving over time as we 
   * develop new queries.  This command sets up the appropriate set of indexes.  It should be
   * able to be called repeatedly without error. 
   * @return True if the index commands succeeded. 
   */
  public boolean indexTables() {
    return this.dbImpl.indexTables();
  }
  
  /**
   * Returns the current number of rows in the specified table.  
   * @param table The table whose rows are to be counted. 
   * @return The number of rows in the table, or -1 if the table does not exist or an error occurs. 
   */
  public int getRowCount(String table) {
    return this.dbImpl.getRowCount(table);
  }
  
  /**
   * Returns a set containing the names of all tables in this database.  Used by clients to 
   * invoke getRowCount with a legal table name. 
   * @return A set of table names.
   */
  public Set<String> getTableNames() {
    return this.dbImpl.getTableNames();
  }
  
}
