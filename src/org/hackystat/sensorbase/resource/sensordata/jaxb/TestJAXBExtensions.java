package org.hackystat.sensorbase.resource.sensordata.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Test hand-coded extensions to JAXB-generated classes to ensure that they exist
 * and function properly in the event that the JAXB classes are re-generated.
 * @author Philip Johnson
 *
 */
public class TestJAXBExtensions {
  
  /**
   * Test the property manipulation extensions to SensorData. 
   */
  @Test 
  public void testSensorDataExtensions() {
    SensorData data = new SensorData();
    data.addProperty("foo", "bar");
    Property property = data.findProperty("foo");
    assertEquals("Testing foo", "foo", property.getKey());
    assertEquals("Testing bar", "bar", property.getValue());
    assertNull("Testing unknown find", data.findProperty("bar")); 
  }
}
