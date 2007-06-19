package org.hackystat.sensorbase.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.server.Server;
import org.hackystat.sensorbase.server.ServerProperties;
import static org.hackystat.sensorbase.server.ServerProperties.DB_DIR_KEY;

/**
 * Provides an interface to persistency for the four resources managed by the SensorBase.
 * @author Philip Johnson
 */
public class DbManager {
  
  /** The Server instance this DbManager is attached to. */
  //private Server server;
  
  /** The JDBC driver. */
  private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
  
  /** The Database name. */
  private static final String dbName = "sensorbase";
  
  /**  The Derby connection URL. */ 
  private static final String connectionURL = "jdbc:derby:" + dbName + ";create=true";
  
  /** Indicates whether this database was initialized or was pre-existing. */
  private boolean isFreshDb;
  
  /** The SQL string for creating the SensorData table. */
  private static final String createSensorDataTableStatement = 
    "create table SensorData  "
    + "("
    + " SensorData_ID INT NOT NULL GENERATED ALWAYS AS IDENTITY " 
    + "   CONSTRAINT SENSORDATA_PK PRIMARY KEY, " 
    + " Name VARCHAR(64) NOT NULL, "
    + " Tstamp TIMESTAMP NOT NULL, "
    + " Sdt VARCHAR(64) NOT NULL, "
    + " Runtime TIMESTAMP NOT NULL, "
    + " Tool VARCHAR(64) NOT NULL, "
    + " Resource VARCHAR(512) NOT NULL, "
    + " XmlResource VARCHAR(32000) NOT NULL, "
    + " XmlRef VARCHAR(1000) NOT NULL, "
    + " LastMod TIMESTAMP NOT NULL "
    + ")" ;
  
  /** An SQL string to test whether the SensorData table exists and has the correct schema. */
  private static final String testSensorDataTableStatement = 
    " UPDATE SensorData SET "
    + " Name = 'TEST', " 
    + " Tstamp = '" + new Timestamp(new Date().getTime()).toString() + "', "
    + " Sdt = 'testSdt',"
    + " Runtime = '" + new Timestamp(new Date().getTime()).toString() + "', "
    + " Tool = 'testTool', "
    + " Resource = 'testResource', "
    + " XmlResource = 'testXmlResource', "
    + " XmlRef = 'testXmlRef', "
    + " LastMod = '" + new Timestamp(new Date().getTime()).toString() + "' "
    + " WHERE 1=3";
  
  private static final String indexSensorDataTableStatement = 
    "CREATE UNIQUE INDEX SensorDataIndex ON SensorData(Name, Tstamp)";

  /**
   * Creates a new DbManager which manages access to the underlying database.
   * @param server The Restlet server instance. 
   */
  public DbManager(Server server) {
    //this.server = server;

    // Set the directory where the DB will be created and/or accessed.
    String dbDir = ServerProperties.get(DB_DIR_KEY);
    System.getProperties().put("derby.system.home", dbDir);

    // Try to load the derby driver. 
    try {
      Class.forName(driver); 
    } 
    catch (java.lang.ClassNotFoundException e) {
      String msg = "Exception during DbManager initialization: Derby not on CLASSPATH.";
      SensorBaseLogger.getLogger().warning(msg + "\n" + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
    
    try {
      // Initialize the database table structure if necessary.
      this.isFreshDb = !isPreExisting();
      SensorBaseLogger.getLogger().info("Fresh DB is: " + this.isFreshDb);
      if (this.isFreshDb) {
        SensorBaseLogger.getLogger().info("About to create DB in: " + dbDir);
        initializeDbSchema();
        SensorBaseLogger.getLogger().info("DB initialized.");
      }
    }
    catch (Exception e) {
      String msg = "Exception during DbManager initialization:";
      SensorBaseLogger.getLogger().warning(msg + "/n" + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
  }
  
  /**
   * Returns true if this Db was freshly initialized upon instantiation of the DbManager.
   * This indicates the need for the various Resource Managers to read in the default data
   * to this database.
   * @return True if this DB was freshly created or not.
   */
  public boolean isFreshDb() {
    return this.isFreshDb;
  }
  
  /**
   * Determine if the database has already been initialized.  
   * Checks to see if a dummy insert on the table will work OK.
   * @return True if the database has been initialized.
   * @throws SQLException If problems occur accessing the database. 
   */
  private boolean isPreExisting() throws SQLException {
    Connection conn = null;
    Statement s = null;
    try {
      conn = DriverManager.getConnection(connectionURL);
      s = conn.createStatement();
      s.execute(testSensorDataTableStatement);
    }  
    catch (SQLException e) {
      String theError = (e).getSQLState();
      System.out.println("  Utils GOT:  " + theError);
      /** If table exists will get -  WARNING 02000: No row was found **/
      if ("42X05".equals(theError)) {
        System.out.println(" table does not exist");
        return false;
      }  
      else if ("42X14".equals(theError) || "42821".equals(theError))  {
        System.out.println("Incorrect table definition.");
        throw e;   
      } 
      else { 
        System.out.println("Unknown SQLException" );
        throw e; 
      }
    }
    finally {
      s.close();
      conn.close();
    }
    System.out.println("Just got the warning - table exists OK ");
    return true;
  }
  
  /**
   * Initialize the database by creating tables for each resource type.
   * @throws SQLException If table creation fails.
   */
  private void initializeDbSchema() throws SQLException {
    Connection conn = null;
    Statement s = null;
    try {
      conn = DriverManager.getConnection(connectionURL);
      s = conn.createStatement();
      s.execute(createSensorDataTableStatement);
      s.execute(indexSensorDataTableStatement);
      s.close();
    }
    finally {
      s.close();
      conn.close();
    }
  }

  /**
   * Inserts the SensorData
   * @param data The sensor data. 
   * @param xmlSensorData The sensor data resource as an XML String.  
   * @param xmlSensorDataRef The sensor data resource as an XML resource reference
   * @return True if the sensor data was succesfully inserted.
   */
  public boolean insertSensorData(SensorData data, String xmlSensorData, String xmlSensorDataRef) {
    
    return true;
    
  }

}
