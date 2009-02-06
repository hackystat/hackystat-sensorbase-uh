package org.hackystat.sensorbase.resource.projects.jaxb;

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
  public void testProjectExtensions() {
    Project project = new Project();
    project.addProperty("foo", "bar");
    Property property = project.findProperty("foo");
    assertEquals("Testing foo", "foo", property.getKey());
    assertEquals("Testing bar", "bar", property.getValue());
    assertNull("Testing unknown find", project.findProperty("bar")); 
  }
}
