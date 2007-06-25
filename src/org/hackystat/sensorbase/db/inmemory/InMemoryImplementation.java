package org.hackystat.sensorbase.db.inmemory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.db.DbImplementation;
import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.sensordata.SensorDataManager;
import org.hackystat.sensorbase.resource.sensordata.Tstamp;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.Server;
import org.hackystat.sensorbase.uripattern.UriPattern;

import static org.hackystat.sensorbase.db.DbManager.sensorDataIndexCloseTag;
import static org.hackystat.sensorbase.db.DbManager.sensorDataIndexOpenTag;


/**
 * Implements a thread-safe in-memory storage system for SensorData.  
 * Probably not useful in production settings due to the large amount of sensor data 
 * under manipulation.
 * @author Philip Johnson
 */
public class InMemoryImplementation extends DbImplementation {
  
  /** The in-memory repository of sensor data, User -> Timestamp-> SensorData. */
  private Map<User, Map<XMLGregorianCalendar, SensorData>> dataMap = 
    new HashMap<User, Map<XMLGregorianCalendar, SensorData>>();
  
  /** Maps a SensorData instance to its corresponding XML string. */
  private Map<SensorData, String> data2xml = new HashMap<SensorData, String>();
  
  /** Maps a SensorData instance to its corresponding SensorDataRef XML string. */
  private Map<SensorData, String> data2ref = new HashMap<SensorData, String>();
  
  /**
   * Creates a new InMemoryImplementation, which is a nonpersistent store for SensorData.
   * @param server The SensorBase server.
   */
  public InMemoryImplementation(Server server) {
    super(server);
  }

  /** {@inheritDoc} */
  @Override
  public synchronized void initialize() {
    // No need to initialize this system.
  }

 
  /** {@inheritDoc} */
  @Override
  public synchronized boolean isFreshlyCreated() {
    return true;
  }
  

  /** {@inheritDoc} */
  @Override
  public synchronized boolean storeSensorData(SensorData data, String xmlSensorData, 
      String xmlSensorDataRef) {
    String owner = data.getOwner();
    String ownerUri = super.getSensorDataManager().convertOwnerToUri(owner);
    String ownerEmail = super.getSensorDataManager().convertOwnerToEmail(owner);
    User user = super.getUserManager().getUser(ownerEmail);

    String sdt = data.getSensorDataType();
    String sdtUri = super.getSensorDataManager().convertSdtToUri(sdt);

    // Now update the SensorData instance with the URI versions of the owner and SDT.
    data.setOwner(ownerUri);
    data.setSensorDataType(sdtUri);

    XMLGregorianCalendar timestamp = data.getTimestamp();
    if (!this.dataMap.containsKey(user)) {
      this.dataMap.put(user, new HashMap<XMLGregorianCalendar, SensorData>());
    }
    //Update the interior map with the [Timestamp, SensorData] entry.
    this.dataMap.get(user).put(timestamp, data);
    this.data2xml.put(data, xmlSensorData);
    this.data2ref.put(data, xmlSensorDataRef);
    return false;
  }
  

  /** {@inheritDoc} */
  @Override
  public synchronized String getSensorDataIndex() {
    StringBuilder builder = new StringBuilder(512);
    builder.append(sensorDataIndexOpenTag);
    for (User user : this.dataMap.keySet()) {
      for (XMLGregorianCalendar timestamp : this.dataMap.get(user).keySet()) {
        SensorData data = this.dataMap.get(user).get(timestamp);
        String ref = this.data2ref.get(data);
        builder.append(ref);
      }
    }
    builder.append(sensorDataIndexCloseTag);
    return builder.toString();
  }
  

  /** {@inheritDoc} */
  @Override
  public synchronized String getSensorDataIndex(User user) {
    StringBuilder builder = new StringBuilder(512);
    builder.append(sensorDataIndexOpenTag);
    if (this.dataMap.containsKey(user)) {
      for (XMLGregorianCalendar timestamp : this.dataMap.get(user).keySet()) {
        SensorData data = this.dataMap.get(user).get(timestamp);
        String ref = this.data2ref.get(data);
        builder.append(ref);
      }
    }
    builder.append(sensorDataIndexCloseTag);
    return builder.toString();
  }


