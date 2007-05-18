package org.hackystat.sensorbase.resource.sensordatatypes;

import java.io.File;
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
import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.w3c.dom.Document;

/**
 * Manages access to both the SensorDataType and SensorDataTypes resources. 
 * Loads default definitions if available. 
 * @author Philip Johnson
 */
public class SdtManager {
  
  /** The in-memory repository of sensor data types, keyed by SDT name. */
  private Map<String, SensorDataType> sdtMap = new HashMap<String, SensorDataType>();

    /** The property name that should resolve to the file containing default SDT definitions. */
  private static String sdtDefaultsProperty = "hackystat.sensorbase.defaults.sensordatatypes";
  
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
    File defaultsFile = findDefaultsFile();
    // Initialize the SDTs if we've found a default file. 
    if (defaultsFile.exists()) {
      SensorBaseLogger.getLogger().info("Loading SDT defaults from " + defaultsFile.getPath());
      try {
        // Initialize marshaller and unmarshaller. 
        JAXBContext jc = 
          JAXBContext.newInstance("org.hackystat.sensorbase.resource.sensordatatypes.jaxb");
        this.unmarshaller = jc.createUnmarshaller();
        this.marshaller = jc.createMarshaller(); 
        
        // Get the default SDT definitions from the XML defaults file. 
        SensorDataTypes sensorDataTypes = (SensorDataTypes) unmarshaller.unmarshal(defaultsFile);
        // Initialize the sdtMap
        for (SensorDataType sdt : sensorDataTypes.getSensorDataType()) {
          sdtMap.put(sdt.getName(), sdt);
        }
        // Initialize documentBuilder
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        this.documentBuilder = dbf.newDocumentBuilder();
      }
      catch (Exception e) {
        String msg = "Exception during SDT JAXB initialization processing";
        SensorBaseLogger.getLogger().warning(msg + StackTrace.toString(e));
        throw new RuntimeException(msg, e);
      }
    }
  }
  
  /**
   * Checks the System property in SdtManager.sdtDefaultsProperty for the file to load.
   * If this property is null, returns the File for ./xml/defaults/sensordatatypes.defaults.xml.
   * @return The File instance (which might not point to an existing file.)
   */
  private File findDefaultsFile() {
    return (System.getProperties().containsKey(sdtDefaultsProperty)) ?
        new File (System.getProperty(sdtDefaultsProperty)) :
          new File (System.getProperty("user.dir") + "/xml/defaults/sensordatatypes.defaults.xml");
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
      ref.setHref(this.server.getHostName() + "sensorbase/sensordatatypes/" + sdt.getName());
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
   * Returns the XML representation of the named SDT.
   * @param name The name of the SDT.
   * @return The XML representation of that SDT, or null if not found.
   */
  public synchronized Document getSensorDataTypeDocument(String name) {
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
   * Processes an attempt to create a new SDT.
   * @param sdtDoc The XML Document for this SDT.
   * @return True if the new SDT was successfully created. 
   */
  public synchronized boolean putSdt(Document sdtDoc) {
    SensorDataType sdt;
    try {
      sdt = (SensorDataType) unmarshaller.unmarshal(sdtDoc);
      sdtMap.put(sdt.getName(), sdt);
    }
    catch (Exception e) {
      return false;
    }
    return true;
  }
  
  /**
   * Utility function for testing purposes that takes an SDT instance and returns it in XML.
   * Note that this does not affect the state of any SdtManager instance. 
   * @param sdt The SensorDataType
   * @return The XML Document instance corresponding to this XML. 
   * @exception Exception If problems occur marshalling the SDT or building the Document instance. 
   */
  public static Document getDocument(SensorDataType sdt) throws Exception {
    JAXBContext jc = 
      JAXBContext.newInstance("org.hackystat.sensorbase.resource.sensordatatypes.jaxb");
    Marshaller marshaller = jc.createMarshaller(); 
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    Document doc = documentBuilder.newDocument();
    marshaller.marshal(sdt, doc);
    return doc;
  }
  
  /**
   * Takes an XML Document representing a SensorDataType and converts it to an instance. 
   * Note that this does not affect the state of any SdtManager instance. 
   * @param doc The XML Document representing a SensorDataType. 
   * @return The corresponding SensorDataType instance. 
   * @throws Exception If problems occur during unmarshalling. 
   */
  public static SensorDataType getSensorDataType(Document doc) throws Exception {
    JAXBContext jc = 
      JAXBContext.newInstance("org.hackystat.sensorbase.resource.sensordatatypes.jaxb");
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    return (SensorDataType) unmarshaller.unmarshal(doc);
  }
}
      
