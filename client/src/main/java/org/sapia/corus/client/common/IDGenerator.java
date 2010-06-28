package org.sapia.corus.client.common;


/**
 * Makes unique IDs based on time and internal counter.
 *
 * @author Yanick Duchesne
 */
public class IDGenerator {
  private static int _count = 0;

  public static synchronized String makeId() {
    String id = "" + System.currentTimeMillis() + _count++;

    if (_count >= 999) {
      _count = 0;
    }

    return id;
  }
}
