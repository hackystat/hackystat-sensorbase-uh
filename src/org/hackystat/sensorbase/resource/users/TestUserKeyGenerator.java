package org.hackystat.sensorbase.resource.users;

import static org.junit.Assert.assertNotNull;

import org.hackystat.sensorbase.server.Server;
import org.junit.Test;

/**
 * Simple test for the User Key Generator
 * @author Philip Johson
 */
public class TestUserKeyGenerator {

  /**
   * Tests that a userkey can be created.
   * @throws Exception If problems occur. 
   */
  @Test public void testUserKey() throws Exception {
    Server server = Server.newInstance(9999);
    UserManager manager = new UserManager(server);
    assertNotNull("Checking generator", UserKeyGenerator.make(manager));
    server.stop();
  }
}
