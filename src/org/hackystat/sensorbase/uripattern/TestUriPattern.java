package org.hackystat.sensorbase.uripattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


/**
 * Test suite for UriPatterns.
 * 
 * @author Philip Johnson, (Cedric) Qin Zhang
 */
public class TestUriPattern  {
  
  private String class1 = "file://C:/dir/Class1.java";
  private String starStarJava = "**/*.java";
  private String beanShell = "/home/jira/beanshell";
  

  /**
   * Tests match of compound pattern.
   */
  @Test
  public void testCompoundPattern() {
    UriPattern p = new UriPattern("(**) - (file://**/Test*)");
    assertTrue("01", p.matches("file://C:/Class1.java"));
    assertFalse("02", p.matches("file://C:/dir/TestClass1.java"));
    
    p = new UriPattern(" (**) -(**/Test*) ");
    assertTrue("03", p.matches(class1));
    assertFalse("04", p.matches("file://C:/dir/TestClass1.java"));

    p = new UriPattern(" (**/*.java) + (**/*.c) ");
    assertTrue("05", p.matches(class1));
    assertTrue("06", p.matches("file://C:/dir/Class1.c"));
    assertFalse("07", p.matches("file://C:/dir/Class1.perl"));
 
    p = new UriPattern(" (**/*.java) + (**/*.c) - (**/Test*.java)");
    assertTrue("08", p.matches(class1));
    assertTrue("09", p.matches("file://C:/dir/Class1.c"));
    assertFalse("10", p.matches("file://C:/dir/Class1.perl"));  
    assertFalse("11", p.matches("file://C:/dir/TestClass1.java")); 

    p = new UriPattern(" (**) - (**/*.c) - (**/*.java)");
    assertFalse("12", p.matches(class1));
    assertFalse("13", p.matches("file://C:/dir/Class1.c"));
    assertTrue("14", p.matches("file://C:/dir/Class1.perl"));  
  }
  
  
 
  /**
   * Test case for other functions in the class.
   */
  @Test
  public void testOthers() {
    UriPattern pattern1a = new UriPattern(starStarJava);
    UriPattern pattern1b = new UriPattern(starStarJava);
    
    UriPattern pattern0 = new UriPattern("*.c");
    
    assertEquals("15", pattern1a, pattern1b);
    assertFalse("16", pattern1a.equals(pattern0));
    assertFalse("17", pattern1b.equals(pattern0));
    
    assertEquals("18", pattern1a.hashCode(), pattern1b.hashCode());
    
    assertTrue("19", pattern1a.compareTo(pattern1b) == 0);
    assertTrue("20", pattern1a.compareTo(pattern0) != 0);
    assertTrue("21", pattern1b.compareTo(pattern0) != 0);
    
    assertEquals("22", new UriPattern("**"), new UriPattern(null));
  }
  
  /**
   * Tests matching of file names starting with backslash.
   */
  @Test
  public void testIakyzoIssueData() {
    UriPattern p = new UriPattern("/home/**");
    assertTrue("23", p.matches(beanShell));
    p = new UriPattern("/**/jira/**");
    assertTrue("24", p.matches(beanShell));
    //This is where things fails in PatternMatcherImpl, fix it in PatternMatcher.
    p = new UriPattern("**");
    assertTrue("25", p.matches(beanShell));
    p = new UriPattern("**/jira/**");
    assertTrue("26", p.matches(beanShell));    
  }
  
  /**
   * Tests equals operation. 
   */
  @Test
  public void testEquals() {
    UriPattern p1 = new UriPattern(starStarJava);
    UriPattern p2 = new UriPattern(starStarJava);
    assertEquals("27", p1, p2);
    assertEquals("28", 0, p1.compareTo(p2));
    assertEquals("29", 0, p2.compareTo(p1));
    assertEquals("30", p1.hashCode(), p2.hashCode());
    
    p1 = new UriPattern("**");
    p2 = new UriPattern(null);
    assertEquals("31", p1, p2);
    assertEquals("32", 0, p1.compareTo(p2));
    assertEquals("33", 0, p2.compareTo(p1));
    assertEquals("34", p1.hashCode(), p2.hashCode());    

    p1 = new UriPattern("dir1/dir2/File.java");
    p2 = new UriPattern("dir1/dir2/File.java");
    assertEquals("35", p1, p2);
    assertEquals("36", 0, p1.compareTo(p2));
    assertEquals("37", 0, p2.compareTo(p1));
    assertEquals("38", p1.hashCode(), p2.hashCode()); 
  }
  
