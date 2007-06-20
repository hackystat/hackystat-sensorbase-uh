package org.hackystat.sensorbase.db.inmemory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.db.DbImplementation;
import org.hackystat.sensorbase.resource.sensordata.SensorDataManager;
import org.hackystat.sensorbase.resource.sensordata.Tstamp;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataRef;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.Server;
import org.w3c.dom.Document;


/**
 * Implements a thread-safe in-memory storage system for SensorData.  
 * Probably not useful in production settings due to the large amount of sensor data 
 * under manipulation.
 * @author Philip Johnson
 */
public class InMemoryImplementation extends DbImplementation {
  
  /** 
   * The in-memory repository of sensor data, User -> Timestamp-> SensorData.
   */
  private Map<User, Map<XMLGregorianCalendar, SensorData>> dataMap = 
    new HashMap<User, Map<XMLGregorianCalendar, SensorData>>();
  
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
  public synchronized boolean saveSensorData(SensorData data, String xmlSensorData, 
      String xmlSensorDataRef) {
    String owner = data.getOwner();
    String ownerUri = SensorDataManager.convertOwnerToUri(owner);
    String ownerEmail = SensorDataManager.convertOwnerToEmail(owner);
    User user = super.getUserManager().getUser(ownerEmail);

    String sdt = data.getSensorDataType();
    String sdtUri = SensorDataManager.convertSdtToUri(sdt);

    // Now update the SensorData instance with the URI versions of the owner and SDT.
    data.setOwner(ownerUri);
    data.setSensorDataType(sdtUri);

    XMLGregorianCalendar timestamp = data.getTimestamp();
    if (!this.dataMap.containsKey(user)) {
      this.dataMap.put(user, new HashMap<XMLGregorianCalendar, SensorData>());
    }
    //Update the interior map with the [Timestamp, SensorData] entry.
    this.dataMap.get(user).put(timestamp, data);
    return false;
  }
  

  /** {@inheritDoc} */
  @Override
  public synchronized Document getSensorDataIndexDocument() {
    SensorDataIndex index = new SensorDataIndex();
    for (User user : this.dataMap.keySet()) {
      for (XMLGregorianCalendar timestamp : this.dataMap.get(user).keySet()) {
        SensorData data = this.dataMap.get(user).get(timestamp);
        SensorDataRef ref = super.getSensorDataManager().makeSensorDataRef(data);
        index.getSensorDataRef().add(ref);
      }
    }
    return super.getSensorDataManager().marshallSensorDataIndex(index);
  }
  

  /** {@inheritDoc} */
  @Override
  public synchronized Document getSensorDataIndexDocument(User user) {
    SensorDataIndex index = new SensorDataIndex();
    if (this.dataMap.containsKey(user)) {
      for (XMLGregorianCalendar timestamp : this.dataMap.get(user).keySet()) {
        SensorData data = this.dataMap.get(user).get(timestamp);
        SensorDataRef ref = super.getSensorDataManager().makeSensorDataRef(data);
        index.getSensorDataRef().add(ref);
      }
    }
    return super.getSensorDataManager().marshallSensorDataIndex(index);
  }


  /** {@inheritDoc} */
  @Override
  public synchronized Document getSensorDataIndexDocument(User user, String sdtName) {
    SensorDataIndex index = new SensorDataIndex();
    if (this.dataMap.containsKey(user)) {
      for (XMLGregorianCalendar timestamp : this.dataMap.get(user).keySet()) {
        SensorData data = this.dataMap.get(user).get(timestamp);
        if (sdtName.equals(SensorDataManager.convertSdtToName(data.getSensorDataType()))) {
          SensorDataRef ref = getSensorDataManager().makeSensorDataRef(data);
          index.getSensorDataRef().add(ref);
        }
      }
    }
    return super.getSensorDataManager().marshallSensorDataIndex(index);
  }
  
  /**
   * Returns true if the passed [key, sdtName, timestamp] has sensor data defined for it.
   * @param user The user.
   * @param sdt The sensor data type name.
   * @param timestamp The timestamp
   * @return True if there is any sensor data for this [key, sdtName, timestamp].
   */
  public synchronized boolean hasSensorData(User user, String sdt, XMLGregorianCalendar timestamp) {
    if (this.dataMap.containsKey(user) &&
        this.dataMap.get(user).containsKey(timestamp)) {
      SensorData data = this.getSensorData(user, timestamp);
      if (sdt.equals(SensorDataManager.convertSdtToName(data.getSensorDataType()))) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Returns true if the passed [user, sdtName, timestamp] has sensor data defined for it.
   * Note that this method returns false if timestamp cannot be converted into XMLGregorianCalendar.
   * @param user The user.
   * @param sdtName The sensor data type name.
   * @param timestamp The timestamp as a string.
   * @return True if there is any sensor data for this [key, sdtName, timestamp].
   */
  public synchronized boolean hasSensorData(User user, String sdtName, String timestamp) {
    try {
      XMLGregorianCalendar tstamp = Tstamp.makeTimestamp(timestamp);
      return 
      this.hasSensorData(user, sdtName, tstamp);
    }
    catch (Exception e) {
      return false;
    }
  }  
  

  /** {@inheritDoc} */
  @Override
  public synchronized void deleteData(User user, XMLGregorianCalendar timestamp) {
    if (this.hasSensorData(user, timestamp)) {
      this.dataMap.get(user).remove(timestamp);
    }
  }
  

  /** {@inheritDoc} */
  @Override
  public synchronized SensorData getSensorData(User user, XMLGregorianCalendar timestamp) {
    if (this.hasSensorData(user, timestamp)) {
      return this.dataMap.get(user).get(timestamp);
    }
    else {
      return null;
    }
  }
  

  /** {@inheritDoc} */
  @Override
  public synchronized Set<SensorData> getSensorData(User user, XMLGregorianCalendar startTime, 
      XMLGregorianCalendar endTime) {
    Set<SensorData> dataSet = new HashSet<SensorData>();
    if (this.dataMap.containsKey(user)) {
      for (XMLGregorianCalendar tstamp : this.dataMap.get(user).keySet())  {
        if (Tstamp.inBetween(startTime, endTime, tstamp)) {
          dataSet.add(this.dataMap.get(user).get(tstamp));
        }
      }
    }
    return dataSet;
  }

  
  /** {@inheritDoc} */
  @Override
  public boolean hasSensorData(User user, XMLGregorianCalendar timestamp) {
    return this.dataMap.containsKey(user) && this.dataMap.get(user).containsKey(timestamp);
  }


  /** {@inheritDoc} */
  @Override
  public boolean hasSensorData(User user, String timestamp) {
    try {
      XMLGregorianCalendar tstamp = Tstamp.makeTimestamp(timestamp);
      return this.hasSensorData(user, tstamp);
    }
    catch (Exception e) {
      return false;
    }
  }  
}
