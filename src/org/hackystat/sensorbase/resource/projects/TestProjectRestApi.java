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
   * Test that GET host/sensorbase/sensordata returns an index containing all Sensor Data.
   * Probably want to @ignore this method on real distributions, since the returned dataset could
   * be extremely large. 
   * @throws Exception If problems occur.
   */
  @Test public void getProjectIndex() throws Exception {
    Response response = makeRequest(Method.GET, "projects");
    
    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful GET index 1", response.getStatus().isSuccess());

    // Ensure that we can find the SampleSdt definition.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//ProjectRef");
    assertNotNull("Checking that we found sensor data 1.", node);
    }
  

}
