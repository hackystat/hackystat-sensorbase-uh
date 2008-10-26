package org.hackystat.sensorbase.resource.db;

import org.hackystat.sensorbase.db.DbManager;
import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * Implements the resource for obtaining the number of rows in a specifed table using
 * GET {host}/db/table/{table}/rowcount.
 * @author Philip Johnson
 */
public class RowCountResource extends SensorBaseResource {
  
  /** Holds the table name passed in the request. */
  private String table;

  /**
   * The standard constructor.
   * 
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public RowCountResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.table = (String) request.getAttributes().get("table");
  }

  /**
   * Returns the row count for the specifed table, or an error if the user is not the admin or
   * the table name is not valid. 
   * @param variant Ignored.
   * @return The row count as a string. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    try {
      if (!validateAuthUserIsAdmin()) {
        return null;
      }
      
      DbManager dbManager = (DbManager) this.server.getContext().getAttributes().get("DbManager");
      int rowCount = dbManager.getRowCount(table);
      // If rowCount is negative, then that means the table name was invalid.
      if (rowCount == -1) {
        setStatusMiscError("Invalid Table Name");
        return null;
      }
      // Otherwise return the row count as a string. 
      return new StringRepresentation(String.valueOf(rowCount));
    }
    catch (RuntimeException e) {
      setStatusInternalError(e);
    }
    return null;
  }
}
