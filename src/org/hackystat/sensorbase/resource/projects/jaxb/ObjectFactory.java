//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.03.26 at 07:55:43 AM HST 
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

    private final static QName _EndTime_QNAME = new QName("", "EndTime");
    private final static QName _Key_QNAME = new QName("", "Key");
    private final static QName _StartTime_QNAME = new QName("", "StartTime");
    private final static QName _Value_QNAME = new QName("", "Value");
    private final static QName _Owner_QNAME = new QName("", "Owner");
    private final static QName _Invitation_QNAME = new QName("", "Invitation");
    private final static QName _Description_QNAME = new QName("", "Description");
    private final static QName _UriPattern_QNAME = new QName("", "UriPattern");
    private final static QName _Member_QNAME = new QName("", "Member");

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
     * Create an instance of {@link Projects }
     * 
     */
    public Projects createProjects() {
        return new Projects();
    }

    /**
     * Create an instance of {@link Properties }
     * 
     */
    public Properties createProperties() {
        return new Properties();
    }

    /**
     * Create an instance of {@link UriPatterns }
     * 
     */
    public UriPatterns createUriPatterns() {
        return new UriPatterns();
    }

    /**
     * Create an instance of {@link Project }
     * 
     */
    public Project createProject() {
        return new Project();
    }

    /**
     * Create an instance of {@link Property }
     * 
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link Members }
     * 
     */
    public Members createMembers() {
        return new Members();
    }

    /**
     * Create an instance of {@link SensorDataSummaries }
     * 
     */
    public SensorDataSummaries createSensorDataSummaries() {
        return new SensorDataSummaries();
    }

    /**
     * Create an instance of {@link SensorDataSummary }
     * 
     */
    public SensorDataSummary createSensorDataSummary() {
        return new SensorDataSummary();
    }

    /**
     * Create an instance of {@link Invitations }
     * 
     */
    public Invitations createInvitations() {
        return new Invitations();
    }

    /**
     * Create an instance of {@link ProjectIndex }
     * 
     */
    public ProjectIndex createProjectIndex() {
        return new ProjectIndex();
    }

    /**
     * Create an instance of {@link ProjectSummary }
     * 
     */
    public ProjectSummary createProjectSummary() {
        return new ProjectSummary();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "EndTime")
    public JAXBElement<XMLGregorianCalendar> createEndTime(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_EndTime_QNAME, XMLGregorianCalendar.class, null, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "StartTime")
    public JAXBElement<XMLGregorianCalendar> createStartTime(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_StartTime_QNAME, XMLGregorianCalendar.class, null, value);
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
    @XmlElementDecl(namespace = "", name = "Owner")
    public JAXBElement<String> createOwner(String value) {
        return new JAXBElement<String>(_Owner_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Invitation")
    public JAXBElement<String> createInvitation(String value) {
        return new JAXBElement<String>(_Invitation_QNAME, String.class, null, value);
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
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "UriPattern")
    public JAXBElement<String> createUriPattern(String value) {
        return new JAXBElement<String>(_UriPattern_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Member")
    public JAXBElement<String> createMember(String value) {
        return new JAXBElement<String>(_Member_QNAME, String.class, null, value);
    }

}
