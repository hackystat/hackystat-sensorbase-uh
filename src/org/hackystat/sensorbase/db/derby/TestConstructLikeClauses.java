package org.hackystat.sensorbase.db.derby;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;


/**
 * Tests the constructUriPattern method, which does a non-trivial transformation of the UriPatterns
 * into LIKE clauses.  This also helps document the behavior of the transformation by illustrating
 * canonical input-output pairs.
 *  
 * @author Philip Johnson
 */
public class TestConstructLikeClauses {

  /**
   * Tests that the LIKE clauses are constructed correctly.
   */ 
  @Test
  public void testUriPatterns() {
    //Test special cases.
    assertEquals("Test null", "", DerbyImplementation.constructLikeClauses(null));
    List<String> patts = new ArrayList<String>();
    patts.add("*");
    assertEquals("Test *", "", DerbyImplementation.constructLikeClauses(patts));
    patts.set(0, "**");
    assertEquals("Test **", "", DerbyImplementation.constructLikeClauses(patts));
    // Note that we need to escape occurrences of '\' character below.
    patts.set(0, "*.java");
    assertEquals("Test single pattern, no path separator", 
        " AND ((RESOURCE LIKE '%.java' ESCAPE '`') )", 
        DerbyImplementation.constructLikeClauses(patts));

    // Same return value, whether */foo/* or *\foo\*.
    String singleSlash = 
      " AND ((RESOURCE LIKE '%/foo/%' ESCAPE '`') OR (RESOURCE LIKE '%\\foo\\%' ESCAPE '`') )";
    patts.set(0, "*/foo/*");
    assertEquals("Test single pattern, forward slash", singleSlash, 
        DerbyImplementation.constructLikeClauses(patts));

    patts.set(0, "*\\foo\\*");
    assertEquals("Test single pattern, backward slash", singleSlash, 
        DerbyImplementation.constructLikeClauses(patts));
    
    // Test escape of an SQL wildcard.
    patts.set(0, "foo_bar");
    assertEquals("Test single pattern, wildcard", " AND ((RESOURCE LIKE 'foo`_bar' ESCAPE '`') )",
        DerbyImplementation.constructLikeClauses(patts));
    
    // Test multiple URIs, path separators, and escapes.
    patts.set(0, "*/foo_bar/*");
    patts.add("*\\Test*.java");
    patts.add("baz.c");
    
    String results = 
      " AND ((RESOURCE LIKE '%/foo`_bar/%' ESCAPE '`') OR " + 
      "(RESOURCE LIKE '%\\foo`_bar\\%' ESCAPE '`') OR " + 
      "(RESOURCE LIKE '%/Test%.java' ESCAPE '`') OR " + 
      "(RESOURCE LIKE '%\\Test%.java' ESCAPE '`') OR " + 
      "(RESOURCE LIKE 'baz.c' ESCAPE '`') )";

    //System.out.println(DerbyImplementation.constructLikeClauses(patts));

    assertEquals("Test multiple URIs, path separators, escapes", results, 
        DerbyImplementation.constructLikeClauses(patts));
  }
}
