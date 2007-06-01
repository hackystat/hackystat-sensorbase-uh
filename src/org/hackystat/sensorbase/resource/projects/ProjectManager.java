package org.hackystat.sensorbase.resource.projects;

import static org.hackystat.sensorbase.server.ServerProperties.XML_DIR_KEY;

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
import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.projects.jaxb.ProjectIndex;
import org.hackystat.sensorbase.resource.projects.jaxb.ProjectRef;
import org.hackystat.sensorbase.resource.projects.jaxb.Projects;
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
      String msg = "Exception during Project initialization processing";
      SensorBaseLogger.getLogger().warning(msg + StackTrace.toString(e));
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
        ProjectRef ref = new ProjectRef();
        ref.setName(project.getName());
        ref.setHref(this.server.getHostName() + "projects/" + userKey + "/" + project.getName());
        index.getProjectRef().add(ref);
      }
    }
    // Now convert it to XML.
    Document doc;
    try {
      doc = this.documentBuilder.newDocument();
      this.marshaller.marshal(index, doc);
    } 
    catch (Exception e ) {
      String msg = "Failed to marshall Projects into an Index";
      SensorBaseLogger.getLogger().warning(msg + StackTrace.toString(e));
      throw new RuntimeException(msg, e);
    }
    return doc;
  }
  
  /**
   * Updates the Manager with this Project. Any old definition is overwritten.
   * @param project The Project. 
   */
  public final synchronized void putProject(Project project) {
    String ownerUserKey = project.getUsers().getUserRef().get(0).getUserKey();
    if (!projectMap.containsKey(ownerUserKey)) {
      projectMap.put(ownerUserKey, new HashMap<String, Project>());
    }
    projectMap.get(ownerUserKey).put(project.getName(), project);
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
  public static Project unmarshallSdt(Document doc) throws Exception {
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
  public static Project unmarshallSdt(String xmlString) throws Exception {
    JAXBContext jc = JAXBContext.newInstance(jaxbPackage);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    return (Project)unmarshaller.unmarshal(new StringReader(xmlString));
  }
}
