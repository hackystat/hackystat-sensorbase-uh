package org.hackystat.sensorbase.resource.users;

import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.resource.users.jaxb.Properties;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.server.ResponseMessage;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;

/**
 * Implements a Restlet Resource for manipulating individual User resources. 
 * @author Philip Johnson
 */
public class UserResource extends SensorBaseResource {
  
  /**
   * Provides the following representational variants: TEXT_XML.
   * @param context The context.
   * @param request The request object.
   * @param response The response object.
   */
  public UserResource(Context context, Request request, Response response) {
    super(context, request, response);
  }
  
  /**
   * Returns the representation of the User resource when requested via GET.
   * Only the authenticated user (or the admin) can request their User resource.
   * @param variant The representational variant requested.
   * @return The representation. 
   */
  @Override
  public Representation getRepresentation(Variant variant) {
    if (!super.userManager.isUser(this.uriUser)) {
      this.responseMsg = ResponseMessage.undefinedUser(this, this.uriUser);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return null;
    } 
    if (!super.userManager.isAdmin(this.authUser) && !this.uriUser.equals(this.authUser)) {
      this.responseMsg = ResponseMessage.adminOrAuthUserOnly(this, this.authUser, this.uriUser);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return null;
    }
    try {
      if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
        String xmlData = super.userManager.getUserString(this.uriUser);
        return super.getStringRepresentation(xmlData);
      }
    }
    catch (RuntimeException e) {
      this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
    }
    return null;
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
   * Only the authenticated user (or the admin) can delete their User resource.
   */
  @Override
  public void delete() {
    if (!super.userManager.isAdmin(this.authUser) && !this.uriUser.equals(this.authUser)) {
      this.responseMsg = ResponseMessage.adminOrAuthUserOnly(this, this.authUser, this.uriUser);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return;
    }
    try {
      super.userManager.deleteUser(uriUser);      
      getResponse().setStatus(Status.SUCCESS_OK);
    }
    catch (RuntimeException e) {
      this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
    }
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
   * <li> Only the authenticated User or the Admin can update their user's properties. 
   * <li> The payload must be an XML representation of a Properties instance.
   * </ul>
   * @param entity The entity to be posted.
   */
  @Override
  public void post(Representation entity) {
    // Return failure if the User doesn't exist.
    if (!super.userManager.isUser(this.uriUser)) {
      this.responseMsg = ResponseMessage.undefinedUser(this, this.uriUser);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return;
    }
    if (!super.userManager.isAdmin(this.uriUser) && !this.uriUser.equals(this.authUser)) {
      this.responseMsg = ResponseMessage.adminOrAuthUserOnly(this, this.authUser, this.uriUser);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return;
    }
    // Attempt to construct a Properties object.
    String entityString = null;
    Properties newProperties;
    // Try to make the XML payload into a Properties instance, return failure if this fails. 
    try { 
      entityString = entity.getText();
      newProperties = super.userManager.makeProperties(entityString);
    }
    catch (Exception e) {
      String msg = "Bad properties representation: " + entityString;
      this.responseMsg = ResponseMessage.miscError(this, msg);
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
      return;
    }
    
    try {
      User user = super.userManager.getUser(this.uriUser);
      super.userManager.updateProperties(user, newProperties);
      getResponse().setStatus(Status.SUCCESS_OK);
    }
    catch (RuntimeException e) {
      this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
    }
  }
}
