package org.sapia.corus.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sapia.corus.client.common.PairTuple;
import org.sapia.corus.client.services.database.ArchiveStatus;
import org.sapia.corus.client.services.database.Archiver;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.Revision;
import org.sapia.corus.client.services.database.persistence.Record;

/**
 * Provides basic behavior for implementing {@link Archiver}.
 * 
 * @author yduchesne
 *
 */
public abstract class ArchiverSupport<K, V> implements Archiver<K, V> {
  
  @Override
  public synchronized ArchiveStatus archive(RevId revId, K key, Record<V> toArchive) {
    Map<K, Revision<K, V>> records = revisions(revId.get());
    if (records.containsKey(key)) {
      records.put(key, new Revision<K, V>(revId.get(), key, toArchive));
      return ArchiveStatus.OVERWRITE;
    } 
    records.put(key, new Revision<K, V>(revId.get(), key, toArchive));
    return ArchiveStatus.CREATE;
  }

  @Override
  public synchronized List<PairTuple<K, Record<V>>> unarchive(RevId revId, boolean remove) {
    Map<K, Revision<K, V>> records = revisions(revId.get());
    List<PairTuple<K, Record<V>>> toReturn = new ArrayList<PairTuple<K, Record<V>>>();
    for (Revision<K, V> rev : records.values()) {
      toReturn.add(new PairTuple<K, Record<V>>(rev.getKey(), rev.getRecord()));
    }
    if (remove) {
      records.clear();
    }
    return toReturn;
  }
  
  @Override
  public synchronized void clear(RevId revId) {
    revisions(revId.get()).clear();
  }
  
  /**
   * Template method to be overridden by inheriting classes.
   * 
   * @param revId a revision ID.
   * @return the {@link Map} to return for the given revision ID.
   */
  protected abstract Map<K, Revision<K, V>> revisions(String revId);

}
