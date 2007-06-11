package org.hackystat.sensorbase.resource.users;

import java.io.File;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.hackystat.sensorbase.resource.projects.ProjectManager;
import org.hackystat.sensorbase.resource.users.jaxb.Properties;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.resource.users.jaxb.UserIndex;
import org.hackystat.sensorbase.resource.users.jaxb.UserRef;
import org.hackystat.sensorbase.resource.users.jaxb.Users;
import org.hackystat.sensorbase.server.Server;
import org.hackystat.sensorbase.server.ServerProperties;
import static org.hackystat.sensorbase.server.ServerProperties.XML_DIR_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.TEST_DOMAIN_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.ADMIN_EMAIL_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.ADMIN_PASSWORD_KEY;
import org.w3c.dom.Document;

/**
 * Manages access to both the User and Users resources. 
 * Loads default definitions if available. 
 * @author Philip Johnson
 */
public class UserManager implements Iterable<User> {
  
  private static String jaxbPackage = "org.hackystat.sensorbase.resource.users.jaxb";
  
  /** The in-memory repository of Users, keyed by Email. */
  private Map<String, User> userMap = new ConcurrentHashMap<String, User>();

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
    try {
      
      // Initialize marshaller and unmarshaller. 
      JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
      this.unmarshaller = jc.createUnmarshaller();
      this.marshaller = jc.createMarshaller(); 

      // Get the default User definitions from the XML defaults file. 
      File defaultsFile = findDefaultsFile();
      // Initialize the SDTs if we've found a default file. 
      if (defaultsFile.exists()) {
        SensorBaseLogger.getLogger().info("Loading User defaults from " + defaultsFile.getPath());  
        Users users = (Users) unmarshaller.unmarshal(defaultsFile);
        
        // Initialize the sdtMap and define the default project.
        for (User user : users.getUser()) {
          userMap.put(user.getEmail(), user);
        }
      }
      // Initialize admin User
      initializeAdminUser();
      // Initialize documentBuilder
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      this.documentBuilder = dbf.newDocumentBuilder();
    }
    catch (Exception e) {
      String msg = "Exception during UserManager initialization processing";
      SensorBaseLogger.getLogger().warning(msg + "/n" + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
  }
  
  /**
   * Ensures a User exists with the admin role given the data in the sensorbase.properties file. 
   * The admin password will be reset to what was in the sensorbase.properties file. 
   */
  private void initializeAdminUser() {
    String adminEmail = ServerProperties.get(ADMIN_EMAIL_KEY);
    String adminPassword = ServerProperties.get(ADMIN_PASSWORD_KEY);
    // First, clear any existing Admin role property.
    for (User user : this.userMap.values()) {
      user.setRole("basic");
    }
    // Now define this user with the admin property.
    if (this.userMap.containsKey(adminEmail)) {
      User user = this.userMap.get(adminEmail);
      user.setPassword(adminPassword);
      user.setRole("admin");
    } else {
      User admin = new User();
      admin.setEmail(adminEmail);
      admin.setPassword(adminPassword);
      admin.setRole("admin");
      this.userMap.put(adminEmail, admin);
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
      ref.setEmail(user.getEmail());
      ref.setHref(this.server.getHostName() + "users/" + user.getEmail());
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
   * @param email The email address of the User
   * @return The XML representation of that User, or null if not found.
   */
  public synchronized Document getUserDocument(String email) {
    // Return null if name is not an SDT
    if (!userMap.containsKey(email)) {
      return null;
    }
    Document doc = null;
    try {
      User user = userMap.get(email);
      doc = this.documentBuilder.newDocument();
      this.marshaller.marshal(user, doc);
    }
    catch (Exception e ) {
      String msg = "Failed to marshall the User: " + email;
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
    userMap.put(user.getEmail(), user);
  }
  
  /**
   * Returns true if the passed user is defined. 
   * @param email An email address
   * @return True if a User with that email address is known to this Manager.
   */
  public synchronized boolean hasUser(String email) {
    return (email != null) && (userMap.containsKey(email));
  }
  
  /**
   * Ensures that the passed User is no longer present in this Manager. 
   * @param email The email address of the User to remove if currently present.
   */
  public synchronized void deleteUser(String email) {
    userMap.remove(email);
  }
  

  /**
   * Returns the User associated with this email address if they are currently registered.
   * @param email The email address
   * @return The User, or null if not found.
   */
  public synchronized User getUser(String email) {
    return userMap.get(email);
  }
  
  /**
   * Returns true if the User as identified by their email address is known to this Manager.
   * @param email The email address of the User of interest.
   * @return True if found in this Manager.
   */
  public synchronized boolean isUser(String email) {
    return userMap.containsKey(email);
  }
  
  /**
   * Returns true if the User as identified by their email address and password
   * is known to this Manager.
   * @param email The email address of the User of interest.
   * @param password The password of this user.
   * @return True if found in this Manager.
   */
  public synchronized boolean isUser(String email, String password) {
    User user = this.userMap.get(email);
    return (user != null) && (password != null) && (password.equals(user.getPassword()));
  }
  
  /**
   * Returns true if email is a defined User with Admin privileges. 
   * @param email An email address. 
   * @return True if email is a User with Admin privileges. 
   */
  public synchronized boolean isAdmin(String email) {
    return (email != null) &&
           userMap.containsKey(email) && 
           email.equals(ServerProperties.get(ADMIN_EMAIL_KEY));
  }
  
  /**
   * Returns true if the passed user is a test user.
   * This is defined as a User whose email address uses the TEST_DOMAIN.  
   * @param user The user. 
   * @return True if the user is a test user. 
   */
  public boolean isTestUser(User user) {
    return user.getEmail().endsWith(ServerProperties.get(TEST_DOMAIN_KEY));
  }
  
  /**
   * Returns a thread-safe Iterator over the set of currently defined users. 
   * @return An iterator. 
   */
  public Iterator<User> iterator() {
    return this.userMap.values().iterator();
  }
  
  /** 
   * If a User with the passed email address exists, then return it.
   * Otherwise create a new User and return it.
   * If the email address ends with the test domain, then the password will be the email.
   * Otherwise, a unique, randomly generated 12 character key is generated as the password. 
   * Defines the Default Project for each new user. 
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
    user.setProperties(new Properties());
    // Password is either the Email in the case of a test user, or the randomly generated string.
    String password = 
      email.endsWith(ServerProperties.get(TEST_DOMAIN_KEY)) ? email : PasswordGenerator.make();
    user.setPassword(password);
    this.userMap.put(email, user);
    ProjectManager projectManager = 
      (ProjectManager)this.server.getContext().getAttributes().get("ProjectManager");
    projectManager.addDefaultProject(user);
    return user;
  } 
  
  /**
   * Utility function for testing purposes that takes a User instance and returns it in XML.
   * Note that this does not affect the state of any Manager instance. 
   * @param user The User instance.
   * @return The XML Document instance corresponding to this XML. 
   * @exception Exception If problems occur marshalling the User or building the Document instance. 
   */
  public static Document marshallUser(User user) throws Exception {
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
   * Utility function for testing purposes that takes a Properties instance and returns it in XML.
   * Note that this does not affect the state of any Manager instance. 
   * @param properties The Properties instance.
   * @return The XML Document instance corresponding to this XML. 
   * @exception Exception If problems occur marshalling the Properties or building the Document.
   */
  public static Document marshallProperties(Properties properties) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Marshaller marshaller = jc.createMarshaller(); 
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    Document doc = documentBuilder.newDocument();
    marshaller.marshal(properties, doc);
    return doc;
  }
  
  /**
   * Takes an XML Document representing a User and converts it to an instance. 
   * Note that this does not affect the state of any Manager instance. 
   * @param doc The XML Document representing a User.
   * @return The corresponding User instance. 
   * @throws Exception If problems occur during unmarshalling. 
   */
  public static User unmarshallUser(Document doc) throws Exception {
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
  public static User unmarshallUser(String xmlString) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    return (User)unmarshaller.unmarshal(new StringReader(xmlString));
  }
  
  /**
   * Takes a String encoding of a Properties in XML format and converts it to an instance. 
   * Note that this does not affect the state of any Manager instance. 
   * 
   * @param xmlString The XML string representing a Properties.
   * @return The corresponding Properties instance. 
   * @throws Exception If problems occur during unmarshalling.
   */
  public static Properties unmarshallProperties(String xmlString) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    return (Properties)unmarshaller.unmarshal(new StringReader(xmlString));
  }
}

