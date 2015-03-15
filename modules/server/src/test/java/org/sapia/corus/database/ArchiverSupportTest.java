package org.sapia.corus.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.PairTuple;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.database.Revision;
import org.sapia.corus.client.services.database.persistence.Record;

public class ArchiverSupportTest {
  
  private Map<String, Map<Integer, Revision<Integer, String>>> revisions;
  private ArchiverSupport<Integer, String> archiver; 
  
 
  @Before
  public void setUp() {
    revisions = new HashMap<String, Map<Integer,Revision<Integer,String>>>();
    
    archiver = new ArchiverSupport<Integer, String>() {
      @Override
      protected Map<Integer, Revision<Integer, String>> revisions(String revId) {
        Map<Integer, Revision<Integer, String>> revMap = revisions.get(revId);
        if (revMap == null) {
          revMap = new HashMap<Integer, Revision<Integer,String>>();
          revisions.put(revId, revMap);
        }
        return revMap;
      }
    };
  }
 
  @Test
  public void testArchive() {
    archiver.archive(RevId.valueOf("rev"), new Integer(1), record("value1"));
    assertTrue(revisions.containsKey(RevId.valueOf("rev").get()));
    assertTrue(revisions.get(RevId.valueOf("rev").get()).containsKey(new Integer(1)));
  }
  
  @Test
  public void testUnarchive() {
    archiver.archive(RevId.valueOf("rev"), new Integer(1), record("value1"));
    List<PairTuple<Integer, Record<String>>> lists = archiver.unarchive(RevId.valueOf("rev"), false);
    assertEquals(1, lists.size());
    assertTrue(revisions.get(RevId.valueOf("rev").get()).containsKey(new Integer(1)));
  }

  @Test
  public void testUnarchive_remove() {
    archiver.archive(RevId.valueOf("rev"), new Integer(1), record("value1"));
    List<PairTuple<Integer, Record<String>>> lists = archiver.unarchive(RevId.valueOf("rev"), true);
    assertEquals(1, lists.size());
    assertEquals(new Integer(1), lists.get(0).getLeft());
    assertFalse(revisions.get(RevId.valueOf("rev").get()).containsKey(new Integer(1)));
  }
  
  @Test
  public void testClear() {
    archiver.archive(RevId.valueOf("rev"), new Integer(1), record("value1"));
    assertTrue(revisions.containsKey(RevId.valueOf("rev").get()));
    archiver.clear(RevId.valueOf("rev"));
    assertFalse(revisions.get(RevId.valueOf("rev").get()).containsKey(new Integer(1)));
  }
  
  private Record<String> record(String value) {
    return Record.createFor(value);
  }
}
