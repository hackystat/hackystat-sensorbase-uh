package org.hackystat.sensorbase.resource.projects;

import static org.hackystat.sensorbase.server.ServerProperties.TEST_DOMAIN_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.xml.datatype.XMLGregorianCalendar;
import org.hackystat.sensorbase.client.SensorBaseClient;
import org.hackystat.sensorbase.client.SensorBaseClientException;
import org.hackystat.sensorbase.resource.projects.jaxb.MultiDayProjectSummary;
import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.projects.jaxb.ProjectIndex;
import org.hackystat.sensorbase.resource.projects.jaxb.ProjectRef;
import org.hackystat.sensorbase.resource.projects.jaxb.ProjectSummary;
import org.hackystat.sensorbase.resource.projects.jaxb.SensorDataSummary;
import org.hackystat.sensorbase.resource.projects.jaxb.UriPatterns;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
import org.hackystat.utilities.tstamp.Tstamp;
import org.junit.Test;

/**
 * Tests the SensorBase REST API for Project resources.
 * 
 * @author Philip M. Johnson
 */
public class TestProjectRestApi extends SensorBaseRestApiHelper {

  private String testUser = "TestUser@hackystat.org";
  private String testProject = "TestProject";
  private String testSdt = "TestSdt";
  private String defaultProject = "Default";
  private static final String nineAm = "2007-04-30T09:00:00.000";
  private String fourHundred = "400";

  /**
   * Test that GET host/sensorbase/projects returns an index containing at least one Project. This
   * is an admin-only request. Probably want to (at)ignore this method on real distributions, 
   * since the returned dataset could be large.
   * @throws Exception If problems occur.
   */
  @Test
  public void getProjectIndex() throws Exception {
    // Create an admin client and check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), adminEmail, adminPassword);
    client.authenticate();
    // Get the index of all Projects.
    ProjectIndex index = client.getProjectIndex();
    // Make sure that we can iterate through the data and dereference all hrefs.
    for (ProjectRef ref : index.getProjectRef()) {
      client.getProject(ref);
    }
    assertTrue("Checking for project data", index.getProjectRef().size() > 1);
  }

  /**
   * Test that GET host/sensorbase/projects/TestUser@hackystat.org returns an index containing at
   * least one ProjectRef.
   * 
   * @throws Exception If problems occur.
   */
  @Test
  public void getTestUserProjectIndex() throws Exception {
    // Create the TestUser client and check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), testUser, testUser);
    client.authenticate();
    // Get the index of this user's Projects.
    ProjectIndex index = client.getProjectIndex(testUser);
    // Make sure that we can iterate through the data and dereference all hrefs.
    for (ProjectRef ref : index.getProjectRef()) {
      client.getUri(ref.getHref());
    }
    assertTrue("Checking for project data", index.getProjectRef().size() > 1);
  }

  /**
   * Test that GET host/sensorbase/projects/TestUser@hackystat.org/TestProject returns a
   * representation of TestProject.
   * 
   * @throws Exception If problems occur.
   */
  @Test
  public void getTestUserProject() throws Exception {
    // Create the TestUser client and check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), testUser, testUser);
    client.authenticate();
    // Retrieve the TestProject project and test a field.
    Project project = client.getProject(testUser, testProject);
    assertEquals("Checking project name", testProject, project.getName());
  }

  /**
   * Test that GET host/sensorbase/projects/TestUser@hackystat.org/TestProject/sensordata returns an
   * index of SensorData.
   * 
   * @throws Exception If problems occur.
   */
  @Test
  public void getTestUserProjectSensorData() throws Exception {
    // Create the TestUser client and check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), testUser, testUser);
    client.authenticate();
    // Retrieve the SensorData for the TestProject project and test a couple of
    // fields.
    SensorDataIndex index = client.getProjectSensorData(testUser, testProject);
    assertTrue("Checking index has entries", index.getSensorDataRef().size() > 1);
  }

