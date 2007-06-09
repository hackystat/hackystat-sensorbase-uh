package org.hackystat.sensorbase.resource.users;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Simple test for the Password Generator
 * @author Philip Johson
 */
public class TestPasswordGenerator {

  /**
   * Tests that a password can be created.
   * @throws Exception If problems occur. 
   */
  @Test public void testPassword() throws Exception {
    assertNotNull("Checking generator", PasswordGenerator.make());
  }
}
