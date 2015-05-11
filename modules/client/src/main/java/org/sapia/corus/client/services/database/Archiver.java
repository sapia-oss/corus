package org.sapia.corus.client.services.database;

import java.util.List;

import org.sapia.corus.client.common.PairTuple;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.database.persistence.Record;
import org.sapia.ubik.util.Func;

/**
 * Specifies the behavior for archiving/unarchiving {@link Revision}s.
 * 
 * @author yduchesne
 *
 */
public interface Archiver<K, V> {
  
  /**
   * @param the key of the given record.
   * @param revId the revision ID.
   * @param toArchive the {@link Record} to archive.
   * @return an {@link ArchiveStatus}.
   */
  public ArchiveStatus archive(RevId revId, K key, Record<V> toArchive);
  
  /**
   * @param revId the revision ID for which to retrieve an archived record.
   * @param remove indicates of the underlying revision should be removed.
   * @return the list of {@link Record}s that were unarchived, associated to their key.
   */
  public List<PairTuple<K, Record<V>>> unarchive(RevId revId, boolean remove);
  
  /**
   * Clears all entries in the context of the revision corresponding to the given ID.
   * 
   * @param revId a revision ID.
   */
  public void clear(RevId revId);
  
  /**
   * @param stream the {@link JsonStream} to dump to.
   * @param descriptor the {@link ClassDescriptor} corresponding to the type of value
   * that this instance stores.
   */
  public void dump(JsonStream stream, ClassDescriptor<V> descriptor);
  
  /**
   * @param input the {@link JsonInput} to load from.
   * @param descriptor the {@link ClassDescriptor} corresponding to the type of value
   * that this instance stores.
   * @param the {@link Func}tion to use to convert json input.
   */
  public void load(JsonInput input,  ClassDescriptor<V> descriptor, Func<V, JsonInput> fromJsonFunction);  
}
