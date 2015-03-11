package org.sapia.corus.client.services.database;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.services.database.persistence.Record;
import org.sapia.ubik.util.Strings;

/**
 * Keeps a {@link Record}, associated to a given revision.
 * 
 * @author yduchesne
 *
 * @param <V> the type of the archived {@link Record}.
 */
public class Revision<K, V> implements Externalizable {
  
   private String    revisionId;
   private K         key;
   private Record<V> record;
   
   /**
    * Do not call: meant for externalization only.
    */
   public Revision() {
   }
   
   public Revision(String revisionId, K key, Record<V> record) {
     this.revisionId = revisionId;
     this.key        = key;
     this.record     = record;
   }
   
   public K getKey() {
     return key;
   }
   
   public String getRevisionId() {
    return revisionId;
  }
   
  public Record<V> getRecord() {
    return record;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    revisionId = in.readUTF();
    key = (K) in.readObject();
    record = (Record<V>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(revisionId);
    out.writeObject(key);
    out.writeObject(record);
  }
   
  @Override
  public String toString() {
    return Strings.toString("revId", revisionId, "key", key, "record", record);
  }
}
