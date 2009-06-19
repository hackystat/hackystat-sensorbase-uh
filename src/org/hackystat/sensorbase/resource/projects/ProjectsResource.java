package org.hackystat.sensorbase.resource.projects;

import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * Implements the Resource for processing GET {host}/projects requests to obtain an index for
 * all Projects defined in this SensorBase for all users.  
 * Requires the admin user. 
 * @author Philip Johnson
 */
public class ProjectsResource extends SensorBaseResource {

  /**
   * The standard constructor.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public ProjectsResource(Context context, Request request, Response response) {
    super(context, request, response);
  }
  
  /**
   * Returns an index of all Projects for all Users, or null if the request is not authorized. 
   * This requires admin authorization. 
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation represent(Variant variant) {
    if (!validateAuthUserIsAdmin()) {
      return null;
    }   
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      String xmlData = super.projectManager.getProjectIndex();
      return super.getStringRepresentation(xmlData);      
    }
    return null;
  }
}
