package org.hackystat.sensorbase.resource.db;

import org.hackystat.sensorbase.db.DbManager;
import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.server.ResponseMessage;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * Implements the Resource for processing PUT {host}/db/table/compress requests. 
 * Requires the admin user.
 * 
 * @author Philip Johnson
 */
public class CompressResource extends SensorBaseResource {

  /**
   * The standard constructor.
   * 
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public CompressResource(Context context, Request request, Response response) {
    super(context, request, response);
  }

  /**
   * Returns 200 if compress command succeeded. This requires admin authorization.
   * 
   * @param variant The representational variant requested.
   */
  @Override
  public void put(Representation variant) {
    try {
      if (!super.userManager.isAdmin(this.authUser)) {
        this.responseMsg = ResponseMessage.adminOnly(this);
        getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, this.responseMsg);
        return;
      }
      DbManager dbManager = (DbManager) this.server.getContext().getAttributes().get("DbManager");
      boolean success = dbManager.compressTables();
      Status status = (success) ? Status.SUCCESS_OK : Status.SERVER_ERROR_INTERNAL;
      getResponse().setStatus(status);
    }
    catch (RuntimeException e) {
      this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
    }

  }
    

  /**
   * Indicate the PUT method is supported.
   * 
   * @return True.
   */
  @Override
  public boolean allowPut() {
    return true;
  }

  /**
   * Indicate that GET is not supported.
   * @return False.
   */
  @Override
  public boolean allowGet() {
    return false;
  }

  /**
   * Get is not supported, but the method must be implemented.
   * 
   * @param variant Ignored.
   * @return Null.
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    getResponse().setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
    return null;
  }

}
