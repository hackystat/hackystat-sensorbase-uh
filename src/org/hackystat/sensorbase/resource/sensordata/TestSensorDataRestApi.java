package org.hackystat.sensorbase.resource.sensordata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.resource.sensordata.jaxb.Property;
import org.hackystat.sensorbase.resource.sensordata.jaxb.Properties;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.XmlRepresentation;
import org.w3c.dom.Node;


/**
 * Tests the SensorBase REST API for Sensor Data resources.
 * @author Philip M. Johnson
 */
public class TestSensorDataRestApi extends SensorBaseRestApiHelper {
  
  /** The test user. */
  private String user = "TestUser@hackystat.org";
  private String sensordata = "sensordata/";

  /**
   * Test that GET host/sensorbase/sensordata returns an index containing all Sensor Data.
   * Probably want to @ignore this method on real distributions, since the returned dataset could
   * be extremely large. 
   * @throws Exception If problems occur.
   */
  @Test public void getSensorDataIndex() throws Exception {
    Response response = makeAdminRequest(Method.GET, "sensordata");
    
    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index 1", response.getStatus().isSuccess());

    // Ensure that we can find some sensor data. 
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//SensorDataRef");
    assertNotNull("Checking that we found at least one sensor data 1.", node);
    }
  
  /**
   * Test that GET host/sensorbase/sensordata/TestUser@hackystat.org returns some sensor data. 
   * @throws Exception If problems occur.
   */
  @Test public void getUserSensorDataIndex() throws Exception {
    Response response = makeRequest(Method.GET, sensordata + user, user);
    
    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index 2", response.getStatus().isSuccess());

    // Ensure that we can find some sensor data. 
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//SensorDataRef");
    assertNotNull("Checking that we found some sensor data 2.", node);
    }
  
  /**
   * Test that GET host/sensorbase/sensordata/TestUser@hackystat.org?sdt=TestSdt returns data.
   * @throws Exception If problems occur.
   */
  @Test public void getUserSdtSensorDataIndex() throws Exception {
    Response response = makeRequest(Method.GET, sensordata + user + "?sdt=TestSdt", user);
    
    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index 3", response.getStatus().isSuccess());

    // Ensure that we can find the SampleSdt definition.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//SensorDataRef");
    assertNotNull("Checking that we found some sensor data 3.", node);
    }
  
  /**
   * Test GET host/sensorbase/sensordata/TestUser@hackystat.org/2007-04-30T09:00:00.000
   * and see that it returns data.
   * @throws Exception If problems occur.
   */
  @Test public void getUserSensorData() throws Exception {
    String uri = sensordata + user + "/2007-04-30T09:00:00.000";
    Response response = makeRequest(Method.GET, uri, user);
    
    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index 4", response.getStatus().isSuccess());

    // Ensure that we got a representation of sensor data back.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//SensorData");
    assertNotNull("Checking that we found the sensor data 4.", node);
    
    // Now check that the admin can get any user's data.
    response = makeAdminRequest(Method.GET, uri);
    
    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index 4.1", response.getStatus().isSuccess());
    }
  
  /**
   * Test GET host/sensorbase/sensordata/TestUser@hackystat.org/1007-04-30T09:00:00.000
   * returns a Client Error Bad Request (data not found).
   * @throws Exception If problems occur.
   */
  @Test public void getNonExistingUserSensorData() throws Exception {
    String uri = sensordata + user + "/1007-04-30T09:00:00.000";
    Response response = makeRequest(Method.GET, uri, user);
    
    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for unsuccessful GET index 4.1", response.getStatus().isClientError());
    }
  
  
  /**
   * Test that PUT and DELETE of 
   * host/sensorbase/sensordata/TestUser@hackystat.org/2007-04-30T02:00:00.000 works.
   * @throws Exception If problems occur.
   */
  @Test public void putSensorData() throws Exception {
    // First, create a sample sensor data instance.
    String timestamp = "2007-04-30T02:00:00.000";
    DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
    XMLGregorianCalendar tstamp = datatypeFactory.newXMLGregorianCalendar(timestamp);
    String sdt = "TestSdt";
    SensorData data = new SensorData();
    data.setTool("Subversion");
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
    
    // Now convert the Sensor Data instance to XML.
    String xmlData = SensorBaseRestApiHelper.sensorDataManager.makeSensorData(data);
    Representation representation = SensorBaseResource.getStringRepresentation(xmlData);
    String uri = sensordata + user + "/" + timestamp;
    Response response = makeRequest(Method.PUT, uri, user, representation);

    // Test that the PUT request was received and processed by the server OK. 
    assertTrue("Testing for successful PUT Sensor Data", response.getStatus().isSuccess());
    
    // Test to see that we can now retrieve it. 
    response = makeRequest(Method.GET, uri, user);
    assertTrue("Testing for successful GET SensorData", response.getStatus().isSuccess());
    XmlRepresentation newData = response.getEntityAsSax();
    assertEquals("Checking SensorData", timestamp, newData.getText("SensorData/Timestamp"));
 
    
    // Test that DELETE gets rid of this SDT.
    response = makeRequest(Method.DELETE, uri, user);
    assertTrue("Testing for successful DELETE SensorData", response.getStatus().isSuccess());
    
    // Test that a second DELETE succeeds, even though da buggah is no longer in there.
    response = makeRequest(Method.DELETE, uri, user);
    assertTrue("Testing for OK second DELETE SensorData", response.getStatus().isSuccess());
  }
}
