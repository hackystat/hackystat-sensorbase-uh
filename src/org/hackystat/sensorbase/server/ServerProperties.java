package org.hackystat.sensorbase.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Provides access to the values stored in the sensorbase.properties file. 
 * @author Philip Johnson
 */
public class ServerProperties {
  
  /** The admin email key. */
  public static final String ADMIN_EMAIL_KEY =     "sensorbase.admin.email";
  /** The admin password. */
  public static final String ADMIN_PASSWORD_KEY =   "sensorbase.admin.password";
  /** The context root key. */
  public static final String CONTEXT_ROOT_KEY =    "sensorbase.context.root";
  /** The database directory key. */
  public static final String DB_DIR_KEY =          "sensorbase.db.dir";
  /** The database implementation class. */
  public static final String DB_IMPL_KEY =          "sensorbase.db.impl";
  /** The hostname key. */
  public static final String HOSTNAME_KEY =        "sensorbase.hostname";
  /** The logging level key. */
  public static final String LOGGING_LEVEL_KEY =   "sensorbase.logging.level";
  /** The Restlet Logging key. */
  public static final String RESTLET_LOGGING_KEY = "sensorbase.restlet.logging";
  /** The SMTP host key. */
  public static final String SMTP_HOST_KEY =       "sensorbase.smtp.host";
  /** The sensorbase port key. */
  public static final String PORT_KEY =            "sensorbase.port";
  /** The sensorbase port key during testing. */
  public static final String TEST_PORT_KEY =       "sensorbase.test.port";
  /** The XML directory key. */
  public static final String XML_DIR_KEY =         "sensorbase.xml.dir";
  /** The test installation key. */
  public static final String TEST_INSTALL_KEY =    "sensorbase.test.install";
  /** The test domain key. */
  public static final String TEST_DOMAIN_KEY =     "sensorbase.test.domain";
  
  /** Where we store the properties. */
  private Properties properties; 
  
  /**
   * Creates a new ServerProperties instance. 
   * Prints an error to the console if problems occur on loading. 
   */
  public ServerProperties() {
    try {
      initializeProperties(false);
    }
    catch (Exception e) {
      System.out.println("Error initializing server properties.");
    }
  }
  
  /**
   * Creates a new ServerProperties instance. If isTestServer is true, then this ServerProperties
   * instance will return with "testing" properties. The user's sensorbase.properties file is
   * read in, but the following property values are then overridden with the following values:
   * <ul>
   * <li> ADMIN_EMAIL_KEY will be "admin@hackystat.org"
   * <li> ADMIN_PASSWORD_KEY will be "admin@hackystat.org"
   * <li> CONTEXT_ROOT_KEY will be "sensorbase"
   * <li> DB_DIR_KEY will be "{user.home}/.hackystat/sensorbase/testdb"
   * <li> HOSTNAME_KEY will be "localhost"
   * <li> PORT_KEY will be the value of TEST_PORT_KEY, which defaults to 9976.
   * <li> TEST_INSTALL_KEY will be "true".
   * </ul>
   * Prints an error to the console if problems occur on loading.
   * 
   * @param isTestServer If a test SensorBase is being created.
   */
  public ServerProperties(boolean isTestServer) {
    try {
      initializeProperties(isTestServer);
    }
    catch (Exception e) {
      System.out.println("Error initializing server properties.");
    }
  }
  
