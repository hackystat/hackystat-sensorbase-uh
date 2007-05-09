package org.hackystat.sensorbase.resource.sensordatatypes;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataTypes;

/**
 * Just some simple code to illustrate JAXB processing with the SensorBase resource jar file.
 * @author Philip Johnson
 *
 */
public class SimpleSDTExample {

  /**
   * Illustrate how to do JAXB processing.
   * @param args ignored. 
   * @throws Exception Ignored.
   */
  public static void main(String[] args) throws Exception {
    // Example file containing sensor data type definitions.
    File dataFile = 
      new File("/Users/johnson/svn-google/sensorbase-uh/xml/examples/sensordatatypes.example.xml");
    // Next two lines are special JAXB magic.
    JAXBContext context = 
      JAXBContext.newInstance("org.hackystat.sensorbase.resource.sensordatatypes.jaxb");
    Unmarshaller unmarshaller = context.createUnmarshaller();
    // Get an instance of our XML file as a Java class.
    SensorDataTypes sensorDataTypes = (SensorDataTypes) unmarshaller.unmarshal(dataFile);
    // Now we can manipulate our XML data in Java easily.
    for (SensorDataType sensorDataType : sensorDataTypes.getSensorDataType()) {
      System.out.println(sensorDataType.getName());
    }
  }
}
