package org.sapia.corus.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Filters the content of iterators, given a {@link Matcher}.
 * 
 * @author yduchesne
 * 
 */
public class IteratorFilter<T> {

  private Matcher<T> matcher;

  /**
   * Creates an instance of this class that will encapsulate the given matcher.
   * 
   * @param matcher
   *          a {@link Matcher}.
   */
  public IteratorFilter(Matcher<T> matcher) {
    this.matcher = matcher;
  }

  /**
   * @param matcher
   *          the {@link Matcher} to use for filtering.
   * @return a new {@link IteratorFilter}.
   */
  public static <T> IteratorFilter<T> newFilter(Matcher<T> matcher) {
    return new IteratorFilter<T>(matcher);
  }

  /**
   * Internally uses the {@link Matcher} that is encapsulated by this instance
   * to filter the content of the given iterator.
   * 
   * @param iterator
   *          an {@link Iterator} to filter.
   * @return the {@link FilterResult} holding the filtered content.
   */
  public FilterResult<T> filter(Iterator<T> iterator) {
    List<T> toReturn = new ArrayList<T>();
    while (iterator.hasNext()) {
      T toMatch = iterator.next();
      if (matcher.matches(toMatch)) {
        toReturn.add(toMatch);
      }
    }
    return new FilterResult<T>(toReturn);
  }

  // --------------------------------------------------------------------------

  /**
   * An instance of this class holds the content resulting from filtering.
   */
  public static class FilterResult<T> implements Iterable<T> {

    private List<T> result;

    public FilterResult(List<T> result) {
      this.result = result;
    }

    public List<T> get() {
      return result;
    }

    @Override
    public Iterator<T> iterator() {
      return result.iterator();
    }

    public FilterResult<T> sort(Comparator<T> c) {
      Collections.sort(result, c);
      return this;
    }
  }

}
