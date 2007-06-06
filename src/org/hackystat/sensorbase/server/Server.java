package org.hackystat.sensorbase.server;

import java.util.Map;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.mail.Mailer;
import org.hackystat.sensorbase.resource.projects.ProjectManager;
import org.hackystat.sensorbase.resource.projects.ProjectsResource;
import org.hackystat.sensorbase.resource.projects.UserProjectResource;
import org.hackystat.sensorbase.resource.projects.UserProjectSensorDataResource;
import org.hackystat.sensorbase.resource.projects.UserProjectsResource;
import org.hackystat.sensorbase.resource.sensordata.SensorDataManager;
import org.hackystat.sensorbase.resource.sensordata.SensorDataResource;
import org.hackystat.sensorbase.resource.sensordata.UserSensorDataResource;
import org.hackystat.sensorbase.resource.sensordatatypes.SdtManager;
import org.hackystat.sensorbase.resource.sensordatatypes.SensorDataTypeResource;
import org.hackystat.sensorbase.resource.sensordatatypes.SensorDataTypesResource;
import org.hackystat.sensorbase.resource.users.UserManager;
import org.hackystat.sensorbase.resource.users.UserResource;
import org.hackystat.sensorbase.resource.users.UsersResource;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Protocol;
import static org.hackystat.sensorbase.server.ServerProperties.HOSTNAME_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.PORT_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.CONTEXT_ROOT_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.LOGGING_LEVEL_KEY;


/**
 * Sets up the HTTP Server process and dispatching to the associated resources. 
 * @author Philip Johnson
 */
public class Server extends Application { 

  /** Holds the Restlet Component associated with this Server. */
  private Component component; 
  
  /** Holds the host name associated with this Server. */
  private String hostName;
  
  /**
   * Creates a new instance of a SensorBase HTTP server, listening on the supplied port.  
   * @return The Server instance created. 
   * @throws Exception If problems occur starting up this server. 
   */
  public static Server newInstance() throws Exception {
    Server server = new Server();
    ServerProperties.initializeProperties();
    server.hostName = "http://" +
                      ServerProperties.get(HOSTNAME_KEY) + 
                      ":" + 
                      ServerProperties.get(PORT_KEY) + 
                      "/" +
                      ServerProperties.get(CONTEXT_ROOT_KEY) +
                      "/";
    int port = Integer.valueOf(ServerProperties.get(PORT_KEY));
    server.component = new Component();
    server.component.getServers().add(Protocol.HTTP, port);
    server.component.getDefaultHost()
      .attach("/" + ServerProperties.get(CONTEXT_ROOT_KEY), server);
    server.component.getLogService().setEnabled(false);
    server.component.start();
    SensorBaseLogger.getLogger().warning("Started SensorBase (Version " + getVersion() + ")");
    SensorBaseLogger.getLogger().warning("Host: " + server.hostName);
    SensorBaseLogger.setLoggingLevel(ServerProperties.get(LOGGING_LEVEL_KEY));
    ServerProperties.echoProperties();
    try {
      Mailer.getInstance();
    }
    catch (Throwable e) {
      String msg = "ERROR: JavaMail not installed correctly! Mail services will fail!";
      SensorBaseLogger.getLogger().warning(msg);
    }
    
    
    // Get rid of the Restlet Logger
    // Save a pointer to this Server instance in this Application's context. 
    Map<String, Object> attributes = server.getContext().getAttributes();
    attributes.put("SdtManager", new SdtManager(server));
    attributes.put("UserManager", new UserManager(server));
    attributes.put("SensorDataManager", new SensorDataManager(server));
    attributes.put("ProjectManager", new ProjectManager(server));
    return server;
  }
  
  /**
   * Starts up the SensorBase web service on port 9876.  Control-c to exit. 
   * @param args Ignored. 
   * @throws Exception if problems occur.
   */
  public static void main(final String[] args) throws Exception {
    Server.newInstance();
  }

  /**
   * Dispatch to the Projects, SensorData, SensorDataTypes, or Users Resource depending on the URL.
   * @return The router Restlet.
   */
  @Override
  public Restlet createRoot() {
    Router router = new Router(getContext());
    router.attach("/sensordatatypes", SensorDataTypesResource.class);
    router.attach("/sensordatatypes/{sensordatatypename}", SensorDataTypeResource.class);
    router.attach("/users", UsersResource.class);
    router.attach("/users?email={email}", UsersResource.class);
    router.attach("/users/{userkey}", UserResource.class);
    router.attach("/sensordata", SensorDataResource.class);
    router.attach("/sensordata/{userkey}", UserSensorDataResource.class);
    router.attach("/sensordata/{userkey}/{sensordatatype}", UserSensorDataResource.class);
    router.attach("/sensordata/{userkey}/{sensordatatype}/{timestamp}", 
        UserSensorDataResource.class);
    router.attach("/projects", ProjectsResource.class);
    router.attach("/projects/{userkey}", UserProjectsResource.class);
    router.attach("/projects/{userkey}/{projectname}", UserProjectResource.class);
    router.attach("/projects/{userkey}/{projectname}/sensordata", 
        UserProjectSensorDataResource.class);
    router.attach(
        "/projects/{userkey}/{projectname}/sensordata?startTime={startTime}&endTime={endTime}", 
        UserProjectSensorDataResource.class);
    return router;
  }


  /**
   * Returns the version associated with this Package, if available from the jar file manifest.
   * If not being run from a jar file, then returns "Development". 
   * @return The version.
   */
  public static String getVersion() {
    String version = 
      Package.getPackage("org.hackystat.sensorbase.server").getImplementationVersion();
    return (version == null) ? "Development" : version; 
  }
  
  /**
   * Returns the host name associated with this server. 
   * Example: "http://localhost:9876/sensorbase/"
   * @return The host name. 
   */
  public String getHostName() {
    return this.hostName;
  }
}

