package org.hackystat.sensorbase.test;

import org.hackystat.sensorbase.server.Server;
import org.junit.BeforeClass;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

/**
 * Provides helpful utility methods to SensorBase test classes, which will
 * normally want to extend this class. 
 * @author Philip Johnson
 *
 */
public class SensorBaseRestApiHelper {
  /** The SensorBase server used in these tests. */
  private static Server server;
  
  /** The Client instance used in these tests. */
  private static Client client;
 
  /**
   * Starts the server going for these tests. 
   * @throws Exception If problems occur setting up the server. 
   */
  @BeforeClass public static void setupServer() throws Exception {
    SensorBaseRestApiHelper.server = Server.newInstance();
    SensorBaseRestApiHelper.client = new Client(Protocol.HTTP);
  }
  
  /**
   * Does the housekeeping for making HTTP requests to the SensorBase.
   * @param method The type of Method.
   * @param requestString A string, such as "users".
   * @return The Response instance returned from the server.
   */
  protected Response makeRequest(Method method, String requestString) {
    return makeRequest(method, requestString, null);
  }
  
  /**
   * Does the housekeeping for making HTTP requests to the SensorBase.
   * @param method The type of Method.
   * @param requestString A string, such as "users".
   * @param entity The representation to be sent with the request. 
   * @return The Response instance returned from the server.
   */
  protected Response makeRequest(Method method, String requestString, Representation entity) {
    String hostName = SensorBaseRestApiHelper.server.getHostName();
    Reference reference = new Reference(hostName + requestString);
    Request request = (entity == null) ? 
        new Request(method, reference) :
          new Request(method, reference, entity);
    Preference<MediaType> xmlMedia = new Preference<MediaType>(MediaType.TEXT_XML);
    request.getClientInfo().getAcceptedMediaTypes().add(xmlMedia); 
    return client.handle(request);
  }
  
  /**
   * Returns the hostname associated with this test server. 
   * @return The host name, including the context root. 
   */
  protected String getHostName() {
    return SensorBaseRestApiHelper.server.getHostName();
  }
}
