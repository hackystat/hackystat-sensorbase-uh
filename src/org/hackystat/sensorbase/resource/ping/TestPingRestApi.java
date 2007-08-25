package org.hackystat.sensorbase.resource.ping;

import static org.junit.Assert.assertTrue;
import org.hackystat.sensorbase.client.SensorBaseClient;
import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
import org.junit.Test;

/**
 * Tests the Ping REST API.
 * 
 * @author Philip Johnson
 */
public class TestPingRestApi extends SensorBaseRestApiHelper {

  /**
   * Test that GET {host}/ping returns the service name.
   * 
   * @throws Exception If problems occur.
   */
  @Test
  public void testPing() throws Exception {
    assertTrue("Checking ping", SensorBaseClient.isHost(getHostName()));
  }
}
