//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.06.27 at 11:37:09 AM GMT-10:00 
//


package org.hackystat.sensorbase.resource.users.jaxb;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}Email"/>
 *         &lt;element ref="{}Password"/>
 *         &lt;element ref="{}Role" minOccurs="0"/>
 *         &lt;element ref="{}Properties" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{}LastMod"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "email",
    "password",
    "role",
    "properties"
})
@XmlRootElement(name = "User")
public class User
    implements Serializable
{

    private final static long serialVersionUID = 12343L;
    @XmlElement(name = "Email", required = true)
    protected String email;
    @XmlElement(name = "Password", required = true)
    protected String password;
    @XmlElement(name = "Role")
    protected String role;
    @XmlElement(name = "Properties")
    protected Properties properties;
    @XmlAttribute(name = "LastMod")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastMod;

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmail(String value) {
        this.email = value;
    }

    public boolean isSetEmail() {
        return (this.email!= null);
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    public boolean isSetPassword() {
        return (this.password!= null);
    }

    /**
     * Gets the value of the role property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRole(String value) {
        this.role = value;
    }

    public boolean isSetRole() {
        return (this.role!= null);
    }

    /**
     * Gets the value of the properties property.
     * 
     * @return
     *     possible object is
     *     {@link Properties }
     *     
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets the value of the properties property.
     * 
     * @param value
     *     allowed object is
     *     {@link Properties }
     *     
     */
    public void setProperties(Properties value) {
        this.properties = value;
    }

    public boolean isSetProperties() {
        return (this.properties!= null);
    }

    /**
     * Gets the value of the lastMod property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastMod() {
        return lastMod;
    }

    /**
     * Sets the value of the lastMod property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastMod(XMLGregorianCalendar value) {
        this.lastMod = value;
    }

    public boolean isSetLastMod() {
        return (this.lastMod!= null);
    }
    
    // Custom methods here.  Must be manually maintained. 

    /**
     * Returns the first Property instance with the specified key, or null if not found.
     * @param key The key for the property of interest. 
     * @return The Property instance for the key, or null if not found. 
     */
    public Property findProperty(String key) {
      for (Property property : this.getProperties().getProperty()) {
        if ((property.getKey() != null) && (property.getKey().equals(key))) {
          return property;
        }
      }
      return null;
    }
    
    /**
     * Adds a new property to this instance with the specified key and value. 
     * @param key The key for the new property.
     * @param value The value for the new property.
     */
    public void addProperty(String key, String value) {
      if (this.getProperties() == null) {
        this.setProperties(new Properties());
      }
      Property property = new Property();
      property.setKey(key);
      property.setValue(value);
      this.getProperties().getProperty().add(property);
    }

}
