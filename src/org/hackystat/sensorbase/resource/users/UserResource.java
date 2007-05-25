package org.hackystat.sensorbase.resource.users;

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
 * Implements a Restlet Resource for obtaining individual User resources. 
 * @author Philip Johnson
 */
public class UserResource extends Resource {

  /** To be retrieved from the URL. */
  private String userKey; 
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UserResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.userKey = (String) request.getAttributes().get("userkey");
    getVariants().clear(); // copied from BookmarksResource.java, not sure why needed.
    getVariants().add(new Variant(MediaType.TEXT_XML));
  }
  
  /**
   * Returns the representation of the User resource. 
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    Representation result = null;
    UserManager manager = (UserManager)getContext().getAttributes().get("UserManager");
    if (manager == null) {
      throw new RuntimeException("Failed to find UserManager");
    }
    if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
      result = new DomRepresentation(MediaType.TEXT_XML, 
          manager.getUserDocument(this.userKey));
    }
    return result;
  }
  
  
  /** 
   * Indicate the DELETE method is supported. 
   * @return True.
   */
  @Override
  public boolean allowDelete() {
    return true;
  }
  
  /**
   * Implement the DELETE method that deletes an existing SDT given its name.
   * <ul> 
   * <li> The SDT must be currently defined in this SdtManager.
   * </ul>
   */
  @Override
  public void delete() {
    UserManager manager = (UserManager)getContext().getAttributes().get("UserManager");
    // Return failure if it doesn't exist.
    if (!manager.hasUser(this.userKey)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Nonexistent User: " + this.userKey);
      return;
    }
    // Otherwise, delete it and return successs.
    manager.deleteUser(userKey);      
    getResponse().setStatus(Status.SUCCESS_OK);
  }
}