  /**
   * Reads in the properties in ~/.hackystat/sensorbase.properties if this file exists,
   * and provides default values for all properties.  See the initialize() method for 
   * documentation of the effect of the isTestServer parameter.
   * @param isTestServer if true, then instantiated with test properties. 
   * @throws Exception if errors occur.
   */
  private void initializeProperties (boolean isTestServer) throws Exception {
    String userHome = System.getProperty("user.home");
    String userDir = System.getProperty("user.dir");
    String hackyHome = userHome + "/.hackystat";
    String sensorBaseHome = hackyHome + "/sensorbase"; 
    String propFile = userHome + "/.hackystat/sensorbase/sensorbase.properties";
    String defaultAdmin = "admin@hackystat.org";
    this.properties = new Properties();
    // Set defaults for 'standard' operation.
    properties.setProperty(ADMIN_EMAIL_KEY, defaultAdmin);
    properties.setProperty(ADMIN_PASSWORD_KEY, defaultAdmin);
    properties.setProperty(CONTEXT_ROOT_KEY, "sensorbase");
    properties.setProperty(DB_DIR_KEY, sensorBaseHome + "/db");
    properties.setProperty(DB_IMPL_KEY, "org.hackystat.sensorbase.db.derby.DerbyImplementation");
    properties.setProperty(HOSTNAME_KEY, "localhost");
    properties.setProperty(LOGGING_LEVEL_KEY, "INFO");
    properties.setProperty(RESTLET_LOGGING_KEY, "false");
    properties.setProperty(SMTP_HOST_KEY, "mail.hawaii.edu");
    properties.setProperty(PORT_KEY, "9876");
    properties.setProperty(TEST_PORT_KEY, "9976");
    properties.setProperty(XML_DIR_KEY, userDir + "/xml");
    properties.setProperty(TEST_INSTALL_KEY, "false");
    properties.setProperty(TEST_DOMAIN_KEY, "hackystat.org");
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(propFile);
      properties.load(stream);
      System.out.println("Loading SensorBase properties from: " + propFile);
    }
    catch (IOException e) {
      System.out.println(propFile + " not found. Using default sensorbase properties.");
    }
    finally {
      if (stream != null) {
        stream.close();
      }
    }
    // If this is a testing situation, then override the following values:
    if (isTestServer) {
      properties.setProperty(ADMIN_EMAIL_KEY, defaultAdmin);
      properties.setProperty(ADMIN_PASSWORD_KEY, defaultAdmin);
      properties.setProperty(CONTEXT_ROOT_KEY, "sensorbase");
      properties.setProperty(DB_DIR_KEY, sensorBaseHome + "/testdb");
      properties.setProperty(HOSTNAME_KEY, "localhost");
      properties.setProperty(PORT_KEY, properties.getProperty(TEST_PORT_KEY));
      properties.setProperty(TEST_INSTALL_KEY, "true");
    }
    
    // Now add to System properties. Since the Mailer class expects to find this stuff on the 
    // System Properties, we will add everything to it.  In general, however, clients should not
    // use the System Properties to get at these values, since that precludes running several
    // SensorBases in a single JVM.   And just is generally bogus. 
    Properties systemProperties = System.getProperties();
    systemProperties.putAll(properties);
    System.setProperties(systemProperties);
  }

  /**
   * Prints all of the sensorbase settings to the logger.
   * @param server The SensorBase server.   
   */
  public void echoProperties(Server server) {
    String cr = System.getProperty("line.separator"); 
    String eq = " = ";
    String pad = "                ";
    String propertyInfo = "SensorBase Properties:" + cr +
      pad + ADMIN_EMAIL_KEY   + eq + get(ADMIN_EMAIL_KEY) + cr +
      pad + ADMIN_PASSWORD_KEY + eq + get(ADMIN_PASSWORD_KEY) + cr +
      pad + HOSTNAME_KEY      + eq + get(HOSTNAME_KEY) + cr +
      pad + CONTEXT_ROOT_KEY  + eq + get(CONTEXT_ROOT_KEY) + cr +
      pad + DB_DIR_KEY        + eq + get(DB_DIR_KEY) + cr +
      pad + DB_IMPL_KEY       + eq + get(DB_IMPL_KEY) + cr +
      pad + LOGGING_LEVEL_KEY + eq + get(LOGGING_LEVEL_KEY) + cr +
      pad + RESTLET_LOGGING_KEY + eq + get(RESTLET_LOGGING_KEY) + cr +
      pad + SMTP_HOST_KEY     + eq + get(SMTP_HOST_KEY) + cr +
      pad + PORT_KEY          + eq + get(PORT_KEY) + cr +
      pad + TEST_INSTALL_KEY  + eq + get(TEST_INSTALL_KEY) + cr + 
      pad + XML_DIR_KEY       + eq + get(XML_DIR_KEY);
    server.getLogger().info(propertyInfo);
  }
  
  /**
   * Returns the value of the Server Property specified by the key.
   * @param key Should be one of the public static final strings in this class.
   * @return The value of the key, or null if not found.
   */
  public String get(String key) {
    return this.properties.getProperty(key);
  }
  
  /**
   * Returns the fully qualified host name, such as "http://localhost:9876/sensorbase/".
   * @return The fully qualified host name.
   */
  public String getFullHost() {
    return "http://" + get(HOSTNAME_KEY) + ":" + get(PORT_KEY) + "/" + get(CONTEXT_ROOT_KEY) + "/";
  }
}
