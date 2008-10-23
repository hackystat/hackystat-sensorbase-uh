package org.hackystat.sensorbase.resource.ping;

import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.server.ResponseMessage;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * The PingResource responds to a GET {host}/ping with the string "SensorBase".
 * It responds to GET  {host}/ping?user={user}&password={password} with
 * "SensorBase authenticated" if the user and password are valid, and 
 * "SensorBase" if not valid. 
 * @author Philip Johnson
 */
public class PingResource extends SensorBaseResource {
  /** From the URI, if authentication is desired. */
  private String user; 
  /** From the URI, if authentication is desired. */
  private String password;
  
  /**
   * The standard constructor.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public PingResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.user = (String) request.getAttributes().get("user");
    this.password = (String) request.getAttributes().get("password");
  }
  
  /**
   * Returns the string "DailyProjectData" or "DailyProjectData authenticated", 
   * depending upon whether credentials are passed as form parameters and whether
   * they are valid. 
   * @param variant The representational variant requested.
   * @return The representation as a string.  
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    try {
      String unauthenticated = "SensorBase";
      String authenticated = "SensorBase authenticated";
      // Don't try to authenticate unless the user has passed both a user and password. 
      if ((user == null) || (password == null)) {
        return new StringRepresentation(unauthenticated);
      }
      boolean OK = this.userManager.isUser(user, password);
      return new StringRepresentation((OK ? authenticated : unauthenticated));
    }
    catch (RuntimeException e) {
      this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
      return null;
    }
  }
  
  

}
