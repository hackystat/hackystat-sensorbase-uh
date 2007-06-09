package org.hackystat.sensorbase.resource.sensordata;

import static org.hackystat.sensorbase.server.ServerProperties.XML_DIR_KEY;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
//import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataRef;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDatas;
import org.hackystat.sensorbase.resource.users.UserManager;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.Server;
import org.hackystat.sensorbase.server.ServerProperties;
import org.w3c.dom.Document;

/**
 * Provides a manager for the Sensor Data resource. 
 * @author Philip Johnson
 */
public class SensorDataManager {
  private static String jaxbPackage = "org.hackystat.sensorbase.resource.sensordata.jaxb";
  
  /** 
   * The in-memory repository of sensor data, User -> SDT-> Timestamp-> SensorData.
   * This will be removed once we have a back-end database repository.  
   */
  private Map<User, Map<String, Map<XMLGregorianCalendar, SensorData>>> dataMap = 
    new HashMap<User, Map<String, Map<XMLGregorianCalendar, SensorData>>>();

  /** The JAXB marshaller for SensorData. */
  private Marshaller marshaller; 
  
  /** The JAXB ummarshaller for SensorData. */
  private Unmarshaller unmarshaller;
  
  /** The DocumentBuilder for documents. */
  private DocumentBuilder documentBuilder; 
  
  /** The Server associated with this SensorDataManager. */
  Server server; 
  
