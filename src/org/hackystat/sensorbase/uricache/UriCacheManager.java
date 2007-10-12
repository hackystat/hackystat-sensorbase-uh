package org.hackystat.sensorbase.uricache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.hackystat.utilities.uricache.UriCache;
import org.hackystat.utilities.uricache.UriCacheException;
import org.hackystat.utilities.uricache.UriCacheProperties;

/**
 * Provides an interface to UriCache for the SensorBase. Currently it provides non-persistent
 * implementation. I.e. data will be wiped out once SensorBase is shutdown.
 * 
 * @author Pavel Senin
 * @since 10-07-2007.
 */
public class UriCacheManager {

  // /** SensorBase handler. */
  // private Server server;
  // /** Logger. */
  // private Logger logger;

  private static final String fileSeparator = System.getProperty("file.separator");

  private static String defaultCacheHome = System.getProperty("user.home") + fileSeparator
      + ".hackystat" + fileSeparator + "sensorbase" + fileSeparator + "cache";

  /** The user email key. */
  public static final String USER_EMAIL_KEY = "uricache.user.email";
  /** The host key. */
  public static final String HOST_KEY = "uricache.host";

  /**
   * Creates new UriCacheManager instance.
   * 
   */
  public UriCacheManager() {
    assert true;
  }

  /**
   * Creates the UriCache for the specified host and user.
   * 
   * @param storagePath caches storage path to search within.
   * @param sensorBaseHost the sensorbase host key.
   * @param userEmail the user email key.
   * @return new cache instance.
   * @throws UriCacheException in the case of error.
   * @throws IOException if unable create property file.
   */
  public static synchronized UriCache<String, Object> getCache(String storagePath,
      String sensorBaseHost, String userEmail) throws UriCacheException, IOException {

    // let's figure out where to look for the cache(s)
    String cacheHome = null;
    if (null == storagePath) {
      cacheHome = defaultCacheHome;
    }
    else {
      cacheHome = storagePath;
    }

    // now analyze if the path exists, if not we are going to create one
    File folder = new File(cacheHome);
    if (!folder.exists()) {
      folder.mkdirs();
    }

    // now we are going to locate cache
    UriCache<String, Object> tryCache = locateCache(cacheHome, sensorBaseHost, userEmail);

    // but if cache is not found we will create one
    if (null == tryCache) {
      String cacheName = "UriCache" + System.currentTimeMillis();
      Properties prop = new Properties();
      prop.setProperty(USER_EMAIL_KEY, userEmail);
      prop.setProperty(HOST_KEY, sensorBaseHost);
      FileOutputStream stream = null;
      stream = new FileOutputStream(cacheHome + fileSeparator + cacheName + ".desc");
      prop.store(stream, "the UriCache properties test file");
      stream.close();
      UriCacheProperties cacheProp = new UriCacheProperties();
      cacheProp.setCacheStoragePath(cacheHome);
      UriCache<String, Object> newCache = new UriCache<String, Object>(cacheName, cacheProp);
      newCache.clear();
      return newCache;
    }
    else {
      return tryCache;
    }
  }

  /**
   * Locates cache for host and user provided, if unable to find cache, reports null.
   * 
   * @param storagePath caches storage path to search within.
   * @param sensorBaseHost the sensorbase host key.
   * @param userEmail the user email key.
   * @return UriCache instance associated with sensorbase and user email keys provided or null if
   *         not found.
   * @throws UriCacheException if unable to operate with cache.
   */
  private static synchronized UriCache<String, Object> locateCache(String storagePath,
      String sensorBaseHost, String userEmail) throws UriCacheException {

    String cacheHome = null;
    if (null == storagePath) {
      cacheHome = defaultCacheHome;
    }
    else {
      cacheHome = storagePath;
    }

    List<UriCacheDescription> cachesList = getCaches(cacheHome);
    if (null != cachesList) {
      List<UriCacheDescription> candidatesList = new ArrayList<UriCacheDescription>();
      for (UriCacheDescription cd : cachesList) {
        if ((userEmail.equalsIgnoreCase(cd.getUserEmail()))
            && (sensorBaseHost.equalsIgnoreCase(cd.getsensorBaseHost()))) {
          candidatesList.add(cd);
        }
      }
      if (candidatesList.isEmpty()) {
        return null;
      }
      else {
        UriCacheProperties cacheProperties = new UriCacheProperties();
        cacheProperties.setCacheStoragePath(cacheHome);
        return new UriCache<String, Object>(candidatesList.get(0).getName(), cacheProperties);
      }
    }
    return null;
  }

  /**
   * Reports list of caches found in the cache home folder.
   * 
   * @param storagePath the cache home folder.
   * @return the set of caches found or null if didn't find anything.
   * @throws UriCacheException if unable to find cache home folder.
   */
  public static synchronized List<UriCacheDescription> getCaches(String storagePath)
      throws UriCacheException {

    String cacheHome = null;
    if (null == storagePath) {
      cacheHome = defaultCacheHome;
    }
    else {
      cacheHome = storagePath;
    }

    List<UriCacheDescription> response = new ArrayList<UriCacheDescription>();
    try {
      File dir = new File(cacheHome);

      if (dir.exists()) {
        File[] files = dir.listFiles(new UriCacheDescriptionFilter());
        for (File f : files) {
          response.add(new UriCacheDescription(f));
        }
      }
      else {
        throw new UriCacheException("Unable to find cache home at " + cacheHome);
      }

    }
    catch (IOException e) {
      e.printStackTrace();
    }
    if (response.isEmpty()) {
      return null;
    }
    else {
      return response;
    }
  }

}
