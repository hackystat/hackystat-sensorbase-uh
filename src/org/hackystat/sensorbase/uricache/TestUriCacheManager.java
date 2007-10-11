package org.hackystat.sensorbase.uricache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

  /** The general storage place. */
  private static final String dcStoragePath = System.getProperties().getProperty("java.io.tmpdir");

  /** Test caches names. */
  private String testCache1Name = null;
  private String testCache2Name = null;
  private String testCache3Name = null;

  /** The user email key. */
  public static final String USER_EMAIL_KEY = "uricache.user.email";
  /** User e-mails */
  private static final String user1Email = "javadude1@javatesthost.org";
  private static final String user2Email = "javadude2@javatesthost.org";
  private static final String user3Email = "javadude3@javatesthost.org";
  /** The host key. */
  public static final String HOST_KEY = "uricache.host";
  /** User host key. */
  private static final String sensorBaseHost1 = "http://sensorbase143.javatesthost.org:20910";
  private static final String sensorBaseHost2 = "http://sensorbase144.javatesthost.org:20910";
  private static final String sensorBaseHost3 = "http://sensorbase147.javatesthost.org:20910";

  private static final String key = "key:";
  private static final String data = "data:";

  /**
   * Sets up test with the temporarily test caches.
   * 
   * @throws Exception if unable to proceed.
   */
  @Before
  public void setUp() throws Exception {
    // setting up first cache
    UriCache<String, Object> testCache1 = UriCacheManager.getCache(dcStoragePath, sensorBaseHost1,
        user1Email);
    testCache1Name = testCache1.getName();
    testCache1.clear();
    for (int i = 0; i < 500; i++) {
      testCache1.cache(key + i, data + i);
    }
    testCache1.shutdown();

    // setting up second cache
    UriCache<String, Object> testCache2 = UriCacheManager.getCache(dcStoragePath, sensorBaseHost2,
        user2Email);
    testCache2Name = testCache2.getName();
    testCache2.clear();
    for (int i = 500; i < 1000; i++) {
      testCache2.cache(key + i, data + i);
    }
    testCache2.shutdown();

    // setting up third cache
    UriCache<String, Object> testCache3 = UriCacheManager.getCache(dcStoragePath, sensorBaseHost3,
        user3Email);
    testCache3Name = testCache3.getName();
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
    List<CacheDescription> caches = UriCacheManager.getCaches(dcStoragePath);
    assertEquals("Should find three caches only", 3, caches.size());
    // put caches in map to ease the test
    TreeMap<String, CacheDescription> cachesMap = new TreeMap<String, CacheDescription>();
    for (CacheDescription d : caches) {
      cachesMap.put(d.getName(), d);
    }
    // now run tests
    assertTrue("Should report right e-mail", user1Email.equalsIgnoreCase(cachesMap.get(
        testCache1Name).getUserEmail()));
    assertTrue("Should report right e-mail", user2Email.equalsIgnoreCase(cachesMap.get(
        testCache2Name).getUserEmail()));
    assertTrue("Should report right e-mail", user3Email.equalsIgnoreCase(cachesMap.get(
        testCache3Name).getUserEmail()));

    assertTrue("Should report right host", sensorBaseHost1.equalsIgnoreCase(cachesMap.get(
        testCache1Name).getsensorBaseHost()));
    assertTrue("Should report right host", sensorBaseHost2.equalsIgnoreCase(cachesMap.get(
        testCache2Name).getsensorBaseHost()));
    assertTrue("Should report right host", sensorBaseHost3.equalsIgnoreCase(cachesMap.get(
        testCache3Name).getsensorBaseHost()));
  }

  /**
   * Tests the getCache() method.
   * @throws UriCacheException in the case of error. 
   */
  @Test
  public void testGetCache() throws UriCacheException {
    try {

      // get cache #1
      UriCache<String, Object> cache1;
      cache1 = UriCacheManager.getCache(dcStoragePath, sensorBaseHost1, user1Email);
      // should be ABLE to read data back
      for (int i = 0; i < 500; i++) {
        String element = (String) cache1.lookup(key + i);
        assertNotNull("Should have recevied an element. " + i, element);
        assertEquals("Element is wrong.", data + i, element);
      }
      cache1.shutdown();

      // get cache #2
      UriCache<String, Object> cache2;
      cache2 = UriCacheManager.getCache(dcStoragePath, sensorBaseHost2, user2Email);
      // should be ABLE to read data back
      for (int i = 500; i < 1000; i++) {
        String element = (String) cache2.lookup(key + i);
        assertNotNull("Should have recevied an element. " + i, element);
        assertEquals("Element is wrong.", data + i, element);
      }
      cache2.shutdown();

      // get cache #3
      UriCache<String, Object> cache3;
      cache3 = UriCacheManager.getCache(dcStoragePath, sensorBaseHost3, user3Email);
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
    // setting up first cache
    UriCache<String, Object> testCache1 = UriCacheManager.getCache(dcStoragePath, sensorBaseHost1,
        user1Email);
    testCache1.clear();
    testCache1.shutdown();
    // setting up first cache
    UriCache<String, Object> testCache2 = UriCacheManager.getCache(dcStoragePath, sensorBaseHost2,
        user2Email);
    testCache2.clear();
    testCache2.shutdown();
    // setting up first cache
    UriCache<String, Object> testCache3 = UriCacheManager.getCache(dcStoragePath, sensorBaseHost3,
        user3Email);
    testCache3.clear();
    testCache3.shutdown();
  }

}
