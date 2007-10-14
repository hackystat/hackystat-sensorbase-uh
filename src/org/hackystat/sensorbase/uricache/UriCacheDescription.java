package org.hackystat.sensorbase.uricache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Encapsulates the UriCache description for sensorbase use.
 * 
 * @author Pavel Senin.
 * 
 */
public class UriCacheDescription {

  /** The user email key. */
  public static final String CREATION_TIME_KEY = "uricache.time.creation";

  /** System deafult file separator. */
  private static final String fileSeparator = System.getProperty("file.separator");

  /** The host key. */
  public static final String HOST_KEY = "uricache.host";

  /** The user email key. */
  public static final String USER_EMAIL_KEY = "uricache.user.email";

  /** The actual cache name (filename) */
  private String cacheName;

  /** The properties provided for the construction. */
  private Properties properties;

  /**
   * Instantiates CacheDescription by loading properties from the file provided.
   * 
   * @param f the cache description file.
   * @throws IOException if unable to open or parse the file.
   */
  public UriCacheDescription(File f) throws IOException {
    this.properties = new Properties();
    FileInputStream stream = null;
    stream = new FileInputStream(f.getAbsolutePath());
    this.properties.load(stream);
    this.cacheName = f.getName().substring(0, f.getName().indexOf(".desc"));
    stream.close();
  }

  /**
   * Instantiates CacheDescription by setting properties as provided.
   * 
   * @param cacheNameBase specifies cache name, the timestamp will be added to this base .
   * @param sensorBaseHost specifies sensorbase host.
   * @param userEmail specifies user e-mail used for authorization.
   */
  public UriCacheDescription(String cacheNameBase, String sensorBaseHost, String userEmail) {
    this.properties = new Properties();
    Long creationTime = System.currentTimeMillis();
    this.cacheName = cacheNameBase + creationTime;
    this.properties.setProperty(HOST_KEY, sensorBaseHost);
    this.properties.setProperty(USER_EMAIL_KEY, userEmail);
    this.properties.setProperty(CREATION_TIME_KEY, creationTime.toString());
  }

  /**
   * Reports the cache creation time.
   * 
   * @return the cache name.
   */
  public Long getCreationTime() {
    return Long.valueOf(this.properties.getProperty(CREATION_TIME_KEY));
  }

  /**
   * Reports the cache name.
   * 
   * @return the cache name.
   */
  public String getName() {
    return this.cacheName;
  }

  /**
   * Reports sensorbase host for this cache.
   * 
   * @return sensorbase host.
   */
  public String getsensorBaseHost() {
    return this.properties.getProperty(HOST_KEY);
  }

  /**
   * Reports user e-mail.
   * 
   * @return user e-mail.
   */
  public String getUserEmail() {
    return this.properties.getProperty(USER_EMAIL_KEY);
  }

  /**
   * Saves the properties in the path provided.
   * 
   * @param path path to the folder where description must be saved "cache home".
   * @throws IOException when IO error encountered.
   */
  public void save(String path) throws IOException {
    FileOutputStream stream = new FileOutputStream(path + fileSeparator + this.cacheName + ".desc");
    this.properties.store(stream, "The UriCache properties file");
    stream.close();
  }
}
