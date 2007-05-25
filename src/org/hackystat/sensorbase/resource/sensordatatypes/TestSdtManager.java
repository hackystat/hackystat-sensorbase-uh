package org.hackystat.sensorbase.resource.sensordatatypes;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.Document;

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
    Document defaults = manager.marshallSdt("UnitTest");
    assertTrue("Checking that UnitTest SDT is defined", (!(defaults == null)));
  }

}
