package org.hackystat.sensorbase.client;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataTypeIndex;
import org.hackystat.sensorbase.resource.users.jaxb.Properties;
import org.hackystat.sensorbase.resource.users.jaxb.User;
import org.hackystat.sensorbase.resource.users.jaxb.UserIndex;
import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.w3c.dom.Document;

/**
 * Provides a high-level interface for Clients wishing to communicate with a SensorBase. 
 * @author Philip Johnson
 *
 */
public class SensorBaseClient {
  
  /** Holds the userEmail to be associated with this client. */
  private String userEmail;
  /** Holds the password to be associated with this client. */
  private String password;
  /** The SensorBase host, such as "http://localhost:9876/sensorbase". */
  private String sensorBaseHost;
  /** The Restlet Client instance used to communicate with the server. */
  private Client client;
  /** SDT JAXBContext */
  private JAXBContext sdtJAXB;
  /** Users JAXBContext */
  private JAXBContext userJAXB;
  /** SDT JAXBContext */
  private JAXBContext sensordataJAXB;
  /** SDT JAXBContext */
  private JAXBContext projectJAXB;
  /** The http authentication approach. */
  private ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
  /** The preferred representation type. */
  private Preference<MediaType> xmlMedia = new Preference<MediaType>(MediaType.TEXT_XML);
  
  /**
   * Initializes a new SensorBaseClient, given the host, userEmail, and password. 
   * @param sensorBaseHost The host, such as 'http://localhost:9876/sensorbase'.
   * @param userEmail The user's email that we will use for authentication. 
   * @param password The password we will use for authentication.
   */
  public SensorBaseClient(String sensorBaseHost, String userEmail, String password) {
    validateArg(sensorBaseHost);
    validateArg(userEmail);
    validateArg(password);
    this.userEmail = userEmail;
    this.password = password;
    this.sensorBaseHost = sensorBaseHost;
    if (!this.sensorBaseHost.endsWith("/")) {
      this.sensorBaseHost = this.sensorBaseHost + "/";
    }
    this.client = new Client(Protocol.HTTP);
    try {
      this.sdtJAXB = 
        JAXBContext.newInstance("org.hackystat.sensorbase.resource.sensordatatypes.jaxb");
      this.userJAXB = 
        JAXBContext.newInstance("org.hackystat.sensorbase.resource.users.jaxb");
      this.sensordataJAXB = 
        JAXBContext.newInstance("org.hackystat.sensorbase.resource.sensordata.jaxb");
      this.projectJAXB = 
        JAXBContext.newInstance("org.hackystat.sensorbase.resource.projects.jaxb");
    }
    catch (Exception e) {
      throw new RuntimeException("Couldn't create JAXB context instances.");
    }
  }
  
  /**
   * Authenticates this user and password with the server.  
   * @return This SensorBaseClient instance. 
   * @throws SensorBaseClientException If authentication is not successful. 
   */
  public synchronized SensorBaseClient authenticate() throws SensorBaseClientException {
    Response response = makeRequest(Method.HEAD, "users/" + this.userEmail, null); //NOPMD
    if (!response.getStatus().isSuccess()) {
      throw new SensorBaseClientException(response.getStatus());
    }
    return this;
  }
  
  /**
   * Returns the index of SensorDataTypes from this server. 
   * @return The SensorDataTypeIndex instance. 
   * @throws SensorBaseClientException If the server does not return the Index or returns an index
   * that cannot be marshalled into Java SensorDataTypeIndex instance. 
   */
  public synchronized SensorDataTypeIndex getSensorDataTypeIndex() 
  throws SensorBaseClientException {
    Response response = makeRequest(Method.GET, "sensordatatypes", null);
    SensorDataTypeIndex index;
    if (!response.getStatus().isSuccess()) {
      throw new SensorBaseClientException(response.getStatus());
    }
    try {
      String xmlData = response.getEntity().getText();
      index = makeSensorDataTypeIndex(xmlData);
    }
    catch (Exception e) {
      throw new SensorBaseClientException(response.getStatus(), e);
    }
    return index; 
  }
  
  /**
   * Returns the named SensorDataType from this server. 
   * @param sdtName The SDT name.
   * @return The SensorDataType instance. 
   * @throws SensorBaseClientException If the server does not return the SDT or returns something
   * that cannot be marshalled into Java SensorDataType instance. 
   */
  public synchronized SensorDataType getSensorDataType(String sdtName) 
  throws SensorBaseClientException {
    Response response = makeRequest(Method.GET, "sensordatatypes/" + sdtName, null);
    SensorDataType sdt;
    if (!response.getStatus().isSuccess()) {
      throw new SensorBaseClientException(response.getStatus());
    }
    try {
      String xmlData = response.getEntity().getText();
      sdt = makeSensorDataType(xmlData);
    }
    catch (Exception e) {
      throw new SensorBaseClientException(response.getStatus(), e);
    }
    return sdt;
  } 

