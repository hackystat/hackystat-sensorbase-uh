//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.3-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.06.04 at 12:34:25 PM HST 
//


package org.hackystat.sensorbase.resource.sensordata.jaxb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.hackystat.sensorbase.resource.sensordata.jaxb package. 
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
    private final static QName _Resource_QNAME = new QName("", "Resource");
    private final static QName _Value_QNAME = new QName("", "Value");
    private final static QName _Owner_QNAME = new QName("", "Owner");
    private final static QName _Tool_QNAME = new QName("", "Tool");
    private final static QName _SensorDataType_QNAME = new QName("", "SensorDataType");
    private final static QName _Runtime_QNAME = new QName("", "Runtime");
    private final static QName _Timestamp_QNAME = new QName("", "Timestamp");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.hackystat.sensorbase.resource.sensordata.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SensorDatas }
     * 
     */
    public SensorDatas createSensorDatas() {
        return new SensorDatas();
    }

    /**
     * Create an instance of {@link Property }
     * 
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link SensorDataIndex }
     * 
     */
    public SensorDataIndex createSensorDataIndex() {
        return new SensorDataIndex();
    }

    /**
     * Create an instance of {@link Properties }
     * 
     */
    public Properties createProperties() {
        return new Properties();
    }

    /**
     * Create an instance of {@link SensorDataRef }
     * 
     */
    public SensorDataRef createSensorDataRef() {
        return new SensorDataRef();
    }

    /**
     * Create an instance of {@link SensorData }
     * 
     */
    public SensorData createSensorData() {
        return new SensorData();
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
    @XmlElementDecl(namespace = "", name = "Resource")
    public JAXBElement<String> createResource(String value) {
        return new JAXBElement<String>(_Resource_QNAME, String.class, null, value);
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
    @XmlElementDecl(namespace = "", name = "Tool")
    public JAXBElement<String> createTool(String value) {
        return new JAXBElement<String>(_Tool_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "SensorDataType")
    public JAXBElement<String> createSensorDataType(String value) {
        return new JAXBElement<String>(_SensorDataType_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Runtime")
    public JAXBElement<XMLGregorianCalendar> createRuntime(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_Runtime_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Timestamp")
    public JAXBElement<XMLGregorianCalendar> createTimestamp(XMLGregorianCalendar value) {
        return new JAXBElement<XMLGregorianCalendar>(_Timestamp_QNAME, XMLGregorianCalendar.class, null, value);
    }

}
