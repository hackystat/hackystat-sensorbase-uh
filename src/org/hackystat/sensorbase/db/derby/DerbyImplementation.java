package org.hackystat.sensorbase.db.derby;

import static org.hackystat.sensorbase.server.ServerProperties.DB_DIR_KEY;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.db.DbImplementation;
import org.hackystat.sensorbase.logger.StackTrace;
import org.hackystat.sensorbase.resource.sensordata.Tstamp;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.Server;
import org.hackystat.sensorbase.server.ServerProperties;
import org.w3c.dom.Document;

/**
 * Provides a implementation of DbImplementation using Derby in embedded mode.
 * @author Philip Johnson
 */
public class DerbyImplementation extends DbImplementation {
  
  /** The JDBC driver. */
  private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
  
  /** The Database name. */
  private static final String dbName = "sensorbase";
  
  /**  The Derby connection URL. */ 
  private static final String connectionURL = "jdbc:derby:" + dbName + ";create=true";
  
  /** Indicates whether this database was initialized or was pre-existing. */
  private boolean isFreshlyCreated;
  
  /** The SQL string for creating the SensorData table. */
  private static final String createSensorDataTableStatement = 
    "create table SensorData  "
    + "("
    + " Owner VARCHAR(64) NOT NULL, "
    + " Tstamp TIMESTAMP NOT NULL, "
    + " Sdt VARCHAR(64) NOT NULL, "
    + " Runtime TIMESTAMP NOT NULL, "
    + " Tool VARCHAR(64) NOT NULL, "
    + " Resource VARCHAR(512) NOT NULL, "
    + " XmlSensorData VARCHAR(32000) NOT NULL, "
    + " XmlSensorDataRef VARCHAR(1000) NOT NULL, "
    + " LastMod TIMESTAMP NOT NULL, "
    + " PRIMARY KEY (Owner, Tstamp) "
    + ")" ;
  
  /** An SQL string to test whether the SensorData table exists and has the correct schema. */
  private static final String testSensorDataTableStatement = 
    " UPDATE SensorData SET "
    + " Owner = 'TestUser', " 
    + " Tstamp = '" + new Timestamp(new Date().getTime()).toString() + "', "
    + " Sdt = 'testSdt',"
    + " Runtime = '" + new Timestamp(new Date().getTime()).toString() + "', "
    + " Tool = 'testTool', "
    + " Resource = 'testResource', "
    + " XmlSensorData = 'testXmlResource', "
    + " XmlSensorDataRef = 'testXmlRef', "
    + " LastMod = '" + new Timestamp(new Date().getTime()).toString() + "' "
    + " WHERE 1=3";
  
  private static final String indexSensorDataTableStatement = 
    "CREATE UNIQUE INDEX SensorDataIndex ON SensorData(Owner, Tstamp)";
  
  /** The SQL state indicating that INSERT tried to add data to a table with a preexisting key. */
  private static final String DUPLICATE_KEY = "23505";

