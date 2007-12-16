package org.hackystat.sensorbase.resource.projects;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.client.SensorBaseClient;
import org.hackystat.sensorbase.client.SensorBaseClientException;
import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.projects.jaxb.UriPatterns;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
import org.hackystat.utilities.tstamp.Tstamp;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * Tests that UriPattern processing works correctly.
 * @author Philip Johnson
 */
public class TestProjectUriPatterns extends SensorBaseRestApiHelper {
  
  private static String testUser = "TestProjectUriPatterns@hackystat.org";
  private static String testProject = "TestProjectUriPatterns";
  private static SensorBaseClient client;
  
  /**
   * Setup this user, and make sure there is no data or project for them.
   * 
   * @throws Exception If problems occur setting up the server.
   */
  @BeforeClass
  public static void setupMembership() throws Exception {
    SensorBaseClient.registerUser(getHostName(), testUser);
    client = new SensorBaseClient(getHostName(), testUser, testUser);
    client.deleteProject(testUser, testProject);
    client.deleteSensorData(testUser);
  }
  
  /**
   * Test various URI patterns.
   * @throws SensorBaseClientException if problems occur. 
   */
  @Test
  public void testDefaultUriPatterns() throws SensorBaseClientException {
    
    // Note that each time the project is PUT, it gets the current time as its
    // start time. Thus, all old data is no longer within scope for the project.
    
    // [1] Test the Default UriPattern.
    List<String> uriPatterns = new ArrayList<String>();
    uriPatterns.add("*");
    putProject(testProject, uriPatterns);
    putSensorData("file://foo/bar/baz.java");
    putSensorData("file://foo\\bar\\baz.java");
    SensorDataIndex index = client.getProjectSensorData(testUser, testProject);
    assertEquals("[1] * uriPattern", 2, index.getSensorDataRef().size());

    // [2] Test empty string, which matches nothing.
    uriPatterns.set(0, "");
    putProject(testProject, uriPatterns);
    putSensorData("file://foo/bar/baz.java");
    index = client.getProjectSensorData(testUser, testProject);
    assertEquals("[2] Empty uriPattern", 0, index.getSensorDataRef().size());

    // [3] Test a wildcard.
    uriPatterns.set(0, "*/foo/*/baz.java");
    putProject(testProject, uriPatterns);
    putSensorData("file://foo/bar/baz.java");
    putSensorData("file://\\foo\\bar\\baz.java");
    putSensorData("file://qux/bar/baz.java");
    index = client.getProjectSensorData(testUser, testProject);
    assertEquals("[3] wildcard uriPattern", 2, index.getSensorDataRef().size());
    
    // [4.1] Test path separators
    uriPatterns.set(0, "*/foo/*/baz.java");
    putProject(testProject, uriPatterns);
    putSensorData("\\foo\\bar\\baz.java");
    putSensorData("/foo/bar/baz.java");
    putSensorData("\\qux\\bar\\baz.java");
    index = client.getProjectSensorData(testUser, testProject);
    assertEquals("[4.1] path separator", 2, index.getSensorDataRef().size());
    
    // [4.2] Test path separators
    uriPatterns.set(0, "*\\foo\\*\\baz.java");
    putProject(testProject, uriPatterns);
    putSensorData("\\foo\\bar\\baz.java");  
    putSensorData("/foo/bar/baz.java");  
    putSensorData("foo/bar/baz.java");  
    putSensorData("\\qux\\bar\\baz.java");  
    index = client.getProjectSensorData(testUser, testProject);
    assertEquals("[4.2] path separator", 2, index.getSensorDataRef().size());
    
    // [5] Test multiple URI patterns.
    uriPatterns.set(0, "foo.java");
    uriPatterns.add("bar.java");
    putProject(testProject, uriPatterns);
    putSensorData("foo.java");  
    putSensorData("bar.java");  
    putSensorData("baz.java");  
    putSensorData("qux.java");  
    index = client.getProjectSensorData(testUser, testProject);
    assertEquals("[5] multi URIs", 2, index.getSensorDataRef().size());
    
    // [6] Test path separators with mixture.
    // NOTE: This illustrates limitation of current mechanism.  
    // Cannot include 'file://' in UriPattern and then match against '\' path separator
    uriPatterns.set(0, "file://*/foo.java");
    uriPatterns.set(1, "file://*/bar.java");
    putProject(testProject, uriPatterns);
    putSensorData("file:///foo.java"); // matches.
    putSensorData("file://\\foo.java"); // should match, but doesn't!
    index = client.getProjectSensorData(testUser, testProject);
    assertEquals("[6] mixture", 1, index.getSensorDataRef().size());
  }
  
  /**
   * Defines a project with the passed name and uriPatterns
   * The project start time is now, and the end time is one day from now.
   * @param projectName The name of the Project.
   * @param uriPatterns The uri patterns.
   * @throws SensorBaseClientException if problems occur. 
   */
  private void putProject(String projectName, List<String> uriPatterns) 
  throws SensorBaseClientException {
    String owner = testUser;
    Project project = new Project();
    project.setOwner(owner);
    project.setName(projectName);
    project.setDescription("Project for UriPattern testing.");
    XMLGregorianCalendar tstamp = Tstamp.makeTimestamp();
    project.setStartTime(tstamp);
    project.setEndTime(Tstamp.incrementDays(tstamp, 1));
    UriPatterns uris = new UriPatterns();
    if (uriPatterns != null) {
      uris.getUriPattern().addAll(uriPatterns);
    }
    project.setUriPatterns(uris);
    client.putProject(project);
  }
  
 
  /**
   * Creates a sample SensorData instance with a given resource.
   * @param resource The resource string.
   * @throws SensorBaseClientException if problems occur. 
   */
  private void putSensorData(String resource) throws SensorBaseClientException {
    SensorData data = new SensorData();
    data.setOwner(testUser);
    data.setTool("Subversion");
    data.setSensorDataType("TestSdt");
    data.setTimestamp(Tstamp.makeTimestamp());
    data.setResource(resource);
    data.setRuntime(Tstamp.makeTimestamp());
    client.putSensorData(data);
  }

}
