package org.hackystat.sensorbase.resource.registration;

import static org.hackystat.sensorbase.server.ServerProperties.ADMIN_EMAIL_KEY;
import static org.hackystat.sensorbase.server.ServerProperties.HOSTNAME_KEY;

import org.hackystat.sensorbase.mailer.Mailer;
import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.utilities.email.ValidateEmailSyntax;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * Provides registration services for this SensorBase. Implements a simple web page for accepting a
 * POSTed form containing an email address to register. Sends email with the password to this user.
 * Note that the email address is always lower-cased regardless of how the user typed it in.
 * 
 * @author Philip Johnson
 * 
 */
public class RegistrationResource extends SensorBaseResource {

  /**
   * The standard constructor.
   * 
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public RegistrationResource(Context context, Request request, Response response) {
    super(context, request, response);
  }

  /**
   * Returns a page providing a registration form. This requires no authorization.
   * 
   * @param variant The representational variant requested.
   * @return The representation.
   */
  @Override
  public Representation represent(Variant variant) {
    String pageHtml = "<html>" + "  <body>" + "  Welcome to the Hackystat SensorBase."
        + "  <p>Please enter your email address below to register."
        + "  <p>A password to this SensorBase will be emailed to you. "
        + "  <form action=\"register\" method=\"POST\">"
        + "  <input name=\"email\" type=\"text\" size=\"15\"/> "
        + "  <input  type=\"submit\" name=\"Submit\" value=\"Register\">" + "  </form>"
        + "  </body>" + "</html>";
    Representation representation = new StringRepresentation(pageHtml);
    representation.setMediaType(MediaType.TEXT_HTML);
    return representation;
  }

  /**
   * Indicate the POST method is supported.
   * 
   * @return True.
   */
  @Override
  public boolean allowPost() {
    return true;
  }

  /**
   * Implement the POST method that registers a new user. We lower case the email address
   * automatically.
   * 
   * @param entity The email address to be registered.
   */
  @Override
  public void acceptRepresentation(Representation entity) {
    // server.getLogger().info("Beginning registration.");
    Form form = new Form(entity);
    String email = form.getFirstValue("email");
    // Return Badness if we don't have the email attribute.
    if (email == null || "".equals(email)) {
      setStatusMiscError("Invalid registration request: empty email");
      return;
    }
    if (!ValidateEmailSyntax.isValid(email)) {
      setStatusMiscError("Invalid registration request: email appears to be invalid.");
      return;
    }
    try {
      // Now try to register.
      User user = super.userManager.registerUser(email);
      super.projectManager.addDefaultProject(user);
      // Now send the email to the (non-test) user and the hackystat admin.
      Mailer mailer = Mailer.getInstance();
      String adminEmail = server.getServerProperties().get(ADMIN_EMAIL_KEY);
      String emailSubject = "Hackystat Version 8 Registration";
      String emailBody = "Welcome to Hackystat. " + "\nYou are registered with: "
          + server.getServerProperties().getFullHost() + "\nYour user name is:       "
          + user.getEmail() + "\nYour password is:        " + user.getPassword()
          + "\nNote that both user name and password are case-sensitive."
          + "\n\nFor questions, email:  " + adminEmail
          + "\nYou can also see documentation at http://www.hackystat.org/"
          + "\nWe hope you enjoy using Hackystat!";

      boolean success = mailer.send(email, emailSubject, emailBody);
      // server.getLogger().info("Email sent " + (success ? "successfully." : "unsuccessfully."));
      if (success) {
        // Don't send the administrator emails about test user registration.
        if (!userManager.isTestUser(user)) {
          mailer.send(adminEmail, "Hackystat 8 Admin Registration", "User " + email
              + " registered and received password: " + user.getPassword() + "\n" + "for host: "
              + server.getServerProperties().get(HOSTNAME_KEY));
        }

        String responseHtml = "<html>" + "  <body>"
            + "    Thank you for registering with this SensorBase. " + "    <p>"
            + "    Your password has been sent to: " + email + "  </body>" + "</html>";
        server.getLogger().info("Registered: " + email + " " + user.getPassword());
        getResponse().setStatus(Status.SUCCESS_OK);
        Representation representation = new StringRepresentation(responseHtml);
        representation.setMediaType(MediaType.TEXT_HTML);
        getResponse().setEntity(representation);
      }
    }
    catch (RuntimeException e) {
      setStatusInternalError(e);
    }
  }
}
