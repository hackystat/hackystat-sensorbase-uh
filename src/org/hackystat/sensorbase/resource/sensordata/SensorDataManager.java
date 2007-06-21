package org.hackystat.sensorbase.resource.sensordata;

import static org.hackystat.sensorbase.server.ServerProperties.XML_DIR_KEY;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.hackystat.sensorbase.db.DbManager;
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
  private static final String jaxbPackage = "org.hackystat.sensorbase.resource.sensordata.jaxb";
  
  /** The JAXB marshaller for SensorData. */
  private Marshaller marshaller; 
  
  /** The JAXB ummarshaller for SensorData. */
  private Unmarshaller unmarshaller;
  
  /** The DocumentBuilder for documents. */
  private DocumentBuilder documentBuilder; 
  
  /** The Server associated with this SensorDataManager. */
  Server server; 
  
  /** The DbManager associated with this server. */
  DbManager dbManager;
  
  /** The http string identifier. */
  private static final String http = "http";
  
  /** 
   * The constructor for SensorDataManagers. 
   * There is one SensorDataManager per Server. 
   * @param server The Server instance associated with this SensorDataManager. 
   */
  public SensorDataManager(Server server) {
    this.server = server;
    this.dbManager = (DbManager)this.server.getContext().getAttributes().get("DbManager");
    UserManager userManager = (UserManager)server.getContext().getAttributes().get("UserManager");
    try {
      // Initialize marshaller, unmarshaller, and documentBuilder. 
      JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
      this.unmarshaller = jc.createUnmarshaller();
      this.marshaller = jc.createMarshaller(); 
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      this.documentBuilder = dbf.newDocumentBuilder();

      if (this.dbManager.isFreshDb()) {
        loadDefaultSensorData(userManager); // NOPMD (Incorrect overridable method warning)
      }
    }
    catch (Exception e) {
      String msg = "Exception during SensorDataManager initialization processing";
      SensorBaseLogger.getLogger().warning(msg + "\n" + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
  }


  /**
   * Loads the sensor data from the sensordata defaults file. 
   * @param userManager The User Manager.
   * @throws Exception If problems occur.
   */
  private void loadDefaultSensorData(UserManager userManager) throws Exception {
    // Get the default Sensor Data definitions from the XML defaults file. 
    File defaultsFile = findDefaultsFile();
    // Initialize the SDTs if we've found a default file. 
    if (defaultsFile.exists()) {
      SensorBaseLogger.getLogger().info("Loading SensorData defaults: " 
          + defaultsFile.getPath());
      SensorDatas sensorDatas = (SensorDatas) unmarshaller.unmarshal(defaultsFile);
      // Initialize the database.
      for (SensorData data : sensorDatas.getSensorData()) {
        String email = convertOwnerToEmail(data.getOwner());
        if (!userManager.hasUser(email)) {
          throw new IllegalArgumentException("Owner is not a defined User: " + email);
        }
        this.dbManager.storeSensorData(data, this.makeSensorDataXmlString(data),
            this.makeSensorDataRefXmlString(data));
      }
    }
  }
  

  /**
   * Converts an "Owner" string to an email address.
   * The owner string might be a URI (starting with http) or an email address. 
   * @param owner The owner string. 
   * @return The email address corresponding to the owner string. 
   */
  public static String convertOwnerToEmail(String owner) {
    if (owner.startsWith(http)) {
      int lastSlash = owner.lastIndexOf('/');
      if (lastSlash < 0) {
        throw new IllegalArgumentException("Could not convert owner to URI");
      }
      return owner.substring(lastSlash + 1); 
    }
    // Otherwise owner is already the email. 
    return owner;
  }
  
  /**
   * Returns the owner string as a URI.
   * The owner string could either be an email address or the URI. 
   * @param owner The owner string. 
   * @return The URI corresponding to the owner string. 
   */
  public static String convertOwnerToUri(String owner) {
    return (owner.startsWith(http)) ? owner :
      ServerProperties.getFullHost() + "users/" + owner;
  }
  
  /**
   * Converts an "sdt" string to the sdt name.
   * The sdt string might be a URI (starting with http) or the sdt name.
   * @param sdt The sdt string. 
   * @return The sdt name corresponding to the sdt string. 
   */
  public static String convertSdtToName(String sdt) {
    if (sdt.startsWith(http)) {
      int lastSlash = sdt.lastIndexOf('/');
      if (lastSlash < 0) {
        throw new IllegalArgumentException("Could not convert sdt to name");
      }
      return sdt.substring(lastSlash + 1); 
    }
    // Otherwise sdt is already the name.
    return sdt;
  }
  
  /**
   * Returns the sdt string as a URI.
   * The sdt string could either be an sdt name or a URI. 
   * @param sdt The sdt string. 
   * @return The URI corresponding to the sdt string. 
   */
  public static String convertSdtToUri(String sdt) {
    return (sdt.startsWith(http)) ? sdt :
      ServerProperties.getFullHost() + "sensordatatypes/" + sdt;
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
   * Returns the XML SensorDataIndex for all sensor data.
   * @return The XML String providing an index of all relevent sensor data resources.
   */
  public String getSensorDataIndex() {
    return dbManager.getSensorDataIndex();
  }
  
  /**
   * Returns the XML SensorDataIndex for all sensor data for this user. 
   * @param user The User whose sensor data is to be returned. 
   * @return The XML String providing an index of all relevent sensor data resources.
   */
  public String getSensorDataIndex(User user) {
    return dbManager.getSensorDataIndex(user);
  }
  
  /**
   * Returns the XML SensorDataIndex for all sensor data for this user and sensor data type.
   * @param user The User whose sensor data is to be returned. 
   * @param sdtName The sensor data type name.
   * @return The XML String providing an index of all relevent sensor data resources.
   */
  public String getSensorDataIndex(User user, String sdtName) {
    return this.dbManager.getSensorDataIndex(user, sdtName);
  }
  
  /**
   * Returns a SensorDataRef instance constructed from the email, sdtName, and timestamp.
   * @param user The user.
   * @param sdt The sensor data type name.
   * @param timestamp The Timestamp.
   * @return A SensorDataRef instance. 
   */
  public SensorDataRef makeSensorDataRef(User user, String sdt, XMLGregorianCalendar timestamp) {
    SensorDataRef ref = new SensorDataRef();
    String email = user.getEmail();
    ref.setOwner(email);
    ref.setSensorDataType(sdt);
    ref.setTimestamp(timestamp);
    ref.setHref(this.server.getHostName() + "sensordata/" + email + "/"  + timestamp.toString()); 
    return ref;
  }
  
  /**
   * Returns a SensorDataRef instance constructed from a SensorData instance.
   * @param data The sensor data instance. 
   * @return A SensorDataRef instance. 
   */
  public SensorDataRef makeSensorDataRef(SensorData data) {
    SensorDataRef ref = new SensorDataRef();
    String email = convertOwnerToEmail(data.getOwner());
    String sdt = convertSdtToName(data.getSensorDataType());
    XMLGregorianCalendar timestamp = data.getTimestamp();
    ref.setOwner(email);
    ref.setSensorDataType(sdt);
    ref.setTimestamp(timestamp);
    ref.setHref(this.server.getHostName() + "sensordata/" + email + "/" +  timestamp.toString()); 
    return ref;
  }
  
  /**
   * Converts a SensorDataIndex instance into a Document and returns it.
   * @param index The SensorDataIndex instance. 
   * @return The Document.
   */
  public Document marshallSensorDataIndex(SensorDataIndex index) {
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
  public void putSensorData(SensorData data) {
    try {
      this.dbManager.storeSensorData(data, this.makeSensorDataXmlString(data),
          this.makeSensorDataRefXmlString(data));
    }
    catch (Exception e) {
      SensorBaseLogger.getLogger().warning("Failed to put sensor data " + StackTrace.toString(e));
    }
  }
  
  /**
   * Returns true if the passed [user, timestamp] has sensor data defined for it.
   * @param user The user.
   * @param timestamp The timestamp
   * @return True if there is any sensor data for this [user, timestamp].
   */
  public boolean hasSensorData(User user, XMLGregorianCalendar timestamp) {
    return this.dbManager.hasSensorData(user, timestamp);
  }
  
  /**
   * Returns true if the passed [user, timestamp] has sensor data defined for it.
   * Note that this method returns false if timestamp cannot be converted into XMLGregorianCalendar.
   * @param user The user.
   * @param timestamp The timestamp as a string.
   * @return True if there is any sensor data for this [user, timestamp].
   */
  public boolean hasSensorData(User user, String timestamp) {
    return this.dbManager.hasSensorData(user, timestamp);
  }
  
  /**
   * Returns the SensorData XML String corresponding to [user, timestamp], or null if not found.
   * @param user The user 
   * @param timestamp The timestamp. 
   * @return The SensorData XML String, or null if not found. 
   */
  public String getSensorData(User user, XMLGregorianCalendar timestamp) {
    return this.dbManager.getSensorData(user, timestamp);
  }
  
  /**
   * Ensures that sensor data with the given user and timestamp is no longer
   * present in this manager.
   * @param user The user.
   * @param timestamp The timestamp associated with this sensor data.
   */
  public void deleteData(User user, XMLGregorianCalendar timestamp) {
    this.dbManager.deleteData(user, timestamp);
  }
  
  /**
   * Ensures that sensor data with the given user and timestamp no longer exists.
   * Note that if the timestamp cannot be parsed into a string, then the sensor data by definition
   * is not in this manager.
   * @param user The User
   * @param timestamp The timestamp associated with this sensor data, as a string.
   */
  public void deleteData(User user, String timestamp) {
    if (this.dbManager.hasSensorData(user, timestamp)) {
      try {
        XMLGregorianCalendar tstamp = Tstamp.makeTimestamp(timestamp);
        deleteData(user, tstamp);
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
   * Returns the passed SensorData instance as a String encoding of its XML representation.
   * @param data The SensorData instance. 
   * @return The XML String representation.
   * @throws Exception If problems occur during translation. 
   */
  public final String makeSensorDataXmlString (SensorData data) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Marshaller marshaller = jc.createMarshaller(); 
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    Document doc = documentBuilder.newDocument();
    marshaller.marshal(data, doc);
    DOMSource domSource = new DOMSource(doc);
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.transform(domSource, result);
    String xmlString = writer.toString();
    // Now remove the processing instruction.  This approach seems like a total hack.
    xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
    return xmlString;
  }

  /**
   * Returns the passed SensorData instance as a String encoding of its XML representation 
   * as a SensorDataRef object.
   * @param data The SensorData instance. 
   * @return The XML String representation of it as a SensorDataRef
   * @throws Exception If problems occur during translation. 
   */
  public final String makeSensorDataRefXmlString (SensorData data) throws Exception {
    SensorDataRef ref = makeSensorDataRef(data);
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Marshaller marshaller = jc.createMarshaller(); 
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    Document doc = documentBuilder.newDocument();
    marshaller.marshal(ref, doc);
    DOMSource domSource = new DOMSource(doc);
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.transform(domSource, result);
    String xmlString = writer.toString();
    // Now remove the processing instruction.  This approach seems like a total hack.
    xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
    return xmlString;

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
    return this.dbManager.getSensorData(user, startTime, endTime);
  }

}
