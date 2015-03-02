package org.sapia.corus.client.services.db.persistence;

import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.db.DbMap;

public class AbstractPersistentTest {

  private DbMap<String, TestAbstractPersistent> map;
  private TestAbstractPersistent persistent;

  @Before
  public void setUp() {
    map = mock(DbMap.class);
    persistent = new TestAbstractPersistent();
    persistent.setDbMap(map);
  }

  @Test
  public void testSave() {
    persistent.save();
    verify(map).put(eq(persistent.getKey()), eq(persistent));
  }

  @Test
  public void testDelete() {
    persistent.delete();
    verify(map).remove(eq(persistent.getKey()));
    assertTrue(persistent.isDeleted());
  }

  @Test(expected = IllegalStateException.class)
  public void testDeleteMoreThanOnce() {
    persistent.delete();
    persistent.delete();
  }
  
  @Test
  public void testRecycle() {
    persistent.delete();
    persistent.recycle();
  
    verify(map).remove(eq(persistent.getKey()));
    verify(map).put(eq(persistent.getKey()), eq(persistent));
   }

  @Test
  public void testMarkDeleted() {
    persistent.markDeleted();
    verify(map, never()).remove(eq(persistent.getKey()));
  }

  @Test
  public void testRefresh() {
    persistent.refresh();
    verify(map).refresh(eq(persistent.getKey()), eq(persistent));
  }

  @Test(expected = IllegalStateException.class)
  public void testRefreshAfterDelete() {
    persistent.delete();
    persistent.refresh();
  }

  class TestAbstractPersistent extends AbstractPersistent<String, TestAbstractPersistent> {

    @Override
    public String getKey() {
      return "test";
    }
  }
}
