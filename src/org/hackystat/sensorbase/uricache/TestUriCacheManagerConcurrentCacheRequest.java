package org.hackystat.sensorbase.uricache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.hackystat.utilities.uricache.UriCache;
import org.hackystat.utilities.uricache.UriCacheException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests CacheManager functionality in the sense of the concurrent access for the cache data.
 * 
 * @author Pavel Senin.
 * 
 */
public class TestUriCacheManagerConcurrentCacheRequest {

  private static final String fileSeparator = System.getProperty("file.separator");

  /** Used for temporarily caches home */
  private static final String tmpFolderName = String.valueOf(System.currentTimeMillis());

  /** The general storage place. */
  private static final String cacheHome = System.getProperties().getProperty("user.dir")
      + fileSeparator + "build" + fileSeparator + "uricache-tests" + fileSeparator + tmpFolderName;

  private static final String userEmail = "javadude@javatesthost.org";
  private static final String sensorBaseHost = "http://sensorbase.javatesthost.org:20910";

  private static final String key = "key:";
  private static final String data = "data:";

  /**
   * Before each test we are setting up one cache instance, putting put some data in and shutting in
   * down.
   * 
   * @throws Exception if unable to proceed.
   */
  @Before
  public void setUp() throws Exception {
    // setting up first cache
    File f = new File(cacheHome);
    if (!f.exists()) {
      f.mkdirs();
    }
    UriCache<String, Object> testCache = UriCacheManager.getCache(cacheHome, sensorBaseHost,
        userEmail);
    testCache.clear();
    for (int i = 0; i < 500; i++) {
      testCache.cache(key + i, data + i);
    }
    testCache.shutdown();
  }

  /**
   * Now we are getting back the only instance of cache from the storage and trying to create
   * another instance of the UriCache for the same user email and host.
   * 
   * @throws UriCacheException in the case of error.
   * @throws IOException if IO error occurs.
   * @throws InterruptedException if unable to sleep in thread.
   */
  @Test
  public void testSecondInstance() throws UriCacheException, IOException, InterruptedException {

    // get cache #1 at test it's functionality
    UriCache<String, Object> cache1 = UriCacheManager
        .getCache(cacheHome, sensorBaseHost, userEmail);
    for (int i = 0; i < 500; i++) {
      String element1 = (String) cache1.lookup(key + i);
      assertNotNull("Should have recevied an element. " + i, element1);
      assertEquals("Element is wrong.", data + i, element1);
    }

    // try to get the cache for the same host and e-mail, should get a cache instance
    try {
      UriCache<String, Object> cache2 = UriCacheManager.getCache(cacheHome, sensorBaseHost,
          userEmail);
      cache2.clear();
      for (int i = 0; i < 500; i++) {
        cache2.cache(key + i, data + i);
      }
      cache2.shutdown();
      Thread.yield();
      Thread.sleep(1000);
      Thread.yield();
      cache2 = UriCacheManager.getCache(cacheHome, sensorBaseHost, userEmail);
      for (int i = 0; i < 500; i++) {
        String element1 = (String) cache1.lookup(key + i);
        assertNotNull("Should have recevied an element. " + i, element1);
        assertEquals("Element is wrong.", data + i, element1);
      }
      cache2.shutdown();
    }
    catch (UriCacheException e) {
      fail("Should get cache instance in here.");
    }
    cache1.shutdown();
  }

  /**
   * Now we are getting back the only instance of cache from the storage, creating four new
   * instances, shutting down three of them and testing obtaining if the very last cache and
   * cleanup.
   * 
   * @throws UriCacheException in the case of error.
   * @throws IOException if IO error occurs.
   * @throws InterruptedException if unable to sleep in thread.
   * 
   */
  @Test
  public void testSecondInstanceAndCleanUp() throws UriCacheException, IOException,
      InterruptedException {

    // get cache #1 at test it's functionality
    UriCache<String, Object> cache1 = UriCacheManager
        .getCache(cacheHome, sensorBaseHost, userEmail);
    for (int i = 0; i < 500; i++) {
      String element1 = (String) cache1.lookup(key + i);
      assertNotNull("Should have recevied an element..." + i, element1);
      assertEquals("Element is wrong...", data + i, element1);
    }

    // getting another 4 instances for the same user and base
    UriCache<String, Object> cache2 = UriCacheManager
        .getCache(cacheHome, sensorBaseHost, userEmail);
    for (int i = 500; i < 1000; i++) {
      cache2.cache(key + i, data + i);
    }

    UriCache<String, Object> cache3 = UriCacheManager
        .getCache(cacheHome, sensorBaseHost, userEmail);
    for (int i = 1000; i < 1500; i++) {
      cache3.cache(key + i, data + i);
    }

    UriCache<String, Object> cache4 = UriCacheManager
        .getCache(cacheHome, sensorBaseHost, userEmail);
    for (int i = 1500; i < 2000; i++) {
      cache4.cache(key + i, data + i);
    }

    UriCache<String, Object> cache5 = UriCacheManager
        .getCache(cacheHome, sensorBaseHost, userEmail);
    for (int i = 2000; i < 2500; i++) {
      cache5.cache(key + i, data + i);
    }

    // close cache 2,3 and 4
    cache2.shutdown();
    cache3.shutdown();
    cache4.shutdown();

    // now try to get the cache - should get cache 4 and delete caches 2 and 3
    try {
      UriCache<String, Object> cache6 = UriCacheManager.getCache(cacheHome, sensorBaseHost,
          userEmail);
      for (int i = 1500; i < 2000; i++) {
        String element1 = (String) cache6.lookup(key + i);
        assertNotNull("Should have recevied an element.." + i, element1);
        assertEquals("Element is wrong..", data + i, element1);
      }
      cache6.shutdown();
    }
    catch (UriCacheException e) {
      fail("Should get cache instance in here.");
    }
    cache1.shutdown();
    cache5.shutdown();
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
