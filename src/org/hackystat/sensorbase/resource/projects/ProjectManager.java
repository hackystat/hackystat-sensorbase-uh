package org.hackystat.sensorbase.resource.projects;

import static org.hackystat.sensorbase.server.ServerProperties.XML_DIR_KEY;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.hackystat.sensorbase.db.DbManager;
import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.hackystat.sensorbase.resource.projects.jaxb.Members;
import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.projects.jaxb.ProjectIndex;
import org.hackystat.sensorbase.resource.projects.jaxb.ProjectRef;
import org.hackystat.sensorbase.resource.projects.jaxb.Projects;
import org.hackystat.sensorbase.resource.projects.jaxb.Properties;
import org.hackystat.sensorbase.resource.projects.jaxb.UriPatterns;
import org.hackystat.sensorbase.resource.sensordata.SensorDataManager;
import org.hackystat.sensorbase.resource.sensordata.Tstamp;
import org.hackystat.sensorbase.resource.users.UserManager;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.Server;
import org.hackystat.sensorbase.server.ServerProperties;
import org.hackystat.sensorbase.uripattern.UriPattern;
import org.w3c.dom.Document;

/**
 * Provides a manager for the Project resource. 
 * @author Philip Johnson
 */
public class ProjectManager {
  
  /** Holds the class-wide JAXBContext, which is thread-safe. */
  private JAXBContext jaxbContext;
  
  /** The Server associated with this SdtManager. */
  Server server; 
  
  /** The DbManager associated with this server. */
  DbManager dbManager;
  
  /** The UserManager */
  UserManager userManager;
  
  /** The ProjectIndex open tag. */
  public static final String projectIndexOpenTag = "<ProjectIndex>";
  
  /** The ProjectIndex close tag. */
  public static final String projectIndexCloseTag = "</ProjectIndex>";
  
  /** The initial size for Collection instances that hold the Projects. */
  private static final int projectSetSize = 127;

  /** The in-memory repository of Projects, keyed by Owner and Project name. */
  private Map<User, Map<String, Project>> owner2name2project = 
    new HashMap<User, Map<String, Project>>(projectSetSize);
  
  /** The in-memory repository of Project XML strings, keyed by Project. */
  private Map<Project, String> project2xml = new HashMap<Project, String>(projectSetSize);
  
  /** The in-memory repository of ProjectRef XML strings, keyed by Project. */
  private Map<Project, String> project2ref = new HashMap<Project, String>(projectSetSize);  
  
  /** The http string identifier. */
  private static final String http = "http";
  
