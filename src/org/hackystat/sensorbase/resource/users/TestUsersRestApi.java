package org.hackystat.sensorbase.resource.users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.hackystat.sensorbase.resource.users.jaxb.Properties;
import org.hackystat.sensorbase.resource.users.jaxb.Property;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.ServerProperties;
import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;

import static org.hackystat.sensorbase.server.ServerProperties.TEST_DOMAIN_KEY;
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
 * Tests the SensorBase REST API for Users and User resources.
 * @author Philip M. Johnson
 */
public class TestUsersRestApi extends SensorBaseRestApiHelper {

  /** The URI string for the TestPost user. */
  private static final String USERS_TEST_POST = "users/TestPost";

  /**
   * Test that GET host/sensorbase/users returns an index containing TestUser.
   * @throws Exception If problems occur.
   */
  @Test public void getUsersIndex() throws Exception {
    Response response = makeRequest(Method.GET, "users");

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
    Response response = makeRequest(Method.GET, "users/TestUser");

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET TestUser", response.getStatus().isSuccess());
    DomRepresentation data = response.getEntityAsDom();
    assertEquals("Checking User", "TestUser", data.getText("User/UserKey"));
    
    //Make it into a Java SDT and ensure the fields are there as expected. 
    User user = UserManager.unmarshallUser(data.getDocument());
    assertEquals("Checking name", "TestUser", user.getUserKey());
    assertEquals("Checking email", "testuser@hackystat.org", user.getEmail());
  }
  
  /**
   * Tests the POST method that registers a new user. 
   * @throws Exception If problems occur.
   */
  @Test public void postUser() throws Exception {
    String testEmail = "TestPost@" + ServerProperties.get(TEST_DOMAIN_KEY);
    Response response = makeRequest(Method.POST, "users?email=" + testEmail);

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful POST of TestPost", response.getStatus().isSuccess());

    // Test that we can now retrieve this user with a GET.
    response = makeRequest(Method.GET, USERS_TEST_POST);
    assertTrue("Testing for successful GET TestPost", response.getStatus().isSuccess());
    DomRepresentation data = response.getEntityAsDom();
    assertEquals("Checking User", "TestPost", data.getText("User/UserKey"));
  }
  
  /**
   * Tests that a user can be deleted after creation. 
   * @throws Exception If problems occur. 
   */
  @Test public void deleteUser () throws Exception {
    String testEmail = "TestPost@" + ServerProperties.get(TEST_DOMAIN_KEY);
    Response response = makeRequest(Method.POST, "users?email=" + testEmail);

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful POST of TestPost", response.getStatus().isSuccess());
    
    // Now try to delete
    response = makeRequest(Method.DELETE, USERS_TEST_POST);
    
    // Test that it was processed OK.
    assertTrue("Testing for successful DELETE of TestPost", response.getStatus().isSuccess());
    
    //Ensure that TestPost is no longer found as a user.
    response = makeRequest(Method.GET, USERS_TEST_POST);
    assertFalse("Testing for unsuccessful GET of TestPost", response.getStatus().isSuccess());
    
    // Ensure that TestPost is not listed in the index.
    response = makeRequest(Method.GET, "users");
    assertTrue("Testing for successful GET index", response.getStatus().isSuccess());

    // Ensure that we can't find the TestPost definition.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//UserRef[@UserKey='TestPost']");
    assertNull("Checking that we didn't find the TestPost user", node);
  }
  
  /**
   * Tests that a user can have their properties updated. 
   * @throws Exception If problems occur. 
   */
  @Test public void postUserProperties () throws Exception {
    // Create (or recreate) the TestPost user
    String testEmail = "TestPost@" + ServerProperties.get(TEST_DOMAIN_KEY);
    Response response = makeRequest(Method.POST, "users?email=" + testEmail);

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful POST of TestPost", response.getStatus().isSuccess());
    
    // Now create a properties object and post it.
    Properties properties = new Properties();
    Property property = new Property();
    property.setKey("testKey");
    property.setValue("testValue");
    properties.getProperty().add(property);
    Document doc = UserManager.marshallProperties(properties);
    Representation representation = new DomRepresentation(MediaType.TEXT_XML, doc);

    response = makeRequest(Method.POST, USERS_TEST_POST, representation);

    // Test that the POST request was received and processed by the server OK. 
    assertTrue("Testing for successful POST TestPost", response.getStatus().isSuccess());
    
    // Retrieve the User representation and see that this property is there. 
    response = makeRequest(Method.GET, "users/TestPost", representation);
    DomRepresentation data = response.getEntityAsDom();
    assertEquals("Testing for testKey", "testKey", data.getText("//User/Properties/Property/Key"));
  }
}
