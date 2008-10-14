package org.hackystat.sensorbase.resource.sensordatatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.hackystat.sensorbase.client.SensorBaseClient;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.Property;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.RequiredField;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.RequiredFields;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataTypeRef;
import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataTypeIndex;
import org.hackystat.sensorbase.test.SensorBaseRestApiHelper;
import org.junit.Test;

/**
 * Tests the SensorBase REST API for both the SensorDataTypes and SensorDataType resources.
 * @author Philip M. Johnson
 */
public class TestSdtRestApi extends SensorBaseRestApiHelper {

  /** The test user. */
  private String user = "TestUser@hackystat.org";

  /**
   * Test that GET host/sensorbase/sensordatatypes returns an index containing TestSDT, and that 
   * all SDTs in the index can be retrieved. 
   * @throws Exception If problems occur.
   */
  @Test public void getSdtIndex() throws Exception {
    SensorBaseClient client = new SensorBaseClient(getHostName(), user, user);
    client.authenticate();
    SensorDataTypeIndex sdtIndex = client.getSensorDataTypeIndex();
    // Make sure that we can iterate through all of the SDTs OK. 
    boolean foundTestSdt = false;
    for (SensorDataTypeRef ref : sdtIndex.getSensorDataTypeRef()) {
      if ("TestSdt".equals(ref.getName())) {
        foundTestSdt = true;
      }
      // Make sure the href is OK. 
      //client.getUri(ref.getHref());
      client.getSensorDataType(ref);
    }
    assertTrue("Checking that we found the TestSdt", foundTestSdt);
    }
  
  /**
   * Test that GET host/sensorbase/sensordatatypes/TestSdt returns the TestSdt SDT.
   * @throws Exception If problems occur.
   */
  @Test public void getSdt() throws Exception {
    SensorBaseClient client = new SensorBaseClient(getHostName(), user, user);
    client.authenticate();
    SensorDataType sdt = client.getSensorDataType("TestSdt");
    assertEquals("Checking name", "TestSdt", sdt.getName());
    assertTrue("Checking description", sdt.getDescription().startsWith("SDT"));
    RequiredField reqField = sdt.getRequiredFields().getRequiredField().get(0);
    assertEquals("Checking required field name", "SampleField", reqField.getName());
    assertEquals("Checking required field value", "Sample Field Value", reqField.getDescription());
    Property property = sdt.getProperties().getProperty().get(0);
    assertEquals("Checking property key", "SampleProperty", property.getKey());
    assertEquals("Checking property value", "Sample Property Value", property.getValue());
    }
  
  /**
   * Test that PUT and DELETE host/sensorbase/sensordatatypes/TestSdt are OK.
   * Note that these operations require admin authentication.
   * @throws Exception If problems occur.
   */
  @Test public void putSdt() throws Exception {
    // First, create a sample SDT. 
    SensorDataType sdt = new SensorDataType();
    String sdtName = "TestSdt2";
    sdt.setName(sdtName);
    sdt.setDescription("Sample SDT");
    RequiredField field = new RequiredField();
    field.setName("Required");
    RequiredFields fields = new RequiredFields();
    fields.getRequiredField().add(field);
    // Now put it to the server. 
    SensorBaseClient client = new SensorBaseClient(getHostName(), adminEmail, adminPassword);
    client.authenticate();
    client.putSensorDataType(sdt);
    assertEquals("Testing sdt retrieval", sdtName, client.getSensorDataType(sdtName).getName());
    client.deleteSensorDataType(sdtName);
  }
}
