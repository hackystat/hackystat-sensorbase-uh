package org.hackystat.sensorbase.resource.projects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.projects.jaxb.UriPatterns;
import org.hackystat.sensorbase.resource.sensordata.SensorDataManager;
import org.hackystat.sensorbase.resource.sensordata.Timestamp;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
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
 * Tests the SensorBase REST API for Project resources.
 * @author Philip M. Johnson
 */
public class TestProjectRestApi extends SensorBaseRestApiHelper {

  /**
   * Test that GET host/sensorbase/projects returns an index containing at least one Project.
   * Probably want to @ignore this method on real distributions, since the returned dataset could
   * be large. 
   * @throws Exception If problems occur.
   */
  @Test public void getProjectIndex() throws Exception {
    Response response = makeRequest(Method.GET, "projects");

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index 1", response.getStatus().isSuccess());

    // Ensure that we can find the SampleProject definition.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//ProjectRef[@Name='SampleProject']");
    assertNotNull("Checking that we found a ProjectRef 1.", node);
  }
  
  /**
   * Test that GET host/sensorbase/projects/SampleUser and SampleUser2 returns an index 
   * containing a Project. SampleUser is the owner and SampleUser2 is a member. 
   * @throws Exception If problems occur.
   */
  @Test public void getSampleUserProjectIndex() throws Exception {
    Response response = makeRequest(Method.GET, "projects/SampleUser");

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index 2", response.getStatus().isSuccess());

    // Ensure that we can find the SampleProject definition.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//ProjectRef[@Name='SampleProject']");
    assertNotNull("Checking that we found a ProjectRef 2.", node);
    
    // Now check that we can retrieve a Project for SampleUser2
    response = makeRequest(Method.GET, "projects/SampleUser2");

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index 2.1", response.getStatus().isSuccess());

    // Ensure that we can find the SampleProject definition.
    data = response.getEntityAsSax();
    node = data.getNode("//ProjectRef[@Name='SampleProject']");
    assertNotNull("Checking that we found a ProjectRef 2.1", node);

  }
  
  /**
   * Test that GET host/sensorbase/projects/SampleUser/SampleProject returns 
   * a representation of SampleProject. Also for SampleUser2.
   * @throws Exception If problems occur.
   */
  @Test public void getSampleUserProject() throws Exception {
    Response response = makeRequest(Method.GET, "projects/SampleUser/SampleProject");

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET 3", response.getStatus().isSuccess());

    // Ensure that we got the SampleProject.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//Project[@Name='SampleProject']");
    assertNotNull("Checking that we found a Project 3.", node);
    
    //Now try for SampleUser2.
    response = makeRequest(Method.GET, "projects/SampleUser2/SampleProject");

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET 3.1", response.getStatus().isSuccess());

    // Ensure that we can find the SampleProject definition.
    data = response.getEntityAsSax();
    node = data.getNode("//Project[@Name='SampleProject']");
    assertNotNull("Checking that we found a Project 3.1.", node);
  }
  
  /**
   * Test that GET host/sensorbase/projects/SampleUser/SampleProject/sensordata returns 
   * an index of SensorData. 
   * @throws Exception If problems occur.
   */
  @Test public void getSampleUserProjectSensorData() throws Exception {
    // Get sensor data for SampleUser and SampleProject, which should have two instances.
    Response response = makeRequest(Method.GET, "projects/SampleUser/SampleProject/sensordata");

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET 4", response.getStatus().isSuccess());

    // Ensure that we got the SampleProject.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//SensorDataIndex/SensorDataRef");
    assertNotNull("Checking that we retrieved some sensor data.", node);
    
    //Now try for SampleUser2, who should not have any associated sensor data. 
    response = makeRequest(Method.GET, "projects/SampleUser2/SampleProject/sensordata");

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET 4.1", response.getStatus().isSuccess());

    // Ensure that there is no sensordata for this user. 
    data = response.getEntityAsSax();
    node = data.getNode("//SensorDataIndex/SensorDataRef");
    assertNull("Checking that we found no sensor data. 4.2", node);
  }
  
  /**
   * Test that GET host/sensorbase/projects/SampleUser/SampleProject/sensordata?
   * startTime=2006-04-30T09:00:00.000&duration=P10S
   * returns an index of SensorData containing one entry. 
   * @throws Exception If problems occur.
   */
  @Test public void getSampleUserProjectSensorDataDuration() throws Exception {
    // Get sensor data for SampleUser and SampleProject, which should have 1 instances.
    String uri = "projects/SampleUser/SampleProject/sensordata?startTime=2007-04-30T09:00:00.000" +
    "&endTime=2007-04-30T09:30:00.000";
    Response response = makeRequest(Method.GET, uri);

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET 5", response.getStatus().isSuccess());

    // Ensure that we got the sensor data.
    DomRepresentation data = response.getEntityAsDom();
    SensorDataIndex index = SensorDataManager.unmarshallSensorDataIndex(data.getDocument());
    assertEquals("Checking that we retrieved 1 sensor data.", 1, index.getSensorDataRef().size());
    
    //Now try for SampleUser2, who should not have any associated sensor data. 
    uri = "projects/SampleUser2/SampleProject/sensordata?startTime=2006-04-30T09:00:00.000" +
    "&endTime=2006-04-30T10:00:00.000";
    response = makeRequest(Method.GET, uri);

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET 5.1", response.getStatus().isSuccess());

    // Ensure that there is no sensordata for this user. 
    XmlRepresentation data2 = response.getEntityAsSax();
    Node node = data2.getNode("//SensorDataIndex/SensorDataRef");
    assertNull("Checking that we found no sensor data. 5.2", node);
  }

  /**
   * Test that PUT and DELETE of host/projects/{user}/{project} works.
   * @throws Exception If problems occur.
   */
  @Test public void putProject() throws Exception {
    // First, create a sample Project. 
    Project project = new Project();
    project.setName("TestProject");
    project.setDescription("Test Project");
    XMLGregorianCalendar tstamp = Timestamp.makeTimestamp();
    project.setStartTime(tstamp);
    project.setEndTime(tstamp);
    project.setOwner("SampleUser");
    UriPatterns uris = new UriPatterns();
    uris.getUriPattern().add("**/test/**");
    project.setUriPatterns(uris);
    
    Document doc = ProjectManager.marshallProject(project);
    Representation representation = new DomRepresentation(MediaType.TEXT_XML, doc);
    String uri = "projects/SampleUser/TestProject";
    Response response = makeRequest(Method.PUT, uri, representation);

    // Test that the PUT request was received and processed by the server OK. 
    assertTrue("Testing for successful PUT TestProject", response.getStatus().isSuccess());
    
    // Test to see that we can now retrieve it. 
    response = makeRequest(Method.GET, uri);
    assertTrue("Testing for successful GET TestProject", response.getStatus().isSuccess());
    XmlRepresentation data = response.getEntityAsSax();
    assertEquals("Checking SDT", "TestProject", data.getText("Project/@Name"));
    
    // Test that DELETE gets rid of this Project.
    response = makeRequest(Method.DELETE, uri);
    assertTrue("Testing for successful DELETE TestProject", response.getStatus().isSuccess());
  }
}
