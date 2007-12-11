package org.hackystat.sensorbase.resource.projects;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.hackystat.sensorbase.client.SensorBaseClient.InvitationReply.ACCEPT;
import static org.hackystat.sensorbase.client.SensorBaseClient.InvitationReply.DECLINE;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hackystat.sensorbase.client.SensorBaseClient;
import org.hackystat.sensorbase.resource.projects.jaxb.Invitations;
import org.hackystat.sensorbase.resource.projects.jaxb.Project;
import org.hackystat.sensorbase.resource.projects.jaxb.UriPatterns;
import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
import org.hackystat.utilities.tstamp.Tstamp;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the SensorBase REST API for Project membership processing.
 * 
 * @author Philip M. Johnson
 */
public class TestProjectMembershipRestApi extends SensorBaseRestApiHelper {

  private static String testUser1 = "TestProjectMembershipUser1@hackystat.org";
  private static String testUser2 = "TestProjectMembershipUser2@hackystat.org";
  private static String testProject1 = "TestInvitationProject1";
  private static SensorBaseClient client1;
  private static SensorBaseClient client2;
  
  /**
   * Starts the server going for these tests, and makes sure our test user is registered.
   * 
   * @throws Exception If problems occur setting up the server.
   */
  @BeforeClass
  public static void setupMembership() throws Exception {
    SensorBaseClient.registerUser(getHostName(), testUser1);
    SensorBaseClient.registerUser(getHostName(), testUser2);
    client1 = new SensorBaseClient(getHostName(), testUser1, testUser1);
    client2 = new SensorBaseClient(getHostName(), testUser2, testUser2);
  }

  /**
   * Tests a "normal" invitation acceptance use case:
   * <ul>
   * <li> testUser1 creates a new project called TestProject1, and puts testUser2 on the 
   * invitation list.
   * <li> testUser2 retrieves the project description.
   * <li> testUser2 accepts the invitation.
   * <li> testUser1 retrieves the project and confirms that testUser2 is a member.
   * <li> testUser1 then deletes the Project.
   * </ul>
   * @throws Exception If problems occur.
   */
  @Test
  public void testInvitation1() throws Exception {
    // Construct the project representation that is owned by testUser1.
    Project project = makeProject(testProject1);
    project.getInvitations().getInvitation().add(testUser2);
    // PUT it to the server.
    client1.putProject(project);
    // Ensure that testUser2 can retrieve it.
    project = client2.getProject(testUser1, testProject1);
    // Make sure that testUser2 is invited.
    assertTrue("Checking invite 1", project.getInvitations().getInvitation().contains(testUser2));
    // Accept the invitation.
    client2.reply(testUser1, testProject1, ACCEPT);
    // Now check to see that testUser2 is a member.
    project = client2.getProject(testUser1, testProject1);
    assertTrue("Checking member 1", project.getMembers().getMember().contains(testUser2));
    // Now delete the Project.
    client1.deleteProject(testUser1, testProject1);
  }
  
  /**
   * Just like testNormalInvitation1, but we use the invite() method to invite testUser2
   * after the project has already been defined.
   * @throws Exception If problems occur.
   */
  @Test
  public void testInvitation2() throws Exception {
    // Construct the project representation that is owned by testUser1.
    Project project = makeProject(testProject1);
    // PUT it to the server.
    client1.putProject(project);
    // Now invite testUser2.
    client1.invite(testUser2, testProject1);
    // Ensure that testUser2 can retrieve it.
    project = client2.getProject(testUser1, testProject1);
    // Make sure that testUser2 is invited.
    assertTrue("Checking invite 2", project.getInvitations().getInvitation().contains(testUser2));
    // Accept the invitation.
    client2.reply(testUser1, testProject1, ACCEPT);
    // Now check to see that testUser2 is a member.
    project = client2.getProject(testUser1, testProject1);
    assertTrue("Checking members 2", project.getMembers().getMember().contains(testUser2));
    // Now delete the Project.
    client1.deleteProject(testUser1, testProject1);
  }
  
  /**
   * We now test that if testUser2 declines the invitation, the project representation no 
   * longer contains testUser2.
   * @throws Exception If problems occur.
   */
  @Test
  public void testInvitation3() throws Exception {
    // Construct the project representation that is owned by testUser1.
    Project project = makeProject(testProject1);
    // PUT it to the server.
    client1.putProject(project);
    // Now invite testUser2.
    client1.invite(testUser2, testProject1);
    // Ensure that testUser2 can retrieve it.
    project = client2.getProject(testUser1, testProject1);
    // Make sure that testUser2 is invited.
    assertTrue("Checking invitation", project.getInvitations().getInvitation().contains(testUser2));
    // Decline the invitation.
    client2.reply(testUser1, testProject1, DECLINE);
    // Now check to see that testUser2 is not a member nor on invitees list.
    project = client1.getProject(testUser1, testProject1);
    assertFalse("Checking members 3", project.getMembers().getMember().contains(testUser2));
    assertFalse("Checking invites 3", project.getInvitations().getInvitation().contains(testUser2));
    // Now delete the Project.
    client1.deleteProject(testUser1, testProject1);
  }
  
  
  /**
   * Create a project with the passed name.
   * @param projectName The name of the Project.
   * @return The newly created Project representation.
   */
  private Project makeProject(String projectName) {
    String owner = testUser1;
    Project project = new Project();
    project.setOwner(owner);
    project.setName(projectName);
    project.setDescription("Test Project Invitation");
    XMLGregorianCalendar tstamp = Tstamp.makeTimestamp();
    project.setStartTime(tstamp);
    project.setEndTime(tstamp);
    UriPatterns uris = new UriPatterns();
    uris.getUriPattern().add("**/test/**");
    project.setUriPatterns(uris);
    Invitations invitations = new Invitations();
    project.setInvitations(invitations);
    return project;
  }

 
}
