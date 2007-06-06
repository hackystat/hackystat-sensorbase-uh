package org.hackystat.sensorbase.resource.projects;

import static org.hackystat.sensorbase.server.ServerProperties.XML_DIR_KEY;

import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.hackystat.sensorbase.resource.sensordata.Timestamp;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataRef;
import org.hackystat.sensorbase.server.Server;
import org.hackystat.sensorbase.server.ServerProperties;
import org.w3c.dom.Document;

/**
 * Provides a manager for the Project resource. 
 * @author Philip Johnson
 */
public class ProjectManager {
  private static String jaxbPackage = "org.hackystat.sensorbase.resource.projects.jaxb";
  
  /** The in-memory repository of Projects, keyed by User and Project name. */
  private Map<String, Map<String, Project>> projectMap = 
    new HashMap<String, Map<String, Project>>();

  /** The JAXB marshaller for Projects. */
  private Marshaller marshaller; 
  
  /** The JAXB ummarshaller for Projects. */
  private Unmarshaller unmarshaller;
  
  /** The DocumentBuilder for documents. */
  private DocumentBuilder documentBuilder; 
  
  /** The Server associated with this ProjectManager. */
  Server server; 
  
  /** 
   * The constructor for ProjectManagers. 
   * There is one ProjectManager per Server. 
   * @param server The Server instance associated with this ProjectManager. 
   */
  public ProjectManager(Server server) {
    this.server = server;
    try {
      // Initialize marshaller and unmarshaller. 
      JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
      this.unmarshaller = jc.createUnmarshaller();
      this.marshaller = jc.createMarshaller(); 

      // Get the default Project definitions from the XML defaults file. 
      File defaultsFile = findDefaultsFile();
      if (defaultsFile.exists()) {
        SensorBaseLogger.getLogger().info("Loading Project defaults:" + defaultsFile.getPath());
        Projects projects = (Projects) unmarshaller.unmarshal(defaultsFile);
        // Initialize the sdtMap
        for (Project project : projects.getProject()) {
          putProject(project);
        }
      }
      // Initialize documentBuilder
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      this.documentBuilder = dbf.newDocumentBuilder();
    }
    catch (Exception e) {
      String msg = "Exception during ProjectManager initialization processing";
      SensorBaseLogger.getLogger().warning(msg + "/n" + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
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
   * Returns the XML Index for all current defined Projects.
   * @return The XML Document instance providing an index to all current SDTs.
   */
  public synchronized Document getProjectIndexDocument() {
    ProjectIndex index = new ProjectIndex();
    for (String userKey : this.projectMap.keySet()) {
      for (String projectName : this.projectMap.get(userKey).keySet()) {
        Project project = this.projectMap.get(userKey).get(projectName);
        index.getProjectRef().add(makeProjectRef(project, userKey));
       }
    }
    return marshallProjectIndex(index);
  }
  
  /**
   * Returns the XML Project for the passed user and projectname. 
   * @param userKey The user.
   * @param projectName The Project name.
   * @return The XML Document instance providing a representation of this Project.
   * @throws Exception If problems during the marshalling, or the User/Project does not exist. 
   */
  public synchronized Document getProjectDocument(String userKey, String projectName) 
  throws Exception {
    Project project = this.projectMap.get(userKey).get(projectName);
    return marshallProject(project);
  }
  
  /**
   * Returns the XML SensorDataIndex for all data associated with this Project.
   * Assumes that User and Project are valid.
   * @param userKey The User. 
   * @param projectName the Project name.
   * @return The XML Document instance providing an index to all current SDTs.
   * @throws Exception every time. 
   */
  public synchronized Document getProjectSensorDataIndexDocument(String userKey, 
      String projectName) throws Exception {
    SensorDataIndex index = new SensorDataIndex();
    SensorDataManager sensorDataManager = 
      (SensorDataManager)this.server.getContext().getAttributes().get("SensorDataManager");
        
    Project project = this.getProject(userKey, projectName);
    XMLGregorianCalendar startTime = project.getStartTime();
    XMLGregorianCalendar endTime = project.getEndTime();
    Set<SensorData> dataSet = sensorDataManager.getData(userKey, startTime, endTime);
    for (SensorData data : dataSet) {
      String sdt = data.getSensorDataType();
      XMLGregorianCalendar timestamp = data.getTimestamp();
      SensorDataRef ref = new SensorDataRef();
      ref.setOwner(userKey);
      ref.setSensorDataType(sdt);
      ref.setTimestamp(timestamp);
      ref.setHref(server.getHostName() + "sensordata/" + userKey + "/" + sdt + "/" + timestamp);
      index.getSensorDataRef().add(ref);
    }
    return sensorDataManager.marshallSensorDataIndex(index);
  }
  
  /**
   * Returns the XML SensorDataIndex for the data associated with this Project within the 
   * specified start and end times.
   * Note that the Project start and end times may further constrain the returned set of data. 
   * This method chooses the greater of startString and the Project startTime, and the lesser of
   * endString and the Project endTime. 
   * Assumes that User and Project are valid.
   * @param userKey The User. 
   * @param projectName the Project name.
   * @param startString The startTime as a string. 
   * @param endString The endTime as a string.
   * @return The XML Document instance providing an index to the sensor data in this project
   * starting at startTime and ending at endTime. 
   * @throws Exception if startString or endString are not XMLGregorianCalendars.
   */
  public synchronized Document getProjectSensorDataIndexDocument(String userKey, 
      String projectName, String startString, String endString) throws Exception {
    SensorDataIndex index = new SensorDataIndex();
    SensorDataManager sensorDataManager = 
      (SensorDataManager)this.server.getContext().getAttributes().get("SensorDataManager");
    Project project = this.getProject(userKey, projectName);
    XMLGregorianCalendar startTime = Timestamp.makeTimestamp(startString);
    XMLGregorianCalendar endTime = Timestamp.makeTimestamp(endString);
    // make startTime the greater of startTime and the Project startTime. 
    startTime = (Timestamp.greaterThan(startTime, project.getStartTime())) ?
        startTime : project.getStartTime();
    // make endTime the lesser of endTime and the Project endTime.
    endTime = (Timestamp.lessThan(endTime, project.getEndTime())) ? endTime : project.getEndTime();
        
    Set<SensorData> dataSet = sensorDataManager.getData(userKey, startTime, endTime);
    for (SensorData data : dataSet) {
      String sdt = data.getSensorDataType();
      XMLGregorianCalendar timestamp = data.getTimestamp();
      SensorDataRef ref = new SensorDataRef();
      ref.setOwner(userKey);
      ref.setSensorDataType(sdt);
      ref.setTimestamp(timestamp);
      ref.setHref(server.getHostName() + "sensordata/" + userKey + "/" + sdt + "/" + timestamp);
      index.getSensorDataRef().add(ref);
    }
    return sensorDataManager.marshallSensorDataIndex(index);
  }
  
  /**
   * Returns the XML Index for all Projects associated with this User.
   * @param userKey The User. 
   * @return The XML Document instance providing an index to all current SDTs.
   */
  public synchronized Document getProjectIndexDocument(String userKey) {
    ProjectIndex index = new ProjectIndex();
    if (hasProjects(userKey)) {
      for (String projectName : this.projectMap.get(userKey).keySet()) {
        Project project = this.projectMap.get(userKey).get(projectName);
        index.getProjectRef().add(makeProjectRef(project, userKey));
       }
    }
    return marshallProjectIndex(index);
  }
  
  /**
   * Converts a SensorDataIndex instance into a Document and returns it.
   * @param index The SensorDataIndex instance. 
   * @return The Document.
   */
  private Document marshallProjectIndex(ProjectIndex index) {
    Document doc;
    try {
      doc = this.documentBuilder.newDocument();
      this.marshaller.marshal(index, doc);
    } 
    catch (Exception e ) {
      String msg = "Failed to marshall ProjectIndex into a Document";
      SensorBaseLogger.getLogger().warning(msg + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
    return doc;
  }
  
  /**
   * Creates a ProjectRef instance from a Project.
   * @param project The project. 
   * @param userKey The UserKey.
   * @return Its representation as a ProjectRef. 
   */
  private ProjectRef makeProjectRef(Project project, String userKey) {
    ProjectRef ref = new ProjectRef();
    ref.setName(project.getName());
    ref.setHref(this.server.getHostName() + "projects/" + userKey + "/" + project.getName());
    return ref; 
  }
  
  /**
   * Creates and stores the "Default" project for the specified UserKey. 
   * @param userKey The user key.
   */
  public synchronized void addDefaultProject(String userKey) {
    Project project = new Project();
    project.setDescription("The default Project");
    project.setStartTime(Timestamp.getDefaultProjectStartTime());
    project.setEndTime(Timestamp.getDefaultProjectEndTime());
    project.setMembers(new Members());
    project.setName("Default");
    project.setOwner(userKey);
    project.setProperties(new Properties());
    UriPatterns uriPatterns = new UriPatterns();
    uriPatterns.getUriPattern().add("**");
    project.setUriPatterns(uriPatterns);
    putProject(project);
  }
  
  /**
   * Updates the Manager with this Project. Any old definition is overwritten.
   * Note that this same Project will be associated with the Owner and all Members.  
   * @param project The Project. 
   */
  public final synchronized void putProject(Project project) {
    // First, put the [owner, project] mapping
    String ownerUserKey = project.getOwner();
    if (!projectMap.containsKey(ownerUserKey)) {
      projectMap.put(ownerUserKey, new HashMap<String, Project>());
    }
    projectMap.get(ownerUserKey).put(project.getName(), project);
    
    // Now put the [member, project] mappings, if any.
    if (project.getMembers() != null) {
      for (String memberUserKey : project.getMembers().getMember()) {
        if (!projectMap.containsKey(memberUserKey)) {
          projectMap.put(memberUserKey, new HashMap<String, Project>());
        }
        projectMap.get(memberUserKey).put(project.getName(), project);
      }
    }
  }
  
  /**
   * Returns true if the passed Project name is defined is defined for this User. 
   * @param  userKey A userkey
   * @param  projectName A project name
   * @return True if a Project with that name is defined for that User.  False if the User or
   * Project is not defined. 
   */
  public synchronized boolean hasProject(String userKey, String projectName) {
    return 
    this.projectMap.containsKey(userKey) &&
    this.projectMap.get(userKey).containsKey(projectName);
  }
  
  /**
   * Returns true if the passed user has any defined Projects.
   * @param  userKey A userkey
   * @return True if that User is defined and has at least one Project.
   */
  public synchronized boolean hasProjects(String userKey) {
    return this.projectMap.containsKey(userKey);
  }
  
  /**
   * Returns the Project associated with userKey and projectName.
   * @param  userKey A userkey
   * @param  projectName A project name
   * @return The project, or null if not found.
   */
  public synchronized Project getProject(String userKey, String projectName) {
    return (hasProject(userKey, projectName)) ? projectMap.get(userKey).get(projectName) : null; 

  }
  
  /**
   * Ensures that the named project is no longer associated with this user. 
   * @param userKey The UserKey
   * @param projectName The project name.
   */
  public synchronized void deleteProject(String userKey, String projectName) {
    if (this.projectMap.containsKey(userKey)) {
      this.projectMap.get(userKey).remove(projectName);
    }
  }
  
  /**
   * Utility function for testing purposes that takes an Project instance and returns it in XML.
   * Note that this does not affect the state of any ProjectManager instance. 
   * @param project The Project instance.
   * @return The XML Document instance corresponding to this XML. 
   * @exception Exception If problems occur marshalling the Project or building the Document.
   */
  public static Document marshallProject(Project project) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Marshaller marshaller = jc.createMarshaller(); 
    
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    Document doc = documentBuilder.newDocument();
    marshaller.marshal(project, doc);
    return doc;
  }
  
  /**
   * Returns the XML representation of the named Project for the User.
   * @param userKey The user.
   * @param projectName The project name.
   * @return The XML representation of that Project or null if not found.
   */
  public synchronized Document marshallProject(String userKey, String projectName) {
    // Return null if there is no project with this name for this user.
    if (!hasProject(userKey, projectName)) {
      return null;
    }
    Document doc = null;
    try {
      Project project = getProject(userKey, projectName);
      doc = this.documentBuilder.newDocument();
      this.marshaller.marshal(project, doc);
    }
    catch (Exception e ) {
      String msg = "Failed to marshall the Project named: " + projectName;
      SensorBaseLogger.getLogger().warning(msg + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
    return doc;
  }
  
  /**
   * Takes an XML Document representing a Project and converts it to an instance. 
   * Note that this does not affect the state of any ProjectManager instance. 
   * @param doc The XML Document representing a Project
   * @return The corresponding Project instance. 
   * @throws Exception If problems occur during unmarshalling. 
   */
  public static Project unmarshallProject(Document doc) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    return (Project) unmarshaller.unmarshal(doc);
  }
  
  /**
   * Takes a String encoding of a Project in XML format and converts it to an instance. 
   * Note that this does not affect the state of any ProjectManager instance. 
   * 
   * @param xmlString The XML string representing a Project.
   * @return The corresponding Project instance. 
   * @throws Exception If problems occur during unmarshalling.
   */
  public static Project unmarshallProject(String xmlString) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    return (Project)unmarshaller.unmarshal(new StringReader(xmlString));
  }

  /**
   * Returns true if the User is the Owner of this Project. 
   * False if User or Project is undefined or the User is not the owner. 
   * @param userKey The user. 
   * @param projectName The project 
   * @return True if the User is the owner of this Project. 
   */
  public synchronized boolean isOwner(String userKey, String projectName) {
    if (!hasProject(userKey, projectName)) {
      return false;
    }
    return getProject(userKey, projectName).getOwner().equals(userKey);
  }
}
