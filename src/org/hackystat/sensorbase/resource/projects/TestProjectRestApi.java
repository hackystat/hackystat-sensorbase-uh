package org.hackystat.sensorbase.resource.projects;

import static org.hackystat.sensorbase.server.ServerProperties.TEST_DOMAIN_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.projects.jaxb.UriPatterns;
import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.resource.sensordata.Tstamp;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
import org.hackystat.sensorbase.server.ServerProperties;
import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.XmlRepresentation;
import org.w3c.dom.Node;

/**
 * Tests the SensorBase REST API for Project resources.
 * @author Philip M. Johnson
 */
public class TestProjectRestApi extends SensorBaseRestApiHelper {
  
  private String user = "TestUser@hackystat.org";
  private String projects = "projects/";
  private String testProjectSensorData = "/TestProject/sensordata";

  /**
   * Test that GET host/sensorbase/projects returns an index containing at least one Project.
   * Probably want to @ignore this method on real distributions, since the returned dataset could
   * be large. 
   * @throws Exception If problems occur.
   */
  @Test public void getProjectIndex() throws Exception {
    Response response = makeAdminRequest(Method.GET, "projects");

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index 1", response.getStatus().isSuccess());

    // Ensure that we can find the SampleProject definition.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//ProjectRef[@Name='TestProject']");
    assertNotNull("Checking that we found a ProjectRef 1.", node);
  }
  
  /**
   * Test that GET host/sensorbase/projects/TestUser@hackystat.org and 
   * TestUser2@hackystat.org returns an index 
   * containing a Project. TestUser is the owner and TestUser2 is a member. 
   * @throws Exception If problems occur.
   */
  @Test public void getTestUserProjectIndex() throws Exception {
    Response response = makeRequest(Method.GET, projects + user, user);

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index 2", response.getStatus().isSuccess());

    // Ensure that we can find the TestProject definition.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//ProjectRef[@Name='TestProject']");
    assertNotNull("Checking that we found a ProjectRef 2.", node);

  }
  
  /**
   * Test that GET host/sensorbase/projects/TestUser@hackystat.org/TestProject returns 
   * a representation of TestProject. Also for TestUser2@hackystat.org.
   * @throws Exception If problems occur.
   */
  @Test public void getTestUserProject() throws Exception {
    Response response = makeRequest(Method.GET, projects + user + "/TestProject", user);

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET 3", response.getStatus().isSuccess());

    // Ensure that we got the TestProject.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//Project[@Name='TestProject']");
    assertNotNull("Checking that we found a Project 3.", node);
  }
  
  /**
   * Test that GET host/sensorbase/projects/TestUser@hackystat.org/TestProject/sensordata returns 
   * an index of SensorData. 
   * @throws Exception If problems occur.
   */
  @Test public void getTestUserProjectSensorData() throws Exception {
    // Get sensor data for TestUser and TestProject, which should have two instances.
    Response response = makeRequest(Method.GET, 
        projects + user + testProjectSensorData, user);

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET 4", response.getStatus().isSuccess());

    // Ensure that we got the TestProject.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//SensorDataIndex/SensorDataRef");
    assertNotNull("Checking that we retrieved some sensor data.", node);
  }
  
  /**
   * Test that GET host/sensorbase/projects/TestUser@hackystat.org/TestProject/sensordata?
   * startTime=2006-04-30T09:00:00.000&endTime=2007-04-30T09:30:00.000
   * returns an index of SensorData containing one entry. 
   * @throws Exception If problems occur.
   */
  @Test public void getTestUserProjectSensorDataInterval() throws Exception {
    // Get sensor data for TestUser and TestProject, which should have 1 instances.
    String uri = projects + user + testProjectSensorData + 
    "?startTime=2007-04-30T09:00:00.000&endTime=2007-04-30T09:30:00.000";
    Response response = makeRequest(Method.GET, uri, user);

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET 5", response.getStatus().isSuccess());

    // Ensure that we got the sensor data.
    String xmlString = response.getEntity().getText();
    SensorDataIndex index = sensorDataManager.makeSensorDataIndex(xmlString);
    assertEquals("Checking that we retrieved 1 sensor data.", 1, index.getSensorDataRef().size());
  }

  /**
   * Test that PUT and DELETE of host/projects/{user}/{project} works.
   * @throws Exception If problems occur.
   */
  @Test public void putProject() throws Exception {
    // First, create a sample Project. 
    Project project = new Project();
    project.setName("TestProject1");
    project.setDescription("Test Project1");
    XMLGregorianCalendar tstamp = Tstamp.makeTimestamp();
    project.setStartTime(tstamp);
    project.setEndTime(tstamp);
    project.setOwner(user);
    UriPatterns uris = new UriPatterns();
    uris.getUriPattern().add("**/test/**");
    project.setUriPatterns(uris);
    
    // Now convert the Project instance to XML.
    String xmlData = SensorBaseRestApiHelper.projectManager.makeProject(project);
    Representation representation = SensorBaseResource.getStringRepresentation(xmlData);

    String uri = projects + user + "/TestProject1";
    Response response = makeRequest(Method.PUT, uri, user, representation);

    // Test that the PUT request was received and processed by the server OK. 
    assertTrue("Testing for successful PUT TestProject", response.getStatus().isSuccess());
    
    // Test to see that we can now retrieve it. 
    response = makeRequest(Method.GET, uri, user);
    assertTrue("Testing for successful GET TestProject", response.getStatus().isSuccess());
    XmlRepresentation data = response.getEntityAsSax();
    assertEquals("Checking GET TestProject1", "TestProject1", data.getText("Project/@Name"));
    
    // Test that DELETE gets rid of this Project.
    response = makeRequest(Method.DELETE, uri, user);
    assertTrue("Testing for successful DELETE TestProject1", response.getStatus().isSuccess());
  }
  
  /**
   * Tests that after creating a new User, it has a Default Project.
   * @throws Exception If problems occur.
   */
  @Test public void newUserTest() throws Exception {
    String newUser = "NewUserTest@" + ServerProperties.get(TEST_DOMAIN_KEY);
    // No authentication for a registration request, so supply NULL in next line.
    Response response = makeRequest(Method.POST, "users?email=" + newUser, null);

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for creation of NewUserTest", response.getStatus().isSuccess());

    // Now test that we can get this User's default project with authentication.
    String uri = projects + newUser + "/Default";
    response = makeRequest(Method.GET, uri, newUser);
    assertTrue("Testing for successful GET Default Project", response.getStatus().isSuccess());
    
    // Now we delete the user and the project.
    response = makeRequest(Method.DELETE, "projects/" + newUser + "/" + "Default", newUser);
    assertTrue("Testing for deletion of NewUserTest", response.getStatus().isSuccess());

    response = makeRequest(Method.DELETE, "users/" + newUser, newUser);
    assertTrue("Testing for deletion of NewUserTest", response.getStatus().isSuccess());


  }
  
  /**
   * Tests that we can retrieve all data for the TestUser under their Default Project.
   * @throws Exception If problems occur.
   */
  @Test public void testUserDefaultProjectData() throws Exception {
    String uri = projects + user + "/Default/sensordata";
    Response response = makeRequest(Method.GET, uri, user);
    assertTrue("Testing for successful GET Default Project 2", response.getStatus().isSuccess());
    
    // Ensure that we got all 3 sensor data instances.
    String xmlString = response.getEntity().getText();
    SensorDataIndex index = sensorDataManager.makeSensorDataIndex(xmlString);
    assertEquals("Checking that we retrieved 3 sensor data.", 3, index.getSensorDataRef().size());
  }
}