  /** 
   * The constructor for ProjectManagers. 
   * There is one ProjectManager per Server. 
   * @param server The Server instance associated with this ProjectManager. 
   */
  public ProjectManager(Server server) {
    this.server = server;
    this.userManager = 
      (UserManager)this.server.getContext().getAttributes().get("UserManager");    
    this.dbManager = (DbManager)this.server.getContext().getAttributes().get("DbManager");
    try {
      this.jaxbContext = 
        JAXBContext.newInstance("org.hackystat.sensorbase.resource.projects.jaxb");
      loadDefaultProjects(); //NOPMD it's throwing a false warning. 
      initializeCache();  //NOPMD 
      initializeDefaultProjects(); //NOPMD
    }
    catch (Exception e) {
      String msg = "Exception during ProjectManager initialization processing";
      SensorBaseLogger.getLogger().warning(msg + "\n" + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
  }
  
  /**
   * Loads the default Projects from the defaults file and adds them to the database. 
   * @throws Exception If problems occur. 
   */
  private final void loadDefaultProjects() throws Exception {
    // Get the default User definitions from the XML defaults file. 
    File defaultsFile = findDefaultsFile();
    // Add these users to the database if we've found a default file. 
    if (defaultsFile.exists()) {
      SensorBaseLogger.getLogger().info("Loading Project defaults from " + defaultsFile.getPath()); 
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      Projects projects = (Projects) unmarshaller.unmarshal(defaultsFile);
      for (Project project : projects.getProject()) {
        if (project.getLastMod() == null) {
          project.setLastMod(Tstamp.makeTimestamp());
        }
        this.dbManager.storeProject(project, this.makeProject(project), 
            this.makeProjectRefString(project));
      }
    }
  } 
  
  /** Read in all Projects from the database and initialize the in-memory cache. */
  private final void initializeCache() {
    try {
      ProjectIndex index = makeProjectIndex(this.dbManager.getProjectIndex());

      for (ProjectRef ref : index.getProjectRef()) {
        String owner = ref.getOwner();
        User user = this.userManager.getUser(owner);
        // Check to make sure user exists.  DB is not normalized! 
        if (user == null) {
          String msg = "Project with undefined user '" + owner + "' found while initializing " 
          + " project cache from database. Project will be ignored.";
          SensorBaseLogger.getLogger().warning(msg);   
        }
        else {
          String projectName = ref.getName();
          String projectString = this.dbManager.getProject(user, projectName);
          Project project = makeProject(projectString);
          this.updateCache(project);
        }
      }
    }
    catch (Exception e) {
      SensorBaseLogger.getLogger().warning("Failed to initialize users " + StackTrace.toString(e));
    }
  }
  
  /**
   * Updates the in-memory cache with information about this Project. 
   * @param project The project to be added to the cache.
   * @throws Exception If problems occur updating the cache. 
   */
  private final void updateCache(Project project) throws Exception {
    updateCache(project, this.makeProject(project), this.makeProjectRefString(project));
  }
  
  /**
   * Updates the cache given all the Project representations.
   * Throws unchecked exceptions if the Owner is not defined as a User.
   * @param project The Project.
   * @param projectXml The Project as an XML string. 
   * @param projectRef The Project as an XML reference. 
   */
  private final void updateCache(Project project, String projectXml, String projectRef) {
    // First, put the [owner, project] mapping
    String email = project.getOwner();
    User user = userManager.getUser(email);
    if (user == null) {
      throw new IllegalArgumentException("Project with undefined User " + email + " " + project);
    }
    if (!owner2name2project.containsKey(user)) {
      owner2name2project.put(user, new HashMap<String, Project>());
    }
    owner2name2project.get(user).put(project.getName(), project);
    this.project2xml.put(project, projectXml);
    this.project2ref.put(project, projectRef);
  }
  
  
  /** Make sure that all Users have a "Default" project defined for them. */ 
  private void initializeDefaultProjects() {
    for (User user : userManager.getUsers()) {
      if (!hasProject(user, "Default")) {
        addDefaultProject(user);
      }
    }
  }
  
  /**
   * Checks the ServerProperties for the XML_DIR property.
   * If this property is null, returns the File for ./xml/defaults/sensordatatypes.defaults.xml.
   * @return The File instance (which might not point to an existing file.)
   */
  private File findDefaultsFile() {
    String defaultsPath = "/defaults/projects.defaults.xml";
    String xmlDir = ServerProperties.get(XML_DIR_KEY);
    return (xmlDir == null) ?
        new File (System.getProperty("user.dir") + "/xml" + defaultsPath) :
          new File (xmlDir + defaultsPath);
  }
  
  /**
   * Converts an "Owner" string to an email address.
   * The owner string might be a URI (starting with http) or an email address. 
   * @param owner The owner string. 
   * @return The email address corresponding to the owner string. 
   */
  public synchronized String convertOwnerToEmail(String owner) {
    if (owner.startsWith(http)) {
      int lastSlash = owner.lastIndexOf('/');
      if (lastSlash < 0) {
        throw new IllegalArgumentException("Could not convert owner to URI");
      }
      return owner.substring(lastSlash + 1); 
    }
    // Otherwise owner is already the email. 
    return owner;
  }
  
  /**
   * Returns the owner string as a URI.
   * The owner string could either be an email address or the URI. 
   * @param owner The owner string. 
   * @return The URI corresponding to the owner string. 
   */
  public synchronized String convertOwnerToUri(String owner) {
    return (owner.startsWith(http)) ? owner :
      ServerProperties.getFullHost() + "users/" + owner;
  }
  
  /**
   * Returns the XML string containing the ProjectIndex with all defined Projects.
   * Uses the in-memory cache of ProjectRef strings.  
   * @return The XML string providing an index to all current Projects.
   */
  public synchronized String getProjectIndex() {
    StringBuilder builder = new StringBuilder(512);
    builder.append(projectIndexOpenTag);
    for (String ref : this.project2ref.values()) {
      builder.append(ref);
    }
    builder.append(projectIndexCloseTag);
    return builder.toString();
  }
  
  /**
   * Returns the XML string containing the ProjectIndex with all defined Projects for the Owner.
   * Uses the in-memory cache of ProjectRef strings.  
   * @param owner The owner whose Projects are to be retrieved.
   * @return The XML string providing an index to all Projects for this owner.
   */
  public synchronized String getProjectIndex(User owner) {
    StringBuilder builder = new StringBuilder(512);
    builder.append(projectIndexOpenTag);
    if (owner != null) {
      Map<String, Project> name2project = this.owner2name2project.get(owner);
      if (name2project != null) {
        for (Project project : name2project.values()) {
          builder.append(this.project2ref.get(project));    
        }
      }
    }
    builder.append(projectIndexCloseTag);
    return builder.toString();
  }  
  
  /**
   * Updates the Manager with this Project. Any old definition is overwritten.
   * @param project The Project.
   */
  public synchronized void putProject(Project project) {
    try {
      project.setLastMod(Tstamp.makeTimestamp());
      String xmlProject =  this.makeProject(project);
      String xmlRef =  this.makeProjectRefString(project);
      this.updateCache(project, xmlProject, xmlRef);
      this.dbManager.storeProject(project, xmlProject, xmlRef);
    }
    catch (Exception e) {
      SensorBaseLogger.getLogger().warning("Failed to put Project" + StackTrace.toString(e));
    }
  }
  
  /**
   * Returns true if the passed Project name is defined for this User (who must be the owner).
   * @param  owner The project owner (can be null). 
   * @param  projectName A project name (can be null).
   * @return True if a Project with that name is owned by that User.  False if the User or
   * Project is not defined. 
   */
  public synchronized boolean hasProject(User owner, String projectName) {
    return 
    (owner != null) &&
    (projectName != null) &&
    this.owner2name2project.containsKey(owner) &&
    this.owner2name2project.get(owner).containsKey(projectName);
  }
  
  /**
   * Ensures that the passed Project is no longer present in this Manager. 
   * @param owner The user who owns this Project.
   * @param projectName The name of the project.
   */
  public synchronized void deleteProject(User owner, String projectName) {
    if (this.owner2name2project.containsKey(owner)) {
      Project project = this.owner2name2project.get(owner).get(projectName);
      this.project2ref.remove(project);
      this.project2xml.remove(project);
      this.owner2name2project.get(owner).remove(projectName);
    }
    this.dbManager.deleteProject(owner, projectName);
  }
  
  /**
   * Returns the Project Xml String associated with this User and project name.
   * @param owner The user that owns this project.
   * @param projectName The name of the project.
   * @return The Project XML string, or null if not found.
   */
  public synchronized String getProjectString(User owner, String projectName) {
    if (hasProject(owner, projectName)) {
      Project project = this.owner2name2project.get(owner).get(projectName);
      return this.project2xml.get(project);
    }
    return null;
  }  
  
  /**
   * Returns a set containing the current Project instances. 
   * For thread safety, a fresh Set of Projects is built each time this is called. 
   * @return A Set containing the current Projects. 
   */
  public synchronized Set<Project> getProjects() {
    Set<Project> projectSet = new HashSet<Project>(projectSetSize);
    for (User user : this.owner2name2project.keySet()) {
      for (String projectName : this.owner2name2project.get(user).keySet()) {
        projectSet.add(this.owner2name2project.get(user).get(projectName));
      }
    }
    return projectSet;
  }
 
 
  /**
   * Returns an XML SensorDataIndex String for all data associated with this User and Project.
   * Assumes that the owner and projectName define an existing Project.
   * @param owner The User that owns this Project.
   * @param projectName the Project name.
   * @return The XML SensorDataIndex string providing an index to all data for this user/project.
   * @throws Exception If things go wrong. 
   */
  public synchronized String getProjectSensorDataIndex(User owner, String projectName) 
  throws Exception {
    SensorDataManager sensorDataManager = 
      (SensorDataManager)this.server.getContext().getAttributes().get("SensorDataManager");
    Project project = this.getProject(owner, projectName);
    XMLGregorianCalendar startTime = project.getStartTime();
    XMLGregorianCalendar endTime = project.getEndTime();
    List<UriPattern> patterns = UriPattern.getPatterns(project);
    return sensorDataManager.getSensorDataIndex(owner, startTime, endTime, patterns);
  }
  

  /**
   * Returns the XML SensorDataIndex string for the data associated with this Project within the 
   * specified start and end times.
   * Note that the Project start and end times may further constrain the returned set of data. 
   * This method chooses the greater of startString and the Project startTime, and the lesser of
   * endString and the Project endTime. 
   * Assumes that User and Project are valid.
   * @param owner The User who owns this Project. 
   * @param projectName the Project name.
   * @param startString The startTime as a string. 
   * @param endString The endTime as a string.
   * @return The XML String providing a SensorDataIndex to the sensor data in this project
   * starting at startTime and ending at endTime. 
   * @throws Exception if startString or endString are not XMLGregorianCalendars.
   */  
  public synchronized String getProjectSensorDataIndex(User owner, 
      String projectName, String startString, String endString) throws Exception {
    SensorDataManager sensorDataManager = 
      (SensorDataManager)this.server.getContext().getAttributes().get("SensorDataManager");
    Project project = this.getProject(owner, projectName);
    XMLGregorianCalendar startTime = Tstamp.makeTimestamp(startString);
    XMLGregorianCalendar endTime = Tstamp.makeTimestamp(endString);
    // make startTime the greater of startTime and the Project startTime. 
    startTime = (Tstamp.greaterThan(startTime, project.getStartTime())) ?
        startTime : project.getStartTime();
    // make endTime the lesser of endTime and the Project endTime.
    endTime = (Tstamp.lessThan(endTime, project.getEndTime())) ? 
        endTime : project.getEndTime();
    List<UriPattern> patterns = UriPattern.getPatterns(project);
    return sensorDataManager.getSensorDataIndex(owner, startTime, endTime, patterns);
    
  }
  
  /**
   * Creates and stores the "Default" project for the specified user. 
   * @param owner The user who will own this Project.
   */
  public final synchronized void addDefaultProject(User owner) {
    Project project = new Project();
    project.setDescription("The default Project");
    project.setStartTime(Tstamp.getDefaultProjectStartTime());
    project.setEndTime(Tstamp.getDefaultProjectEndTime());
    project.setMembers(new Members());
    project.setName("Default");
    project.setOwner(owner.getEmail());
    project.setProperties(new Properties());
    project.setLastMod(Tstamp.makeTimestamp());
    UriPatterns uriPatterns = new UriPatterns();
    uriPatterns.getUriPattern().add("**");
    project.setUriPatterns(uriPatterns);
    putProject(project);
  }
  
  /**
   * Returns true if the passed user has any defined Projects.
   * @param  owner The user who is the owner of the Projects.
   * @return True if that User is defined and has at least one Project.
   */
  public synchronized boolean hasProjects(User owner) {
    return this.owner2name2project.containsKey(owner);
  }
  
  /**
   * Returns the Project associated with user and projectName.
   * @param  owner The user. 
   * @param  projectName A project name
   * @return The project, or null if not found.
   */
  public synchronized Project getProject(User owner, String projectName) {
    return (hasProject(owner, projectName)) ? owner2name2project.get(owner).get(projectName) : null;

  }
  
  /**
   * Takes a String encoding of a Project in XML format and converts it to an instance. 
   * 
   * @param xmlString The XML string representing a Project
   * @return The corresponding Project instance. 
   * @throws Exception If problems occur during unmarshalling.
   */
  public final synchronized Project makeProject(String xmlString) throws Exception {
    Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
    return (Project)unmarshaller.unmarshal(new StringReader(xmlString));
  }
  
  /**
   * Takes a String encoding of a ProjectIndex in XML format and converts it to an instance. 
   * 
   * @param xmlString The XML string representing a ProjectIndex.
   * @return The corresponding ProjectIndex instance. 
   * @throws Exception If problems occur during unmarshalling.
   */
  public final synchronized ProjectIndex makeProjectIndex(String xmlString) 
  throws Exception {
    Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
    return (ProjectIndex)unmarshaller.unmarshal(new StringReader(xmlString));
  }
  
  /**
   * Returns the passed Project instance as a String encoding of its XML representation.
   * Final because it's called in constructor.
   * @param project The Project instance. 
   * @return The XML String representation.
   * @throws Exception If problems occur during translation. 
   */
  public final synchronized String makeProject (Project project) throws Exception {
    Marshaller marshaller = jaxbContext.createMarshaller(); 
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    Document doc = documentBuilder.newDocument();
    marshaller.marshal(project, doc);
    DOMSource domSource = new DOMSource(doc);
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.transform(domSource, result);
    String xmlString = writer.toString();
    // Now remove the processing instruction.  This approach seems like a total hack.
    xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
    return xmlString;
  }

  /**
   * Returns the passed Project instance as a String encoding of its XML representation 
   * as a ProjectRef object.
   * Final because it's called in constructor.
   * @param project The Project instance. 
   * @return The XML String representation of it as a ProjectRef
   * @throws Exception If problems occur during translation. 
   */
  public final synchronized String makeProjectRefString (Project project) 
  throws Exception {
    ProjectRef ref = makeProjectRef(project);
    Marshaller marshaller = jaxbContext.createMarshaller(); 
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    Document doc = documentBuilder.newDocument();
    marshaller.marshal(ref, doc);
    DOMSource domSource = new DOMSource(doc);
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.transform(domSource, result);
    String xmlString = writer.toString();
    // Now remove the processing instruction.  This approach seems like a total hack.
    xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
    return xmlString;
  }
  
  /**
   * Returns a ProjectRef instance constructed from a Project instance.
   * @param project The Project instance. 
   * @return A ProjectRef instance. 
   */
  public synchronized ProjectRef makeProjectRef(Project project) {
    ProjectRef ref = new ProjectRef();
    String ownerEmail = convertOwnerToEmail(project.getOwner());
    ref.setName(project.getName());
    ref.setOwner(ownerEmail);
    ref.setLastMod(project.getLastMod());
    ref.setHref(this.server.getHostName() + "projects/" + ownerEmail + "/" + project.getName()); 
    return ref;
  }
  
}
