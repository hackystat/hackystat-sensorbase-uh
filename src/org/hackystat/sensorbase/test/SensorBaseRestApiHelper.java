package org.hackystat.sensorbase.test;

import org.hackystat.sensorbase.resource.projects.ProjectManager;
import org.hackystat.sensorbase.resource.sensordata.SensorDataManager;
import org.hackystat.sensorbase.resource.sensordatatypes.SdtManager;
import org.hackystat.sensorbase.resource.users.UserManager;
import org.hackystat.sensorbase.server.Server;
import static org.hackystat.sensorbase.server.ServerProperties.ADMIN_EMAIL_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.ADMIN_PASSWORD_KEY;
import org.junit.BeforeClass;


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
  //private static Client client;
  
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
    //SensorBaseRestApiHelper.client = new Client(Protocol.HTTP);
    SensorBaseRestApiHelper.sensorDataManager = 
      (SensorDataManager)server.getContext().getAttributes().get("SensorDataManager");
    SensorBaseRestApiHelper.userManager = 
      (UserManager)server.getContext().getAttributes().get("UserManager");
    SensorBaseRestApiHelper.sdtManager = 
      (SdtManager)server.getContext().getAttributes().get("SdtManager");
    SensorBaseRestApiHelper.projectManager = 
      (ProjectManager)server.getContext().getAttributes().get("ProjectManager");
    SensorBaseRestApiHelper.adminEmail = server.getServerProperties().get(ADMIN_EMAIL_KEY);
    SensorBaseRestApiHelper.adminPassword = server.getServerProperties().get(ADMIN_PASSWORD_KEY);
  }

  /**
   * Returns the hostname associated with this test server. 
   * @return The host name, including the context root. 
   */
  protected String getHostName() {
    return SensorBaseRestApiHelper.server.getHostName();
  }
}
