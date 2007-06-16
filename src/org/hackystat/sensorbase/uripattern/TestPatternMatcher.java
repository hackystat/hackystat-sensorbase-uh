package org.hackystat.sensorbase.uripattern;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Test suite for <code>PatternMatcher</code>.
 *
 * @author Qin Zhang
 * @version $Id: TestPatternMatcher.java,v 1.1.1.1 2005/10/20 23:56:40 johnson Exp $
 */
public class TestPatternMatcher {
  
  private String testStarJava = "**/*.java";
  private String testBJava = "a/TestB.java";
  private String src = "src/";
  private String starStarTestClass = "org.**.Test*Class";
  private String testClass = "org.src.hackystat.TestClass";
  private String testAClass = "org.src.hackystat.TestAClass";
  private String testABClass = "org.src.hackystat.TestABClass";
  private String testQuestionClass = "org.**.Test?Class";
  //private String testClass2 = starStarTestClass;
  //private String testClass3 = testQuestionClass;
  private String testStarJava2 = "**/Test*.java";
  

  /**
   * Tests <code>matchesFilePath()</code>.
   */
  @Test
  public void testMatchesFilePath() {
    //file names
    assertTrue("01", PatternMatcher.matchesFilePath(testStarJava, "c:/src/a/B.java", true));
    assertTrue("02", PatternMatcher.matchesFilePath(testStarJava, "B.java", true));
    assertTrue("03", PatternMatcher.matchesFilePath("**/a/**/*.java", "c:/src/a/B.java", true));
    assertTrue("04", 
        PatternMatcher.matchesFilePath("**/a/**/*.java", "c:/src/a/c/B.java", true));
    assertTrue("05", PatternMatcher.matchesFilePath("a/**/*.java", "a/d/e/B.java", true));
    assertTrue("06", PatternMatcher.matchesFilePath("a/**/*.java", "a/B.java", true));
    assertTrue("07", 
        PatternMatcher.matchesFilePath(testStarJava2, "c:/src/a/TestB.java", true));
    assertTrue("08", PatternMatcher.matchesFilePath(testStarJava2, testBJava, true));
    assertTrue("09", PatternMatcher.matchesFilePath(testStarJava2, "TestB.java", true));

    assertTrue("10", PatternMatcher.matchesFilePath(src, src, true));
    assertFalse("11", PatternMatcher.matchesFilePath(src, "src/a", true));
    assertFalse("13", PatternMatcher.matchesFilePath(src, "src/a/a", true));

    assertFalse("14", PatternMatcher.matchesFilePath("src/**/org/", "sRc/a/b/org", true));
    assertFalse("15", PatternMatcher.matchesFilePath("src/**/org/", "sRc/a/b/org/A.java", false));

    assertFalse("16", PatternMatcher.matchesFilePath(testStarJava2, "c:/Test/B.java", true));

    assertTrue("18", PatternMatcher.matchesFilePath(null, testBJava, false));
    assertTrue("19", PatternMatcher.matchesFilePath("**", testBJava, true));
    assertTrue("20", PatternMatcher.matchesFilePath("**", testBJava, false));
  }

  /**
   * Tests <code>matches()</code>.
   */
  @Test
  public void testMatches() {
    assertTrue("21", PatternMatcher.matchesFilePath("org.**.Test*", "org.hackystat.TestA", true));
    assertFalse("22", PatternMatcher.matchesFilePath("oRg.**.Test*", "org.hackystat.TestA", true));
    assertTrue("23", PatternMatcher.matchesFilePath("oRg.**.Test*", "org.hackystat.TestA", false));

    assertTrue("24", 
      PatternMatcher.matchesFilePath(starStarTestClass, testClass, true));
    assertTrue("25", 
      PatternMatcher.matchesFilePath(starStarTestClass, testAClass, true));
    assertTrue("26", 
      PatternMatcher.matchesFilePath(starStarTestClass, testABClass, true));
    assertTrue("27", 
      PatternMatcher.matchesFilePath(starStarTestClass, "org.src.hackystat.TestABCClass", true));
    assertTrue("28", 
      PatternMatcher.matchesFilePath(testQuestionClass, testAClass, true));

    assertFalse("29", 
      PatternMatcher.matchesFilePath(testQuestionClass, testClass, true));
    assertFalse("30", 
      PatternMatcher.matchesFilePath(testQuestionClass, testABClass, true));

    assertTrue("32", 
      PatternMatcher.matchesFilePath(starStarTestClass, testClass, false));
    assertTrue("33", 
      PatternMatcher.matchesFilePath(starStarTestClass, testAClass, false));
    assertTrue("34", 
      PatternMatcher.matchesFilePath(starStarTestClass, testABClass, false));
    assertTrue("35", 
      PatternMatcher.matchesFilePath(starStarTestClass, "org.src.hackystat.TestABCClass", false));
    assertTrue("36", 
      PatternMatcher.matchesFilePath(testQuestionClass, testAClass, false));

    assertFalse("37", 
      PatternMatcher.matchesFilePath(testQuestionClass, testClass, false));
    assertFalse("38", 
      PatternMatcher.matchesFilePath(testQuestionClass, testABClass, false));
    assertTrue("39", 
      PatternMatcher.matchesFilePath(testQuestionClass, testAClass, false));

    assertTrue("40", PatternMatcher.matchesFilePath("**.a.b.c**", "AAA.a.b.cBBB", true));
    assertTrue("41", PatternMatcher.matchesFilePath("**.a.b.c**", ".a.b.c", true));
    assertTrue("42", PatternMatcher.matchesFilePath("?.???.?", "a.?*b.c", true));

    assertTrue("43", PatternMatcher.matchesFilePath(null, "a.?*b.c", true));
    assertTrue("44", PatternMatcher.matchesFilePath(null, "a.?*b.c", false));
  }
}
