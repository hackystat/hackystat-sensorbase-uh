package org.hackystat.sensorbase.test;

import org.hackystat.sensorbase.resource.projects.ProjectManager;
import org.hackystat.sensorbase.resource.sensordata.SensorDataManager;
import org.hackystat.sensorbase.resource.sensordatatypes.SdtManager;
import org.hackystat.sensorbase.resource.users.UserManager;
import org.hackystat.sensorbase.server.Server;
import org.hackystat.sensorbase.server.ServerProperties;
import static org.hackystat.sensorbase.server.ServerProperties.ADMIN_EMAIL_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.ADMIN_PASSWORD_KEY;
import org.junit.BeforeClass;
import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
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
  protected static Server server;
  
  /** The Client instance used in these tests. */
  private static Client client;
  
  /** Make a Manager available to this test class. */
  protected static SensorDataManager sensorDataManager; 

  /** Make a Manager available to this test class. */
  protected static UserManager userManager; 

  /** Make a Manager available to this test class. */
  protected static SdtManager sdtManager; 

  /** Make a Manager available to this test class. */
  protected static ProjectManager projectManager; 
  
  /** The admin email. */
  protected static String adminEmail;
  /** The admin password. */
  protected static String adminPassword;

  /**
   * Starts the server going for these tests. 
   * @throws Exception If problems occur setting up the server. 
   */
  @BeforeClass public static void setupServer() throws Exception {
    SensorBaseRestApiHelper.server = Server.newInstance();
    SensorBaseRestApiHelper.client = new Client(Protocol.HTTP);
    SensorBaseRestApiHelper.sensorDataManager = 
      (SensorDataManager)server.getContext().getAttributes().get("SensorDataManager");
    SensorBaseRestApiHelper.userManager = 
      (UserManager)server.getContext().getAttributes().get("UserManager");
    SensorBaseRestApiHelper.sdtManager = 
      (SdtManager)server.getContext().getAttributes().get("SdtManager");
    SensorBaseRestApiHelper.projectManager = 
      (ProjectManager)server.getContext().getAttributes().get("ProjectManager");
    SensorBaseRestApiHelper.adminEmail = ServerProperties.get(ADMIN_EMAIL_KEY);
    SensorBaseRestApiHelper.adminPassword = ServerProperties.get(ADMIN_PASSWORD_KEY);
  }
  
  /**
   * Does the housekeeping for making HTTP requests to the SensorBase by a test user.
   * @param method The type of Method.
   * @param requestString A string, such as "users".
   * @param userEmail The email of a test user, used for authentication. Note that test users
   * have their password defined to be the same as their email address during registration. 
   * If userEmail is null, then no authentication credentials are added.
   * @return The Response instance returned from the server.
   */
  protected Response makeRequest(Method method, String requestString, String userEmail) {
    return makeRequest(method, requestString, userEmail, null);
  }
  
 /** 
  * Does the housekeeping for making HTTP requests to the SensorBase by the admin user. 
  * @param method The type of Method.
  * @param requestString A string, such as "users".
  * @return The Response instance returned from the server.
  */
  protected Response makeAdminRequest(Method method, String requestString) {
    return makeRequestInternal(method, requestString, ServerProperties.get(ADMIN_EMAIL_KEY),
        ServerProperties.get(ADMIN_PASSWORD_KEY), null);
  }
  
  /** 
   * Does the housekeeping for making HTTP requests to the SensorBase by the admin user. 
   * @param method The type of Method.
   * @param requestString A string, such as "users".
   * @param entity The representation to be sent with the request. 
   * @return The Response instance returned from the server.
   */
   protected Response makeAdminRequest(Method method, String requestString,
       Representation entity) {
     return makeRequestInternal(method, requestString, ServerProperties.get(ADMIN_EMAIL_KEY),
         ServerProperties.get(ADMIN_PASSWORD_KEY), entity);
   }
  
  /**
   * Does the housekeeping for making HTTP requests to the SensorBase by a test user. 
   * @param method The type of Method.
   * @param requestString A string, such as "users".
   * @param userEmail The email of a test user, used for authentication. Note that test users
   * have their password defined to be the same as their email address during registration. 
   * If userEmail is null, then no authentication credentials are added.
   * @param entity The representation to be sent with the request. 
   * @return The Response instance returned from the server.
   */
  protected Response makeRequest(Method method, String requestString, String userEmail, 
      Representation entity) {
    return makeRequestInternal(method, requestString, userEmail, userEmail, entity);
  }
  
  /**
   * Does the housekeeping for making HTTP requests to the SensorBase by a test or admin user. 
   * @param method The type of Method.
   * @param requestString A string, such as "users".
   * @param userEmail The email of the user (testuser or admin user).
   * @param userPassword The password of the user (testuser or admin user).
   * @param entity The representation to be sent with the request. 
   * @return The Response instance returned from the server.
   */
  private Response makeRequestInternal(Method method, String requestString, String userEmail,
      String userPassword, Representation entity) {
    String hostName = SensorBaseRestApiHelper.server.getHostName();
    Reference reference = new Reference(hostName + requestString);
    Request request = (entity == null) ? 
        new Request(method, reference) :
          new Request(method, reference, entity);
    Preference<MediaType> xmlMedia = new Preference<MediaType>(MediaType.TEXT_XML);
    request.getClientInfo().getAcceptedMediaTypes().add(xmlMedia); 
    // Add authentication info unless userEmail is null.
    if (userEmail != null) {
      ChallengeScheme scheme =  ChallengeScheme.HTTP_BASIC;
      ChallengeResponse authentication = new ChallengeResponse(scheme, userEmail, userPassword);
      request.setChallengeResponse(authentication);
    }
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
