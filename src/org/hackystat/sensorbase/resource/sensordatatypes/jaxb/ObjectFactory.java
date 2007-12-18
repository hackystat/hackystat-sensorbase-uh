//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.12.18 at 12:24:18 PM GMT-10:00 
//


package org.hackystat.sensorbase.resource.sensordatatypes.jaxb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.hackystat.sensorbase.resource.sensordatatypes.jaxb package. 
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

    private final static QName _Name_QNAME = new QName("", "Name");
    private final static QName _Description_QNAME = new QName("", "Description");
    private final static QName _Value_QNAME = new QName("", "Value");
    private final static QName _Key_QNAME = new QName("", "Key");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.hackystat.sensorbase.resource.sensordatatypes.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Property }
     * 
     */
    public Property createProperty() {
        return new Property();
    }

    /**
     * Create an instance of {@link RequiredField }
     * 
     */
    public RequiredField createRequiredField() {
        return new RequiredField();
    }

    /**
     * Create an instance of {@link SensorDataType }
     * 
     */
    public SensorDataType createSensorDataType() {
        return new SensorDataType();
    }

    /**
     * Create an instance of {@link RequiredFields }
     * 
     */
    public RequiredFields createRequiredFields() {
        return new RequiredFields();
    }

    /**
     * Create an instance of {@link SensorDataTypes }
     * 
     */
    public SensorDataTypes createSensorDataTypes() {
        return new SensorDataTypes();
    }

    /**
     * Create an instance of {@link SensorDataTypeIndex }
     * 
     */
    public SensorDataTypeIndex createSensorDataTypeIndex() {
        return new SensorDataTypeIndex();
    }

    /**
     * Create an instance of {@link SensorDataTypeRef }
     * 
     */
    public SensorDataTypeRef createSensorDataTypeRef() {
        return new SensorDataTypeRef();
    }

    /**
     * Create an instance of {@link Properties }
     * 
     */
    public Properties createProperties() {
        return new Properties();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Name")
    public JAXBElement<String> createName(String value) {
        return new JAXBElement<String>(_Name_QNAME, String.class, null, value);
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
    @XmlElementDecl(namespace = "", name = "Value")
    public JAXBElement<String> createValue(String value) {
        return new JAXBElement<String>(_Value_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "Key")
    public JAXBElement<String> createKey(String value) {
        return new JAXBElement<String>(_Key_QNAME, String.class, null, value);
    }

}
