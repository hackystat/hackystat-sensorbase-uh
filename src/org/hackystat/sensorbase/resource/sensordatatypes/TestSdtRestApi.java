package org.hackystat.sensorbase.resource.sensordatatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.hackystat.sensorbase.server.Server;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.XmlRepresentation;

/**
 * Tests the SensorBase REST API for the SensorDataType resource.
 *
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
   * Test that GET host/sensorbase/sensordatatypes returns the index.
   */
  @Test public void getSdtIndex() {
    // Set up the call.
    Method method = Method.GET;
    String hostName = TestSdtRestApi.server.getHostName();
    Reference reference = new Reference(hostName + "sensorbase/sensordatatypes");
    Request request = new Request(method, reference);

    // Make the call.
    Client client = new Client(Protocol.HTTP);
    Response response = client.handle(request);

    // Test that the request was received and processed by the server OK. 
    assertTrue("Testing for successful status", response.getStatus().isSuccess());

    // Now test that the response is OK by seeing that the first SDT is UnitTest.
    // This is kind of brittle, but we can fix it later. 
    XmlRepresentation data = response.getEntityAsSax();
    assertEquals("Checking SDT", "UnitTest", 
        data.getText("SensorDataTypeIndex/SensorDataTypeRef/@Name"));
    }
}
