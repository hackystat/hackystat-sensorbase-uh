package org.hackystat.sensorbase.resource.users;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.hackystat.sensorbase.resource.users.jaxb.User;
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
   * Indicate the PUT method is supported. 
   * @return True.
   */
  @Override
  public boolean allowPut() {
      return true;
  }

  /**
   * Implement the PUT method that creates a new User. 
   * <ul>
   * <li> The XML must be marshallable into an SDT instance using the SDT XmlSchema definition.
   * <li> There must not be an existing SDT with that user key.
   * <li> The user key in the URI string must match the user key in the XML.
   * </ul>
   * @param entity The XML representation of the new User. 
   */
  @Override
  public void put(Representation entity) {
    String entityString = null;
    User user;
    // Try to make the XML payload into a User, return failure if this fails. 
    try { 
      entityString = entity.getText();
      user = UserManager.getUser(entityString);
    }
    catch (Exception e) {
      SensorBaseLogger.getLogger().warning("Bad User Definition in PUT: " + StackTrace.toString(e));
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad User: " + entityString);
      return;
    }
    // Return failure if the payload XML SDT is already defined.  
    UserManager manager = (UserManager)getContext().getAttributes().get("UserManager");
    if (manager.hasUser(user.getUserKey())) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "User defined:" + user.getUserKey());
      return;
    }
    // Return failure if the URI SdtName is not the same as the XML SdtName.
    if (!(this.userKey.equals(user.getUserKey()))) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "URI/XML name mismatch");
      return;
      
    }
    // otherwise we add it to the Manager and return success.
    manager.putUser(user);      
    getResponse().setStatus(Status.SUCCESS_CREATED);
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
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Nonexisting User: " + this.userKey);
      return;
    }
    // Otherwise, delete it and return successs.
    manager.deleteUser(userKey);      
    getResponse().setStatus(Status.SUCCESS_OK);
  }
}
