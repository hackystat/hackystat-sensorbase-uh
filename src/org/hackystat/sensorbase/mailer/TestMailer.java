package org.hackystat.sensorbase.mailer;

import static org.junit.Assert.assertTrue;

import org.hackystat.sensorbase.server.Server;
import org.hackystat.sensorbase.server.ServerProperties;
import org.junit.Test;

/**
 * Tests the Mailer class.
 * @author Philip Johnson
 */
public class TestMailer {
  
  /**
   * Checks to see that the Mailer instance works.
   * @exception Exception if problems occur. 
   */
  @Test public void testMailer() throws Exception {
    ServerProperties properties = new ServerProperties();
    properties.setTestProperties();
    Server server = Server.newInstance(properties);
    Mailer mailer = Mailer.getInstance();
    assertTrue("Checking mailer", mailer.send("johnson@hackystat.org", "Test Subject", "TestBody"));
    server.stop();
  }

}
