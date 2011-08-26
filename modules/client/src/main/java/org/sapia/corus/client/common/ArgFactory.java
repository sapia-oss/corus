package org.sapia.corus.client.common;


/**
 * This class parses a command argument, returning the object
 * representations thereof.
 * <p>
 * It also provides static methods for creating {@link Arg}s of 
 * given types.
 * 
 * @author yduchesne
 *
 */
public class ArgFactory {
  
  public static final String PATTERN = "*";
  
  
  /**
   * @param token an arbitrary string, that can also
   * represent a pattern.
   * @return the corresponding {@link Arg} object.
   */
  public static Arg parse(String token){
    if(token == null){
      return new PatternArg(PATTERN);
    }
    else if(isPattern(token)){
      return new PatternArg(token);
    }
    else{
      return new StringArg(token);
    }
  } 
  
  /**
   * @return an {@link Arg} that matches any string.
   */
  public static Arg any(){
    return new PatternArg(PATTERN);
  }

  /**
   * @param str a {@link String}
   * @return an {@link Arg} that exactly matches the given string.
   */
  public static Arg exact(String str){
    return new StringArg(str);
  }  

  /**
   * @param token an arbitrary {@link String}
   * @return <code>true</code> of the given token corresponds to a pattern.
   */
  public static boolean isPattern(String token){
    return (token.indexOf(PATTERN) >= 0);  
  }
  
  /**
   * @param arg an {@link Arg} instance.
   * @return the passed in {@link Arg} if it's not null, or an {@link Arg} instance that will
   * match any character string if it is.
   */
  public static Arg anyIfNull(Arg arg){
    return arg == null ? any() : arg; 
  }

}
