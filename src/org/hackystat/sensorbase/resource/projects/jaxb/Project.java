//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.06.27 at 11:37:11 AM GMT-10:00 
//


package org.hackystat.sensorbase.resource.projects.jaxb;

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
 *         &lt;element ref="{}Description"/>
 *         &lt;element ref="{}StartTime" minOccurs="0"/>
 *         &lt;element ref="{}EndTime" minOccurs="0"/>
 *         &lt;element ref="{}Owner" minOccurs="0"/>
 *         &lt;element ref="{}Members"/>
 *         &lt;element ref="{}Invitations" minOccurs="0"/>
 *         &lt;element ref="{}Spectators" minOccurs="0"/>
 *         &lt;element ref="{}UriPatterns"/>
 *         &lt;element ref="{}Properties"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{}Name"/>
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
    "description",
    "startTime",
    "endTime",
    "owner",
    "members",
    "invitations",
    "spectators",
    "uriPatterns",
    "properties"
})
@XmlRootElement(name = "Project")
public class Project
    implements Serializable
{

    private final static long serialVersionUID = 12343L;
    @XmlElement(name = "Description", required = true)
    protected String description;
    @XmlElement(name = "StartTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startTime;
    @XmlElement(name = "EndTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar endTime;
    @XmlElement(name = "Owner")
    protected String owner;
    @XmlElement(name = "Members", required = true)
    protected Members members;
    @XmlElement(name = "Invitations")
    protected Invitations invitations;
    @XmlElement(name = "Spectators")
    protected Spectators spectators;
    @XmlElement(name = "UriPatterns", required = true)
    protected UriPatterns uriPatterns;
    @XmlElement(name = "Properties", required = true)
    protected Properties properties;
    @XmlAttribute(name = "Name")
    protected String name;
    @XmlAttribute(name = "LastMod")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastMod;

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    public boolean isSetDescription() {
        return (this.description!= null);
    }

    /**
     * Gets the value of the startTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartTime() {
        return startTime;
    }

    /**
     * Sets the value of the startTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartTime(XMLGregorianCalendar value) {
        this.startTime = value;
    }

    public boolean isSetStartTime() {
        return (this.startTime!= null);
    }

    /**
     * Gets the value of the endTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEndTime() {
        return endTime;
    }

    /**
     * Sets the value of the endTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEndTime(XMLGregorianCalendar value) {
        this.endTime = value;
    }

    public boolean isSetEndTime() {
        return (this.endTime!= null);
    }

    /**
     * Gets the value of the owner property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwner(String value) {
        this.owner = value;
    }

    public boolean isSetOwner() {
        return (this.owner!= null);
    }

    /**
     * Gets the value of the members property.
     * 
     * @return
     *     possible object is
     *     {@link Members }
     *     
     */
    public Members getMembers() {
        return members;
    }

    /**
     * Sets the value of the members property.
     * 
     * @param value
     *     allowed object is
     *     {@link Members }
     *     
     */
    public void setMembers(Members value) {
        this.members = value;
    }

    public boolean isSetMembers() {
        return (this.members!= null);
    }

    /**
     * Gets the value of the invitations property.
     * 
     * @return
     *     possible object is
     *     {@link Invitations }
     *     
     */
    public Invitations getInvitations() {
        return invitations;
    }

    /**
     * Sets the value of the invitations property.
     * 
     * @param value
     *     allowed object is
     *     {@link Invitations }
     *     
     */
    public void setInvitations(Invitations value) {
        this.invitations = value;
    }

    public boolean isSetInvitations() {
        return (this.invitations!= null);
    }

    /**
     * Gets the value of the spectators property.
     * 
     * @return
     *     possible object is
     *     {@link Spectators }
     *     
     */
    public Spectators getSpectators() {
        return spectators;
    }

    /**
     * Sets the value of the spectators property.
     * 
     * @param value
     *     allowed object is
     *     {@link Spectators }
     *     
     */
    public void setSpectators(Spectators value) {
        this.spectators = value;
    }

    public boolean isSetSpectators() {
        return (this.spectators!= null);
    }

    /**
     * Gets the value of the uriPatterns property.
     * 
     * @return
     *     possible object is
     *     {@link UriPatterns }
     *     
     */
    public UriPatterns getUriPatterns() {
        return uriPatterns;
    }

    /**
     * Sets the value of the uriPatterns property.
     * 
     * @param value
     *     allowed object is
     *     {@link UriPatterns }
     *     
     */
    public void setUriPatterns(UriPatterns value) {
        this.uriPatterns = value;
    }

    public boolean isSetUriPatterns() {
        return (this.uriPatterns!= null);
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
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    public boolean isSetName() {
        return (this.name!= null);
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

}
