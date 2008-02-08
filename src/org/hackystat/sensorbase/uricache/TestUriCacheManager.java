package org.hackystat.sensorbase.uricache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

import org.hackystat.utilities.uricache.UriCache;
import org.hackystat.utilities.uricache.UriCacheException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests CacheManager functionality.
 * 
 * @author Pavel Senin.
 * 
 */
public class TestUriCacheManager {

  private static final String fileSeparator = System.getProperty("file.separator");

  /** Used for temporarily caches home */
  private static final String tmpFolderName = String.valueOf(System.currentTimeMillis());
  /** The general storage place. */
  private static final String cacheHome = System.getProperties().getProperty("user.dir")
      + fileSeparator + "build" + fileSeparator + "uricache-tests" + fileSeparator + tmpFolderName;

  /** User e-mails */
  private static final String user1Email = "javadude1@javatesthost.org";
  private static final String user2Email = "javadude2@javatesthost.org";
  private static final String user3Email = "javadude3@javatesthost.org";

  /** User host key. */
  private static final String sensorBaseHost1 = "http://sensorbase143.javatesthost.org:20910";
  private static final String sensorBaseHost2 = "http://sensorbase144.javatesthost.org:20910";
  private static final String sensorBaseHost3 = "http://sensorbase147.javatesthost.org:20910";

  /** Making code checkers happy ;-)))) */
  private static final String key = "key:";
  private static final String data = "data:";

  // storage for the cache names
  private String cache1Name;
  private String cache2Name;
  private String cache3Name;

  /**
   * Sets up three test caches.
   * 
   * @throws Exception if unable to proceed.
   */
  @Before
  public void setUp() throws Exception {
    // setting up the cacheHome
    File f = new File(cacheHome);
    if (!f.exists()) {
      f.mkdirs();
    }

    // setting up first cache
    UriCache testCache1 = UriCacheManager.getCache(cacheHome, sensorBaseHost1,
        user1Email);
    this.cache1Name = testCache1.getName();
    testCache1.clear();
    for (int i = 0; i < 500; i++) {
      testCache1.cache(key + i, data + i);
    }
    testCache1.shutdown();

    // setting up second cache
    UriCache testCache2 = UriCacheManager.getCache(cacheHome, sensorBaseHost2,
        user2Email);
    this.cache2Name = testCache2.getName();
    testCache2.clear();
    for (int i = 500; i < 1000; i++) {
      testCache2.cache(key + i, data + i);
    }
    testCache2.shutdown();

    // setting up third cache
    UriCache testCache3 = UriCacheManager.getCache(cacheHome, sensorBaseHost3,
        user3Email);
    this.cache3Name = testCache3.getName();
    testCache3.clear();
    for (int i = 1000; i < 1500; i++) {
      testCache3.cache(key + i, data + i);
    }
    testCache3.shutdown();
  }

  /**
   * Tests the getCaches() method.
   * 
   * @throws UriCacheException in the case of error.
   */
  @Test
  public void testGetCaches() throws UriCacheException {
    // get list of caches
    List<UriCacheDescription> caches = UriCacheManager.getCaches(cacheHome);
    assertEquals("Should find three caches only", 3, caches.size());
    // put caches in map to ease the test
    TreeMap<String, UriCacheDescription> cachesMap = new TreeMap<String, UriCacheDescription>();
    for (UriCacheDescription d : caches) {
      cachesMap.put(d.getName(), d);
    }
    // now run tests
    assertTrue("Should report right e-mail", user1Email.equalsIgnoreCase(cachesMap.get(
        this.cache1Name).getUserEmail()));
    assertTrue("Should report right e-mail", user2Email.equalsIgnoreCase(cachesMap.get(
        this.cache2Name).getUserEmail()));
    assertTrue("Should report right e-mail", user3Email.equalsIgnoreCase(cachesMap.get(
        this.cache3Name).getUserEmail()));

    assertTrue("Should report right host", sensorBaseHost1.equalsIgnoreCase(cachesMap.get(
        this.cache1Name).getsensorBaseHost()));
    assertTrue("Should report right host", sensorBaseHost2.equalsIgnoreCase(cachesMap.get(
        this.cache2Name).getsensorBaseHost()));
    assertTrue("Should report right host", sensorBaseHost3.equalsIgnoreCase(cachesMap.get(
        this.cache3Name).getsensorBaseHost()));
  }

  /**
   * Tests the getCache() method.
   * 
   * @throws UriCacheException in the case of error.
   */
  @Test
  public void testGetCache() throws UriCacheException {
    try {

      // get cache #1
      UriCache cache1;
      cache1 = UriCacheManager.getCache(cacheHome, sensorBaseHost1, user1Email);
      // should be ABLE to read data back
      for (int i = 0; i < 500; i++) {
        String element = (String) cache1.lookup(key + i);
        assertNotNull("Should have recevied an element. " + i, element);
        assertEquals("Element is wrong.", data + i, element);
      }
      cache1.shutdown();

      // get cache #2
      UriCache cache2;
      cache2 = UriCacheManager.getCache(cacheHome, sensorBaseHost2, user2Email);
      // should be ABLE to read data back
      for (int i = 500; i < 1000; i++) {
        String element = (String) cache2.lookup(key + i);
        assertNotNull("Should have recevied an element. " + i, element);
        assertEquals("Element is wrong.", data + i, element);
      }
      cache2.shutdown();

      // get cache #3
      UriCache cache3;
      cache3 = UriCacheManager.getCache(cacheHome, sensorBaseHost3, user3Email);
      // should be ABLE to read data back
      for (int i = 1000; i < 1500; i++) {
        String element = (String) cache3.lookup(key + i);
        assertNotNull("Should have recevied an element. " + i, element);
        assertEquals("Element is wrong.", data + i, element);
      }
      cache3.shutdown();
    }
    catch (UriCacheException e) {
      fail("Unable to proceed with test " + e.getMessage());
    }
    catch (IOException e) {
      fail("Unable to proceed with test " + e.getMessage());
    }

  }

  /**
   * Tears down test environment and clears all leftovers.
   * 
   * @throws Exception if error encountered.
   */
  @After
  public void tearDown() throws Exception {
    // remove all files
    File dir = new File(cacheHome);
    File[] files = dir.listFiles();
    for (File f : files) {
      f.delete();
    }
    // remove folder
    dir.delete();
  }

}
