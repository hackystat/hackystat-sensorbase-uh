package org.hackystat.sensorbase.uricache;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

  private static final String fileSeparator = System.getProperty("file.separator");

  private static final String defaultCacheName = "UriCache";

  private static String defaultCacheHome = System.getProperty("user.home") + fileSeparator
      + ".hackystat" + fileSeparator + "sensorbase" + fileSeparator + "cache";

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
  public static synchronized UriCache getCache(String storagePath,
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
    UriCache tryCache = locateCache(cacheHome, sensorBaseHost, userEmail);

    // but if cache is not found we will create one
    if (null == tryCache) {
      // create and save properties
      UriCacheDescription cacheDesc = new UriCacheDescription(defaultCacheName, sensorBaseHost,
          userEmail);
      cacheDesc.save(cacheHome);
      // create, save cache and return UriCache handler
      UriCacheProperties cacheProp = new UriCacheProperties();
      cacheProp.setCacheStoragePath(cacheHome);
      UriCache newCache = new UriCache(cacheDesc.getName(),
          cacheProp);
      newCache.clear();
      return newCache;
    }
    else {
      return tryCache;
    }
  }

  /**
   * Locates cache for host and user provided, if unable to find cache, reports null. Meanwhile
   * doing search cleans up old caches which are basically leftovers.
   * 
   * @param storagePath caches storage path to search within.
   * @param sensorBaseHost the sensorbase host key.
   * @param userEmail the user email key.
   * @return UriCache instance associated with sensorbase and user email keys provided or null if
   *         not found.
   * @throws UriCacheException if unable to operate with cache.
   */
  private static synchronized UriCache locateCache(String storagePath,
      String sensorBaseHost, String userEmail) throws UriCacheException {

    // getting cache home folder
    String cacheHome = null;
    if (null == storagePath) {
      cacheHome = defaultCacheHome;
    }
    else {
      cacheHome = storagePath;
    }

    // getting list of caches associated with the host and user email provided
    List<UriCacheDescription> cachesList = getCaches(cacheHome);
    List<UriCacheDescription> candidatesList = new ArrayList<UriCacheDescription>();
    if (null != cachesList) {
      for (UriCacheDescription cd : cachesList) {
        if ((userEmail.equalsIgnoreCase(cd.getUserEmail()))
            && (sensorBaseHost.equalsIgnoreCase(cd.getsensorBaseHost()))) {
          candidatesList.add(cd);
        }
      }
    }

    // if found nothing report it
    if (candidatesList.isEmpty()) {
      return null;
    }

    // ok, now let's pick the cache which is latest and not active
    Collections.sort(candidatesList, cacheDescriptionTimeComparator());
    Integer length = candidatesList.size();

    Integer position = null;
    UriCache cache = null;

    // We are going to iterate over caches from the latest file up to oldest one. The latest cache
    // which is not active will be returned from this method, all other inactive caches will be
    // deleted.
    for (int i = length - 1; i >= 0; i--) {
      UriCacheDescription desc = candidatesList.get(i);
      // check if this instance is available
      try {
        UriCacheProperties cacheProperties = new UriCacheProperties();
        cacheProperties.setCacheStoragePath(cacheHome);
        cache = new UriCache(desc.getName(), cacheProperties);
        // if we got this cache - break the loop and clean the rest of caches
        position = i;
        break;
      }
      catch (UriCacheException e) {
        String errorMessage = e.getMessage();
        if (errorMessage.contains("is in use")) {
          assert true;
        }
        else {
          throw new UriCacheException(e);
        }
      }
    }

    // here we suppose to have two variable to check the cache and position
    // basically we are going to return this cache instance, it's null if we didn't get cache
    // if we got a cache than we do a little clean-up and return cache anyway
    if ((null != position) && (position > 0)) {
      for (int i = position - 1; i >= 0; i--) {
        UriCacheDescription desc = candidatesList.get(i);
        try {
          UriCacheProperties cacheProperties = new UriCacheProperties();
          cacheProperties.setCacheStoragePath(cacheHome);
          UriCache tryCache = new UriCache(desc.getName(),
              cacheProperties);
          // if we were able to get this instance - this is leftover - wipe it
          tryCache.shutdown();
          String fileName = cacheHome + fileSeparator + desc.getName();
          File file2Delete = new File(fileName + ".data");
          file2Delete.delete();
          file2Delete = new File(fileName + ".key");
          file2Delete.delete();
          file2Delete = new File(fileName + ".desc");
          file2Delete.delete();
        }
        catch (UriCacheException e) {
          String errorMessage = e.getMessage();
          if (errorMessage.contains("is in use")) {
            assert true;
          }
          else {
            throw new UriCacheException(e);
          }
        }
      }
    }

    return cache;
  }

  /**
   * Reports a comparator instance for sorting descriptions.
   * 
   * @return a comparator instance for sorting descriptions.
   */
  private static Comparator<UriCacheDescription> cacheDescriptionTimeComparator() {
    return new UriCacheDescriptionTimeComparator();
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
