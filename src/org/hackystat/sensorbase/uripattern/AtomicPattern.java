package org.hackystat.sensorbase.uripattern;

/**
 * Atomic pattern.
 * 
 * @author (Cedric) Qin ZHANG
 */
class AtomicPattern implements Pattern {

  private String pattern;
  
  /**
   * Constructor.
   * 
   * @param pattern The pattern string.
   */
  AtomicPattern(String pattern) {
    this.pattern = pattern;
  }
  
  /**
   * Tests whether the pattern matches a file path.
   * 
   * @param filePath The file path to match.
   * @return True if there is a match.
   */
  public boolean matches(String filePath) {
    if (this.pattern == null || "**".equals(this.pattern)) {
      return true;
    }
    else {
      return PatternMatcher.matchesFilePath(this.pattern, filePath, true);
    }
  }
}