  /** 
   * The constructor for SensorDataManagers. 
   * There is one SensorDataManager per Server. 
   * @param server The Server instance associated with this SensorDataManager. 
   */
  public SensorDataManager(Server server) {
    this.server = server;
    try {
      // Initialize marshaller and unmarshaller. 
      JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
      this.unmarshaller = jc.createUnmarshaller();
      this.marshaller = jc.createMarshaller(); 

      // Get the default Sensor Data definitions from the XML defaults file. 
      File defaultsFile = findDefaultsFile();
      // Initialize the SDTs if we've found a default file. 
      if (defaultsFile.exists()) {
        SensorBaseLogger.getLogger().info("Loading SensorData defaults: " + defaultsFile.getPath());
        SensorDatas sensorDatas = (SensorDatas) unmarshaller.unmarshal(defaultsFile);
        // Initialize the sdtMap
        for (SensorData data : sensorDatas.getSensorData()) {
          putSensorDataInternal(data);
        }
      }
      // Initialize documentBuilder
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      this.documentBuilder = dbf.newDocumentBuilder();
      // Initialize datatypefactory for XMLGregorianCalendar conversion.
  //    this.datatypeFactory = DatatypeFactory.newInstance();
    }
    catch (Exception e) {
      String msg = "Exception during SensorDataManager initialization processing";
      SensorBaseLogger.getLogger().warning(msg + "\n" + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
  }
  
  /**
   * Puts to the in-memory repository of sensor data, User -> SDT-> Timestamp-> SensorData.
   * Throws an illegal argument exception if the sensor data Owner (email) is not a defined User.
   * @param data The SensorData instance.
   */
  private void putSensorDataInternal(SensorData data) {
    String email = data.getOwner();
    String sdtName = data.getSensorDataType();
    UserManager userManager = (UserManager)server.getContext().getAttributes().get("UserManager");
    User user = userManager.getUser(email);
    if (user == null) {
      throw new IllegalArgumentException("Owner is not a defined User: " + email);
    }
    XMLGregorianCalendar timestamp = data.getTimestamp();
    if (!this.dataMap.containsKey(user)) {
      this.dataMap.put(user, new HashMap<String, Map<XMLGregorianCalendar, SensorData>>());
    }
    Map<String, Map<XMLGregorianCalendar, SensorData>> map1 = this.dataMap.get(user);
    if (!map1.containsKey(sdtName)) {
      map1.put(sdtName, new HashMap<XMLGregorianCalendar, SensorData>());
    }
    Map<XMLGregorianCalendar, SensorData> map2 = this.dataMap.get(user).get(sdtName);
    //Update the final map with the [Timestamp, SensorData] entry.
    map2.put(timestamp, data);
  }

  /**
   * Checks the ServerProperties for the XML_DIR property.
   * If this property is null, returns the File for ./xml/defaults/sensordatatypes.defaults.xml.
   * @return The File instance (which might not point to an existing file.)
   */
  private File findDefaultsFile() {
    String defaultsPath = "/defaults/sensordata.defaults.xml";
    String xmlDir = ServerProperties.get(XML_DIR_KEY);
    return (xmlDir == null) ?
        new File (System.getProperty("user.dir") + "/xml" + defaultsPath) :
          new File (xmlDir + defaultsPath);
  }
  
  /**
   * Returns the XML Index for all sensor data.
   * @return The XML Document instance providing an index of all relevent sensor data resources.
   */
  public synchronized Document getSensorDataIndexDocument() {
    SensorDataIndex index = new SensorDataIndex();
    for (User user : this.dataMap.keySet()) {
      for (String sdt : this.dataMap.get(user).keySet()) {
        for (XMLGregorianCalendar timestamp : this.dataMap.get(user).get(sdt).keySet()) {
          index.getSensorDataRef().add(makeSensorDataRef(user, sdt, timestamp));
        }
      }
    }
    return marshallSensorDataIndex(index);
  }
  
  /**
   * Returns the XML Index for all sensor data for this user. 
   * @param user The User whose sensor data is to be returned. 
   * @return The XML Document instance providing an index of all relevent sensor data resources.
   */
  public synchronized Document getSensorDataIndexDocument(User user) {
    SensorDataIndex index = new SensorDataIndex();
    for (String sdt : this.dataMap.get(user).keySet()) {
      for (XMLGregorianCalendar timestamp : this.dataMap.get(user).get(sdt).keySet()) {
        index.getSensorDataRef().add(makeSensorDataRef(user, sdt, timestamp));
      }
    }
    return marshallSensorDataIndex(index);
  }
  
  /**
   * Returns the XML Index for all sensor data for this user and sensor data type.
   * @param user The User whose sensor data is to be returned. 
   * @param sdtName The sensor data type name.
   * @return The XML Document instance providing an index of all relevent sensor data resources.
   */
  public synchronized Document getSensorDataIndexDocument(User user, String sdtName) {
    SensorDataIndex index = new SensorDataIndex();
    for (XMLGregorianCalendar timestamp : this.dataMap.get(user).get(sdtName).keySet()) {
      index.getSensorDataRef().add(makeSensorDataRef(user, sdtName, timestamp));
    }
    return marshallSensorDataIndex(index);
  }
  
  /**
   * Returns a SensorDataRef instance constructed from the email, sdtName, and timestamp.
   * @param user The user.
   * @param sdt The sensor data type name.
   * @param timestamp The Timestamp.
   * @return A SensorDataRef instance. 
   */
  private SensorDataRef makeSensorDataRef(User user, String sdt, XMLGregorianCalendar timestamp) {
    SensorDataRef ref = new SensorDataRef();
    ref.setOwner(user.getEmail());
    ref.setSensorDataType(sdt);
    ref.setTimestamp(timestamp);
    ref.setHref(this.server.getHostName() + "sensordata/" + user.getEmail() + "/" + sdt + "/" +
        timestamp.toString()); 
    return ref;
  }
  
  /**
   * Converts a SensorDataIndex instance into a Document and returns it.
   * @param index The SensorDataIndex instance. 
   * @return The Document.
   */
  public synchronized Document marshallSensorDataIndex(SensorDataIndex index) {
    Document doc;
    try {
      doc = this.documentBuilder.newDocument();
      this.marshaller.marshal(index, doc);
    } 
    catch (Exception e ) {
      String msg = "Failed to marshall sensor data index into a Document";
      SensorBaseLogger.getLogger().warning(msg + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
    return doc;
  }
  
  /**
   * Updates the Manager with this sensor data. Any old definition is overwritten.
   * @param data The sensor data. 
   */
  public synchronized void putSensorData(SensorData data) {
    putSensorDataInternal(data);
  }
  
  /**
   * Returns true if the passed [key, sdtName, timestamp] has sensor data defined for it.
   * @param user The user.
   * @param sdt The sensor data type name.
   * @param timestamp The timestamp
   * @return True if there is any sensor data for this [key, sdtName, timestamp].
   */
  public synchronized boolean hasData(User user, String sdt, XMLGregorianCalendar timestamp) {
    return 
    this.dataMap.containsKey(user) &&
    this.dataMap.get(user).containsKey(sdt) &&
    this.dataMap.get(user).get(sdt).containsKey(timestamp);
  }
  
  /**
   * Returns true if the passed [user, sdtName, timestamp] has sensor data defined for it.
   * Note that this method returns false if timestamp cannot be converted into XMLGregorianCalendar.
   * @param user The user.
   * @param sdtName The sensor data type name.
   * @param timestamp The timestamp as a string.
   * @return True if there is any sensor data for this [key, sdtName, timestamp].
   */
  public synchronized boolean hasData(User user, String sdtName, String timestamp) {
    try {
      XMLGregorianCalendar tstamp = Timestamp.makeTimestamp(timestamp);
      return 
      this.dataMap.containsKey(user) &&
      this.dataMap.get(user).containsKey(sdtName) &&
      this.dataMap.get(user).get(sdtName).containsKey(tstamp);
    }
    catch (Exception e) {
      return false;
    }
  }
  
  /**
   * Ensures that sensor data with the given user, SDT name, and timestamp is no longer
   * present in this manager.
   * @param user The user.
   * @param sdtName The SDT associated with this sensor data.
   * @param timestamp The timestamp associated with this sensor data.
   */
  public synchronized void deleteData(User user, String sdtName, XMLGregorianCalendar timestamp) {
    if (this.hasData(user, sdtName, timestamp)) {
      this.dataMap.get(user).get(sdtName).remove(timestamp);
    }
  }
  
  /**
   * Ensures that sensor data with the given user, SDT name, and timestamp is no longer
   * present in this manager.
   * Note that if the timestamp cannot be parsed into a string, then the sensor data by definition
   * is not in this manager.
   * @param user The User
   * @param sdtName The SDT associated with this sensor data.
   * @param timestamp The timestamp associated with this sensor data, as a string.
   */
  public synchronized void deleteData(User user, String sdtName, String timestamp) {
    if (hasData(user, sdtName, timestamp)) {
      try {
        XMLGregorianCalendar tstamp = Timestamp.makeTimestamp(timestamp);
        deleteData(user, sdtName, tstamp);
      }
      catch (Exception e) { // NOPMD
        // data cannot be in map by definition.
      }
    }
  }
  
  /**
   * Utility function for testing purposes that takes a Sensor Data instance and returns it in XML.
   * Note that this does not affect the state of any SensorDataManager instance. 
   * @param data The Sensor Data instance. 
   * @return The XML Document instance corresponding to this sensor data. 
   * @exception Exception If problems occur marshalling the sensor data or building the Document.
   */
  public static Document marshallSensorData(SensorData data) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Marshaller marshaller = jc.createMarshaller(); 
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    Document doc = documentBuilder.newDocument();
    marshaller.marshal(data, doc);
    return doc;
  }
  
  /**
   * Returns the XML representation of the sensor data represented by user, SDT, timestamp.
   * @param user The User
   * @param sdtName The SDT name.
   * @param timestamp The timestamp.
   * @return The XML representation of that sensor data, or null if not found.
   */
  public synchronized Document marshallSensorData(User user, String sdtName, 
      XMLGregorianCalendar timestamp) {
    // Return null if name is not an SDT
    if (!this.hasData(user, sdtName, timestamp)) {
      return null;
    }
    Document doc = null;
    try {
      SensorData data = this.dataMap.get(user).get(sdtName).get(timestamp);
      doc = this.documentBuilder.newDocument();
      this.marshaller.marshal(data, doc);
    }
    catch (Exception e ) {
      String msg = "Failed to marshall the sensor data: " + user + " " + sdtName + " " + timestamp;
      SensorBaseLogger.getLogger().warning(msg + "/n" + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
    return doc;
  }
  
  /**
   * Takes an XML Document representing a SensorData and converts it to an instance. 
   * Note that this does not affect the state of any SensorDataManager instance. 
   * @param doc The XML Document representing a SensorData. 
   * @return The corresponding SensorData instance. 
   * @throws Exception If problems occur during unmarshalling. 
   */
  public static SensorData unmarshallSensorData(Document doc) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    return (SensorData) unmarshaller.unmarshal(doc);
  }
  
  /**
   * Takes an XML Document representing a SensorDataIndex and converts it to an instance. 
   * Note that this does not affect the state of any SensorDataManager instance. 
   * @param doc The XML Document representing a SensorDataIndex. 
   * @return The corresponding SensorDataIndex instance. 
   * @throws Exception If problems occur during unmarshalling. 
   */
  public static SensorDataIndex unmarshallSensorDataIndex(Document doc) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    return (SensorDataIndex) unmarshaller.unmarshal(doc);
  }
  
  /**
   * Takes a String encoding of a SensorData in XML format and converts it to an instance. 
   * Note that this does not affect the state of any SensorDataManager instance. 
   * 
   * @param xmlString The XML string representing a SensorData.
   * @return The corresponding SensorData instance. 
   * @throws Exception If problems occur during unmarshalling.
   */
  public static SensorData unmarshallSensorData(String xmlString) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    return (SensorData)unmarshaller.unmarshal(new StringReader(xmlString));
  }

  /**
   * Returns a (possibly empty) set of SensorData instances associated with the 
   * given user between the startTime and endTime. 
   * @param user The user
   * @param startTime The start time.
   * @param endTime The end time.
   * @return The set of SensorData instances. 
   */
  public Set<SensorData> getData(User user, XMLGregorianCalendar startTime, 
      XMLGregorianCalendar endTime) {
    Set<SensorData> dataSet = new HashSet<SensorData>();
    if (this.dataMap.containsKey(user)) {
      for (Map.Entry<String, Map<XMLGregorianCalendar, SensorData>> entry : 
        this.dataMap.get(user).entrySet()) {
        for (XMLGregorianCalendar tstamp : entry.getValue().keySet())  {
            if (Timestamp.inBetween(startTime, endTime, tstamp)) {
              dataSet.add(entry.getValue().get(tstamp));
          }
        }
      }
    }
    return dataSet;
  }

}
