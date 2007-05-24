package org.hackystat.sensorbase.resource.users;

import java.util.Random;

/**
 * Provides a static function for generating new, unique user keys. A user key is a
 * 12 character String that is used for identifying the user and also for naming the directory in
 * which their data resides. 
 * Package private because only UserManager should be interacting with this class.
 *  
 * @author    Philip M. Johnson
 */
class UserKeyGenerator {
  /**
   * Eliminate letters/numbers that are easily confused, such as 0, O, l, 1, I, etc.
   * Do NOT include the '@' char, because that is used to distinguish dirkeys from email addresses.
   */
  private static String[] charset = {
    "A", "B", "C", "D", "E", "F", "G",
    "H", "J", "K", "L", "M", "N",
    "P", "Q", "R", "S", "T", "U",
    "V", "W", "X", "Y", "Z",
    "a", "b", "c", "d", "e", "f", "g",
    "h", "i", "j", "k", "m", "n", "p",
    "q", "r", "s", "t", "u", "v", "w",
    "x", "y", "z",
    "2", "3", "4", "5", "6", "7", "8", "9"};
    
  /** Length of the user key. */
  private static final int USERKEY_LENGTH = 12;
  
  /**
    * Creates and returns a new unique, randomly generated user key.  
    *
    * @param manager the User Manager. 
    * @return  The random string.
    */
   public static String make(UserManager manager) {
     Random generator = new Random(System.currentTimeMillis());
     StringBuffer userKey = null;
     boolean isUniqueKey = false;
     while (!isUniqueKey) {
       userKey = new StringBuffer(UserKeyGenerator.USERKEY_LENGTH);
       for (int i = 0; i < UserKeyGenerator.USERKEY_LENGTH; i++) {
         userKey.append(charset[generator.nextInt(charset.length)]);
       }
       isUniqueKey = !manager.isUserKey(userKey.toString());
     }
     return userKey.toString();
   }
   
   /** Ensure that no one can create an instance of this class. */
   private UserKeyGenerator() {
   }

}
