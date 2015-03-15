package org.sapia.corus.client.common;

/**
 * This class parses a command argument, returning the object representations
 * thereof.
 * <p>
 * It also provides static methods for creating {@link ArgMatcher}s of given types.
 * 
 * @author yduchesne
 * 
 */
public class ArgMatchers {

  public static final String PATTERN = "*";

  /**
   * @param token
   *          an arbitrary string, that can also represent a pattern.
   * @return the corresponding {@link ArgMatcher} object.
   */
  public static ArgMatcher parse(String token) {
    if (token == null) {
      return new PatternArgMatcher(PATTERN);
    } else if (isPattern(token)) {
      return new PatternArgMatcher(token);
    } else {
      return new StringArg(token);
    }
  }

  /**
   * @return an {@link ArgMatcher} that matches any string.
   */
  public static ArgMatcher any() {
    return new PatternArgMatcher(PATTERN);
  }

  /**
   * @param str
   *          a {@link String}
   * @return an {@link ArgMatcher} that exactly matches the given string.
   */
  public static ArgMatcher exact(String str) {
    return new StringArg(str);
  }

  /**
   * @param token
   *          an arbitrary {@link String}
   * @return <code>true</code> of the given token corresponds to a pattern.
   */
  public static boolean isPattern(String token) {
    return (token.indexOf(PATTERN) >= 0);
  }

  /**
   * @param arg
   *          an {@link ArgMatcher} instance.
   * @return the passed in {@link ArgMatcher} if it's not null, or an {@link ArgMatcher}
   *         instance that will match any character string if it is.
   */
  public static ArgMatcher anyIfNull(ArgMatcher arg) {
    return arg == null ? any() : arg;
  }

}
