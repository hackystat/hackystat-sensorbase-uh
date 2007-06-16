package org.hackystat.sensorbase.uripattern;

/**
 * Compound pattern.
 * 
 * @author (Cedric) Qin ZHANG
 */
class CompoundPattern implements Pattern {
  
  private Operator operator;
  private Pattern[] patterns;
  
  /**
   * Creates a new compound pattern.
   * 
   * @param operator The logic operator.
   * @param operands The operands.
   */
  CompoundPattern(Operator operator, Pattern[] operands) {
    if (operator.getArity() != operands.length) {
      throw new RuntimeException("Operator arity does not match the number of operands.");
    }
    this.operator = operator;
    this.patterns = operands;
  }
  
  /**
   * Tests whether the pattern matches a file path.
   * 
   * @param filePath The file path to match.
   * @return True if there is a match.
   */
  public boolean matches(String filePath) {
    return this.operator.matches(this.patterns, filePath);
  }
}
