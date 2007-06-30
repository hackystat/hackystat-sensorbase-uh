package org.hackystat.sensorbase.resource.sensordata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.client.SensorBaseClient;
import org.hackystat.sensorbase.client.SensorBaseClientException;
import org.hackystat.sensorbase.resource.sensordata.jaxb.Property;
import org.hackystat.sensorbase.resource.sensordata.jaxb.Properties;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataRef;
import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
import org.junit.Test;


/**
 * Tests the SensorBase REST API for Sensor Data resources.
 * @author Philip M. Johnson
 */
public class TestSensorDataRestApi extends SensorBaseRestApiHelper {
  
  /** The test user. */
  private String user = "TestUser@hackystat.org";
  
  /**
   * Test that GET host/sensorbase/sensordata returns an index containing all Sensor Data.
   * Probably want to @ignore this method on real distributions, since the returned dataset could
   * be extremely large. 
   * @throws Exception If problems occur.
   */
  @Test public void getSensorDataIndex() throws Exception {
    // Create an admin client and check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), adminEmail, adminPassword);
    client.authenticate();
    // Get the index of all sensordata. 
    SensorDataIndex index = client.getSensorDataIndex();
    // Make sure that we can iterate through the data and dereference all hrefs. 
    for (SensorDataRef ref : index.getSensorDataRef()) {
      client.getUri(ref.getHref());
    }
    assertTrue("Checking for sensor data 1", index.getSensorDataRef().size() > 1);
  }
  
  /**
   * Test that GET host/sensorbase/sensordata/TestUser@hackystat.org returns some sensor data. 
   * @throws Exception If problems occur.
   */
  @Test public void getUserSensorDataIndex() throws Exception {
    // Create the TestUser client and check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), user, user);
    client.authenticate();
    // Retrieve the TestUser User resource and test a couple of fields.
    SensorDataIndex index = client.getSensorDataIndex(user);
    // Make sure that we can iterate through the data and dereference all hrefs. 
    for (SensorDataRef ref : index.getSensorDataRef()) {
      client.getSensorData(ref);
    }
    assertTrue("Checking for sensor data 2", index.getSensorDataRef().size() > 1);
  }
  
  /**
   * Test that GET host/sensorbase/sensordata/TestUser@hackystat.org?sdt=TestSdt returns data.
   * @throws Exception If problems occur.
   */
  @Test public void getUserSdtSensorDataIndex() throws Exception {
    // Create the TestUser client and check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), user, user);
    client.authenticate();
    // Retrieve the TestUser User resource and test a couple of fields.
    SensorDataIndex index = client.getSensorDataIndex(user, "TestSdt");
    // Make sure that we can iterate through the data and dereference all hrefs. 
    for (SensorDataRef ref : index.getSensorDataRef()) {
      client.getUri(ref.getHref());
    }
    assertTrue("Checking for sensor data 3", index.getSensorDataRef().size() > 1);
  }
  
  /**
   * Test GET host/sensorbase/sensordata/TestUser@hackystat.org/2007-04-30T09:00:00.000
   * and see that it returns a SensorData instance..
   * @throws Exception If problems occur.
   */
  @Test public void getUserSensorData() throws Exception {
    // Create the TestUser client and check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), user, user);
    client.authenticate();
    // Retrieve the TestUser User resource and test a couple of fields.
    XMLGregorianCalendar timestamp = Tstamp.makeTimestamp("2007-04-30T09:00:00.000");
    SensorData data = client.getSensorData(user, timestamp);
    assertEquals("Checking timestamp 1", timestamp, data.getTimestamp());
    
    // Check that the admin can retrieve other people's data.
    client = new SensorBaseClient(getHostName(), adminEmail, adminPassword);
    client.authenticate();
    data = client.getSensorData(user, timestamp);
    assertEquals("Checking timestamp 2", timestamp, data.getTimestamp());
  }
  
  
  /**
   * Test GET host/sensorbase/sensordata/TestUser@hackystat.org/9999-04-30T09:00:00.000
   * throws a SensorBaseClientException, since the data does not exist.
   * @throws Exception If problems occur.
   */
  @Test(expected = SensorBaseClientException.class) 
  public void getNonExistingUserSensorData() throws Exception {
    // Create the TestUser client and check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), user, user);
    client.authenticate();
    // Request a non-existing sensordata instance, which should throw SensorBaseClientException.
    XMLGregorianCalendar timestamp = Tstamp.makeTimestamp("9999-04-30T09:00:00.000");
    client.getSensorData(user, timestamp);
  }
  
  
  /**
   * Test that PUT and DELETE of 
   * host/sensorbase/sensordata/TestUser@hackystat.org/2007-04-30T02:00:00.000 works.
   * @throws Exception If problems occur.
   */
  @Test public void putSensorData() throws Exception {
    // First, create a sample sensor data instance.
    XMLGregorianCalendar tstamp = Tstamp.makeTimestamp("2007-04-30T02:00:00.000");
    String sdt = "TestSdt";
    SensorData data = new SensorData();
    String tool = "Subversion";
    data.setTool(tool);
    data.setOwner(user);
    data.setSensorDataType(sdt);
    data.setTimestamp(tstamp);
    data.setResource("file://foo/bar/baz.txt");
    data.setRuntime(tstamp);
    Property property = new Property();
    property.setKey("SampleField");
    property.setValue("The test value for Sample Field");
    Properties properties = new Properties();
    properties.getProperty().add(property);
    data.setProperties(properties);
    
    // Create the TestUser client and check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), user, user);
    client.authenticate();
    // Send the sensor data.
    client.putSensorData(data);

    // Now see that we can retrieve it and check a field for equality. 
    SensorData data2 = client.getSensorData(user, tstamp);
    assertEquals("Checking data Tool field", tool, data2.getTool());
    
    // Test that DELETE gets rid of this sensor data.
    client.deleteSensorData(user, tstamp);
    
    // Test that a second DELETE succeeds, even though da buggah is no longer in there.
    client.deleteSensorData(user, tstamp);
  }
}
