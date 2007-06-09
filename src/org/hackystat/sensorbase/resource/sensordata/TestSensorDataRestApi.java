package org.hackystat.sensorbase.resource.sensordata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.resource.sensordata.jaxb.Property;
import org.hackystat.sensorbase.resource.sensordata.jaxb.Properties;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.XmlRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * Tests the SensorBase REST API for Sensor Data resources.
 * @author Philip M. Johnson
 */
public class TestSensorDataRestApi extends SensorBaseRestApiHelper {

  /**
   * Test that GET host/sensorbase/sensordata returns an index containing all Sensor Data.
   * Probably want to @ignore this method on real distributions, since the returned dataset could
   * be extremely large. 
   * @throws Exception If problems occur.
   */
  @Test public void getSensorDataIndex() throws Exception {
    Response response = makeRequest(Method.GET, "sensordata");
    
    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index 1", response.getStatus().isSuccess());

    // Ensure that we can find the SampleSdt definition.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//SensorDataRef");
    assertNotNull("Checking that we found sensor data 1.", node);
    }
  
  /**
   * Test that GET host/sensorbase/sensordata/TestUser@hackystat.org returns some sensor data. 
   * @throws Exception If problems occur.
   */
  @Test public void getUserSensorDataIndex() throws Exception {
    Response response = makeRequest(Method.GET, "sensordata/TestUser@hackystat.org");
    
    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index 2", response.getStatus().isSuccess());

    // Ensure that we can find the SampleSdt definition.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//SensorDataRef");
    assertNotNull("Checking that we found sensor data 2.", node);
    }
  
  /**
   * Test that GET host/sensorbase/sensordata/TestUser@hackystat.org/SampleSdt returns data.
   * @throws Exception If problems occur.
   */
  @Test public void getUserSdtSensorDataIndex() throws Exception {
    Response response = makeRequest(Method.GET, "sensordata/TestUser@hackystat.org/TestSdt");
    
    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index 3", response.getStatus().isSuccess());

    // Ensure that we can find the SampleSdt definition.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//SensorDataRef");
    assertNotNull("Checking that we found sensor data 3.", node);
    }
  
  /**
   * Test GET host/sensorbase/sensordata/TestUser@hackystat.org/TestSdt/2007-04-30T09:00:00.000
   * and see that it returns data.
   * @throws Exception If problems occur.
   */
  @Test public void getUserSensorData() throws Exception {
    String uri = "sensordata/TestUser@hackystat.org/TestSdt/2007-04-30T09:00:00.000";
    Response response = makeRequest(Method.GET, uri);
    
    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index 4", response.getStatus().isSuccess());

    // Ensure that we can find the SampleSdt definition.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//SensorData");
    assertNotNull("Checking that we found sensor data 4.", node);
    }
  
  
  /**
   * Test that PUT and DELETE of 
   * host/sensorbase/sensordata/TestUser@hackystat.org/TestSdt/2007-04-30T02:00:00.000 works.
   * @throws Exception If problems occur.
   */
  @Test public void putSensorData() throws Exception {
    // First, create a sample sensor data instance.
    String user = "TestUser@hackystat.org";
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
    Document doc = SensorDataManager.marshallSensorData(data);
    Representation representation = new DomRepresentation(MediaType.TEXT_XML, doc);
    String uri = "sensordata/" + user + "/" + sdt + "/" + timestamp;
    Response response = makeRequest(Method.PUT, uri, representation);

    // Test that the PUT request was received and processed by the server OK. 
    assertTrue("Testing for successful PUT Sensor Data", response.getStatus().isSuccess());
    
    // Test to see that we can now retrieve it. 
    response = makeRequest(Method.GET, uri);
    assertTrue("Testing for successful GET SensorData", response.getStatus().isSuccess());
    XmlRepresentation newData = response.getEntityAsSax();
    assertEquals("Checking SensorData", timestamp, newData.getText("SensorData/Timestamp"));
 
    
    // Test that DELETE gets rid of this SDT.
    response = makeRequest(Method.DELETE, uri);
    assertTrue("Testing for successful DELETE SensorData", response.getStatus().isSuccess());
    
    // Test that a second DELETE succeeds, even though da buggah is no longer in there.
    response = makeRequest(Method.DELETE, uri);
    assertTrue("Testing for OK second DELETE SensorData", response.getStatus().isSuccess());
  }

}
