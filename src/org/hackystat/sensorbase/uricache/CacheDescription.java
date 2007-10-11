package org.hackystat.sensorbase.uricache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Encapsulates the UriCache description for sensorbase use.
 * 
 * @author Pavel Senin.
 * 
 */
public class CacheDescription {

  /** The properties provided for the construction. */
  private Properties properties;
  /** The user email key. */
  public static final String USER_EMAIL_KEY = "uricache.user.email";
  /** User e-mail */
  private String userEmail;
  /** The host key. */
  public static final String HOST_KEY = "uricache.host";
  /** User host key. */
  private String sensorBaseHost;
  /** The actual cache name (filename) */
  private String cacheName;

  /**
   * Builds new CacheDescription instance from the file provided.
   * 
   * @param f the cache description file.
   * @throws IOException if unable to open or parse the file.
   */
  public CacheDescription(File f) throws IOException {
    this.properties = new Properties();
    FileInputStream stream = null;
    stream = new FileInputStream(f.getAbsolutePath());
    this.properties.load(stream);
    this.userEmail = this.properties.getProperty(USER_EMAIL_KEY);
    this.sensorBaseHost = this.properties.getProperty(HOST_KEY);
    this.cacheName = f.getName().substring(0, f.getName().indexOf(".desc"));
    stream.close();
  }

  /**
   * Reports sensorbase host for this cache.
   * 
   * @return sensorbase host.
   */
  public String getsensorBaseHost() {
    return this.sensorBaseHost;
  }

  /**
   * Reports user e-mail.
   * 
   * @return user e-mail.
   */
  public String getUserEmail() {
    return this.userEmail;
  }

  /**
   * Reports the cache name.
   * 
   * @return the cache name.
   */
  public String getName() {
    return this.cacheName;
  }

}
