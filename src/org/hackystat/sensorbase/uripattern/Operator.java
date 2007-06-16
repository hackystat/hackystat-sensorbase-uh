package org.hackystat.sensorbase.uripattern;

/**
 * Logic operator for two patterns.
 * 
 * @author (Cedric) Qin ZHANG
 */
interface Operator {

  /** And operator. */
  static Operator AND = new AndOperator();
  /** Or Operator. */
  static Operator OR = new OrOperator();
  /** Not operator. */
  static Operator NOT = new NotOperator();
  
  /**
   * Gets the arity of this operator.
   * 
   * @return The arity.
   */
  int getArity();
  
  /**
   * Perform operator operation.
   * 
   * @param patterns The operand.
   * @param filePath The file path to match.
   * 
   * @return True if there is a match.
   */
  boolean matches(Pattern[] patterns, String filePath);
}
