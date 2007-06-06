package org.hackystat.sensorbase.resource.sensordatatypes;

import static org.hackystat.sensorbase.server.ServerProperties.XML_DIR_KEY;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataTypeIndex;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataTypeRef;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataTypes;
import org.hackystat.sensorbase.server.Server;
import org.hackystat.sensorbase.server.ServerProperties;
import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.w3c.dom.Document;

/**
 * Manages access to both the SensorDataType and SensorDataTypes resources. 
 * Loads default definitions if available. 
 * @author Philip Johnson
 */
public class SdtManager {
  
  private static String jaxbPackage = "org.hackystat.sensorbase.resource.sensordatatypes.jaxb";
  
  /** The in-memory repository of sensor data types, keyed by SDT name. */
  private Map<String, SensorDataType> sdtMap = new HashMap<String, SensorDataType>();

  /** The JAXB marshaller for SensorDataTypes. */
  private Marshaller marshaller; 
  
  /** The JAXB ummarshaller for SensorDataTypes. */
  private Unmarshaller unmarshaller;
  
  /** The DocumentBuilder for documents. */
  private DocumentBuilder documentBuilder; 
  
  /** The Server associated with this SdtManager. */
  Server server; 
  
  /** 
   * The constructor for SdtManagers. 
   * There is one SdtManager per Server. 
   * @param server The Server instance associated with this SdtManager. 
   */
  public SdtManager(Server server) {
    this.server = server;
    try {
      // Initialize marshaller and unmarshaller. 
      JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
      this.unmarshaller = jc.createUnmarshaller();
      this.marshaller = jc.createMarshaller(); 

      // Get the default SDT definitions from the XML defaults file. 
      File defaultsFile = findDefaultsFile();
      // Initialize the SDTs if we've found a default file. 
      if (defaultsFile.exists()) {
        SensorBaseLogger.getLogger().info("Loading SDT defaults from " + defaultsFile.getPath());  
        SensorDataTypes sensorDataTypes = (SensorDataTypes) unmarshaller.unmarshal(defaultsFile);
        // Initialize the sdtMap
        for (SensorDataType sdt : sensorDataTypes.getSensorDataType()) {
          sdtMap.put(sdt.getName(), sdt);
        }
      }
      // Initialize documentBuilder
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      this.documentBuilder = dbf.newDocumentBuilder();
    }
    catch (Exception e) {
      String msg = "Exception during SdtManager initialization processing";
      SensorBaseLogger.getLogger().warning(msg + "\n" + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
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
   * Returns the XML Index for all current defined SDTs.
   * @return The XML Document instance providing an index to all current SDTs.
   */
  public synchronized Document getSensorDataTypeIndexDocument() {
    // First, create the freakin index.
    SensorDataTypeIndex index = new SensorDataTypeIndex();
    for (SensorDataType sdt : this.sdtMap.values()) {
      SensorDataTypeRef ref = new SensorDataTypeRef();
      ref.setName(sdt.getName());
      ref.setHref(this.server.getHostName() + "sensordatatypes/" + sdt.getName());
      index.getSensorDataTypeRef().add(ref);
    }
    // Now convert it to XML.
    Document doc;
    try {
      doc = this.documentBuilder.newDocument();
      this.marshaller.marshal(index, doc);
    } 
    catch (Exception e ) {
      String msg = "Failed to marshall SDTs into an Index";
      SensorBaseLogger.getLogger().warning(msg + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
    return doc;
  }
  
  /**
   * Updates the Manager with this SDT. Any old definition is overwritten.
   * @param sdt The SensorDataType.
   */
  public synchronized void putSdt(SensorDataType sdt) {
    sdtMap.put(sdt.getName(), sdt);
  }
  
  /**
   * Returns true if the passed SDT name is defined. 
   * @param sdtName A SensorDataType name
   * @return True if a SensorDataType with that name is already known to this SdtManager.
   */
  public synchronized boolean hasSdt(String sdtName) {
    return sdtMap.containsKey(sdtName);
  }
  
  /**
   * Ensures that the passed sdtName is no longer present in this Manager. 
   * @param sdtName The name of the SDT to remove if currently present.
   */
  public synchronized void deleteSdt(String sdtName) {
    sdtMap.remove(sdtName);
  }
  
  /**
   * Utility function for testing purposes that takes an SDT instance and returns it in XML.
   * Note that this does not affect the state of any SdtManager instance. 
   * @param sdt The SensorDataType
   * @return The XML Document instance corresponding to this XML. 
   * @exception Exception If problems occur marshalling the SDT or building the Document instance. 
   */
  public static Document marshallSdt(SensorDataType sdt) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Marshaller marshaller = jc.createMarshaller(); 
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    Document doc = documentBuilder.newDocument();
    marshaller.marshal(sdt, doc);
    return doc;
  }
  
  /**
   * Returns the XML representation of the named SDT.
   * @param name The name of the SDT.
   * @return The XML representation of that SDT, or null if not found.
   */
  public synchronized Document marshallSdt(String name) {
    // Return null if name is not an SDT
    if (!sdtMap.containsKey(name)) {
      return null;
    }
    Document doc = null;
    try {
      SensorDataType sdt = sdtMap.get(name);
      doc = this.documentBuilder.newDocument();
      this.marshaller.marshal(sdt, doc);
    }
    catch (Exception e ) {
      String msg = "Failed to marshall the SDT named: " + name;
      SensorBaseLogger.getLogger().warning(msg + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
    return doc;
  }
  
  /**
   * Takes an XML Document representing a SensorDataType and converts it to an instance. 
   * Note that this does not affect the state of any SdtManager instance. 
   * @param doc The XML Document representing a SensorDataType. 
   * @return The corresponding SensorDataType instance. 
   * @throws Exception If problems occur during unmarshalling. 
   */
  public static SensorDataType unmarshallSdt(Document doc) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    return (SensorDataType) unmarshaller.unmarshal(doc);
  }
  
  /**
   * Takes a String encoding of a SensorDataType in XML format and converts it to an instance. 
   * Note that this does not affect the state of any SdtManager instance. 
   * 
   * @param xmlString The XML string representing a SensorDataType
   * @return The corresponding SensorDataType instance. 
   * @throws Exception If problems occur during unmarshalling.
   */
  public static SensorDataType unmarshallSdt(String xmlString) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    return (SensorDataType)unmarshaller.unmarshal(new StringReader(xmlString));
  }
}
      
