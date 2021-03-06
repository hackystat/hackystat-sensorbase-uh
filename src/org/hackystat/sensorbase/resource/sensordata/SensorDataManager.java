package org.hackystat.sensorbase.resource.sensordata;

import static org.hackystat.sensorbase.server.ServerProperties.XML_DIR_KEY;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
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
import org.hackystat.utilities.stacktrace.StackTrace;
import org.hackystat.utilities.tstamp.Tstamp;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataRef;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDatas;
import org.hackystat.sensorbase.resource.users.UserManager;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.Server;
import org.w3c.dom.Document;

/**
 * Provides a manager for the Sensor Data resource.
 * As with all of the Resource managers the methods in this class can be grouped into 
 * three general categories:
 * <ul>
 * <li> URI/Name conversion methods (convert*): these methods translate between the URI and 
 * Name-based representations of SensorDataType and User resources. 
 * <li> Database access methods (get*, has*, put*):  these methods communicate with the underlying 
 * storage system. 
 * <li> XML/Java translation methods (make*): these methods translate between the XML String
 * representation of a resource and its Java class instance representation. 
 * </ul>
 * <p>  
 * See https://jaxb.dev.java.net/guide/Performance_and_thread_safety.html for info 
 * on JAXB performance and thread safety.
 * @author Philip Johnson
 */
public class SensorDataManager {
  
