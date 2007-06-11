package org.hackystat.sensorbase.resource.sensorbase;

import org.hackystat.sensorbase.resource.projects.ProjectManager;
import org.hackystat.sensorbase.resource.sensordata.SensorDataManager;
import org.hackystat.sensorbase.resource.sensordatatypes.SdtManager;
import org.hackystat.sensorbase.resource.users.UserManager;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

/**
 * An abstract superclass for all SensorBase resources that supplies common 
 * initialization processing. 
 * This includes:
 * <ul>
 * <li> Extracting the authenticated user identifier (when authentication available)
 * <li> Extracting the user email from the URI (when available)
 * <li> Declares that the TEXT/XML representational variant is supported.
 * <li> Providing instance variables bound to the ProjectManager, SdtManager, UserManager, and 
 * SensorDataManager.
 * </ul>
 * 
 * @author Philip Johnson
 *
 */
public abstract class SensorBaseResource extends Resource {
  
  /** To be retrieved from the URL as the 'email' template parameter, or null. */
  protected String uriUser = null; 
  
  /** The authenticated user, retrieved from the ChallengeResponse, or null */
  protected String authUser = null;
  
  /** The ProjectManager. */
  protected ProjectManager projectManager = null;
  
  /** The UserManager. */
  protected UserManager userManager = null;
  
  /** The SdtManager. */
  protected SdtManager sdtManager = null;
  
  /** The SensorDataManager. */
  protected SensorDataManager sensorDataManager = null;
  
  /** The standard error message returned from invalid authentication. */
  protected String badAuth = "User is not admin and authenticated user does not not match URI user";
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public SensorBaseResource(Context context, Request request, Response response) {
    super(context, request, response);
    if (request.getChallengeResponse() != null) {
      this.authUser = request.getChallengeResponse().getIdentifier();
    }
    this.uriUser = (String) request.getAttributes().get("user");
    this.projectManager = (ProjectManager)getContext().getAttributes().get("ProjectManager");
    this.userManager = (UserManager)getContext().getAttributes().get("UserManager");
    this.sdtManager = (SdtManager)getContext().getAttributes().get("SdtManager");
    this.sensorDataManager = 
      (SensorDataManager)getContext().getAttributes().get("SensorDataManager");
    getVariants().clear(); // copied from BookmarksResource.java, not sure why needed.
    getVariants().add(new Variant(MediaType.TEXT_XML));
  }

  /**
   * The Restlet getRepresentation method which must be overridden by all concrete Resources.
   * @param variant The variant requested.
   * @return The Representation. 
   */
  @Override
  public abstract Representation getRepresentation(Variant variant);
}
