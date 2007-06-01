package org.hackystat.sensorbase.resource.projects;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.resource.XmlRepresentation;
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

    // Ensure that we can find the SampleSdt definition.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//ProjectRef");
    assertNotNull("Checking that we found a ProjectRef 1.", node);
  }
  
  /**
   * Test that GET host/sensorbase/projects/SampleUser returns an index containing a Project.
   * @throws Exception If problems occur.
   */
  @Test public void getSampleUserProjectIndex() throws Exception {
    Response response = makeRequest(Method.GET, "projects/SampleUser");

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index 2", response.getStatus().isSuccess());

    // Ensure that we can find the SampleSdt definition.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//ProjectRef");
    assertNotNull("Checking that we found a ProjectRef 2.", node);
  }
  
  /**
   * Test that GET host/sensorbase/projects/SampleUser/SampleProject returns 
   * a representation of SampleProject.
   * @throws Exception If problems occur.
   */
  @Test public void getSampleUserProject() throws Exception {
    Response response = makeRequest(Method.GET, "projects/SampleUser/SampleProject");

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET 3", response.getStatus().isSuccess());

    // Ensure that we can find the SampleSdt definition.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//Project");
    assertNotNull("Checking that we found a Project 1.", node);
  }

}
