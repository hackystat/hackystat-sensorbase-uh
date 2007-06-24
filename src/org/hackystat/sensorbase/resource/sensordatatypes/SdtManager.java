package org.hackystat.sensorbase.resource.sensordatatypes;

import static org.hackystat.sensorbase.server.ServerProperties.XML_DIR_KEY;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataTypeIndex;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataTypeRef;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataTypes;
import org.hackystat.sensorbase.server.Server;
import org.hackystat.sensorbase.server.ServerProperties;
import org.hackystat.sensorbase.db.DbManager;
import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.w3c.dom.Document;

/**
 * Provides a manager for the SensorDataType resource.
 * As with all of the Resource managers the methods in this class can be grouped into 
 * three general categories, of which this Manager uses the following:
 * <ul>
 * <li> Database access methods (get*, has*, put*, delete*):  these methods communicate with the 
 * underlying storage system (or cache). 
 * <li> XML/Java translation methods (make*): these methods translate between the XML String
 * representation of a resource and its Java class instance representation. 
 * </ul>
 * <p>  
 * See https://jaxb.dev.java.net/guide/Performance_and_thread_safety.html for info 
 * on JAXB performance and thread safety.
 * <p>
 * All public methods of this class are synchronized so that we can maintain the cache along with
 * the underlying persistent store in a thread-safe fashion.
 * 
 * @author Philip Johnson
 *
 */
public class SdtManager {
  
  /** Holds the class-wide JAXBContext, which is thread-safe. */
  private JAXBContext jaxbContext;
  
  /** The Server associated with this SdtManager. */
  Server server; 
  
  /** The DbManager associated with this server. */
  DbManager dbManager;
  
  /** The SensorDataTypeIndex open tag. */
  public static final String sensorDataTypeIndexOpenTag = "<SensorDataTypeIndex>";
  
  /** The SensorDataTypeIndex close tag. */
  public static final String sensorDataTypeIndexCloseTag = "</SensorDataTypeIndex>";
  
  /** An in-memory cache mapping SDT names to their corresponding instance. */
  private Map<String, SensorDataType> name2sdt = new HashMap<String, SensorDataType>();
  
  /** An in-memory cache mapping a SensorDataType instance to its SensorDataType XML string. */
  private Map<SensorDataType, String> sdt2xml = new HashMap<SensorDataType, String>();
  
