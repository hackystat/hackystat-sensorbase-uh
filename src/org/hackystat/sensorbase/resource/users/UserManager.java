package org.hackystat.sensorbase.resource.users;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.resource.users.jaxb.UserIndex;
import org.hackystat.sensorbase.resource.users.jaxb.UserRef;
import org.hackystat.sensorbase.resource.users.jaxb.Users;
import org.hackystat.sensorbase.server.Server;
import org.hackystat.sensorbase.server.ServerProperties;
import static org.hackystat.sensorbase.server.ServerProperties.XML_DIR_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.TEST_DOMAIN_KEY;
import org.w3c.dom.Document;

/**
 * Manages access to both the User and Users resources. 
 * Loads default definitions if available. 
 * @author Philip Johnson
 */
public class UserManager {
  
  private static String jaxbPackage = "org.hackystat.sensorbase.resource.users.jaxb";
  
  /** The in-memory repository of Users, keyed by UserKey. */
  private Map<String, User> userMap = new HashMap<String, User>();

  /** The JAXB marshaller for Users. */
  private Marshaller marshaller; 
  
  /** The JAXB ummarshaller for Users. */
  private Unmarshaller unmarshaller;
  
  /** The DocumentBuilder for documents. */
  private DocumentBuilder documentBuilder; 
  
  /** The Server associated with this Manager. */
  Server server; 
  
  /** 
   * The constructor for UserManagers. 
   * There is one UserManager per Server. 
   * @param server The Server instance associated with this UserManager. 
   */
  public UserManager(Server server) {
    this.server = server;
    File defaultsFile = findDefaultsFile();
    // Initialize the SDTs if we've found a default file. 
    if (defaultsFile.exists()) {
      SensorBaseLogger.getLogger().info("Loading User defaults from " + defaultsFile.getPath());
      try {
        // Initialize marshaller and unmarshaller. 
        JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
        this.unmarshaller = jc.createUnmarshaller();
        this.marshaller = jc.createMarshaller(); 
        
        // Get the default User definitions from the XML defaults file. 
        Users users = (Users) unmarshaller.unmarshal(defaultsFile);
        // Initialize the sdtMap
        for (User user : users.getUser()) {
          userMap.put(user.getUserKey(), user);
        }
        // Initialize documentBuilder
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        this.documentBuilder = dbf.newDocumentBuilder();
      }
      catch (Exception e) {
        String msg = "Exception during SDT JAXB initialization processing";
        SensorBaseLogger.getLogger().warning(msg + StackTrace.toString(e));
        throw new RuntimeException(msg, e);
      }
    }
  }
  
  /**
   * Checks ServerProperties for the XML_DIR property.
   * If this property is null, returns the File for ./xml/defaults/sensordatatypes.defaults.xml.
   * @return The File instance (which might not point to an existing file.)
   */
  private File findDefaultsFile() {
    String defaultsPath = "/defaults/users.defaults.xml";
    String xmlDir = ServerProperties.get(XML_DIR_KEY);
    return (xmlDir == null) ?
        new File (System.getProperty("user.dir") + "/xml" + defaultsPath) :
          new File (xmlDir + defaultsPath);
  }


