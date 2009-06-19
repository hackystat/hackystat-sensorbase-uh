package org.hackystat.sensorbase.resource.projects;

import java.util.ArrayList;
import java.util.List;

import org.hackystat.sensorbase.resource.projects.jaxb.Invitations;
import org.hackystat.sensorbase.resource.projects.jaxb.Members;
import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * The resource for processing POST host/projects/{email}/{projectname}/invitation/{rsvp}.
 * 
 * @author Philip Johnson
 */
public class UserProjectInvitationResource extends SensorBaseResource {
  
  /** To be retrieved from the URL; should be "accept" or "decline". */
  private String rsvp;
  /** The accept string. */
  private static final String ACCEPT = "accept";
  /** The decline string. */
  private static final String DECLINE = "decline";
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UserProjectInvitationResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.rsvp = (String) request.getAttributes().get("rsvp");
  }
  
  /** 
   * Indicate the GET method is not supported.
   * @return False.
   */
  @Override
  public boolean allowGet() {
    return false;
  }
  
  /**
   * Returns nothing since GET is not supported.
   * 
   * @param variant The representational variant requested, or null if conditions are violated.
   * @return The representation. 
   */
  @Override
  public Representation represent(Variant variant) {
    return null;
  }
  
  /** 
   * Indicate the POST method is supported. 
   * @return True.
   */
  @Override
  public boolean allowPost() {
    return true;
  }

  /**
   * Implement the POST method that processes a membership invitation RSVP. 
   * <ul>
   * <li> UriUser must be a defined user, and user/project must be a defined project.
   * <li> The rsvp part of the URL must be either "accept" or "decline".
   * <li> The authorized user must be either a member or an invitee.
   * <li> Cannot accept or decline the Default project.
   * <li> Owner cannot accept or decline.
   * <li> If accept, then make sure not in Invitations and make sure on Members.
   * <li> If decline, then make sure not in either Invitations and Members.
   * </ul>
   * @param entity The XML representation of the new Project.
   */
  @Override
  public void acceptRepresentation(Representation entity) {
    try {
      if (!validateUriUserIsUser() ||
          !validateUriProjectName()) {
        return;
      }
      
      // Error if rsvp is not "accept" or "decline".
      if (!ACCEPT.equals(this.rsvp) && !DECLINE.equals(this.rsvp)) {
        setStatusMiscError("URL must end with 'accept' or 'decline'");
        return;
      }
      // Get the (possibly empty) list of members and invitees.
      List<String> members = ((project.getMembers() == null) ? new ArrayList<String>()
          : project.getMembers().getMember());
      List<String> invitees = ((project.getInvitations() == null) ? 
          new ArrayList<String>() : project.getInvitations().getInvitation());
      
      // Make sure that authorized user is either an invitee or a member.
      if (!members.contains(this.authUser) && !invitees.contains(this.authUser)) {
        setStatusMiscError(String.format("User %s is not a member or invitee of Project %s", 
            this.authUser, this.projectName));
        return;
      }
      
      // Cannot accept or decline the default project.
      if (ProjectManager.DEFAULT_PROJECT_NAME.equals(this.projectName)) {
        setStatusMiscError("Cannot accept or decline the default project.");
        return;
      }
      
      // Owner cannot accept or decline their own project.
      if (project.getOwner().equals(this.authUser)) {
        setStatusMiscError("Owner cannot accept or decline their own project.");
        return;
      }
      
       // Now update Project data structure. First make sure Members and Invitations exist.
      if (project.getMembers() == null) {
        project.setMembers(new Members());
      }
      if (project.getInvitations() == null) {
        project.setInvitations(new Invitations());
      }
      
      // If accepting, make sure it's not in invitees, and make sure it is in members.
      if (ACCEPT.equals(this.rsvp)) {
        // Make sure authUser is no longer in invitees.
        project.getInvitations().getInvitation().remove(this.authUser);
        // Make sure authUser is present in members if not there already.
        if (!project.getMembers().getMember().contains(this.authUser)) {
          project.getMembers().getMember().add(this.authUser);
        }
      }
      // If declining, make sure it's nowhere.
      if (DECLINE.equals(this.rsvp)) {
        project.getInvitations().getInvitation().remove(this.authUser);
        project.getMembers().getMember().remove(this.authUser);
      }
      
      // Now we add it to the Manager and return success.
      super.projectManager.putProject(project);      
      getResponse().setStatus(Status.SUCCESS_OK);
    }
    catch (RuntimeException e) {
      setStatusInternalError(e);
    }
  }
  
}
