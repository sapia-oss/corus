package org.sapia.corus.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.common.tuple.PairTuple;
import org.sapia.corus.client.services.database.ArchiveStatus;
import org.sapia.corus.client.services.database.Archiver;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.Revision;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.database.persistence.Persistent;
import org.sapia.corus.client.services.database.persistence.Record;
import org.sapia.ubik.util.Func;

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
  
  @Override
  public void dump(JsonStream stream, ClassDescriptor<V> descriptor) {
    if (JsonStreamable.class.isAssignableFrom(descriptor.getType())) {
      stream.field(descriptor.getType().getName() + "-revisions").beginArray();
      for (RevId r : revisionIds()) {
        Map<K, Revision<K, V>> revisions = revisions(r.get());
        stream.beginObject();
        stream.field("revisionId").value(r.get());
        stream.field("values").beginArray();
        for (Map.Entry<K, Revision<K, V>> entry : revisions.entrySet()) {
          V value = entry.getValue().getRecord().toObject(descriptor);
          ((JsonStreamable) value).toJson(stream, ContentLevel.DETAIL);
        }
        stream.endArray();
        stream.endObject();
      }
      stream.endArray();
    }
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void load(JsonInput input, ClassDescriptor<V> descriptor,
      Func<V, JsonInput> fromJsonFunction) {
    for (JsonInput in : input.iterate(descriptor.getType().getName() + "-revisions")) {
      RevId id = RevId.valueOf(in.getString("revisionId"));
      Map<K, Revision<K, V>> records = revisions(id.get());

      for (JsonInput jsonValue : in.iterate("values")) {
        V value = fromJsonFunction.call(jsonValue);
        K key = ((Persistent<K, V>) value).getKey();
        records.put(key, new Revision<K, V>(id.get(), key, Record.createFor(descriptor, value)));
      }
    }
  }
  
  /**
   * Template method to be overridden by inheriting classes.
   * 
   * @param revId a revision ID.
   * @return the {@link Map} to return for the given revision ID.
   */
  protected abstract Map<K, Revision<K, V>> revisions(String revId);
  
  /**
   * Template method to be overridden by inheriting classes.
   * 
   * @return the {@link Set} of {@link RevId}s held by this instance.
   */
  protected abstract Set<RevId> revisionIds();

}
