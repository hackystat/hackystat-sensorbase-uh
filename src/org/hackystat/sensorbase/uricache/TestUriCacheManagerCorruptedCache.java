package org.hackystat.sensorbase.uricache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;

import org.hackystat.utilities.uricache.UriCache;
import org.hackystat.utilities.uricache.UriCacheException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests CacheManager functionality if the cache data is corrupted. This situation could happen when
 * system is restarted after crash.
 * 
 * @author Pavel Senin.
 * 
 */
public class TestUriCacheManagerCorruptedCache {

  private static final String fileSeparator = System.getProperty("file.separator");

  /** Used for temporarily caches home */
  private static final String tmpFolderName = String.valueOf(System.currentTimeMillis());
  /** The general storage place. */
  private static final String cacheHome = System.getProperties().getProperty("user.dir")
      + fileSeparator + "build" + fileSeparator + "uricache-tests" + fileSeparator + tmpFolderName;

  /** Test cache and files names. */
  private static final String testCacheName = "TestCorruptedCache";
  private static final String testCacheDescName = testCacheName + ".desc";
  private static final String testCacheDataName = testCacheName + ".data";
  private static final String testCacheKeyName = testCacheName + ".key";

  /** User e-mail key. */
  private static final String USER_EMAIL_KEY = "uricache.user.email";
  /** User e-mail */
  private static final String userEmail = "javadude@javatesthost.org";

  /** The host key. */
  private static final String HOST_KEY = "uricache.host";
  /** User host key. */
  private static final String sensorBaseHost = "http://sensorbase143.javatesthost.org:20910";

  private static final String key = "key:";
  private static final String data = "data:";

  /**
   * Sets up test with the temporarily test caches.
   * 
   * @throws Exception if unable to proceed.
   */
  @Before
  public void setUp() throws Exception {
    // setting up the test area
    File f = new File(cacheHome);
    if (!f.exists()) {
      f.mkdirs();
    }

    // creating description file
    String fName = cacheHome + fileSeparator + testCacheDescName;
    Properties prop = new Properties();
    prop.setProperty(USER_EMAIL_KEY, userEmail);
    prop.setProperty(HOST_KEY, sensorBaseHost);
    FileOutputStream stream = null;
    stream = new FileOutputStream(fName);
    prop.store(stream, "the UriCache properties test file");
    stream.close();

    // populating junk into .data file
    fName = cacheHome + fileSeparator + testCacheDataName;
    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fName)));
    for (int i = 0; i < 5000; i++) {
      bw.write("number:" + i);
    }
    bw.close();

    // populating junk into .key file
    fName = cacheHome + fileSeparator + testCacheKeyName;
    bw = new BufferedWriter(new FileWriter(new File(fName)));
    for (int i = 5000; i > 0; i--) {
      bw.write("number:" + i);
    }
    bw.close();

  }

  /**
   * Tests the corrupted cache processing.
   * 
   * @throws UriCacheException in the case of error.
   * @throws IOException if IO error occurs.
   * @throws InterruptedException if thread unable to sleep.
   */
  @Test
  public void testCorruptedCache() throws UriCacheException, IOException, InterruptedException {
    // get list of caches
    List<UriCacheDescription> caches = UriCacheManager.getCaches(cacheHome);
    assertEquals("Should find one cache only", 1, caches.size());
    // put caches in map to ease the test
    TreeMap<String, UriCacheDescription> cachesMap = new TreeMap<String, UriCacheDescription>();
    for (UriCacheDescription d : caches) {
      cachesMap.put(d.getName(), d);
    }

    // now run test of description - should pass
    assertTrue("Should report right e-mail", userEmail.equalsIgnoreCase(cachesMap
        .get(testCacheName).getUserEmail()));
    assertTrue("Should report right host", sensorBaseHost.equalsIgnoreCase(cachesMap.get(
        testCacheName).getsensorBaseHost()));

    // get cache
    UriCache<String, Object> cache;
    cache = UriCacheManager.getCache(cacheHome, sensorBaseHost, userEmail);
    //
    // Should pass this point without any exception and reset the cache
    //
    // now load darta into cache
    for (int i = 0; i < 500; i++) {
      cache.cache(key + i, data + i);
    }
    //
    // and finally should be ABLE to read data back
    for (int i = 0; i < 500; i++) {
      String element = (String) cache.lookup(key + i);
      assertNotNull("Should have recevied an element. " + i, element);
      assertEquals("Element is wrong.", data + i, element);
    }
    cache.shutdown();

    //
    // now just for fun try to test persistence
    //
    Thread.yield();
    Thread.sleep(1000);
    Thread.yield();
    cache = UriCacheManager.getCache(cacheHome, sensorBaseHost, userEmail);
    // should be ABLE to read data back
    for (int i = 0; i < 500; i++) {
      String element = (String) cache.lookup(key + i);
      assertNotNull("Should have recevied an element. " + i, element);
      assertEquals("Element is wrong.", data + i, element);
    }
    cache.shutdown();
  }

  /**
   * Tears down test environment and clears all leftovers.
   * 
   * @throws Exception if error encountered.
   */
  @After
  public void tearDown() throws Exception {
    // remove files
    String fName = cacheHome + fileSeparator + testCacheDescName;
    File f = new File(fName);
    f.delete();

    fName = cacheHome + fileSeparator + testCacheDataName;
    f = new File(fName);
    f.delete();

    fName = cacheHome + fileSeparator + testCacheKeyName;
    f = new File(fName);
    f.delete();

    // remove folder
    f = new File(cacheHome);
    if (f.exists()) {
      f.delete();
    }
  }

}
