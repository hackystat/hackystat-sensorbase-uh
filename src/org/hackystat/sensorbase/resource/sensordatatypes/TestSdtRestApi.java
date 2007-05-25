package org.hackystat.sensorbase.resource.sensordatatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.Property;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.RequiredField;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
import org.hackystat.sensorbase.server.Server;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.XmlRepresentation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Tests the SensorBase REST API for both the SensorDataTypes and SensorDataType resources.
 * @author Philip M. Johnson
 */
public class TestSdtRestApi {
  
  /** The SensorBase server used in these tests. */
  private static Server server;  
  /**
   * Starts the server going for these tests. 
   * @throws Exception If problems occur setting up the server. 
   */
  @BeforeClass public static void setupServer() throws Exception {
    TestSdtRestApi.server = Server.newInstance(9876);
  }

  /**
   * Test that GET host/sensorbase/sensordatatypes returns an index containing SampleSDT.
   * @throws Exception If problems occur.
   */
  @Test public void getSdtIndex() throws Exception {
    // Set up the call.
    Method method = Method.GET;
    String hostName = TestSdtRestApi.server.getHostName();
    Reference reference = new Reference(hostName + "sensordatatypes");
    Request request = new Request(method, reference);
    Preference<MediaType> xmlMedia = new Preference<MediaType>(MediaType.TEXT_XML);
    request.getClientInfo().getAcceptedMediaTypes().add(xmlMedia); 

    // Make the call.
    Client client = new Client(Protocol.HTTP);
    Response response = client.handle(request);

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index", response.getStatus().isSuccess());

    // Ensure that we can find the SampleSdt definition.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//SensorDataTypeRef[@Name='SampleSdt']");
    assertNotNull("Checking that we found the SampleSdt", node);
    }
  
  /**
   * Test that GET host/sensorbase/sensordatatypes/SampleSdt returns the SampleSdt SDT.
   * @throws Exception If problems occur.
   */
  @Test public void getIndividualSdt() throws Exception {
    // Set up the call.
    Method method = Method.GET;
    String hostName = TestSdtRestApi.server.getHostName();
    Reference reference = new Reference(hostName + "sensordatatypes/SampleSdt");
    Request request = new Request(method, reference);
    Preference<MediaType> xmlMedia = new Preference<MediaType>(MediaType.TEXT_XML);
    request.getClientInfo().getAcceptedMediaTypes().add(xmlMedia); 


    // Make the call.
    Client client = new Client(Protocol.HTTP);
    Response response = client.handle(request);

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET SampleSdt", response.getStatus().isSuccess());
    DomRepresentation data = response.getEntityAsDom();
    assertEquals("Checking SDT", "SampleSdt", data.getText("SensorDataType/@Name"));
    
    //Make it into a Java SDT and ensure the fields are there as expected. 
    SensorDataType sdt = SdtManager.getSensorDataType(data.getDocument());
    assertEquals("Checking name", "SampleSdt", sdt.getName());
    assertTrue("Checking description", sdt.getDescription().startsWith("Sample SDT"));
    RequiredField reqField = sdt.getRequiredFields().getRequiredField().get(0);
    assertEquals("Checking required field name", "SampleField", reqField.getName());
    assertEquals("Checking required field value", "Sample Field Value", reqField.getDescription());
    Property property = sdt.getProperties().getProperty().get(0);
    assertEquals("Checking property key", "SampleProperty", property.getKey());
    assertEquals("Checking property value", "Sample Property Value", property.getValue());
    }
  
  /**
   * Test that PUT host/sensorbase/sensordatatypes/TestSdt works.
   * @throws Exception If problems occur.
   */
  @Test public void putSdt() throws Exception {
    // First, create a sample SDT. Note that our XmlSchema is too lenient right now. 
    SensorDataType sdt = new SensorDataType();
    sdt.setName("TestSdt");
    
    // Got a Java SDT. Now make it into XML.
    Document doc = SdtManager.getDocument(sdt);
    
    // Now set up the call.
    String hostName = TestSdtRestApi.server.getHostName();
    String uri = "sensordatatypes/TestSdt";
    Reference ref = new Reference(hostName + uri);
    Request request = new Request(Method.PUT, ref, new DomRepresentation(MediaType.TEXT_XML, doc));
    Preference<MediaType> xmlMedia = new Preference<MediaType>(MediaType.TEXT_XML);
    request.getClientInfo().getAcceptedMediaTypes().add(xmlMedia); 

    // Make the call to PUT the new SDT.
    Client client = new Client(Protocol.HTTP);
    Response response = client.handle(request);

    // Test that the PUT request was received and processed by the server OK. 
    assertTrue("Testing for successful PUT TestSdt", response.getStatus().isSuccess());
    
    // Test to see that we can now retrieve it. 
    request = new Request(Method.GET, ref);
    request.getClientInfo().getAcceptedMediaTypes().add(xmlMedia); 
    response = client.handle(request); 
    assertTrue("Testing for successful GET TestSdt", response.getStatus().isSuccess());
    XmlRepresentation data = response.getEntityAsSax();
    assertEquals("Checking SDT", "TestSdt", data.getText("SensorDataType/@Name"));
    
    // Test that PUTting it again is an error. 
    request = new Request(Method.PUT, ref, new DomRepresentation(MediaType.TEXT_XML, doc));
    request.getClientInfo().getAcceptedMediaTypes().add(xmlMedia); 
    response = client.handle(request);
    assertFalse("Testing for unsuccessful PUT TestSdt", response.getStatus().isSuccess());
    
    // Test that DELETE gets rid of this SDT.
    request = new Request(Method.DELETE, ref);
    response = client.handle(request); 
    assertTrue("Testing for successful DELETE TestSdt", response.getStatus().isSuccess());
    
    // Test that a second DELETE fails, since da buggah is no longer in there.
    response = client.handle(request); 
    assertTrue("Testing for failed second DELETE TestSdt", response.getStatus().isClientError());
 
  }
}
