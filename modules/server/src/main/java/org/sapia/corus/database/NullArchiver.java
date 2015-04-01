package org.sapia.corus.database;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.common.PairTuple;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.services.database.ArchiveStatus;
import org.sapia.corus.client.services.database.Archiver;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.database.persistence.Record;
import org.sapia.ubik.util.Func;

/**
 * An {@link Archiver} implementation that does nothing.
 * 
 * @author yduchesne
 *
 */
public class NullArchiver<K, V> implements Archiver<K, V>{

  public ArchiveStatus archive(RevId revId, K key, Record<V> toArchive) {
    return ArchiveStatus.CREATE;
  }
  
  @Override
  public List<PairTuple<K, Record<V>>> unarchive(RevId revId, boolean remove) {
    return new ArrayList<PairTuple<K,Record<V>>>();
  }
  
  @Override
  public void clear(RevId revId) {
  }
  
  @Override
  public void dump(JsonStream stream, ClassDescriptor<V> descriptor) {
  }
  
  @Override
  public void load(JsonInput input, ClassDescriptor<V> descriptor,
      Func<V, JsonInput> fromJsonFunction) {
  }
  
}
