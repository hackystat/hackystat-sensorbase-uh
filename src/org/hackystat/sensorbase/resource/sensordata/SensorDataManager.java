package org.hackystat.sensorbase.resource.sensordata;

import static org.hackystat.sensorbase.server.ServerProperties.XML_DIR_KEY;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataRef;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDatas;
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
   * The in-memory repository of sensor data, UserKey->SDT->Timestamp->SensorData.
   * This will be removed once we have a back-end database repository.  
   */
  private Map<String, Map<String, Map<XMLGregorianCalendar, SensorData>>> dataMap = 
    new HashMap<String, Map<String, Map<XMLGregorianCalendar, SensorData>>>();

  /** The JAXB marshaller for SensorData. */
  private Marshaller marshaller; 
  
  /** The JAXB ummarshaller for SensorData. */
  private Unmarshaller unmarshaller;
  
  /** The DocumentBuilder for documents. */
  private DocumentBuilder documentBuilder; 
  
  /** The Server associated with this SensorDataManager. */
  Server server; 
  
  /** 
   * The constructor for SdtManagers. 
   * There is one SdtManager per Server. 
   * @param server The Server instance associated with this SdtManager. 
   */
  public SensorDataManager(Server server) {
    this.server = server;
    File defaultsFile = findDefaultsFile();
    // Initialize the SDTs if we've found a default file. 
    if (defaultsFile.exists()) {
      SensorBaseLogger.getLogger().info("Loading sensor data defaults: " + defaultsFile.getPath());
      try {
        // Initialize marshaller and unmarshaller. 
        JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
        this.unmarshaller = jc.createUnmarshaller();
        this.marshaller = jc.createMarshaller(); 
        
        // Get the default Sensor Data definitions from the XML defaults file. 
        SensorDatas sensorDatas = (SensorDatas) unmarshaller.unmarshal(defaultsFile);
        // Initialize the sdtMap
        for (SensorData data : sensorDatas.getSensorData()) {
          putSensorDataInternal(data);
        }
        // Initialize documentBuilder
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        this.documentBuilder = dbf.newDocumentBuilder();
      }
      catch (Exception e) {
        String msg = "Exception during SensorData JAXB initialization processing";
        SensorBaseLogger.getLogger().warning(msg + StackTrace.toString(e));
        throw new RuntimeException(msg, e);
      }
    }
  }
  
  /**
   * Puts to the in-memory repository of sensor data, UserKey->SDT->Timestamp->SensorData.
   * @param data The SensorData instance.
   */
  private void putSensorDataInternal(SensorData data) {
    String userKey = data.getUser();
    String sdtName = data.getSensorDataType();
    XMLGregorianCalendar timestamp = data.getTimestamp();
    if (!this.dataMap.containsKey(userKey)) {
      this.dataMap.put(userKey, new HashMap<String, Map<XMLGregorianCalendar, SensorData>>());
    }
    Map<String, Map<XMLGregorianCalendar, SensorData>> map1 = this.dataMap.get(userKey);
    if (!map1.containsKey(sdtName)) {
      map1.put(sdtName, new HashMap<XMLGregorianCalendar, SensorData>());
    }
    Map<XMLGregorianCalendar, SensorData> map2 = this.dataMap.get(userKey).get(sdtName);
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
   * Returns the XML Index for all current defined senor data.
   * @return The XML Document instance providing an index of all current sensor data resources.
   */
  public synchronized Document getSensorDataIndexDocument() {
    // First, create the freakin index.
    SensorDataIndex index = new SensorDataIndex();
    for (String userKey : this.dataMap.keySet()) {
      for (String sdt : this.dataMap.get(userKey).keySet()) {
        for (XMLGregorianCalendar timestamp : this.dataMap.get(userKey).get(sdt).keySet()) {
          SensorDataRef ref = new SensorDataRef();
          ref.setUserKey(userKey);
          ref.setSensorDataType(sdt);
          ref.setTimestamp(timestamp);
          ref.setHref(this.server.getHostName() + 
              "sensordata/" +
              userKey + "/" +
              sdt + "/" +
              timestamp.toString());
          index.getSensorDataRef().add(ref);
        }
      }
    }
    // Now convert it to XML.
    Document doc;
    try {
      doc = this.documentBuilder.newDocument();
      this.marshaller.marshal(index, doc);
    } 
    catch (Exception e ) {
      String msg = "Failed to marshall sensor data into an Index";
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
   * @param key The user key.
   * @param sdtName The sensor data type name.
   * @param timestamp The timestamp
   * @return True if there is any sensor data for this [key, sdtName, timestamp].
   */
  public synchronized boolean hasData(String key, String sdtName, XMLGregorianCalendar timestamp) {
    return 
    this.dataMap.containsKey(key) &&
    this.dataMap.get(key).containsKey(sdtName) &&
    this.dataMap.get(key).get(sdtName).containsKey(timestamp);
  }
  
  /**
   * Ensures that sensor data with the given userkey, SDT name, and timestamp is no longer
   * present in this manager.
   * @param key the UserKey associated with this sensor data.
   * @param sdtName The SDT associated with this sensor data.
   * @param timestamp The timestamp associated with this sensor data.
   */
  public synchronized void deleteData(String key, String sdtName, XMLGregorianCalendar timestamp) {
    if (this.hasData(key, sdtName, timestamp)) {
      this.dataMap.get(key).get(sdtName).remove(timestamp);
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
   * Returns the XML representation of the sensor data represented by userKey, SDT, timestamp.
   * @param key The user key.
   * @param sdtName The SDT name.
   * @param timestamp The timestamp.
   * @return The XML representation of that sensor data, or null if not found.
   */
  public synchronized Document marshallSensorData(String key, String sdtName, 
      XMLGregorianCalendar timestamp) {
    // Return null if name is not an SDT
    if (!this.hasData(key, sdtName, timestamp)) {
      return null;
    }
    Document doc = null;
    try {
      SensorData data = this.dataMap.get(key).get(sdtName).get(timestamp);
      doc = this.documentBuilder.newDocument();
      this.marshaller.marshal(data, doc);
    }
    catch (Exception e ) {
      String msg = "Failed to marshall the sensor data: " + key + " " + sdtName + " " + timestamp;
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

}
