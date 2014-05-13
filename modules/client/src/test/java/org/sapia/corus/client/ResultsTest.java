package org.sapia.corus.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sapia.ubik.util.Func;

public class ResultsTest {

  private Results<Integer> results;

  @Before
  public void setUp() throws Exception {
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
      results.addResult(new Result<Integer>(null, v));
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
  public void testErrorUsage() {
    List<Integer> vals = new ArrayList<Integer>();
    for (int i = 0; i < 5; i++) {
      vals.add(new Integer(i));
      results.incrementInvocationCount();
    }

    // adding one too many...
    results.incrementInvocationCount();

    for (Integer v : vals) {
      results.addResult(new Result<Integer>(null, v));
    }

    assertTrue(!results.isFinished());

    int count = 0;
    while (results.hasNext()) {
      results.next();
      count++;
    }

    assertEquals(vals.size(), count);

  }

  @Test
  public void testFilter() {
    List<Integer> vals = new ArrayList<Integer>();
    for (int i = 0; i < 5; i++) {
      vals.add(new Integer(i));
      results.incrementInvocationCount();
    }

    for (Integer v : vals) {
      results.addResult(new Result<Integer>(null, v));
    }

    Results<Integer> filtered = results.filter(new Func<Integer, Integer>() {
      @Override
      public Integer call(Integer arg) {
        return new Integer(0);
      }
    });

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