  /**
   * Tests equals(), comparesTo(), and hashCode() implementation.
   */
  @Test
  public void testEqualsAndComparison() {
    String[] patterns = new String[]{
        starStarJava, starStarJava,
        "**",
        starStarJava, "/home/A.java", starStarJava, "A.java"
    };
    for (int i = 0; i < patterns.length; i++) {
      for (int j = 0; j < patterns.length; j++) {
        String s1 = patterns[i];
        String s2 = patterns[j];
        UriPattern p1 = new UriPattern(s1);
        UriPattern p2 = new UriPattern(s2);
        //System.out.println(s1 + ",  " + s2);
        //test equals
        if (s1.equals(s2)) {
          assertEquals("39", p1, p2);
          assertEquals("40", p2, p1);
          assertEquals("41", p1.hashCode(), p2.hashCode());
          assertEquals("42", 0, p1.compareTo(p2));
          assertEquals("43", 0, p2.compareTo(p1));
        }
        //test comparesTo
        int result = p1.compareTo(p2);
        if (result == 0) {
          assertEquals("44", p1, p2);
          assertEquals("45", p2, p1);          
          assertEquals("46", 0, p2.compareTo(p1));
          assertEquals("47", p1.hashCode(), p2.hashCode());
        }
        else if (result > 0) {
          assertFalse("48", p1.equals(p2));
          assertFalse("49", p2.equals(p1));
          assertTrue("50", p2.compareTo(p1) < 0);
        }
        else {
          assertFalse("51", p1.equals(p2));
          assertFalse("52", p2.equals(p1));
          assertTrue("53", p2.compareTo(p1) > 0);           
        }
      }
    }
  }
  
  /**
   * Tests the optimized match() method for workspaces that detects "top-level" UriPatterns
   * and determines whether they match the workspace or not without going to the underlying
   * Ant-based parser.  This results in significantly improved performance in certain
   * DailyProjectData subclasses such as DailyProjectFileMetric.
   */
  @Test
  public void testWorkspaceOptimizationHack() {
    UriPattern UriPattern1 = new UriPattern("hackyFoo/**");
    UriPattern UriPattern2 = new UriPattern("/foo/**");
    UriPattern UriPattern3 = new UriPattern("hackyFoo/**/foo.java");
    UriPattern UriPattern4 = new UriPattern("*/**");
    UriPattern UriPattern5 = new UriPattern("**");
    assertTrue("Testing isTopLevel hackyFoo/**", UriPattern1.isTopLevel());
    assertTrue("Testing isTopLevel /foo/**", UriPattern2.isTopLevel());
    assertFalse("Testing isTopLevel hackyFoo/foo", UriPattern3.isTopLevel());
    assertFalse("Testing isTopLevel */**", UriPattern4.isTopLevel());
    assertFalse("Testing isTopLevel **", UriPattern5.isTopLevel());
    
    // Check that our top-level UriPattern does the right thing.
    assertTrue("Testing matches2() 1", UriPattern1.matches("hackyFoo/bar/baz"));
    assertTrue("Testing matches2() 2", UriPattern1.matches("hackyFoo/bar/baz"));
    assertTrue("Testing matches2() 3", UriPattern1.matches("hackyFoo"));
    assertFalse("Testing ~matches2() 4", UriPattern1.matches("hackyFood/bar/baz"));
    assertFalse("Testing ~matches2() 5", UriPattern1.matches("hacky"));
    assertFalse("Testing ~matches2() 6", UriPattern1.matches("bar"));
    
    // Now check that a non-top-level UriPattern also does the right thing.
    assertTrue("Testing matches2() 6", UriPattern2.matches("/foo"));
    assertTrue("Testing matches2() 7", UriPattern3.matches("hackyFoo/bar/foo.java"));
    UriPattern UriPattern6 = new UriPattern("hackyApp_TelemetryControlCenter/**");
    assertFalse("Testing field error", 
        UriPattern6.matches("hackySdt_Cli/src/overview.html/"));
  }
}
