package org.hackystat.sensorbase.uripattern;


/**
 * A utility class which does pattern matches.
 *
 * @author Qin Zhang
 */
public class PatternMatcher {

  /**
   * Determines whether a file name matches a pattern. The pattern rule follows ant file set
   * selections rules that is used in ant. <p>
   * Some systems use slash, while others use back-slash as path separator. This function will
   * not distinguish them (effectively,they are treated as the same character). <p>
   * If a pattern ends with a path separator, then '**' will be append to it before matching.
   * (i.e. if pattern is 'src/', then it will be treated as 'src/**'.
   *
   * @param pattern The pattern. If null, then the method will always return true.
   * @param fileName The file name to be tested against the pattern.
   * @param isCaseSensitive Whether fileName is case sensitive.
   *
   * @return True if the file name matches the pattern.
   */
  static boolean matchesFilePath(String pattern, String fileName, boolean isCaseSensitive) {
    //PatternMatcher does not work well with file name starts with "/"
    //e.g. PatternMatcherImpl.matchPath("**", "/d/d/file", ture) returns false.
    //everything's ok if path does not start with a slash.
    //Fix: get rid of the starting "/" in fileName, if pattern starts with **
    if (pattern == null) {
      return true;
    }
    else {
      
//      pattern = pattern.replace('/', File.separatorChar).replace('\\', File.separatorChar);
//      fileName = fileName.replace('/', File.separatorChar).replace('\\', File.separatorChar);
//      if (pattern.endsWith(File.separator)) {
//        pattern += "**";
//      }
//      //our fix.
//      if (pattern.startsWith("**") && fileName.length() > 0 
//          && fileName.charAt(0) == File.separatorChar) {
//        fileName = fileName.substring(1);
//      }
      return PatternMatcherImpl.matchPath(pattern, fileName, isCaseSensitive);
    }
  }

  /**
   * Tests whether a string matches a pattern or not. Wild characters are:
   * <ul>
   *   <li>'*': zero or more characters. </li>
   *   <li>'?': one and only one character.</li>
   * </ul>
   * <p>
   * Note that this method is NOT designed to match file path, use 
   * <code>matchesFilePath</code> instead.
   * 
   * @param pattern The pattern. If null, the function always returns true.
   * @param str The string to be matched against the pattern.
   * @param caseSensitive Whether the matches is case sensitive or not.
   *
   * @return True if string matches the pattern.
   */
  public static boolean matchesPattern(String pattern, String str, boolean caseSensitive) {
    if (pattern == null) {
      return true;
    }
    else {
      return PatternMatcherImpl.match(pattern, str, caseSensitive);
    }
  }
}
