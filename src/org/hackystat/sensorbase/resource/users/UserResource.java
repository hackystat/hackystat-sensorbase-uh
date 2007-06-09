package org.hackystat.sensorbase.resource.users;

import org.hackystat.sensorbase.logger.SensorBaseLogger;
import org.hackystat.sensorbase.logger.StackTrace;
import org.hackystat.sensorbase.resource.users.jaxb.Properties;
import org.hackystat.sensorbase.resource.users.jaxb.Property;
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
  private String email; 
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UserResource(Context context, Request request, Response response) {
    super(context, request, response);
    this.email = (String) request.getAttributes().get("email");
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
      if (!manager.hasUser(this.email)) {
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown: " + this.email);
      }
        result =  new DomRepresentation(MediaType.TEXT_XML, 
            manager.getUserDocument(this.email));
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
   * Implement the DELETE method that deletes an existing User given their email.
   * Does not matter if the User current exists or not. 
   */
  @Override
  public void delete() {
    UserManager manager = (UserManager)getContext().getAttributes().get("UserManager");
    manager.deleteUser(email);      
    getResponse().setStatus(Status.SUCCESS_OK);
  }
  
  /** 
   * Indicate the POST method is supported. 
   * @return True.
   */
  @Override
  public boolean allowPost() {
    return true;
  }
  
  /**
   * Implement the POST method that updates the properties associated with a user.
   * <ul> 
   * <li> The User must be currently defined in this UserManager.
   * <li> The payload must be an XML representation of a Properties instance.
   * </ul>
   * @param entity The entity to be posted.
   */
  @Override
  public void post(Representation entity) {
    UserManager manager = (UserManager)getContext().getAttributes().get("UserManager");
    // Return failure if the User doesn't exist.
    if (!manager.hasUser(this.email)) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown User: " + this.email);
      return;
    }
    // Attempt to construct a Properties object.
    String entityString = null;
    Properties newProperties;
    // Try to make the XML payload into a Properties instance, return failure if this fails. 
    try { 
      entityString = entity.getText();
      newProperties = UserManager.unmarshallProperties(entityString);
    }
    catch (Exception e) {
      SensorBaseLogger.getLogger().warning("Bad Properties Definition: " + StackTrace.toString(e));
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad Properties: " + entityString);
      return;
    }
    User user = manager.getUser(this.email);
    // Update the existing property list with these new properties. 
    for (Property property : newProperties.getProperty()) {
      user.getProperties().getProperty().add(property);
    }
    getResponse().setStatus(Status.SUCCESS_OK);
  }
}
