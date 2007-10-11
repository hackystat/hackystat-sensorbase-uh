package org.hackystat.sensorbase.uricache;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests UriCacheDescriptionFilter functionality.
 * 
 * @author Pavel Senin.
 * 
 */
public class TestUriCacheDescriptionFilter {

  private static final String dcStoragePath = System.getProperties().getProperty("java.io.tmpdir");
  private static final String descFileName = "testCache1.desc";
  private String tempFileName;

  /**
   * Sets up test file in the system.temp folder.
   * 
   * @throws Exception if unable to setup test file.
   */
  @Before
  public void setUp() throws Exception {
    this.tempFileName = dcStoragePath + "/" + descFileName;
    BufferedWriter writer = new BufferedWriter(new FileWriter(new File(this.tempFileName)));
    writer.write("test\n");
    writer.close();
  }

  /**
   * Tests UriCacheDescriptionFilter.
   */
  @Test
  public void testUriCacheDescriptionFilter() {
    File dir = new File(dcStoragePath);
    File[] files = dir.listFiles(new UriCacheDescriptionFilter());
    boolean found = false;
    for (File f : files) {
      if (descFileName.equalsIgnoreCase(f.getName())) {
        found = true;
        break;
      }
    }
    assertTrue("Should find the temporarily cache description", found);
  }

  /**
   * Deletes test description file from the test folder.
   * 
   * @throws Exception if error encountered.
   */
  @After
  public void tearDown() throws Exception {
    File file2Delete = new File(this.tempFileName);
    file2Delete.delete();
  }

}
