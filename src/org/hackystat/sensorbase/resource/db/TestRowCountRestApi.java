package org.hackystat.sensorbase.resource.db;

import static org.junit.Assert.assertTrue;

import org.hackystat.sensorbase.client.SensorBaseClient;
import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
import org.junit.Test;

/**
 * Tests the RowCount API.
 * Note that if you implement an alternative database backend that does not have a table named
 * SensorData, then you will need to provide a System parameter called TestRowCountRestApi.tableName
 * that will be used instead in order to get this test case to pass. 
 * 
 * @author Philip Johnson
 */
public class TestRowCountRestApi extends SensorBaseRestApiHelper {
  
  private String tableNameKey = "TestRowCountRestApi.tableName";

  /**
   * Test that GET {host}/db/table/SensorData/rowcount runs a non-zero value. 
   * 
   * @throws Exception If problems occur.
   */
  @Test
  public void testRowCountRestApi() throws Exception {
    String tableName = "SensorData";
    if (System.getProperties().containsKey(tableNameKey)) {
      tableName = System.getProperty(tableNameKey);
    }
    SensorBaseClient client = new SensorBaseClient(getHostName(), adminEmail, adminPassword);
    client.authenticate();
    int rowCount = client.rowCount(tableName);
    assertTrue("Testing row count", (rowCount >= 0));
  }
}
