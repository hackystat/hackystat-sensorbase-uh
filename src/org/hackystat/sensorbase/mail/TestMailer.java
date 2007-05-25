package org.hackystat.sensorbase.mail;

import static org.junit.Assert.assertTrue;

import org.hackystat.sensorbase.server.Server;
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
    Server server = Server.newInstance();
    Mailer mailer = Mailer.getInstance();
    assertTrue("Checking mailer", mailer.send("johnson@hackystat.org", "Test Subject", "TestBody"));
    server.stop();
  }

}
