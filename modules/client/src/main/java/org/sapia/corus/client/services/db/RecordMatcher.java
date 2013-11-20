package org.sapia.corus.client.services.db;

import org.sapia.corus.client.services.db.persistence.Record;

/**
 * An instance of this interface acts as a matcher of {@link Record}s.
 * 
 * @author yduchesne
 * 
 * @param <V>
 *          a generic type of record to match.
 */
public interface RecordMatcher<V> {

  /**
   * @param rec
   *          a {@link Record} to match.
   * @return <code>true</code> if the given record is determined to match,
   *         according to this instance.
   */
  public boolean matches(Record<V> rec);

}
