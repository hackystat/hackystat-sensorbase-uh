package org.hackystat.sensorbase.resource.db;


import org.hackystat.sensorbase.client.SensorBaseClient;
import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
import org.junit.Test;
/**
 * Tests the Compress REST API.
 * 
 * @author Philip Johnson
 */
public class TestCompressRestApi extends SensorBaseRestApiHelper {

  /**
   * Test that PUT {host}/db/compress compresses the database tables.
   * 
   * @throws Exception If problems occur.
   */
  @Test
  public void testCompress() throws Exception { //NOPMD
    SensorBaseClient client = new SensorBaseClient(getHostName(), adminEmail, adminPassword);
    client.authenticate();
    client.setTimeout(200000);
    client.compressTables();
  }
}
