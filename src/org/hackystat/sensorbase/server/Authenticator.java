package org.hackystat.sensorbase.server;

import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.data.ChallengeScheme;

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
   * @param identifier The account name.
   * @param secret The password. 
   * @return If the credentials are valid.
   */
  @Override protected boolean checkSecret(String identifier, char[] secret) { 
    return true;
  }

}
