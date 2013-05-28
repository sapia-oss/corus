package org.sapia.corus.db;

import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.db.RecordMatcher;
import org.sapia.corus.client.services.db.persistence.AbstractPersistent;
import org.sapia.corus.client.services.db.persistence.ClassDescriptor;
import org.sapia.ubik.util.Collections2;

public class CachingDbMapTest {
  
  private DbMap<String, TestAbstractPersistent> delegate;
  private CachingDbMap<String, TestAbstractPersistent> cache;
  
  @Before
  public void setUp() {
    delegate = mock(DbMap.class);
    cache = new CachingDbMap<String, CachingDbMapTest.TestAbstractPersistent>(delegate);
  }

  @Test
  public void testClear() {
    cache.clear();
    verify(delegate).clear();
  }

  @Test
  public void testClose() {
    cache.close();
    verify(delegate).close();
  }

  @Test
  public void testCreateMatcherFor() {
    RecordMatcher<TestAbstractPersistent> matcher = mock(RecordMatcher.class);
    when(delegate.createMatcherFor(any(TestAbstractPersistent.class))).thenReturn(matcher);
    TestAbstractPersistent template = new TestAbstractPersistent().setKey("test");
    
    assertNotNull(cache.createMatcherFor(template));
    verify(delegate).createMatcherFor(eq(template));
  }

  @Test
  public void testGet() {
    cache.get("test");
    verify(delegate).get("test");
  }

  @Test
  public void testGetClassDescriptor() {
    ClassDescriptor<TestAbstractPersistent> desc = new ClassDescriptor<TestAbstractPersistent>(TestAbstractPersistent.class);
    when(delegate.getClassDescriptor()).thenReturn(desc);
    cache.getClassDescriptor();
    verify(delegate).getClassDescriptor();
  }

  @Test
  public void testKeys() {
    when(delegate.keys()).thenReturn(Collections2.arrayToList("test1", "test2").iterator());
    cache.keys();
    verify(delegate).keys();
  }

  @Test
  public void testPut() {
    TestAbstractPersistent obj = new TestAbstractPersistent().setKey("test");
    cache.put(obj.key, obj);
    verify(delegate).put(eq(obj.key), eq(obj));
  }

  @Test
  public void testRefresh() {
    TestAbstractPersistent obj = new TestAbstractPersistent().setKey("test");
    cache.refresh(obj.key, obj);
    verify(delegate).refresh(eq(obj.key), eq(obj));
  }

  @Test
  public void testRemove() {
    cache.remove("test");
    verify(delegate).remove("test");
  }

  @Test
  public void testValues() {
    Iterator itr = mock(Iterator.class);
    when(delegate.values()).thenReturn(itr);
    cache.values();
    verify(delegate).values();
  }

  @Test
  public void testValuesForRecordMatcher() {
    Collection coll = mock(Collection.class);
    RecordMatcher<TestAbstractPersistent> matcher = mock(RecordMatcher.class);
    when(delegate.values(any(RecordMatcher.class))).thenReturn(coll);
    Iterator itr = mock(Iterator.class);
    when(coll.iterator()).thenReturn(itr);
    cache.values(matcher);
    verify(delegate).values(any(RecordMatcher.class));
  }

  public static class TestAbstractPersistent extends AbstractPersistent<String, TestAbstractPersistent> {
  
    private String key;
    
    public TestAbstractPersistent setKey(String key) {
      this.key = key;
      return this;
    }
    
    public TestAbstractPersistent() {
    }
    @Override
    public String getKey() {
      return key;
    }
  }
}
