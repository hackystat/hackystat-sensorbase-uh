package org.hackystat.sensorbase.resource.users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.Server;
import org.hackystat.sensorbase.server.ServerProperties;
import static org.hackystat.sensorbase.server.ServerProperties.TEST_DOMAIN_KEY;
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
import org.w3c.dom.Node;

/**
 * Tests the SensorBase REST API for Users and User resources.
 * @author Philip M. Johnson
 */
public class TestUsersRestApi {

  /** The SensorBase server used in these tests. */
  private static Server server;
 
  /**
   * Starts the server going for these tests. 
   * @throws Exception If problems occur setting up the server. 
   */
  @BeforeClass public static void setupServer() throws Exception {
    TestUsersRestApi.server = Server.newInstance(9876);
  }

  /**
   * Test that GET host/sensorbase/users returns an index containing TestUser.
   * @throws Exception If problems occur.
   */
  @Test public void getUsersIndex() throws Exception {
    // Set up the call.
    Method method = Method.GET;
    String hostName = TestUsersRestApi.server.getHostName();
    Reference reference = new Reference(hostName + "sensorbase/users");
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
    Node node = data.getNode("//UserRef[@UserKey='TestUser']");
    assertNotNull("Checking that we found the TestUser", node);
  }
  
  /**
   * Test that GET host/sensorbase/users/TestUser returns the TestUser test user. 
   * @throws Exception If problems occur.
   */
  @Test public void getUser() throws Exception {
    // Set up the call.
    Method method = Method.GET;
    String hostName = TestUsersRestApi.server.getHostName();
    Reference reference = new Reference(hostName + "sensorbase/users/TestUser");
    Request request = new Request(method, reference);
    Preference<MediaType> xmlMedia = new Preference<MediaType>(MediaType.TEXT_XML);
    request.getClientInfo().getAcceptedMediaTypes().add(xmlMedia); 

    // Make the call.
    Client client = new Client(Protocol.HTTP);
    Response response = client.handle(request);

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET TestUser", response.getStatus().isSuccess());
    DomRepresentation data = response.getEntityAsDom();
    assertEquals("Checking User", "TestUser", data.getText("User/UserKey"));
    
    //Make it into a Java SDT and ensure the fields are there as expected. 
    User user = UserManager.getUser(data.getDocument());
    assertEquals("Checking name", "TestUser", user.getUserKey());
    assertEquals("Checking email", "testuser@hackystat.org", user.getEmail());
  }
  
  /**
   * Tests the POST method that registers a new user. 
   * @throws Exception If problems occur.
   */
  @Test public void postUser() throws Exception {
    // Set up the call.
    Method method = Method.POST;
    String hostName = TestUsersRestApi.server.getHostName();
    String testEmail = "TestPost@" + ServerProperties.get(TEST_DOMAIN_KEY);
    Reference reference = new Reference(hostName + "sensorbase/users?email=" + testEmail);
    Request request = new Request(method, reference);

    // Make the call.
    Client client = new Client(Protocol.HTTP);
    Response response = client.handle(request);

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful POST to", response.getStatus().isSuccess());
  }
}
