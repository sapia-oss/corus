package org.sapia.corus.client.common;

/**
 * A generator of identifiers for commands and requests. The generator will
 * create numeric identifiers up to a value of 999999; then the internal counter
 * will be reset.
 * 
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 */
public class CyclicIdGenerator {

  public static final int MAX_ID = 999999;

  private static int commandCount = 0;
  private static int requestCount = 0;

  /**
   * Generates the next command identifier.
   * 
   * @return The next command identifier.
   */
  public static synchronized String newCommandId() {
    if (commandCount >= MAX_ID) {
      commandCount = 0;
    }

    return String.valueOf(++commandCount);
  }

  /**
   * Generates the next request identifier.
   * 
   * @return The next request identifier.
   */
  public static synchronized String newRequestId() {
    if (requestCount >= MAX_ID) {
      requestCount = 0;
    }

    return String.valueOf(++requestCount);
  }
}
