package org.hackystat.sensorbase.resource.sensordatatypes;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests the SdtManager interface.
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
    SdtManager manager = SdtManager.getInstance();
    String defaults = manager.getSensorDataTypes();
    assertTrue("Checking nonempty SDTs", (defaults.length() > 0));
    assertTrue("Checking that Commit exists", defaults.contains("Commit"));
  }

}
