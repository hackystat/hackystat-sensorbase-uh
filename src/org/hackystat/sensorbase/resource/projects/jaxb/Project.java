//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.08.30 at 01:31:36 PM GMT-10:00 
//


package org.hackystat.sensorbase.resource.projects.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
 *         &lt;element ref="{}StartTime"/>
 *         &lt;element ref="{}EndTime"/>
 *         &lt;element ref="{}Owner"/>
 *         &lt;element ref="{}Members"/>
 *         &lt;element ref="{}UriPatterns"/>
 *         &lt;element ref="{}Properties"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{}LastMod"/>
 *       &lt;attribute ref="{}Name use="required""/>
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
    "uriPatterns",
    "properties"
})
@XmlRootElement(name = "Project")
public class Project {

    @XmlElement(name = "Description", required = true)
    protected String description;
    @XmlElement(name = "StartTime", required = true)
    protected XMLGregorianCalendar startTime;
    @XmlElement(name = "EndTime", required = true)
    protected XMLGregorianCalendar endTime;
    @XmlElement(name = "Owner", required = true)
    protected String owner;
    @XmlElement(name = "Members", required = true)
    protected Members members;
    @XmlElement(name = "UriPatterns", required = true)
    protected UriPatterns uriPatterns;
    @XmlElement(name = "Properties", required = true)
    protected Properties properties;
    @XmlAttribute(name = "LastMod")
    protected XMLGregorianCalendar lastMod;
    @XmlAttribute(name = "Name", required = true)
    protected String name;

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

}
