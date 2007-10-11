package org.hackystat.sensorbase.uricache;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Exercises CacheDescription functionality.
 * 
 * @author Pavel Senin.
 * 
 */
public class TestUriCacheDescription {

  private static final String dcStoragePath = System.getProperties().getProperty("java.io.tmpdir");
  private static final String descFileName = "testCache1.desc";
  private String tempFileName;
  /** The user email key. */
  public static final String USER_EMAIL_KEY = "uricache.user.email";
  /** User e-mail */
  private static final String userEmail = "javadude@javatesthost.org";
  /** The host key. */
  public static final String HOST_KEY = "uricache.host";
  /** User host key. */
  private static final String sensorBaseHost = "http://sensorbase143.javatesthost.org:20910";

  /**
   * Sets up test with the temporarily test description file.
   * 
   * @throws Exception if unable to proceed.
   */
  @Before
  public void setUp() throws Exception {
    this.tempFileName = dcStoragePath + "/" + descFileName;
    Properties prop = new Properties();
    prop.setProperty(USER_EMAIL_KEY, TestUriCacheDescription.userEmail);
    prop.setProperty(HOST_KEY, TestUriCacheDescription.sensorBaseHost);
    FileOutputStream stream = null;
    stream = new FileOutputStream(this.tempFileName);
    prop.store(stream, "the UriCache properties test file");
    stream.close();
  }

  /**
   * Tests CacheDescription functionality.
   */
  @Test
  public void testCacheDescription() {
    try {
      CacheDescription desc = new CacheDescription(new File(this.tempFileName));
      assertTrue("Should load properties from the file.", TestUriCacheDescription.sensorBaseHost
          .equalsIgnoreCase(desc.getsensorBaseHost()));
      assertTrue("Should load properties from the file.", TestUriCacheDescription.userEmail
          .equalsIgnoreCase(desc.getUserEmail()));
      assertTrue("Should load properties from the file.", "testCache1".equalsIgnoreCase(desc
          .getName()));
    }
    catch (IOException e) {
      fail("Should be able to load cache properties!\n" + e.getMessage());

    }

  }

  /**
   * Tears down test environment by deleting temporarily test description file.
   * 
   * @throws Exception if error encountered.
   */
  @After
  public void tearDown() throws Exception {
    File file2Delete = new File(this.tempFileName);
    file2Delete.delete();
  }

}