  /**
   * Test that GET host/sensorbase/projects/TestUser@hackystat.org/TestProject/sensordata?
   * startTime=2006-04-30T09:00:00.000&endTime=2007-04-30T09:30:00.000 returns an index of
   * SensorData containing one entry.
   * 
   * @throws Exception If problems occur.
   */
  @Test
  public void getTestUserProjectSensorSummary() throws Exception {
    // Create the TestUser client and check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), testUser, testUser);
    client.authenticate();
    // Retrieve the SensorData for the TestProject project within the time
    // interval.
    XMLGregorianCalendar startTime = Tstamp.makeTimestamp(nineAm);
    XMLGregorianCalendar endTime = Tstamp.makeTimestamp("2007-04-30T09:30:00.000");
    ProjectSummary summary = client.getProjectSummary(testUser, testProject, startTime, endTime);
    assertEquals("Checking summary size", 1, 
        summary.getSensorDataSummaries().getNumInstances().intValue());
    SensorDataSummary dataSummary = summary.getSensorDataSummaries().getSensorDataSummary().get(0);
    assertEquals("Checking summary tool", "Subversion", dataSummary.getTool());
    assertEquals("Checking summary type", testSdt, dataSummary.getSensorDataType());
    assertEquals("Checking summary instances", 1, dataSummary.getNumInstances().intValue());
    // Now try for multiple days.
    MultiDayProjectSummary multiSummary = client.getMultiDayProjectSummary(testUser, testProject, 
        startTime, 3);
    assertEquals("Checking MultiDayProjectSummary", 3, multiSummary.getProjectSummary().size());
  }
  
  /**
   * Tests the snapshot API.
   * @throws Exception If problems occur.
   */
  @Test
  public void getTestUserProjectSnapshot() throws Exception {
    // Create the TestUser client and check authentication.
    String snapshotUser = "TestProjectSnapshot@hackystat.org";
    SensorBaseClient.registerUser(getHostName(), snapshotUser);
    SensorBaseClient client = new SensorBaseClient(getHostName(), snapshotUser, snapshotUser);
    client.authenticate();
    // The time interval we'll work in. These will also be our runtimes.
    XMLGregorianCalendar startTime = Tstamp.makeTimestamp(nineAm);
    XMLGregorianCalendar endTime = Tstamp.makeTimestamp("2007-04-30T09:30:00.000");

    XMLGregorianCalendar tstamp1 = Tstamp.incrementMinutes(startTime, 1);
    XMLGregorianCalendar tstamp2 = Tstamp.incrementMinutes(startTime, 2);
    XMLGregorianCalendar tstamp3 = Tstamp.incrementMinutes(startTime, 3);
    String sdt = testSdt;
    String tool1 = "Tool1";
    String tool2 = "Tool2";
    client.putSensorData(makeSensorData(tstamp1, tstamp1, snapshotUser, sdt, tool1));
    client.putSensorData(makeSensorData(tstamp2, tstamp1, snapshotUser, sdt, tool1));
    client.putSensorData(makeSensorData(tstamp3, tstamp3, snapshotUser, sdt, tool2));

    // We should get the last sensor data item.
    SensorDataIndex snap1 = 
      client.getProjectSensorDataSnapshot(snapshotUser, defaultProject, startTime, endTime, sdt);
    assertEquals("Checking snap1 size", 1, snap1.getSensorDataRef().size()); 
    assertEquals("Checking snap1 tstamp", tstamp3, snap1.getSensorDataRef().get(0).getTimestamp());
    
    // Now check that we get the other two when we specify the tool.
    SensorDataIndex snap2 = 
      client.getProjectSensorDataSnapshot(snapshotUser, defaultProject, startTime, endTime, sdt, 
          tool1);
    assertEquals("Checking snap2 size", 2, snap2.getSensorDataRef().size()); 
  }
  
