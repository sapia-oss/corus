package org.sapia.corus.admin;

/**
 * This class parses a command arguments, returning the object
 * representations thereof.
 * 
 * @author yduchesne
 *
 */
public class CommandArgParser {
  
  public static final String PATTERN = "*";
  
  
  /**
   * @param token a command token.
   * @return the corresponding <code>CommandArg</code> object.
   */
  public static CommandArg parse(String token){
    if(isPattern(token)){
      return new PatternCommandArg(token);
    }
    else{
      return new StringCommandArg(token);
    }
  } 
  
  public static CommandArg any(){
    return new PatternCommandArg(PATTERN);
  }
  
  public static CommandArg exact(String str){
    return new StringCommandArg(str);
  }  
  
  public static boolean isPattern(String token){
    return (token.indexOf(PATTERN) >= 0);  
  }

}