  /** An in-memory cache mapping a SensorDataType instance to its SensorDataTypeRef XML string. */
  private Map<SensorDataType, String> sdt2ref = new HashMap<SensorDataType, String>();
  
  
  /** 
   * The constructor for SdtManagers. Loads in default data and sets up the in-memory caches. 
   * @param server The Server instance associated with this SdtManager. 
   */
  public SdtManager(Server server) {
    this.server = server;
    this.dbManager = (DbManager)this.server.getContext().getAttributes().get("DbManager");
    try {
      this.jaxbContext = 
        JAXBContext.newInstance("org.hackystat.sensorbase.resource.sensordatatypes.jaxb");
      loadDefaultSensorDataTypes(); //NOPMD it's throwing a false warning. 
      initializeCache();            //NOPMD 
    }
    catch (Exception e) {
      String msg = "Exception during SdtManager initialization processing";
      SensorBaseLogger.getLogger().warning(msg + "\n" + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
  }

  /**
   * Loads the default SensorDataTypes from the defaults file. 
   * @throws Exception If problems occur. 
   */
  private void loadDefaultSensorDataTypes() throws Exception {
    // Get the default SDT definitions from the XML defaults file. 
    File defaultsFile = findDefaultsFile();
    // Initialize the SDTs if we've found a default file. 
    if (defaultsFile.exists()) {
      SensorBaseLogger.getLogger().info("Loading SDT defaults from " + defaultsFile.getPath()); 
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      SensorDataTypes sensorDataTypes = (SensorDataTypes) unmarshaller.unmarshal(defaultsFile);
      for (SensorDataType sdt : sensorDataTypes.getSensorDataType()) {
        this.dbManager.storeSensorDataType(sdt, this.makeSensorDataType(sdt), 
            this.makeSensorDataTypeRefString(sdt));
      }
    }
  }
  
  /**
   * Read the SDTs from the underlying database and initialize the in-memory cache.
   */
  private void initializeCache() {
    try {
      SensorDataTypeIndex index = makeSensorDataTypeIndex(this.dbManager.getSensorDataTypeIndex());
      for (SensorDataTypeRef ref : index.getSensorDataTypeRef()) {
        String sdtName = ref.getName();
        String sdtString = this.dbManager.getSensorDataType(sdtName);
        SensorDataType sdt = makeSensorDataType(sdtString);
        String sdtRef = this.makeSensorDataTypeRefString(sdt);
        this.name2sdt.put(sdtName, sdt);
        this.sdt2ref.put(sdt, sdtRef);
        this.sdt2xml.put(sdt, sdtString);
      }
    }
    catch (Exception e) {
      SensorBaseLogger.getLogger().warning("Failed to initialize SDTs " + StackTrace.toString(e));
    }
  }
  
  /**
   * Checks the ServerProperties for the XML_DIR property.
   * If this property is null, returns the File for ./xml/defaults/sensordatatypes.defaults.xml.
   * @return The File instance (which might not point to an existing file.)
   */
  private File findDefaultsFile() {
    String defaultsPath = "/defaults/sensordatatypes.defaults.xml";
    String xmlDir = ServerProperties.get(XML_DIR_KEY);
    return (xmlDir == null) ?
        new File (System.getProperty("user.dir") + "/xml" + defaultsPath) :
          new File (xmlDir + defaultsPath);
  }


  /**
   * Returns the XML string containing the SensorDataTypeIndex with all defined SDTs.
   * Uses the in-memory cache of SensorDataTypeRef strings.  
   * @return The XML string providing an index to all current SDTs.
   */
  public synchronized String getSensorDataTypeIndex() {
    StringBuilder builder = new StringBuilder(512);
    builder.append(sensorDataTypeIndexOpenTag);
    for (String ref : this.sdt2ref.values()) {
      builder.append(ref);
    }
    builder.append(sensorDataTypeIndexCloseTag);
    return builder.toString();
  }
  
  /**
   * Returns the XML String representation of a SensorDataType, given its name.  
   * @param sdtName The name of the SDT. 
   * @return Its string representation, or null if not found. 
   */
  public synchronized String getSensorDataTypeString(String sdtName) {
    SensorDataType sdt = this.name2sdt.get(sdtName);
    return (sdt == null) ? null : this.sdt2xml.get(sdt);
  }
  
  /**
   * Updates the Manager with this SDT. Any old definition with this name is overwritten.
   * Updates in-memory caches and persistent store. 
   * @param sdt The SensorDataType.
   */
  public synchronized void putSdt(SensorDataType sdt) {
    try {
      String sdtString = this.makeSensorDataType(sdt);
      String sdtRefString = this.makeSensorDataTypeRefString(sdt);
      this.dbManager.storeSensorDataType(sdt, sdtString, sdtRefString);
      this.name2sdt.put(sdt.getName(), sdt);
      this.sdt2ref.put(sdt, sdtRefString);
      this.sdt2xml.put(sdt, sdtString);
    }
    catch (Exception e) {
      SensorBaseLogger.getLogger().warning("Failed to put SDT" + StackTrace.toString(e));
    }
  }
  
  /**
   * Returns true if the passed SDT name is defined. 
   * @param sdtName A SensorDataType name
   * @return True if a SensorDataType with that name is already known to this SdtManager.
   */
  public synchronized boolean hasSdt(String sdtName) {
    return this.name2sdt.containsKey(sdtName);
  }
  
  /**
   * Ensures that the passed sdtName is no longer present in this Manager. 
   * @param sdtName The name of the SDT to remove if currently present.
   */
  public synchronized void deleteSdt(String sdtName) {
    SensorDataType sdt = this.name2sdt.get(sdtName);
    if (sdt != null) {
      this.name2sdt.remove(sdtName);
      this.sdt2ref.remove(sdt);
      this.sdt2xml.remove(sdt);
    }
    this.dbManager.deleteSensorDataType(sdtName);
  }
  
  /**
   * Takes a String encoding of a SensorDataType in XML format and converts it to an instance. 
   * 
   * @param xmlString The XML string representing a SensorDataType
   * @return The corresponding SensorDataType instance. 
   * @throws Exception If problems occur during unmarshalling.
   */
  public final synchronized SensorDataType makeSensorDataType(String xmlString) throws Exception {
    Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
    return (SensorDataType)unmarshaller.unmarshal(new StringReader(xmlString));
  }
  
  /**
   * Takes a String encoding of a SensorDataTypeIndex in XML format and converts it to an instance. 
   * Note that this does not affect the state of any SdtManager instance. 
   * 
   * @param xmlString The XML string representing a SensorDataTypeIndex.
   * @return The corresponding SensorDataTypeIndex instance. 
   * @throws Exception If problems occur during unmarshalling.
   */
  public final synchronized SensorDataTypeIndex makeSensorDataTypeIndex(String xmlString) 
  throws Exception {
    Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
    return (SensorDataTypeIndex)unmarshaller.unmarshal(new StringReader(xmlString));
  }
  
  /**
   * Returns the passed SensorDataType instance as a String encoding of its XML representation.
   * Final because it's called in constructor.
   * @param sdt The SensorDataType instance. 
   * @return The XML String representation.
   * @throws Exception If problems occur during translation. 
   */
  public final synchronized String makeSensorDataType (SensorDataType sdt) throws Exception {
    Marshaller marshaller = jaxbContext.createMarshaller(); 
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    Document doc = documentBuilder.newDocument();
    marshaller.marshal(sdt, doc);
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
   * Returns the passed SensorDataType instance as a String encoding of its XML representation 
   * as a SensorDataTypeRef object.
   * Final because it's called in constructor.
   * @param sdt The SensorDataType instance. 
   * @return The XML String representation of it as a SensorDataTypeRef
   * @throws Exception If problems occur during translation. 
   */
  public final synchronized String makeSensorDataTypeRefString (SensorDataType sdt) 
  throws Exception {
    SensorDataTypeRef ref = makeSensorDataTypeRef(sdt);
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
   * Returns a SensorDataTypeRef instance constructed from a SensorDataType instance.
   * @param sdt The SensorDataType instance. 
   * @return A SensorDataTypeRef instance. 
   */
  public synchronized SensorDataTypeRef makeSensorDataTypeRef(SensorDataType sdt) {
    SensorDataTypeRef ref = new SensorDataTypeRef();
    ref.setName(sdt.getName());
    ref.setHref(this.server.getHostName() + "sensordatatypes/" + sdt.getName()); 
    return ref;
  }
  
}
      
