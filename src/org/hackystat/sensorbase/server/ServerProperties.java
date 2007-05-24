package org.hackystat.sensorbase.server;

import java.io.FileInputStream;
import java.util.Properties;

import org.hackystat.sensorbase.logger.SensorBaseLogger;

/**
 * Provides access to the values stored in the sensorbase.properties file. 
 * @author Philip Johnson
 */
public class ServerProperties {
  
  /** The admin email key. */
  public static final String ADMIN_EMAIL_KEY =     "sensorbase.admin.email";
  /** The admin userkey key. */
  public static final String ADMIN_USERKEY_KEY =   "sensorbase.admin.userkey";
  /** The context root key. */
  public static final String CONTEXT_ROOT_KEY =    "sensorbase.context.root";
  /** The database directory key. */
  public static final String DB_DIR_KEY =          "sensorbase.db.dir";
  /** The hostname key. */
  public static final String HOSTNAME_KEY =        "sensorbase.hostname";
  /** The logging level key. */
  public static final String LOGGING_LEVEL_KEY =   "sensorbase.logging.level";
  /** The SMTP host key. */
  public static final String SMTP_HOST_KEY =       "sensorbase.smtp.host";
  /** The sensorbase port key. */
  public static final String PORT_KEY =            "sensorbase.port";
  /** The XML directory key. */
  public static final String XML_DIR_KEY =         "sensorbase.xml.dir";
  /** The test installation key. */
  public static final String TEST_INSTALL_KEY =    "sensorbase.test.install";
  /** The test domain key. */
  public static final String TEST_DOMAIN_KEY =     "sensorbase.test.domain";
  
  /**
   * Reads in the properties in ~/.hackystat/sensorbase.properties if this file exists,
   * and provides default values for all properties. .
   * @throws Exception if errors occur.
   */
  static void initializeProperties () throws Exception {
    String userHome = System.getProperty("user.home");
    String userDir = System.getProperty("user.dir");
    String hackyHome = userHome + "/.hackystat";
    String sensorBaseHome = hackyHome + "/sensorbase"; 
    String propFile = userHome + "/.hackystat/sensorbase.properties";
    Properties properties = new Properties();
    // Set defaults
    properties.setProperty(ADMIN_EMAIL_KEY, "admin@hackystat.org");
    properties.setProperty(ADMIN_USERKEY_KEY, "admin");
    properties.setProperty(CONTEXT_ROOT_KEY, "sensorbase");
    properties.setProperty(DB_DIR_KEY, sensorBaseHome + "/db");
    properties.setProperty(HOSTNAME_KEY, "localhost");
    properties.setProperty(LOGGING_LEVEL_KEY, "INFO");
    properties.setProperty(SMTP_HOST_KEY, "mail.hawaii.edu");
    properties.setProperty(PORT_KEY, "9876");
    properties.setProperty(XML_DIR_KEY, userDir + "/xml");
    properties.setProperty(TEST_INSTALL_KEY, "false");
    properties.setProperty(TEST_DOMAIN_KEY, "hackystat.org");
    FileInputStream stream = null;
    try {
      stream = new FileInputStream(propFile);
      properties.load(stream);
    }
    catch (Exception e) {
      System.out.println(propFile + " not found. Using default sensorbase properties.");
    }
    finally {
      if (stream != null) {
        stream.close();
      }
    }
    // Now add to System properties.
    Properties systemProperties = System.getProperties();
    systemProperties.putAll(properties);
    System.setProperties(systemProperties);
  }

  /**
   * Prints all of the sensorbase settings to the logger.  
   */
  static void echoProperties() {
    String cr = System.getProperty("line.separator"); 
    String eq = " = ";
    String propertyInfo = cr + "SensorBase Properties:" + cr +
      " " + ADMIN_EMAIL_KEY   + eq + get(ADMIN_EMAIL_KEY) + cr +
      " " + ADMIN_USERKEY_KEY + eq + get(ADMIN_USERKEY_KEY) + cr +
      " " + HOSTNAME_KEY      + eq + get(HOSTNAME_KEY) + cr +
      " " + CONTEXT_ROOT_KEY  + eq + get(CONTEXT_ROOT_KEY) + cr +
      " " + DB_DIR_KEY        + eq + get(DB_DIR_KEY) + cr +
      " " + LOGGING_LEVEL_KEY + eq + get(LOGGING_LEVEL_KEY) + cr +
      " " + SMTP_HOST_KEY     + eq + get(SMTP_HOST_KEY) + cr +
      " " + PORT_KEY          + eq + get(PORT_KEY) + cr +
      " " + TEST_INSTALL_KEY  + eq + get(TEST_INSTALL_KEY) + cr + 
      " " + XML_DIR_KEY       + eq + get(XML_DIR_KEY);
    SensorBaseLogger.getLogger().info(propertyInfo);
  }
  
  /**
   * Returns the value of the Server Property specified by the key.
   * @param key Should be one of the public static final strings in this class.
   * @return The value of the key, or null if not found.
   */
  public static String get(String key) {
    return System.getProperty(key);
  }

}
