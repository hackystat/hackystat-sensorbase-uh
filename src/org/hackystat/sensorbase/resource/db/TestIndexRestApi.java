package org.hackystat.sensorbase.resource.db;

import org.hackystat.sensorbase.client.SensorBaseClient;
import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
import org.junit.Test;

/**
 * Tests the Index REST API.
 * 
 * @author Philip Johnson
 */
public class TestIndexRestApi extends SensorBaseRestApiHelper {

  /**
   * Test that PUT {host}/db/index runs the index method. 
   * 
   * @throws Exception If problems occur.
   */
  @Test
  public void testIndex() throws Exception { //NOPMD
    SensorBaseClient client = new SensorBaseClient(getHostName(), adminEmail, adminPassword);
    client.authenticate();
    client.setTimeout(200000);
    client.indexTables();
  }
}