  /**
   * Creates the passed SDT on the server. This is an admin-only operation. 
   * @param sdt The SDT to create. 
   * @throws SensorBaseClientException If the user is not the admin or if there is some 
   * problem with the SDT instance. 
   */
  public synchronized void putSensorDataType(SensorDataType sdt) 
  throws SensorBaseClientException {
    try {
      String xmlData = makeSensorDataType(sdt);
      Representation representation = SensorBaseResource.getStringRepresentation(xmlData);
      String uri = "sensordatatypes/" + sdt.getName();
      Response response = makeRequest(Method.PUT, uri, representation);
      if (!response.getStatus().isSuccess()) {
        throw new SensorBaseClientException(response.getStatus());
      }
    }
    // Allow SensorBaseClientExceptions to be thrown out of this method.
    catch (SensorBaseClientException f) {
      throw f;
    }
    // All other exceptions are caught and rethrown. 
    catch (Exception e) {
      throw new SensorBaseClientException("Error marshalling SDT", e);
    }
  }
  
  /**
   * Deletes the SDT given its name.
   * @param sdtName The name of the SDT to delete.  
   * @throws SensorBaseClientException If the server does not indicate success.
   */
  public synchronized void deleteSensorDataType(String sdtName) 
  throws SensorBaseClientException {
    Response response = makeRequest(Method.DELETE, "sensordatatypes/" + sdtName, null);
    if (!response.getStatus().isSuccess()) {
      throw new SensorBaseClientException(response.getStatus());
    }
  }  
  
  /**
   * Returns the index of Users from this server.  This is an admin-only operation.
   * @return The UserIndex instance. 
   * @throws SensorBaseClientException If the server does not return the Index or returns an index
   * that cannot be marshalled into Java UserIndex instance. 
   */
  public synchronized UserIndex getUserIndex() throws SensorBaseClientException {
    Response response = makeRequest(Method.GET, "users", null);
    UserIndex index;
    if (!response.getStatus().isSuccess()) {
      throw new SensorBaseClientException(response.getStatus());
    }
    try {
      String xmlData = response.getEntity().getText();
      index = makeUserIndex(xmlData);
    }
    catch (Exception e) {
      throw new SensorBaseClientException(response.getStatus(), e);
    }
    return index; 
  }
  
  
  /**
   * Returns the named User from this server.
   * @param email The user email. 
   * @return The User. 
   * @throws SensorBaseClientException If the server does not return the SDT or returns something
   * that cannot be marshalled into Java User instance. 
   */
  public synchronized User getUser(String email) throws SensorBaseClientException {
    Response response = makeRequest(Method.GET, "users/" + userEmail, null);
    User user;
    if (!response.getStatus().isSuccess()) {
      throw new SensorBaseClientException(response.getStatus());
    }
    try {
      String xmlData = response.getEntity().getText();
      user = makeUser(xmlData);
    }
    catch (Exception e) {
      throw new SensorBaseClientException(response.getStatus(), e);
    }
    return user;
  }  
  
  /**
   * Deletes the User given their email.
   * @param email The email of the User to delete.  
   * @throws SensorBaseClientException If the server does not indicate success.
   */
  public synchronized void deleteUser(String email) 
  throws SensorBaseClientException {
    Response response = makeRequest(Method.DELETE, "users/" + email, null);
    if (!response.getStatus().isSuccess()) {
      throw new SensorBaseClientException(response.getStatus());
    }
  }  
  
  /**
   * Deletes the User given their email.
   * @param email The email of the User whose properties are to be deleted.
   * @param properties The properties to post. 
   * @throws SensorBaseClientException If the server does not indicate success.
   */
  public synchronized void updateUserProperties(String email, Properties properties) 
  throws SensorBaseClientException {
    String xmlData;
    try {
    xmlData = makeProperties(properties);
    }
    catch (Exception e) {
      throw new SensorBaseClientException("Failed to marshall Properties instance.", e); 
    }
    Representation representation = SensorBaseResource.getStringRepresentation(xmlData);
    Response response = makeRequest(Method.POST, "users/" + email, representation);
    if (!response.getStatus().isSuccess()) {
      throw new SensorBaseClientException(response.getStatus());
    }
  }    
  
  /**
   * GETs the URI string and returns the Restlet Response if the server indicates success.
   * @param uriString The URI String, such as "http://localhost:9876/sensorbase/sensordatatypes".
   * @return The response instance if the GET request succeeded.
   * @throws SensorBaseClientException If the server indicates that a problem occurred. 
   */
  public synchronized Response getUri(String uriString) throws SensorBaseClientException {
    Reference reference = new Reference(uriString);
    Request request = new Request(Method.GET, reference);
    request.getClientInfo().getAcceptedMediaTypes().add(xmlMedia); 
    ChallengeResponse authentication = new ChallengeResponse(scheme, this.userEmail, this.password);
    request.setChallengeResponse(authentication);
    Response response = this.client.handle(request);
    if (!response.getStatus().isSuccess()) {
      throw new SensorBaseClientException(response.getStatus());
    }
    return response; 
  }
  