  /**
   * Instantiates the Derby implementation.  Throws a Runtime exception if the Derby
   * jar file cannot be found on the classpath.
   * @param server The SensorBase server instance. 
   */
  public DerbyImplementation(Server server) {
    super(server);
    // Try to load the derby driver. 
    try {
      Class.forName(driver); 
    } 
    catch (java.lang.ClassNotFoundException e) {
      String msg = "Exception during DbManager initialization: Derby not on CLASSPATH.";
      this.logger.warning(msg + "\n" + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
  }


  /** {@inheritDoc} */
  @Override
  public void initialize() {
    try {
      // Set the directory where the DB will be created and/or accessed.
      String dbDir = ServerProperties.get(DB_DIR_KEY);
      System.getProperties().put("derby.system.home", dbDir);
      // Initialize the database table structure if necessary.
      this.isFreshlyCreated = !isPreExisting();
      String dbStatusMsg = (this.isFreshlyCreated) ? "DB uninitialized." : "DB initialized.";
      this.logger.info(dbStatusMsg);
      if (this.isFreshlyCreated) {
        this.logger.info("About to create DB in: " + dbDir);
        createTables();
        this.logger.info("DB initialized.");
      }
    }
    catch (Exception e) {
      String msg = "Exception during DerbyImplementation initialization:";
      this.logger.warning(msg + "\n" + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }

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
      if ("42X05".equals(theError)) {
        // If table exists will get -  WARNING 02000: No row was found 
        return false;
      }  
      else if ("42X14".equals(theError) || "42821".equals(theError))  {
        // Incorrect table definition. 
        throw e;   
      } 
      else { 
        // Unknown SQLException
        throw e; 
      }
    }
    finally {
      s.close();
      conn.close();
    }
    // Got the warning, so OK.
    return true;
  }
  
  /**
   * Initialize the database by creating tables for each resource type.
   * @throws SQLException If table creation fails.
   */
  private void createTables() throws SQLException {
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


  /** {@inheritDoc} */
 @Override
  public boolean saveSensorData(SensorData data, String xmlSensorData, String xmlSensorDataRef) {
    Connection conn = null;
    PreparedStatement s = null;
    try {
      conn = DriverManager.getConnection(connectionURL);
      s = conn.prepareStatement("INSERT INTO SensorData VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
      // Order: Owner Tstamp Sdt Runtime Tool Resource XmlSensorData XmlSensorDataRef LastMod
      s.setString(1, data.getOwner());
      s.setTimestamp(2, Tstamp.makeTimestamp(data.getTimestamp()));
      s.setString(3, data.getSensorDataType());
      s.setTimestamp(4, Tstamp.makeTimestamp(data.getRuntime()));
      s.setString(5, data.getTool());
      s.setString(6, data.getResource());
      s.setString(7, xmlSensorData);
      s.setString(8, xmlSensorDataRef);
      s.setTimestamp(9, new Timestamp(new Date().getTime()));
      s.executeUpdate();
      this.logger.info("DB: Inserted " + data.getOwner() + " " + data.getTimestamp());
    }
    catch (SQLException e) {
      if (DUPLICATE_KEY.equals(e.getSQLState())) {
        try {
          // Do an update, not an insert.
          s = conn.prepareStatement(
              "UPDATE SensorData SET "
              + " Sdt=?, Runtime=?, Tool=?, Resource=?, XmlSensorData=?, " 
              + " XmlSensorDataRef=?, LastMod=?"
              + " WHERE Owner=? AND Tstamp=?");
          s.setString(1, data.getSensorDataType());
          s.setTimestamp(2, Tstamp.makeTimestamp(data.getRuntime()));
          s.setString(3, data.getTool());
          s.setString(4, data.getResource());
          s.setString(5, xmlSensorData);
          s.setString(6, xmlSensorDataRef);
          s.setTimestamp(7, new Timestamp(new Date().getTime()));
          s.setString(8, data.getOwner());
          s.setTimestamp(9, Tstamp.makeTimestamp(data.getTimestamp()));
          s.executeUpdate();
          this.logger.info("DB: Updated " + data.getOwner() + " " + data.getTimestamp());
        }
        catch (SQLException f) {
          this.logger.info("DB: Error " + StackTrace.toString(f));
        }
      }
    }
    finally {
      try {
        s.close();
        conn.close();
      }
      catch (SQLException e) {
        this.logger.warning("Couldn't close the DB" + StackTrace.toString(e));
      }
    }
    return true;
  }


  /** {@inheritDoc} */
  @Override
  public boolean isFreshlyCreated() {
    return this.isFreshlyCreated;
  }

  /** {@inheritDoc} */
  @Override
  public Document getSensorDataIndexDocument() {
    // TODO Auto-generated method stub
    return null;
  }
  
  /** {@inheritDoc} */
  @Override
  public Document getSensorDataIndexDocument(User user) {
    // TODO Auto-generated method stub
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public Document getSensorDataIndexDocument(User user, String sdtName) {
    // TODO Auto-generated method stub
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasSensorData(User user, XMLGregorianCalendar timestamp) {
    // TODO Auto-generated method stub
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasSensorData(User user, String timestamp) {
    // TODO Auto-generated method stub
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public void deleteData(User user, XMLGregorianCalendar timestamp) {
    // TODO Auto-generated method stub
    
  }

  /** {@inheritDoc} */
  @Override
  public SensorData getSensorData(User user, XMLGregorianCalendar timestamp) {
    // TODO Auto-generated method stub
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public Set<SensorData> getSensorData(User user, XMLGregorianCalendar startTime, 
      XMLGregorianCalendar endTime) {
    // TODO Auto-generated method stub
    return null;
  }
}
