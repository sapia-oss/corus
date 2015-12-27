package org.sapia.corus.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonObjectInput;
import org.sapia.corus.client.common.json.WriterJsonStream;
import org.sapia.corus.client.common.tuple.PairTuple;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.Revision;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.database.persistence.Record;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

public class ArchiverSupportTest {
  
  private Map<String, Map<Integer, Revision<Integer, TestDbObject>>> revisions, revisionsCopy;
  private ArchiverSupport<Integer, TestDbObject> archiver, archiverCopy; 
  private ClassDescriptor<TestDbObject> cd;
  private TestDbObject object;
  
  @Before
  public void setUp() {
    revisions     = new HashMap<String, Map<Integer,Revision<Integer,TestDbObject>>>();
    revisionsCopy = new HashMap<String, Map<Integer,Revision<Integer,TestDbObject>>>();
    archiver      = newArchiver(revisions);
    archiverCopy  = newArchiver(revisionsCopy);
    cd = new ClassDescriptor<TestDbObject>(TestDbObject.class);
    object = newDbObject();
  }
 
  @Test
  public void testArchive() {
    archiver.archive(RevId.valueOf("rev"), new Integer(1), Record.createFor(cd, object));
    assertTrue(revisions.containsKey(RevId.valueOf("rev").get()));
    assertTrue(revisions.get(RevId.valueOf("rev").get()).containsKey(new Integer(1)));
  }
  
  @Test
  public void testUnarchive() {
    archiver.archive(RevId.valueOf("rev"), new Integer(1), Record.createFor(cd, object));
    List<PairTuple<Integer, Record<TestDbObject>>> lists = archiver.unarchive(RevId.valueOf("rev"), false);
    assertEquals(1, lists.size());
    assertTrue(revisions.get(RevId.valueOf("rev").get()).containsKey(new Integer(1)));
  }

  @Test
  public void testUnarchive_remove() {
    archiver.archive(RevId.valueOf("rev"), new Integer(1), Record.createFor(cd, object));
    List<PairTuple<Integer, Record<TestDbObject>>> lists = archiver.unarchive(RevId.valueOf("rev"), true);
    assertEquals(1, lists.size());
    assertEquals(new Integer(1), lists.get(0).getLeft());
    assertFalse(revisions.get(RevId.valueOf("rev").get()).containsKey(new Integer(1)));
  }
  
  @Test
  public void testClear() {
    archiver.archive(RevId.valueOf("rev"), new Integer(1), Record.createFor(cd, object));
    assertTrue(revisions.containsKey(RevId.valueOf("rev").get()));
    archiver.clear(RevId.valueOf("rev"));
    assertFalse(revisions.get(RevId.valueOf("rev").get()).containsKey(new Integer(1)));
  }
  
  @Test
  public void testDumpLoad() {
    archiver.archive(RevId.valueOf("rev"), 1, Record.createFor(cd, object));
    StringWriter     writer = new StringWriter();
    WriterJsonStream stream = new WriterJsonStream(writer);
  
    stream.beginObject();
    archiver.dump(stream, cd);
    stream.endObject();
    
    archiverCopy.load(JsonObjectInput.newInstance(writer.toString()), cd, new Func<TestDbObject, JsonInput>() {
      public TestDbObject call(JsonInput in) {
        return TestDbObject.fromJson(in);
      }
    });
    
    List<PairTuple<Integer, Record<TestDbObject>>> lists = archiver.unarchive(RevId.valueOf("rev"), false);
    
    TestDbObject copy = lists.get(0).getRight().toObject(cd);
    
    assertEquals(object.getName(), copy.getName());
    assertEquals(object.getDescription(), copy.getDescription());
  }
  
  private TestDbObject newDbObject() {
    TestDbObject object = new TestDbObject();
    object.setDescription("desc");
    object.setName("name");
    return object;
  }
  
  private ArchiverSupport<Integer, TestDbObject> newArchiver(final Map<String, Map<Integer, Revision<Integer, TestDbObject>>> revisions) {
    ArchiverSupport<Integer, TestDbObject> archiver = new ArchiverSupport<Integer, TestDbObject>() {
      @Override
      protected Map<Integer, Revision<Integer, TestDbObject>> revisions(String revId) {
        Map<Integer, Revision<Integer, TestDbObject>> revMap = revisions.get(revId);
        if (revMap == null) {
          revMap = new HashMap<Integer, Revision<Integer,TestDbObject>>();
          revisions.put(revId, revMap);
        }
        return revMap;
      }
      
      @Override
      protected Set<RevId> revisionIds() {
        return Collects.convertAsSet(revisions.keySet(), new Func<RevId, String>() {
          @Override
          public RevId call(String s) {
            return RevId.valueOf(s);
          }
        });
      }
    };
    
    return archiver;
  }
}
