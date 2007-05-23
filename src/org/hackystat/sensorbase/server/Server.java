package org.hackystat.sensorbase.server;

import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
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
   * @param port The port number for this Server. 
   * @return The Server instance created. 
   * @throws Exception If problems occur starting up this server. 
   */
  public static Server newInstance(int port) throws Exception {
    Server server = new Server();
    server.initializeProperties();
    server.hostName = "http://" +
                      System.getProperty("sensorbase.hostname") + 
                      ":" + 
                      System.getProperty("sensorbase.port") + 
                      "/";
    server.component = new Component();
    server.component.getServers().add(Protocol.HTTP, port);
    server.component.getDefaultHost()
      .attach("/" + System.getProperty("sensorbase.context.root"), server);
    server.component.getLogService().setEnabled(false);
    server.component.start();
    SensorBaseLogger.getLogger().warning("Started SensorBase (Version " + getVersion() + ")");
    SensorBaseLogger.getLogger().warning("Host: " + server.hostName);
    SensorBaseLogger.setLoggingLevel(System.getProperty("sensorbase.logging.level"));
    server.echoProperties();
    
    // Get rid of the Restlet Logger
    // Save a pointer to this Server instance in this Application's context. 
    Map<String, Object> attributes = server.getContext().getAttributes();
    attributes.put("SdtManager", new SdtManager(server));
    attributes.put("UserManager", new UserManager(server));
    return server;
  }
  
  /**
   * Starts up the SensorBase web service on port 9876.  Control-c to exit. 
   * @param args Ignored. 
   * @throws Exception if problems occur.
   */
  public static void main(final String[] args) throws Exception {
    Server.newInstance(9876);
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
    router.attach("/users/{userkey}", UserResource.class);
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
   * Example: "http://localhost:9876/"
   * @return The host name. 
   */
  public String getHostName() {
    return this.hostName;
  }
  
  /**
   * Adds the properties in ~/.hackystat/sensorbase.properties into System Properties.
   */
  private void initializeProperties () {
    String userHome = System.getProperty("user.home");
    String userDir = System.getProperty("user.dir");
    String hackyHome = userHome + "/.hackystat";
    String sensorBaseHome = hackyHome + "/sensorbase"; 
    String propFile = userHome + "/.hackystat/sensorbase.properties";
    Properties properties = new Properties();
    // Set defaults
    properties.setProperty("sensorbase.admin.email", "admin@hackystat.org");
    properties.setProperty("sensorbase.admin.userkey", "admin");
    properties.setProperty("sensorbase.context.root", "sensorbase");
    properties.setProperty("sensorbase.db.dir", sensorBaseHome + "/db");
    properties.setProperty("sensorbase.hostname", "localhost");
    properties.setProperty("sensorbase.logging.level", "INFO");
    properties.setProperty("sensorbase.mail.server", "mail.hawaii.edu");
    properties.setProperty("sensorbase.port", "9876");
    properties.setProperty("sensorbase.xml.dir", userDir + "/xml");
    try {
      properties.load(new FileInputStream(propFile));
    }
    catch (Exception e) {
      System.out.println(propFile + " not found. Using default sensorbase properties.");
    }
    // Now add to System properties.
    Properties systemProperties = System.getProperties();
    systemProperties.putAll(properties);
    System.setProperties(systemProperties);
  }

  /**
   * Prints all of the sensorbase settings to the logger.  
   */
  private void echoProperties() {
    String cr = System.getProperty("line.separator"); 
    String propertyInfo = cr + "SensorBase Properties:" + cr +
      "  sensorbase.admin.email:   " + System.getProperty("sensorbase.admin.email") + cr +
      "  sensorbase.admin.userkey: " + System.getProperty("sensorbase.admin.userkey") + cr +
      "  sensorbase.hostname:      " + System.getProperty("sensorbase.hostname") + cr +
      "  sensorbase.context.root:  " + System.getProperty("sensorbase.context.root") + cr +
      "  sensorbase.db.dir:        " + System.getProperty("sensorbase.db.dir") + cr +
      "  sensorbase.logging.level: " + System.getProperty("sensorbase.logging.level") + cr +
      "  sensorbase.mail.server :  " + System.getProperty("sensorbase.mail.server") + cr +
      "  sensorbase.port:          " + System.getProperty("sensorbase.port") + cr +
      "  sensorbase.test.install:  " + System.getProperty("sensorbase.test.install") + cr + 
      "  sensorbase.xml.dir:       " + System.getProperty("sensorbase.xml.dir");
    SensorBaseLogger.getLogger().info(propertyInfo);
  }
}

