package org.hackystat.sensorbase.resource.users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.hackystat.sensorbase.client.SensorBaseClient;
import org.hackystat.sensorbase.client.SensorBaseClientException;
import org.hackystat.sensorbase.resource.users.jaxb.Properties;
import org.hackystat.sensorbase.resource.users.jaxb.Property;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.resource.users.jaxb.UserIndex;
import org.hackystat.sensorbase.resource.users.jaxb.UserRef;
import org.hackystat.sensorbase.server.ServerProperties;
import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;

import static org.hackystat.sensorbase.server.ServerProperties.TEST_DOMAIN_KEY;
import org.junit.Test;


/**
 * Tests the SensorBase REST API for Users and User resources.
 * @author Philip M. Johnson
 */
public class TestUsersRestApi extends SensorBaseRestApiHelper {

  /** The TestUser user email. */
  private String testUser = "TestUser@hackystat.org";


  /**
   * Test that GET host/sensorbase/users returns an index containing TestUser and
   * that the HREFs are OK. 
   * @throws Exception If problems occur.
   */
  @Test public void getUsersIndex() throws Exception {  
    // Create an admin client and check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), adminEmail, adminPassword);
    client.authenticate();
    // Get the index of all users. 
    UserIndex userIndex = client.getUserIndex();
    // Make sure that we can iterate through the Users, find the test user, and dereference hrefs. 
    boolean foundTestUser = false;
    for (UserRef ref : userIndex.getUserRef()) {
      if (testUser.equals(ref.getEmail())) {
        foundTestUser = true;
      }
      // Make sure the href is OK. 
      client.getUri(ref.getHref());
    }
    assertTrue("Checking that we found the TestUser", foundTestUser);
  }
  
  /**
   * Test that the SensorBaseClient.isHost() method operates correctly.
   * @throws Exception If problems occur.
   */
  @Test public void isHost() throws Exception {
    assertTrue("Checking isHost() with sensorbase", SensorBaseClient.isHost(getHostName()));
    assertFalse("Checking isHost() with bogus URL", SensorBaseClient.isHost("http://Foo"));
  }
  
  /**
   * Test that the SensorBaseClient.isRegistered() method operates correctly.
   * @throws Exception If problems occur.
   */
  @Test public void isRegistered() throws Exception {
    assertTrue("OK register", SensorBaseClient.isRegistered(getHostName(), testUser, testUser));
    assertFalse("Bad register", SensorBaseClient.isRegistered(getHostName(), "foo", "bar"));
  }

  /**
   * Test that GET host/sensorbase/users/TestUser@hackystat.org returns the TestUser test user. 
   * @throws Exception If problems occur.
   */
  @Test public void getUser() throws Exception {
    // Create the TestUser client and check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), testUser, testUser);
    client.authenticate();
    // Retrieve the TestUser User resource and test a couple of fields.
    User user = client.getUser(testUser);
    assertEquals("Bad email", testUser, user.getEmail());
    assertEquals("Bad password", testUser, user.getPassword());
  }
  
  /**
   * Tests the POST method that registers a new user. 
   * @throws Exception If problems occur.
   */
  @Test public void registerUser() throws Exception {
    // Register the TestPost@hackystat.org user.
    String testPost = "TestPost@" + ServerProperties.get(TEST_DOMAIN_KEY);
    SensorBaseClient.registerUser(getHostName(), testPost);
    // Now that TestPost is registered, see if we can retrieve him (her?) 
    SensorBaseClient client = new SensorBaseClient(getHostName(), testPost, testPost);
    client.authenticate();
    User user = client.getUser(testPost);
    assertEquals("Bad email", testPost, user.getEmail());
    // Clean up, get rid of this user. 
    client.deleteUser(testPost);
  }
  
  /**
   * Tests that a user can be deleted after creation. 
   * @throws Exception If problems occur. 
   */
  @Test public void deleteUser () throws Exception {
    // Register the TestPost@hackystat.org user
    String testPost = "TestPost@" + ServerProperties.get(TEST_DOMAIN_KEY);
    SensorBaseClient.registerUser(getHostName(), testPost);
    // Now that TestPost is registered, see if we can delete him (her?) 
    SensorBaseClient client = new SensorBaseClient(getHostName(), testPost, testPost);
    client.deleteUser(testPost);

    //Ensure that TestPost is no longer found as a user.
    try {
      client.authenticate();
      fail("Authentication should not have succeeded.");
    }
    catch (SensorBaseClientException e) { //NOPMD
      // good, we got here. 
      // We can't use the JUnit annotation idiom because the code above us could throw
      // the same exception type, and that would be a valid error. 
    }
  }
  
  /**
   * Tests that a user can have their properties updated. 
   * @throws Exception If problems occur. 
   */
  @Test public void postUserProperties () throws Exception {
    // Register the TestPost@hackystat.org user.
    String testPost = "TestPost@" + ServerProperties.get(TEST_DOMAIN_KEY);
    SensorBaseClient.registerUser(getHostName(), testPost);
    // Now that TestPost is registered, see if we can update his properties. 
    Properties properties = new Properties();
    Property property = new Property();
    property.setKey("testKey");
    property.setValue("testValue");
    properties.getProperty().add(property);
    
    SensorBaseClient client = 
      new SensorBaseClient(getHostName(), testPost, testPost);
    client.updateUserProperties(testPost, properties);

    User user = client.getUser(testPost);
    Property theProperty = user.getProperties().getProperty().get(0);
    assertEquals("Got the property", "testValue", theProperty.getValue());
    // Clean up, get rid of this user. 
    client.deleteUser(testPost);
  }
}
