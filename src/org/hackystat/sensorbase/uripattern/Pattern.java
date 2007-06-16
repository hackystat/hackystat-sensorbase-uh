package org.hackystat.sensorbase.uripattern;

/**
 * Pattern.
 * 
 * @author (Cedric) Qin ZHANG
 */
interface Pattern {

  /**
   * Tests whether the pattern matches a file path.
   * 
   * @param filePath The file path to match.
   * @return True if there is a match.
   */
  boolean matches(String filePath);
}
