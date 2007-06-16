package org.hackystat.sensorbase.uripattern;

/**
 * And logic operator for two patterns.
 * 
 * @author (Cedric) Qin ZHANG
 */
class AndOperator implements Operator {
  
  /**
   * Gets the arity of this operator.
   * 
   * @return The arity.
   */
  public int getArity() {
    return 2;
  }

  /**
   * Perform operator operation.
   * 
   * @param patterns The operand.
   * @param filePath The file path to match.
   * 
   * @return True if there is a match.
   */
  public boolean matches(Pattern[] patterns, String filePath) {
    if (patterns.length != 2) {
      throw new RuntimeException("AND operator expects exactly 1 operand.");
    }
    return patterns[0].matches(filePath) && patterns[1].matches(filePath);
  }
}