  /**
   * Creates a sample SensorData instance for use in testing snapshots. 
   * @param tstamp The timestamp.
   * @param runtime The runtime
   * @param user The user.
   * @param sdt The sensor data type.
   * @param tool The tool.
   * @return The new SensorData instance. 
   */
  private SensorData makeSensorData(XMLGregorianCalendar tstamp, XMLGregorianCalendar runtime,
      String user, String sdt, String tool) {
    SensorData data = new SensorData();
    data.setTool(tool);
    data.setOwner(user);
    data.setSensorDataType(sdt);
    data.setTimestamp(tstamp);
    data.setResource("file://foo/bar/baz.txt");
    data.setRuntime(runtime);
    return data;
  }
  
  /**
   * Test that GET host/sensorbase/projects/TestUser@hackystat.org/TestProject/sensordata?
   * startTime=2006-04-30T09:00:00.000&endTime=2007-04-30T09:30:00.000 returns an index of
   * SensorData containing one entry.
   * 
   * @throws Exception If problems occur.
   */
  @Test
  public void getTestUserProjectSensorDataInterval() throws Exception {
    // Create the TestUser client and check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), testUser, testUser);
    client.authenticate();
    // Retrieve the SensorData for the TestProject project within the time
    // interval.
    XMLGregorianCalendar startTime = Tstamp.makeTimestamp(nineAm);
    XMLGregorianCalendar endTime = Tstamp.makeTimestamp("2007-04-30T09:30:00.000");
    SensorDataIndex index = client.getProjectSensorData(testUser, testProject, startTime, endTime);
    assertEquals("Checking index contains one entry", 1, index.getSensorDataRef().size());
  }
  
  /**
   * Test that the GET of a project with the SDT parameter works correctly.
   * Assumes that the default XML data files are loaded, which result in two sensor data entries
   * for TestUser@hackystat.org on 2007-04-30, one with SDT=TestSdt and one with SDT=SampleSdt.
   * 
   * @throws Exception If problems occur.
   */
  @Test
  public void getTestUserProjectSdtParam() throws Exception {
    // Create the TestUser client and check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), testUser, testUser);
    client.authenticate();
    // Retrieve the SensorData for the TestProject project within the time
    // interval, and make sure we have the two entries that we expect. 
    XMLGregorianCalendar startTime = Tstamp.makeTimestamp(nineAm);
    XMLGregorianCalendar endTime = Tstamp.makeTimestamp("2007-04-30T10:00:00.000");
    SensorDataIndex index = client.getProjectSensorData(testUser, testProject, startTime, endTime);
    assertEquals("Checking our test data has 2 entries", 2, index.getSensorDataRef().size());
    
    // Now test the SDT param call to make sure we get only the SDT of interest.
    index = client.getProjectSensorData(testUser, testProject, startTime, endTime, testSdt);
    assertEquals("Checking our test data has 1 entries", 1, index.getSensorDataRef().size());
    
    // Now test to see that the tool param works correctly. 
    index = client.getProjectSensorData(testUser, testProject, startTime, endTime, testSdt, 
    "Subversion");
    assertEquals("Checking our data has 1 entries", 1, index.getSensorDataRef().size());
    index = client.getProjectSensorData(testUser, testProject, startTime, endTime, testSdt, 
    "CVS");
    assertEquals("Checking our data has no entries", 0, index.getSensorDataRef().size());
  }
  
  
  /**
   * Test that the GET of a project with the startIndex/maxInstances parameters work correctly.
   * Assumes that the default XML data files are loaded, which result in two sensor data entries
   * for TestUser@hackystat.org on 2007-04-30, one with SDT=TestSdt and one with SDT=SampleSdt.
   * We'll try a few combinations of startIndex and maxInstances to see that the proper number
   * of instances are returned.
   * 
   * @throws Exception If problems occur.
   */
  @Test
  public void getTestUserProjectStartIndexMaxInstancesParams() throws Exception {
    // Create the TestUser client and check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), testUser, testUser);
    client.authenticate();
    // Retrieve the SensorData for the TestProject project within the time
    // interval, and make sure we have the two entries that we expect. 
    XMLGregorianCalendar startTime = Tstamp.makeTimestamp(nineAm);
    XMLGregorianCalendar endTime = Tstamp.makeTimestamp("2007-04-30T10:00:00.000");
    SensorDataIndex index = client.getProjectSensorData(testUser, testProject, startTime, endTime);
    assertEquals("Checking our original data has 2 entries", 2, index.getSensorDataRef().size());
    
    // Now test the startIndex/maxInstances params.
    index = client.getProjectSensorData(testUser, testProject, startTime, endTime, 0, 100);
    assertEquals("Checking startindex 1 is 2", 2, index.getSensorDataRef().size());

    index = client.getProjectSensorData(testUser, testProject, startTime, endTime, 1, 100);
    assertEquals("Checking startindex 2 is 1", 1, index.getSensorDataRef().size());

    index = client.getProjectSensorData(testUser, testProject, startTime, endTime, 0, 1);
    assertEquals("Checking startindex 3 is 1", 1, index.getSensorDataRef().size());

    index = client.getProjectSensorData(testUser, testProject, startTime, endTime, 1, 1);
    assertEquals("Checking startindex 4 is 1", 1, index.getSensorDataRef().size());
    
    index = client.getProjectSensorData(testUser, testProject, startTime, endTime, 2, 1);
    assertEquals("Checking startindex 5 is 0", 0, index.getSensorDataRef().size());


  }

