package org.hackystat.sensorbase.server;

import java.util.Enumeration;
import java.util.Map;

import org.hackystat.sensorbase.db.DbManager;
import org.hackystat.sensorbase.mail.Mailer;
import org.hackystat.sensorbase.resource.projects.ProjectManager;
import org.hackystat.sensorbase.resource.projects.ProjectsResource;
import org.hackystat.sensorbase.resource.projects.UserProjectResource;
import org.hackystat.sensorbase.resource.projects.UserProjectSensorDataResource;
import org.hackystat.sensorbase.resource.projects.UserProjectsResource;
import org.hackystat.sensorbase.resource.registration.RegistrationResource;
import org.hackystat.sensorbase.resource.sensordata.SensorDataManager;
import org.hackystat.sensorbase.resource.sensordata.SensorDataResource;
import org.hackystat.sensorbase.resource.sensordata.UserSensorDataResource;
import org.hackystat.sensorbase.resource.sensordatatypes.SdtManager;
import org.hackystat.sensorbase.resource.sensordatatypes.SensorDataTypeResource;
import org.hackystat.sensorbase.resource.sensordatatypes.SensorDataTypesResource;
import org.hackystat.sensorbase.resource.users.UserManager;
import org.hackystat.sensorbase.resource.users.UserResource;
import org.hackystat.sensorbase.resource.users.UsersResource;
import org.hackystat.utilities.logger.HackystatLogger;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Guard;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Protocol;

import static org.hackystat.sensorbase.server.ServerProperties.HOSTNAME_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.PORT_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.CONTEXT_ROOT_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.LOGGING_LEVEL_KEY;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Sets up the HTTP Server process and dispatching to the associated resources. 
 * @author Philip Johnson
 */
public class Server extends Application { 

  /** Holds the Restlet Component associated with this Server. */
  private Component component; 
  
  /** Holds the host name associated with this Server. */
  private String hostName;
  
  /** Holds the HackystatLogger for the sensorbase. */
  private Logger logger; 
  
  /**
   * Creates a new instance of a SensorBase HTTP server, listening on the supplied port.  
   * @return The Server instance created. 
   * @throws Exception If problems occur starting up this server. 
   */
  public static Server newInstance() throws Exception {
    Server server = new Server();
    server.logger = HackystatLogger.getLogger("org.hackystat.sensorbase");
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
 
    
    try {
      Mailer.getInstance();
    }
    catch (Throwable e) {
      String msg = "ERROR: JavaMail not installed correctly! Mail services will fail!";
      server.logger.warning(msg);
    }

    // Now create all of the Resource Managers and store them in the Context.
    // Ordering constraints: 
    // - DbManager must precede all resource managers so it can initialize the tables
    //   before the resource managers add data to them.
    // - UserManager must be initialized before ProjectManager, since ProjectManager needs
    //   to know about the Users. 
    Map<String, Object> attributes = server.getContext().getAttributes();
    attributes.put("DbManager", new DbManager(server));
    attributes.put("SdtManager", new SdtManager(server));
    attributes.put("UserManager", new UserManager(server));
    attributes.put("ProjectManager", new ProjectManager(server));
    attributes.put("SensorDataManager", new SensorDataManager(server));
    attributes.put("SensorBaseServer", server);
    
    // Now let's open for business. 
    server.logger.warning("Host: " + server.hostName);
    HackystatLogger.setLoggingLevel(server.logger, ServerProperties.get(LOGGING_LEVEL_KEY));
    ServerProperties.echoProperties(server);
    server.logger.warning("SensorBase (Version " + getVersion() + ") now running.");
    server.component.start();
    disableRestletLogging();
    return server;
  }

  /**
   * Disable all loggers from com.noelios and org.restlet. 
   */
  private static void disableRestletLogging() {
    LogManager logManager = LogManager.getLogManager();
    for (Enumeration e = logManager.getLoggerNames(); e.hasMoreElements() ;) {
      String logName = e.nextElement().toString();
      if (logName.startsWith("com.noelios") ||
          logName.startsWith("org.restlet")) {
        logManager.getLogger(logName).setLevel(Level.OFF);
      }
    }
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
   * We will authenticate all requests except for registration (users?email={email}).
   * @return The router Restlet.
   */
  @Override
  public Restlet createRoot() {
    // First, create a Router that will have a Guard placed in front of it so that this Router's
    // requests will require authentication.
    Router authRouter = new Router(getContext());
    authRouter.attach("/sensordatatypes", SensorDataTypesResource.class);
    authRouter.attach("/sensordatatypes/{sensordatatypename}", SensorDataTypeResource.class);
    authRouter.attach("/users", UsersResource.class);
    authRouter.attach("/users/{user}", UserResource.class);
    authRouter.attach("/sensordata", SensorDataResource.class);
    authRouter.attach("/sensordata/{user}", UserSensorDataResource.class);
    authRouter.attach("/sensordata/{user}?sdt={sensordatatype}", UserSensorDataResource.class);
    authRouter.attach("/sensordata/{user}/{timestamp}", 
        UserSensorDataResource.class);
    authRouter.attach("/projects", ProjectsResource.class);
    authRouter.attach("/projects/{user}", UserProjectsResource.class);
    authRouter.attach("/projects/{user}/{projectname}", UserProjectResource.class);
    authRouter.attach("/projects/{user}/{projectname}/sensordata", 
        UserProjectSensorDataResource.class);
    authRouter.attach(
        "/projects/{user}/{projectname}/sensordata?startTime={startTime}&endTime={endTime}", 
        UserProjectSensorDataResource.class);
    // Here's the Guard that we will place in front of authRouter.
    Guard guard = new Authenticator(getContext());
    guard.setNext(authRouter);
    
    // Now create our "top-level" router which will allow the registration URI to proceed without
    // authentication, but all other URI patterns will go to the guarded Router. 
    Router router = new Router(getContext());
    router.attach("/register", RegistrationResource.class);
    router.attachDefault(guard);
    
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
  
  /**
   * Returns the logger for the SensorBase. 
   * @return The logger.
   */
  public Logger getLogger() {
    return this.logger;
  }
}

