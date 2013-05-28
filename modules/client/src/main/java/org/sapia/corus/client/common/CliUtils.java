package org.sapia.corus.client.common;

import org.sapia.corus.client.facade.CorusConnectionContext;

/**
 * Holds various command-line utility methods.
 * 
 * @author yduchesne
 *
 */
public final class CliUtils {
  
  private CliUtils() {
  }

  /**
   * 
   * @param corus the {@link CorusConnectionContext} instance for which to generate a command-line prompt.
   * @return a command-line prompt.
   */
  public static final String getPromptFor(CorusConnectionContext context) {
    StringBuffer prompt = new StringBuffer()
    .append("[")
    .append(context.getAddress().toString())
    .append("@")
    .append(context.getDomain())
    .append("]>> ");
    return prompt.toString();
  }
}