  /**
   * Test that PUT, rename, and DELETE of host/projects/{user}/{project} works.
   * 
   * @throws Exception If problems occur.
   */
  @Test
  public void putProject() throws Exception {
    // First, create a sample Project.
    String owner = testUser;
    String projectName = "TestProject1";
    Project project = new Project();
    project.setOwner(owner);
    project.setName(projectName);
    project.setDescription("Test Project1");
    XMLGregorianCalendar tstamp = Tstamp.makeTimestamp();
    project.setStartTime(tstamp);
    project.setEndTime(tstamp);
    UriPatterns uris = new UriPatterns();
    uris.getUriPattern().add("**/test/**");
    project.setUriPatterns(uris);

    // Create the TestUser client, check authentication, and post the Project to
    // the server.
    SensorBaseClient client = new SensorBaseClient(getHostName(), testUser, testUser);
    client.authenticate();
    client.putProject(project);

    // Check that we can now retrieve it.
    Project project2 = client.getProject(owner, projectName);
    assertEquals("Testing for GET TestProject1", projectName, project2.getName());
    
    // Check that we can now rename it. 
    String newProjectName = "NewTestProjectName";
    client.renameProject(owner, projectName, newProjectName);
    ProjectIndex index = client.getProjectIndex(testUser);
    assertTrue("Checking renamed project is in index", hasProjectName(index, newProjectName));
    assertFalse("Checking old project name is not in index", hasProjectName(index, projectName));

    // Test that DELETE gets rid of this Project.
    client.deleteProject(owner, newProjectName);
  }

