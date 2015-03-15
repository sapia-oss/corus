package org.sapia.corus.client.services.database.persistence;

import java.util.ArrayList;
import java.util.List;

public class Transaction {

  interface Logic<T> {
    public T execute();
  }

  private List<Persistent<?, ?>> persistentObjects = new ArrayList<Persistent<?, ?>>();

  public <K, V> Transaction add(Persistent<K, V> p) {
    persistentObjects.add(p);
    return this;
  }

  public void commit() {
    for (Persistent<?, ?> p : persistentObjects) {
      p.save();
    }
  }

  public <T> T execute(Logic<T> logic) {
    T toReturn = logic.execute();
    commit();
    return toReturn;
  }

}
