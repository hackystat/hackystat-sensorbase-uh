package org.hackystat.sensorbase.resource.sensordatatypes;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Very simple testing of the SdtManager implementation.
 * More comprehensive testing occurs in the TestSdtRestApi class. 
 * Assumes that the JUnit execution environment will set up the System property
 * hackystat.sensorbase.defaults.sensordatatypes and that the default set will contain
 * the Commit SDT. 
 * @author Philip Johnson
 */
public class TestSdtManager {
  
  /**
   * Tests the SdtManager interface. Basically just instantiates it and sees if 
   * the default definitions appear to be there. 
   */
  @Test public void testSdtManager () {
    SdtManager manager = new SdtManager(null);
    String defaults = manager.getSensorDataTypesString();
    assertTrue("Checking nonempty SDTs", (defaults.length() > 0));
    assertTrue("Checking that UnitTest exists", defaults.contains("UnitTest"));
  }

}