  /**
   * Returns the XML Index for all current defined Users
   * @return The XML Document instance providing an index to all current Users.
   */
  public synchronized Document getUserIndexDocument() {
    // First, create the freakin index.
    UserIndex index = new UserIndex();
    for (User user : this.userMap.values()) {
      UserRef ref = new UserRef();
      ref.setUserKey(user.getUserKey());
      ref.setHref(this.server.getHostName() + "users/" + user.getUserKey());
      index.getUserRef().add(ref);
    }
    // Now convert it to XML.
    Document doc;
    try {
      doc = this.documentBuilder.newDocument();
      this.marshaller.marshal(index, doc);
    } 
    catch (Exception e ) {
      String msg = "Failed to marshall Users into an Index";
      SensorBaseLogger.getLogger().warning(msg + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
    return doc;
  }
  
  /**
   * Returns the XML representation of the named User.
   * @param userKey The UserKey of the User
   * @return The XML representation of that User, or null if not found.
   */
  public synchronized Document getUserDocument(String userKey) {
    // Return null if name is not an SDT
    if (!userMap.containsKey(userKey)) {
      return null;
    }
    Document doc = null;
    try {
      User user = userMap.get(userKey);
      doc = this.documentBuilder.newDocument();
      this.marshaller.marshal(user, doc);
    }
    catch (Exception e ) {
      String msg = "Failed to marshall the User named: " + userKey;
      SensorBaseLogger.getLogger().warning(msg + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
    return doc;
  }
  
  /**
   * Updates the Manager with this SDT. Any old definition is overwritten.
   * @param user The SensorDataType.
   */
  public synchronized void putUser(User user) {
    userMap.put(user.getUserKey(), user);
  }
  
  /**
   * Returns true if the passed userKey is defined. 
   * @param userKey A userKey
   * @return True if a User with that userKey is known to this Manager.
   */
  public synchronized boolean hasUser(String userKey) {
    return userMap.containsKey(userKey);
  }
  
  /**
   * Ensures that the passed User is no longer present in this Manager. 
   * @param userKey The userKey of the User to remove if currently present.
   */
  public synchronized void deleteUser(String userKey) {
    userMap.remove(userKey);
  }
  
  /**
   * Returns true if the passed UserKey is known to this Manager.
   * @param userKey The UserKey of interest.
   * @return True if found in this Manager.
   */
  public synchronized boolean isUserKey(String userKey) {
    return userMap.containsKey(userKey);
  }
  
  /** 
   * If a User with the passed email address exists, then return it.
   * Otherwise create a new User and return it.
   * If the email address ends with the test domain, then the UserKey will be the lower-cased
   * account name. For example, "foo@hackystat.org" will create a UserKey called "foo" if
   * hackystat.org is the test domain.  Otherwise, a unique, randomly generated 12 character key 
   * is generated and used as the UserKey.
   * @param email The email address for the user. 
   * @return The retrieved or newly created User.
   */
  public synchronized User registerUser(String email) {
    // registering happens rarely, so we'll just iterate through the userMap.
    for (User user : this.userMap.values()) {
      if (user.getEmail().equals(email)) {
        return user;
      }
    }
    // if we got here, we need to create a new User.
    User user = new User();
    user.setEmail(email);
    // UserKey is either the lowercased account in the case of a test user, or the random string.
    String userKey = 
      email.endsWith(ServerProperties.get(TEST_DOMAIN_KEY)) ?
          email.substring(0, email.indexOf('@')) :
            UserKeyGenerator.make(this);
    user.setUserKey(userKey);
    return user;
  } 
  
  /**
   * Utility function for testing purposes that takes a User instance and returns it in XML.
   * Note that this does not affect the state of any Manager instance. 
   * @param user The User instance.
   * @return The XML Document instance corresponding to this XML. 
   * @exception Exception If problems occur marshalling the User or building the Document instance. 
   */
  public static Document getDocument(User user) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Marshaller marshaller = jc.createMarshaller(); 
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    Document doc = documentBuilder.newDocument();
    marshaller.marshal(user, doc);
    return doc;
  }
  
  /**
   * Takes an XML Document representing a User and converts it to an instance. 
   * Note that this does not affect the state of any Manager instance. 
   * @param doc The XML Document representing a User.
   * @return The corresponding User instance. 
   * @throws Exception If problems occur during unmarshalling. 
   */
  public static User getUser(Document doc) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    return (User) unmarshaller.unmarshal(doc);
  }
  
  /**
   * Takes a String encoding of a User in XML format and converts it to an instance. 
   * Note that this does not affect the state of any Manager instance. 
   * 
   * @param xmlString The XML string representing a User.
   * @return The corresponding User instance. 
   * @throws Exception If problems occur during unmarshalling.
   */
  public static User getUser(String xmlString) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    return (User)unmarshaller.unmarshal(new StringReader(xmlString));
  }
  
  /**
   * Returns true if the passed user is a test user.
   * This is defined as their email address using the TEST_DOMAIN.  
   * @param user The user. 
   * @return True if the user is a test user. 
   */
  public boolean isTestUser(User user) {
    return user.getEmail().endsWith(ServerProperties.get(TEST_DOMAIN_KEY));
  }
}