  /**
   * Returns true if the passed projectName is in the ProjectIndex.
   * @param index The ProjectIndex. 
   * @param projectName The projectname of interest. 
   * @return True if the projectName is in the index. 
   */
  private boolean hasProjectName(ProjectIndex index, String projectName) {
    for (ProjectRef ref : index.getProjectRef()) {
      if (ref.getName().equals(projectName)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Test that PUT of incomplete project definitions causes errors. 
   * 
   * @throws Exception If problems occur.
   */
  @Test
  public void putBadProjects() throws Exception {
    // Create the TestUser client, check authentication.
    SensorBaseClient client = new SensorBaseClient(getHostName(), testUser, testUser);
    client.authenticate();
    
    // First, create a sample Project with no fields.
    Project project = new Project();
    String owner = testUser;
    project.setOwner(owner);
    // See if we get the appropriate error
    try {
      client.putProject(project);
    }
    catch (SensorBaseClientException e) {
      assertTrue("Test bad project name", e.getMessage().startsWith(fourHundred));
    }
    // Fix the project name, try again.    
    String projectName = "TestProject1";
    project.setName(projectName);
    try {
      client.putProject(project);
    }
    catch (SensorBaseClientException e) {
      if (!e.getMessage().startsWith(fourHundred)) {
        System.out.println("About to fail test since error is: " + e.getMessage());
      }
      assertTrue("Test bad start", e.getMessage().startsWith(fourHundred));
    }
    XMLGregorianCalendar tstamp = Tstamp.makeTimestamp();
    project.setStartTime(tstamp);
    
    try {
      client.putProject(project);
    }
    catch (SensorBaseClientException e) {
      if (!e.getMessage().startsWith(fourHundred)) {
        System.out.println("About to fail test since error is: " + e.getMessage());
      }
      assertTrue("Test bad end", e.getMessage().startsWith(fourHundred));
    }
    project.setEndTime(tstamp);
    // Now this should succeed.
    client.putProject(project);

    // Check that we can now retrieve it.
    Project project2 = client.getProject(owner, projectName);
    assertEquals("Testing for GET TestProject1", projectName, project2.getName());

    // Test that DELETE gets rid of this Project.
    client.deleteProject(owner, projectName);
  }

  /**
   * Test that PUT multiple times does not cause a problem for getProjectIndex.
   * 
   * @throws Exception If problems occur.
   */
  //@Ignore
  @Test
  public void putMultipleProject() throws Exception {
    // First, create a sample Project.
    String owner = testUser;
    String projectName = "TestProject1";
    Project project = new Project();
    project.setOwner(owner);
    project.setName(projectName);
    project.setDescription("Test Project1");
    XMLGregorianCalendar tstamp = Tstamp.makeTimestamp();
    project.setStartTime(tstamp);
    project.setEndTime(tstamp);
    UriPatterns uris = new UriPatterns();
    uris.getUriPattern().add("**/test/**");
    project.setUriPatterns(uris);

    // Create the TestUser client.
    SensorBaseClient client = new SensorBaseClient(getHostName(), adminEmail, adminPassword);
    client.authenticate();
    // Now add a project.
    client.putProject(project);
    // Get the size after adding.
    int before = client.getProjectIndex().getProjectRef().size();
    // Now add it again.
    client.putProject(project);
    // Get the size after adding.
    int after = client.getProjectIndex().getProjectRef().size();
    // Check that the size hasn't changed.
    assertEquals("Checking ProjectIndex after adding duplicate Project", before, after);
  }
  


  /**
   * Tests that after creating a new User, it has a Default Project.
   * 
   * @throws Exception If problems occur.
   */
  @Test
  public void newUserTest() throws Exception {
    // Register a new user.
    String newUser = "NewUserTest@" + server.getServerProperties().get(TEST_DOMAIN_KEY);
    SensorBaseClient.registerUser(getHostName(), newUser);
    // Create a Client for this new user.
    SensorBaseClient client = new SensorBaseClient(getHostName(), newUser, newUser);
    client.authenticate();

    Project project = client.getProject(newUser, defaultProject);
    assertEquals("Checking default project", defaultProject, project.getName());

    // Now we delete the user
    client.deleteUser(newUser);
  }

  /**
   * Tests that we can retrieve all data for the TestUser under their Default Project.
   * 
   * @throws Exception If problems occur.
   */
  @Test
  public void testUserDefaultProjectData() throws Exception {
    SensorBaseClient client = new SensorBaseClient(getHostName(), testUser, testUser);
    client.authenticate();

    SensorDataIndex index = client.getProjectSensorData(testUser, defaultProject);
    assertTrue("Checking for testuser sensordata", index.getSensorDataRef().size() >= 3);
  }
}