  /** Holds the class-wide JAXBContext, which is thread-safe. */
  private JAXBContext jaxbContext;
  
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
      this.jaxbContext  = 
        JAXBContext.newInstance(
            org.hackystat.sensorbase.resource.sensordata.jaxb.ObjectFactory.class);
      loadDefaultSensorData(userManager); // NOPMD (Incorrect overridable method warning)
    }
    catch (Exception e) {
      String msg = "Exception during SensorDataManager initialization processing";
      server.getLogger().warning(msg + "\n" + StackTrace.toString(e));
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
      server.getLogger().info("Loading SensorData defaults: " 
          + defaultsFile.getPath());
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      SensorDatas sensorDatas = (SensorDatas) unmarshaller.unmarshal(defaultsFile);
      // Initialize the database.
      for (SensorData data : sensorDatas.getSensorData()) {
        data.setLastMod(Tstamp.makeTimestamp());
        String email = convertOwnerToEmail(data.getOwner());
        if (!userManager.isUser(email)) {
          throw new IllegalArgumentException("Owner is not a defined User: " + email);
        }
        this.dbManager.storeSensorData(data, this.makeSensorData(data), 
            this.makeSensorDataRefString(data));
      }
    }
  }
  
  /**
   * Checks the ServerProperties for the XML_DIR property.
   * If this property is null, returns the File for ./xml/defaults/sensordatatypes.defaults.xml.
   * @return The File instance (which might not point to an existing file.)
   */
  private File findDefaultsFile() {
    String defaultsPath = "/defaults/sensordata.defaults.xml";
    String xmlDir = server.getServerProperties().get(XML_DIR_KEY);
    return (xmlDir == null) ?
        new File (System.getProperty("user.dir") + "/xml" + defaultsPath) :
          new File (xmlDir + defaultsPath);
  }
  

  /**
   * Converts an "Owner" string to an email address.
   * The owner string might be a URI (starting with http) or an email address. 
   * @param owner The owner string. 
   * @return The email address corresponding to the owner string. 
   */
  public String convertOwnerToEmail(String owner) {
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
  public String convertOwnerToUri(String owner) {
    return (owner.startsWith(http)) ? owner :
      server.getServerProperties().getFullHost() + "users/" + owner;
  }
  
  /**
   * Converts an "sdt" string to the sdt name.
   * The sdt string might be a URI (starting with http) or the sdt name.
   * @param sdt The sdt string. 
   * @return The sdt name corresponding to the sdt string. 
   */
  public String convertSdtToName(String sdt) {
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
  public String convertSdtToUri(String sdt) {
    return (sdt.startsWith(http)) ? sdt :
      server.getServerProperties().getFullHost() + "sensordatatypes/" + sdt;
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
    return this.dbManager.getSensorDataIndex(users, startTime, endTime, uriPatterns, startIndex,
        maxInstances);
  }  
  
  /**
   * Returns the XML SensorDataIndex for all sensor data matching these users, start/end time, and 
   * whose resource string matches at least one in the list of UriPatterns. 
   * @param users The users. 
   * @param startTime The start time. 
   * @param endTime The end time. 
   * @param uriPatterns A list of UriPatterns. 
   * @param sdt The sensordatatype of interest, or null if sensordata from all SDTs should be
   * retrieved.
   * @param tool The tool of interest. 
   * @return The XML SensorDataIndex string corresponding to the matching sensor data. 
   */
  public String getSensorDataIndex(List<User> users, XMLGregorianCalendar startTime, 
      XMLGregorianCalendar endTime, List<String> uriPatterns, String sdt, String tool) {
    return this.dbManager.getSensorDataIndex(users, startTime, endTime, uriPatterns, sdt, tool);
  }  
  
  /**
   * Returns the XML SensorDataIndex for all sensor data matching these users, start/end time, and 
   * whose resource string matches at least one in the list of UriPatterns. 
   * @param users The users. 
   * @param startTime The start time. 
   * @param endTime The end time. 
   * @param uriPatterns A list of UriPatterns. 
   * @param sdt The sensordatatype of interest, or null if sensordata from all SDTs should be
   * retrieved.
   * @return The XML SensorDataIndex string corresponding to the matching sensor data. 
   */
  public String getSensorDataIndex(List<User> users, XMLGregorianCalendar startTime, 
      XMLGregorianCalendar endTime, List<String> uriPatterns, String sdt) {
    return this.dbManager.getSensorDataIndex(users, startTime, endTime, uriPatterns, sdt);
  }  
  
  /**
   * Returns the XML SensorDataIndex for all sensor data for the given user that arrived
   * at the server between the given timestamps.  This method uses the LastMod timestamp
   * rather than the "regular" timestamp, and is used for real-time monitoring of data
   * arriving at the server. 
   * @param user The user whose data is being monitored.
   * @param lastModStartTime  The lastMod start time of interest. 
   * @param lastModEndTime  The lastMod end time of interest. 
   * @return The XML SensorDataIndex for the recently arrived data based upon the timestamps.
   */
  public String getSensorDataIndexLastMod(User user, XMLGregorianCalendar lastModStartTime, 
      XMLGregorianCalendar lastModEndTime) {
    return this.dbManager.getSensorDataIndexLastMod(user, lastModStartTime, lastModEndTime);
  }
  
  
  /**
   * Updates the Manager with this sensor data. Any old definition is overwritten for
   * this user and timestamp.
   * If runtime is not specified, it is defaulted to the timestamp.
   * If tool, resource, or SDT are not specified, they default to "".
   * @param data The sensor data. 
   */
  public void putSensorData(SensorData data) {
    if (data.getRuntime() == null) {
      data.setRuntime(data.getTimestamp());
    }
    if (data.getTool() == null) {
      data.setTool("");
    }
    if (data.getResource() == null) {
      data.setResource("");
    }
    if (data.getSensorDataType() == null) { 
      data.setSensorDataType("");
    }
    try {
      data.setLastMod(Tstamp.makeTimestamp());
      this.dbManager.storeSensorData(data, this.makeSensorData(data),
          this.makeSensorDataRefString(data));
      server.getLogger().info("Put: " + data.getTimestamp() + " " + data.getOwner() + 
          " " + data.getTool() + " " + data.getSensorDataType() + " " + data.getResource());
    }
    catch (Exception e) {
      server.getLogger().warning("Failed to put sensor data " + StackTrace.toString(e));
    }
  }
  
  /**
   * Returns true if the passed [user, timestamp] has sensor data defined for it.
   * Generally, you will want to use getSensorData(user, timestamp) and check for the 
   * return value == null to avoid a redundant DB call. 
   * @param user The user.
   * @param timestamp The timestamp
   * @return True if there is any sensor data for this [user, timestamp].
   */
  public boolean hasSensorData(User user, XMLGregorianCalendar timestamp) {
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
    this.dbManager.deleteSensorData(user, timestamp);
  }
  
  /**
   * Ensures that sensor data with the given user is no longer present in this manager.
   * @param user The user.
   */
  public void deleteData(User user) {
    this.dbManager.deleteSensorData(user);
  }
  

  /**
   * Takes an XML Document representing a SensorDataIndex and converts it to an instance. 
   *
   * @param xmlString The XML string representing a SensorDataIndex. 
   * @return The corresponding SensorDataIndex instance. 
   * @throws Exception If problems occur during unmarshalling. 
   */
  public SensorDataIndex makeSensorDataIndex(String xmlString) throws Exception {
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    return (SensorDataIndex) unmarshaller.unmarshal(new StringReader(xmlString));
  }
  
  /**
   * Takes a String encoding of a SensorData in XML format and converts it to an instance. 
   * 
   * @param xmlString The XML string representing a SensorData.
   * @return The corresponding SensorData instance. 
   * @throws Exception If problems occur during unmarshalling.
   */
  public SensorData makeSensorData(String xmlString) throws Exception {
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    return (SensorData)unmarshaller.unmarshal(new StringReader(xmlString));
  }
  
  /**
   * Takes a String encoding of a SensorDatas in XML format and converts it to an instance. 
   * 
   * @param xmlString The XML string representing a SensorData.
   * @return The corresponding SensorData instance. 
   * @throws Exception If problems occur during unmarshalling.
   */
  public SensorDatas makeSensorDatas(String xmlString) throws Exception {
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    return (SensorDatas)unmarshaller.unmarshal(new StringReader(xmlString));
  }
  
  /**
   * Returns the passed SensorData instance as a String encoding of its XML representation.
   * Final because it's called in constructor.
   * @param data The SensorData instance. 
   * @return The XML String representation.
   * @throws Exception If problems occur during translation. 
   */
  public final String makeSensorData (SensorData data) throws Exception {
    Marshaller marshaller = jaxbContext.createMarshaller(); 
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
   * Final because it's called in constructor.
   * @param data The SensorData instance. 
   * @return The XML String representation of it as a SensorDataRef
   * @throws Exception If problems occur during translation. 
   */
  public final String makeSensorDataRefString (SensorData data) throws Exception {
    SensorDataRef ref = makeSensorDataRef(data);
    Marshaller marshaller = jaxbContext.createMarshaller(); 
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
    ref.setTool(data.getTool());
    ref.setHref(this.server.getHostName() + "sensordata/" + email + "/" +  timestamp.toString()); 
    ref.setLastMod(data.getLastMod());
    return ref;
  }
 
}
