package org.hackystat.sensorbase.resource.sensordata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.resource.XmlRepresentation;
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
    assertTrue("Testing for successful GET index", response.getStatus().isSuccess());

    // Ensure that we can find the SampleSdt definition.
    XmlRepresentation data = response.getEntityAsSax();
    Node node = data.getNode("//SensorDataRef");
    assertNotNull("Checking that we found some sensor data.", node);
    }
  

}
