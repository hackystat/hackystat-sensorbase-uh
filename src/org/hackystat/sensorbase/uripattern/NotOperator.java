package org.hackystat.sensorbase.uripattern;

/**
 * Not logic operator for two patterns.
 * 
 * @author (Cedric) Qin ZHANG
 */
class NotOperator implements Operator {

  /**
   * Gets the arity of this operator.
   * 
   * @return The arity.
   */
  public int getArity() {
    return 1;
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
    if (patterns.length != 1) {
      throw new RuntimeException("NOT operator expects exactly 1 operand.");
    }
    return ! patterns[0].matches(filePath);
  }

}
