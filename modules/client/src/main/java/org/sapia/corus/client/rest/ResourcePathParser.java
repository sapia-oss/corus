package org.sapia.corus.client.rest;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that parses REST resource paths. Supports using '[' and ']' when
 * a path segment contains a '/', and that segment should be used as a parameter. For example:
 * <pre>
 *  /docker/images/[mini/memcached]
 * </pre>
 * In the case of the above path, the following 3 segments will be parsed:
 * <ol>
 *   <li>docker
 *   <li>images
 *   <li>mini/memcached.
 * </ol>
 * Note that the '[' and ']' characters are not preserved.
 * 
 * @author yduchesne
 *
 */
public class ResourcePathParser {
  
  private enum State {
    NORMAL, IN_BRACKETS;
  }
  
  private ResourcePathParser() {
  }

  public static String[] parse(String literal) {
    
    State state = State.NORMAL;
    
    List<String> parts = new ArrayList<String>();
    StringBuilder part = new StringBuilder();
    for (int i = 0; i < literal.length(); i++) {
      char c = literal.charAt(i);
      if (c == '/') {
        if (state == State.IN_BRACKETS) {
          part.append(c);
        } else {
          if (part.length() > 0) {
            parts.add(part.toString());
            part.delete(0,  part.length());
          }
        }
      } else if (c == '[') {
        if (state == State.IN_BRACKETS) {
          throw new IllegalArgumentException("Opening '[' cannot be followed by another (must be closed with ']' before) => " + literal);
        } else {
          state = State.IN_BRACKETS;
        }
      } else if (c == ']') {
        if (state == State.IN_BRACKETS) {
          if (part.length() > 0) {
            parts.add(part.toString());
            part.delete(0,  part.length());
          }
          state = State.NORMAL;
        } else {
          throw new IllegalArgumentException("Closing ']' must preceded by '[' => " + literal);
        }
      } else {
        part.append(c);
      }
    }
    if (state != State.NORMAL) {
      throw new IllegalArgumentException("Opening '[' not followed by closing ']' => " + literal);
    }
    if (part.length() > 0) {
      parts.add(part.toString());
    }
    return parts.toArray(new String[parts.size()]);
  }
}
