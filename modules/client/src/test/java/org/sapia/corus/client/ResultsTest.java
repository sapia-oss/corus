package org.sapia.corus.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.ThreadWrapper;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.ubik.net.ServerAddress;

@RunWith(MockitoJUnitRunner.class)
public class ResultsTest {

  private Results<Integer> results;
  private CorusHost origin;

  @Before
  public void setUp() throws Exception {
    origin = CorusHost.newInstance("test-node", new Endpoint(mock(ServerAddress.class), mock(ServerAddress.class)), "os", "jvm", mock(PublicKey.class));
    results = new Results<Integer>();
    results.setTimeout(1000);
  }

  @Test
  public void testNormalUsage() {
    List<Integer> vals = new ArrayList<Integer>();
    for (int i = 0; i < 5; i++) {
      vals.add(new Integer(i));
      results.incrementInvocationCount();
    }

    for (Integer v : vals) {
      results.addResult(new Result<Integer>(origin, v, Result.Type.forClass(Integer.class)));
    }

    assertTrue(results.isFinished());

    int count = 0;
    while (results.hasNext()) {
      results.next();
      count++;
    }

    assertEquals(vals.size(), count);

  }
  
  @Test
  public void testNormalUsage_async() throws Exception {
    final List<Integer> vals = new ArrayList<Integer>();
    for (int i = 0; i < 5; i++) {
      vals.add(new Integer(i));
    }
    results.setInvocationCount(vals.size());
    
    ThreadWrapper t = ThreadWrapper.wrap(new Thread(new Runnable() {
      @Override
      public void run() {
        for (Integer v : vals) {
          results.addResult(new Result<Integer>(origin, v, Result.Type.forClass(Integer.class)));
        }        
      }
    })).start();
    
    int count = 0;
    while (results.hasNext()) {
      results.next();
      count++;
    }

    t.stop();

    assertEquals(vals.size(), count);
  }
  
  @Test
  public void testNormalUsage_iterate() throws Exception {
    final List<Integer> vals = new ArrayList<Integer>();
    for (int i = 0; i < 5; i++) {
      vals.add(new Integer(i));
    }
    results.setInvocationCount(vals.size());
    
    ThreadWrapper t = ThreadWrapper.wrap(new Thread(new Runnable() {
      @Override
      public void run() {
        for (Integer v : vals) {
          results.addResult(new Result<Integer>(origin, v, Result.Type.forClass(Integer.class)));
        }        
      }
    })).start();
    
    int count = 0;
    
    for (Result<Integer> r : results) {
      count++;
    }
    
    t.stop();

    assertEquals(vals.size(), count);
  }


  @Test
  public void testErrorUsage() {
    List<Integer> vals = new ArrayList<Integer>();
    for (int i = 0; i < 5; i++) {
      vals.add(new Integer(i));
      results.incrementInvocationCount();
    }

    // adding one too many...
    results.incrementInvocationCount();

    for (Integer v : vals) {
      results.addResult(new Result<Integer>(origin, v, Result.Type.forClass(Integer.class)));
    }

    assertTrue(!results.isFinished());

    int count = 0;
    while (count < 5 && results.hasNext()) {
      results.next();
      count++;
    }

    assertEquals(vals.size(), count);

  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testAddingNullResult() {
    results.addResult(null);
  }

  @Test
  public void testFilter() {
    List<Integer> vals = new ArrayList<Integer>();
    for (int i = 0; i < 5; i++) {
      vals.add(new Integer(i));
      results.incrementInvocationCount();
    }

    for (Integer v : vals) {
      results.addResult(new Result<Integer>(origin, v, Result.Type.forClass(Integer.class)));
    }

    Results<Integer> filtered = results.filter((arg) -> new Integer(0), (err) -> {});

    List<Integer> filteredList = new ArrayList<Integer>();
    while (filtered.hasNext()) {
      filteredList.add(filtered.next().getData());
    }

    assertEquals(vals.size(), filteredList.size());
    for (Integer i : filteredList) {
      assertEquals(new Integer(0), i);
    }
  }

}
