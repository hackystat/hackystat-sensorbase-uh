//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.3-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.05.08 at 04:35:29 PM HST 
//


package org.hackystat.sensorbase.resource.projects.jaxb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.hackystat.sensorbase.resource.projects.jaxb package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Key_QNAME = new QName("", "Key");
    private final static QName _Value_QNAME = new QName("", "Value");
    private final static QName _Description_QNAME = new QName("", "Description");
    private final static QName _StartDay_QNAME = new QName("", "StartDay");
    private final static QName _UriPattern_QNAME = new QName("", "UriPattern");
    private final static QName _EndDay_QNAME = new QName("", "EndDay");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.hackystat.sensorbase.resource.projects.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ProjectRef }
     * 
     */
    public ProjectRef createProjectRef() {
        return new ProjectRef();
    }

    /**
     * Create an instance of {@link ProjectIndex }
     * 
     */
    public ProjectIndex createProjectIndex() {
        return new ProjectIndex();
    }

    /**
     * Create an instance of {@link Properties }
     * 
     */
    public Properties createProperties() {
        return new Properties();
    }

    /**
     * Create an instance of {@link Property }
     * 
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link Project }
     * 
     */
    public Project createProject() {
        return new Project();
    }

    /**
     * Create an instance of {@link UriPatterns }
     * 
     */
    public UriPatterns createUriPatterns() {
        return new UriPatterns();
    }

    /**
     * Create an instance of {@link Users }
     * 
     */
    public Users createUsers() {
        return new Users();
    }

    /**
     * Create an instance of {@link UserRef }
     * 
     */
    public UserRef createUserRef() {
        return new UserRef();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Key")
    public JAXBElement<String> createKey(String value) {
        return new JAXBElement<String>(_Key_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Value")
    public JAXBElement<String> createValue(String value) {
        return new JAXBElement<String>(_Value_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Description")
    public JAXBElement<String> createDescription(String value) {
        return new JAXBElement<String>(_Description_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "StartDay")
    public JAXBElement<XMLGregorianCalendar> createStartDay(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_StartDay_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "UriPattern")
    public JAXBElement<String> createUriPattern(String value) {
        return new JAXBElement<String>(_UriPattern_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "EndDay")
    public JAXBElement<XMLGregorianCalendar> createEndDay(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_EndDay_QNAME, XMLGregorianCalendar.class, null, value);
    }

}