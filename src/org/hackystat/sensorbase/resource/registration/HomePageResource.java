package org.hackystat.sensorbase.resource.registration;

import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * Provides a home page for this SensorBase.
 * Implements a simple web page for display when traversing to the sensorbase URL in a browser.
 * @author Philip Johnson
 *
 */
public class HomePageResource extends SensorBaseResource {
  
  /**
   * The standard constructor.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public HomePageResource(Context context, Request request, Response response) {
    super(context, request, response);
  }
  
  /**
   * Returns a page providing home page info.
   * This requires no authorization.
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation represent(Variant variant) {
    String pageHtml = 
      "<html>" +
      "  <body>" +
      "Welcome to the Hackystat SensorBase!" +
      "<p>Note that this service does not provide any user interface facilities.  " +
      "<p>You will want to use a service such as ProjectViewer, SensorDataBrowser, " +
      "TelemetryViewer, or something similar to view and manipulate your data. " +
      "<p>Contact your Hackystat administrator or the Hackystat user group for details " +
      "on how to do this." +
      "</body> </html>";
    Representation representation = new StringRepresentation(pageHtml);
    representation.setMediaType(MediaType.TEXT_HTML);
    return representation;
  }
}
