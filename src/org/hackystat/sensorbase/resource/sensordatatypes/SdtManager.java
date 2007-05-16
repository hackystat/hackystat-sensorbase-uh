package org.hackystat.sensorbase.resource.sensordatatypes;

import java.io.File;
import java.io.StringWriter;

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

  /** The set of defined SensorDataType instances. */
  private SensorDataTypes sensorDataTypes = new SensorDataTypes();

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
        JAXBContext jc = 
          JAXBContext.newInstance("org.hackystat.sensorbase.resource.sensordatatypes.jaxb");
        this.unmarshaller = jc.createUnmarshaller();
        this.marshaller = jc.createMarshaller(); 
        this.sensorDataTypes = (SensorDataTypes) unmarshaller.unmarshal(defaultsFile);
        
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
   * Returns the set of SDTs as an XML string.
   * @return The set of SDTs as an XML string.
   */
  public synchronized String getSensorDataTypesString() {
    String sdtString = "";
    try {
      StringWriter writer = new StringWriter();
      this.marshaller.marshal(this.sensorDataTypes, writer);
      sdtString = writer.toString();
    } 
    catch (Exception e ) {
      String msg = "Failed to marshall SDTs into a String";
      SensorBaseLogger.getLogger().warning(msg + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
    return sdtString;
  }
  
  /**
   * Returns the XML Index for all current defined SDTs.
   * @return The XML Document instance providing an index to all current SDTs.
   */
  public synchronized Document getSensorDataTypeIndexDocument() {
    // First, create the freakin index.
    SensorDataTypeIndex index = new SensorDataTypeIndex();
    for (SensorDataType sdt : sensorDataTypes.getSensorDataType()) {
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
    // Iterate through all SDTs, break when we find the one we need. 
    SensorDataType sdt = null; 
    Document doc = null;
    for (SensorDataType tempSdt : sensorDataTypes.getSensorDataType()) {
      if (tempSdt.getName().equals(name)) {
        sdt = tempSdt;
        // Now convert it to XML.
        try {
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
    }
    // If we got here, it's not good, since that means we didn't find the SDT. 
    // Return a null doc.
    return doc;
  }
}
      
