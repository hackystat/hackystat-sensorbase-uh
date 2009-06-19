package org.hackystat.sensorbase.server;

import org.hackystat.sensorbase.resource.users.UserManager;
import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;

/**
 * Performs authentication of each HTTP request using HTTP Basic authentication. 
 * 
 * @author Philip Johnson
 */
public class Authenticator extends Guard {
  

  /**
   * Initializes this Guard to do HTTP Basic authentication.
   * @param context The server context.
   */
  public Authenticator (Context context) {
    super(context, ChallengeScheme.HTTP_BASIC,  "SensorBase");
  }
  
  /**
   * Returns true if the passed credentials are OK.
   * @param request The request. 
   * @param identifier The account name.
   * @param secret The password. 
   * @return If the credentials are valid.
   */
  @Override public boolean checkSecret(Request request, String identifier, char[] secret) {
    UserManager manager = (UserManager)getContext().getAttributes().get("UserManager");
    //SensorBaseLogger.getLogger().info("Authenticating: " + identifier + " " + new String(secret));
    return manager.isUser(identifier, new String(secret));
  }
}
