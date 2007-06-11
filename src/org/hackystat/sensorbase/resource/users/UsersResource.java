package org.hackystat.sensorbase.resource.users;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.server.ServerProperties;
import static org.hackystat.sensorbase.server.ServerProperties.ADMIN_EMAIL_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.HOSTNAME_KEY;
import org.hackystat.sensorbase.mail.Mailer;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

/**
 * Implements a Restlet Resource representing an index of Hackystat Users. 
 * @author Philip Johnson
 */
public class UsersResource extends Resource {
  
  /** The email attribute if supplied in a POST request. */
  private String email = null;
  
  /** The authenticated user, retrieved from the ChallengeResponse. */
  private String authUser = null;
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UsersResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.email = (String) request.getAttributes().get("email");
    if (request.getChallengeResponse() != null) {
      this.authUser = request.getChallengeResponse().getIdentifier();
    }
    getVariants().clear(); // copied from BookmarksResource.java, not sure why needed.
    getVariants().add(new Variant(MediaType.TEXT_XML));
  }
  
  /**
   * Returns the representation of an index of all the users in the system. 
   * Only the administrator can request this resource. 
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    Representation result = null;
    UserManager manager = (UserManager)getContext().getAttributes().get("UserManager");
    if (!manager.isAdmin(this.authUser)) {
      String msg = "Only the admin can obtain the index of all users.";
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, msg);
      return null;
    }
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      result = new DomRepresentation(MediaType.TEXT_XML, manager.getUserIndexDocument());
    }
    return result;
  }
  
  /** 
   * Indicate the POST method is supported, which enables user registration.
   * @return True.
   */
  @Override
  public boolean allowPost() {
    return true;
  }

  /**
   * Implement the POST method that registers a new User.
   * <ul>
   * <li> There must be an "email" parameter that specifies the email address for this user.
   * <li> No authentication is required. 
   * </ul>
   * @param entity The XML representation of the new User. 
   */
  @Override
  public void post(Representation entity) {
    UserManager manager = (UserManager)getContext().getAttributes().get("UserManager");
    // Return Badness if we don't have the email attribute.
    if (this.email == null || "".equals(this.email)) {
      SensorBaseLogger.getLogger().warning("No email parameter"); 
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Missing email parameter");
      return;
    }
    User user = manager.registerUser(this.email);
    // Now send the email to the (non-test) user and the hackystat admin.
    Mailer mailer = Mailer.getInstance();
    String emailSubject = "Hackystat Version 8 Registration";
    String emailBody = 
      "Welcome to Hackystat Version 8. " +
      "\nYou are registered with the server: " + ServerProperties.get(HOSTNAME_KEY) +
      "\nYour email is: " + user.getEmail() +
      "\nYour password is: " + user.getPassword() +
      "\n\nNote that email and password are both case sensitive!";
    boolean success = mailer.send(this.email, emailSubject, emailBody);
    if (success) {
      // Don't send the administrator emails about test user registration.
      if (!manager.isTestUser(user)) {
        mailer.send(ServerProperties.get(ADMIN_EMAIL_KEY), 
          "Hackystat 8 Admin Registration",
          "User " + this.email + " registered and received password: " + user.getPassword() + "\n" +
          "for host: " + ServerProperties.get(HOSTNAME_KEY));
      }
    SensorBaseLogger.getLogger().warning("Registered: " +  this.email); 
    getResponse().setStatus(Status.SUCCESS_CREATED);
    }
  }
}
