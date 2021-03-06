//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.06.27 at 11:37:07 AM GMT-10:00 
//


package org.hackystat.sensorbase.resource.sensordatatypes.jaxb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;element ref="{}SensorDataType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sensorDataType"
})
@XmlRootElement(name = "SensorDataTypes")
public class SensorDataTypes
    implements Serializable
{

    private final static long serialVersionUID = 12343L;
    @XmlElement(name = "SensorDataType", required = true)
    protected List<SensorDataType> sensorDataType;

    /**
     * Gets the value of the sensorDataType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sensorDataType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSensorDataType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SensorDataType }
     * 
     * 
     */
    public List<SensorDataType> getSensorDataType() {
        if (sensorDataType == null) {
            sensorDataType = new ArrayList<SensorDataType>();
        }
        return this.sensorDataType;
    }

    public boolean isSetSensorDataType() {
        return ((this.sensorDataType!= null)&&(!this.sensorDataType.isEmpty()));
    }

    public void unsetSensorDataType() {
        this.sensorDataType = null;
    }

}