  /**
   * Registers the given user email with the given SensorBase.
   * @param sensorBaseHost The host name, such as "http://localhost:9876/sensorbase".
   * @param email The user email. 
   * @throws SensorBaseClientException If problems occur during registration. 
   */
  public static void registerUser(String sensorBaseHost, String email) 
  throws SensorBaseClientException {
    Reference reference = new Reference(sensorBaseHost + "users?email=" + email);
    Request request = new Request(Method.POST, reference);
    Client client = new Client(Protocol.HTTP);
    Response response = client.handle(request);
    if (!response.getStatus().isSuccess()) {
      throw new SensorBaseClientException(response.getStatus());
    }
  }
  
  /**
   * Throws an unchecked illegal argument exception if the arg is null or empty. 
   * @param arg The String that must be non-null and non-empty. 
   */
  private void validateArg(String arg) {
    if ((arg == null) || ("".equals(arg))) {
      throw new IllegalArgumentException(arg + " cannot be null or the empty string.");
    }
  }
  
  /**
   * Does the housekeeping for making HTTP requests to the SensorBase by a test or admin user. 
   * @param method The type of Method.
   * @param requestString A string, such as "users". No preceding slash. 
   * @param entity The representation to be sent with the request, or null if not needed.  
   * @return The Response instance returned from the server.
   */
  private Response makeRequest(Method method, String requestString, Representation entity) {
    Reference reference = new Reference(this.sensorBaseHost + requestString);
    Request request = (entity == null) ? 
        new Request(method, reference) :
          new Request(method, reference, entity);
    request.getClientInfo().getAcceptedMediaTypes().add(xmlMedia); 
    ChallengeResponse authentication = new ChallengeResponse(scheme, this.userEmail, this.password);
    request.setChallengeResponse(authentication);
    return this.client.handle(request);
  }
  
  /**
   * Takes a String encoding of a SensorDataType in XML format and converts it to an instance. 
   * @param xmlString The XML string representing a SensorDataType
   * @return The corresponding SensorDataType instance. 
   * @throws Exception If problems occur during unmarshalling.
   */
  private SensorDataType makeSensorDataType(String xmlString) throws Exception {
    Unmarshaller unmarshaller = this.sdtJAXB.createUnmarshaller();
    return (SensorDataType)unmarshaller.unmarshal(new StringReader(xmlString));
  }
  
  /**
   * Takes a String encoding of a SensorDataTypeIndex in XML format and converts it to an instance. 
   * @param xmlString The XML string representing a SensorDataTypeIndex.
   * @return The corresponding SensorDataTypeIndex instance. 
   * @throws Exception If problems occur during unmarshalling.
   */
  private SensorDataTypeIndex makeSensorDataTypeIndex(String xmlString) throws Exception {
    Unmarshaller unmarshaller = this.sdtJAXB.createUnmarshaller();
    return (SensorDataTypeIndex)unmarshaller.unmarshal(new StringReader(xmlString));
  }
  
  /**
   * Returns the passed SensorDataType instance as a String encoding of its XML representation.
   * @param sdt The SensorDataType instance. 
   * @return The XML String representation.
   * @throws Exception If problems occur during translation. 
   */
  private String makeSensorDataType (SensorDataType sdt) throws Exception {
    Marshaller marshaller = this.sdtJAXB.createMarshaller(); 
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    Document doc = documentBuilder.newDocument();
    marshaller.marshal(sdt, doc);
    DOMSource domSource = new DOMSource(doc);
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.transform(domSource, result);
    String xmlString = writer.toString();
    // Now remove the processing instruction.  This approach seems like a total hack.
    xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
    return xmlString;
  }
  
  /**
   * Takes a String encoding of a User in XML format and converts it to an instance. 
   * @param xmlString The XML string representing a User
   * @return The corresponding User instance. 
   * @throws Exception If problems occur during unmarshalling.
   */
  private User makeUser(String xmlString) throws Exception {
    Unmarshaller unmarshaller = this.userJAXB.createUnmarshaller();
    return (User)unmarshaller.unmarshal(new StringReader(xmlString));
  }
  
  /**
   * Takes a String encoding of a UserIndex in XML format and converts it to an instance. 
   * @param xmlString The XML string representing a UserIndex.
   * @return The corresponding UserIndex instance. 
   * @throws Exception If problems occur during unmarshalling.
   */
  private UserIndex makeUserIndex(String xmlString) throws Exception {
    Unmarshaller unmarshaller = this.userJAXB.createUnmarshaller();
    return (UserIndex)unmarshaller.unmarshal(new StringReader(xmlString));
  }
  
  /**
   * Returns the passed Properties instance as a String encoding of its XML representation.
   * @param properties The Properties instance. 
   * @return The XML String representation.
   * @throws Exception If problems occur during translation. 
   */
  private String makeProperties (Properties properties) throws Exception {
    Marshaller marshaller = this.userJAXB.createMarshaller(); 
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
    Document doc = documentBuilder.newDocument();
    marshaller.marshal(properties, doc);
    DOMSource domSource = new DOMSource(doc);
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.transform(domSource, result);
    String xmlString = writer.toString();
    // Now remove the processing instruction.  This approach seems like a total hack.
    xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
    return xmlString;
  }

}