  /** {@inheritDoc} */
  @Override
  public synchronized String getSensorDataIndex(User user, String sdtName) {
    StringBuilder builder = new StringBuilder(512);
    builder.append(sensorDataIndexOpenTag);
    if (this.dataMap.containsKey(user)) {
      for (XMLGregorianCalendar timestamp : this.dataMap.get(user).keySet()) {
        SensorData data = this.dataMap.get(user).get(timestamp);
        SensorDataManager manager = super.getSensorDataManager();
        if (sdtName.equals(manager.convertSdtToName(data.getSensorDataType()))) {
          String ref = this.data2ref.get(data);
          builder.append(ref);
        }
      }
    }
    builder.append(sensorDataIndexCloseTag);
    return builder.toString();
  }
  
  /** {@inheritDoc} */
  @Override
  public String getSensorDataIndex(User user, XMLGregorianCalendar startTime, 
      XMLGregorianCalendar endTime, List<UriPattern> uriPatterns) {
    StringBuilder builder = new StringBuilder(512);
    builder.append(sensorDataIndexOpenTag);
    if (this.dataMap.containsKey(user)) {
      for (XMLGregorianCalendar timestamp : this.dataMap.get(user).keySet()) {
        SensorData data = this.dataMap.get(user).get(timestamp);
        String resource = data.getResource();
        if (Tstamp.inBetween(startTime, endTime, timestamp) &&
            UriPattern.matches(resource, uriPatterns)) {
          String ref = this.data2ref.get(data);
          builder.append(ref);
        }
      }
    }
    builder.append(sensorDataIndexCloseTag);
    return builder.toString();
  }


  /** {@inheritDoc} */
  @Override
  public synchronized void deleteSensorData(User user, XMLGregorianCalendar timestamp) {
    if (this.hasSensorData(user, timestamp)) {
      this.dataMap.get(user).remove(timestamp);
    }
  }
  

  /** {@inheritDoc} */
  @Override
  public synchronized String getSensorData(User user, XMLGregorianCalendar timestamp) {
    if (this.hasSensorData(user, timestamp)) {
      SensorData data =  this.dataMap.get(user).get(timestamp);
      return this.data2xml.get(data);
    }
    else {
      return null;
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasSensorData(User user, XMLGregorianCalendar timestamp) {
    return this.dataMap.containsKey(user) && this.dataMap.get(user).containsKey(timestamp);
  }

  /** {@inheritDoc} */
  @Override
  public boolean storeSensorDataType(SensorDataType sdt, String xmlSensorDataType, 
      String xmlSensorDataTypeRef) {
    // TODO Auto-generated method stub
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public void deleteSensorDataType(String sdtName) {
    // TODO Auto-generated method stub
    
  }

  /** {@inheritDoc} */
  @Override
  public String getSensorDataTypeIndex() {
    // TODO Auto-generated method stub
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public String getSensorDataType(String sdtName) {
    // TODO Auto-generated method stub
    return null;
  }
  /** {@inheritDoc} */
  @Override
  public void deleteUser(String email) {
    // TODO Auto-generated method stub
    
  }
  /** {@inheritDoc} */
  @Override
  public String getUser(String email) {
    // TODO Auto-generated method stub
    return null;
  }
  /** {@inheritDoc} */
  @Override
  public String getUserIndex() {
    // TODO Auto-generated method stub
    return null;
  }
  /** {@inheritDoc} */
  @Override
  public boolean storeUser(User user, String xmlUser, String xmlUserRef) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void deleteProject(User owner, String projectName) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String getProject(User owner, String projectName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getProjectIndex() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean storeProject(Project project, String xmlProject, String xmlProjectRef) {
    // TODO Auto-generated method stub
    return false;
  }
}
