package org.sapia.corus.util;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a composite {@link Matcher}.
 * 
 * @author yduchesne
 * 
 */
public class CompositeMatcher<T> implements Matcher<T> {

  private List<Matcher<T>> matchers = new ArrayList<Matcher<T>>();

  /**
   * @param m
   *          a {@link Matcher} to add to this instance.
   * @return this instance.
   */
  public CompositeMatcher<T> add(Matcher<T> m) {
    matchers.add(m);
    return this;
  }

  /**
   * This method goes through this instance's internal {@link Matcher}s. If all
   * match, this method returns <code>true</code>, otherwise it returns
   * <code>false</code>
   * 
   * @see Matcher#matches(Object)
   */
  public boolean matches(T object) {
    for (Matcher<T> m : matchers) {
      if (!m.matches(object)) {
        return false;
      }
    }
    return true;
  }

}
