package org.hackystat.sensorbase.logger;

import static org.junit.Assert.assertEquals;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

/**
 * Tests the SensorBaseLogger mechanism.
 * @author Philip Johnson
 */
public class TestSensorBaseLogger {
  
  /**
   * Tests the logger. 
   * Instantiates the logger and writes a test message. 
   */
  @Test public void testLogging () {
    Logger logger = SensorBaseLogger.getLogger();
    SensorBaseLogger.setLoggingLevel(Level.INFO);
    logger.info("(Test message)");
    assertEquals("Checking logger identity", "sensorbase-uh", logger.getName());
  }
}
