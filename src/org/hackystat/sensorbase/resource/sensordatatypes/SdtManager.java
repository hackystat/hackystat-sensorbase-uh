package org.hackystat.sensorbase.resource.sensordatatypes;

import java.io.File;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataTypes;
import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;

/**
 * A singleton, thread-safe class providing access to the Sensor Data Type resources.
 * Uses the System property "hackystat.sensorbase.defaults.sensordatatypes" to find the defaults. 
 * @author Philip Johnson
 */
public class SdtManager {

  /** Set to true once the init() method has been called.   */ 
  private boolean initialized = false;

  /** The set of defined SensorDataType instances. */
  private SensorDataTypes sensorDataTypes = new SensorDataTypes();

  /** The private singleton instance. */
  private static SdtManager manager = new SdtManager();

  /** The property name that should resolve to the file containing default SDT definitions. */
  private static String sdtDefaultsProperty = "hackystat.sensorbase.defaults.sensordatatypes";

  /** The private constructor of the singleton instance. */
  private SdtManager() {
  }


  /**
   * Initialize the set of SensorDataTypes by reading in default definitions, if any.
   */
  private void init() {
    // First, see if we can find a file containing default definitions for SDTs.
    String sdtDefaultsFileName =  System.getProperty(sdtDefaultsProperty, "");
    File defaultsFile = new File(sdtDefaultsFileName);

    // Initialize the SDTs if we've found a default file. 
    if (defaultsFile.exists()) {
      try {
        JAXBContext jc = 
          JAXBContext.newInstance("org.hackystat.sensorbase.resource.sensordatatypes.jaxb");
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        this.sensorDataTypes = (SensorDataTypes) unmarshaller.unmarshal(defaultsFile);
      }
      catch (Exception e) {
        SensorBaseLogger.getLogger().warning("Failed to unmarshall SDTs." + StackTrace.toString(e));
        throw new RuntimeException("Failed to unmarshall SDTs", e);
      }
    }
  }

  /**
   * Returns the singleton instance of the SDT Manager. 
   * @return The singleton manager. 
   */
  public static synchronized SdtManager getInstance() {
    // invoke the init() method the first time getInstance is called. 
    if (!SdtManager.manager.initialized) {
      SdtManager.manager.initialized = true;
      SdtManager.manager.init();
    }
    return SdtManager.manager;
  } 

  /**
   * Returns the set of SDTs as an XML string.
   * @return The set of SDTs as an XML string.
   */
  public synchronized String getSensorDataTypes() {
    String sdtString = "";
    try {
      JAXBContext jaxbContext = 
        JAXBContext.newInstance("org.hackystat.sensorbase.resource.sensordatatypes.jaxb");
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT , Boolean.TRUE);
      StringWriter writer = new StringWriter();
      marshaller.marshal(this.sensorDataTypes, writer);
      sdtString = writer.toString();
    } 
    catch (Exception e ) {
      SensorBaseLogger.getLogger().warning("Failed to marshall SDTs." + StackTrace.toString(e));
      throw new RuntimeException("Failed to marshall SDTs", e);
    }
    return sdtString;
  }
}
