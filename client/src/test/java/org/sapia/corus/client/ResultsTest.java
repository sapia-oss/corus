package org.sapia.corus.client;


import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class ResultsTest {

  private Results<Integer> results;
  
  @Before
  public void setUp() throws Exception {
    results = new Results<Integer>();
    results.setTimeout(1000);
  }
  
  @Test
  public void testNormalUsage(){
    List<Integer> vals = new ArrayList<Integer>();
    for(int i = 0; i < 5; i++){
      vals.add(new Integer(i));
      results.incrementInvocationCount();
    }
    
    for(Integer v:vals){
      results.addResult(new Result<Integer>(null, v));
    }
    
    Assert.assertTrue(results.isFinished());
    
    int count = 0;
    while(results.hasNext()){
      results.next();
      count++; 
    }
    
    Assert.assertEquals(vals.size(), count);
    
  }
  
  @Test
  public void testErrorUsage(){
    List<Integer> vals = new ArrayList<Integer>();
    for(int i = 0; i < 5; i++){
      vals.add(new Integer(i));
      results.incrementInvocationCount();
    }
    
    // adding one too many...
    results.incrementInvocationCount();
    
    for(Integer v:vals){
      results.addResult(new Result<Integer>(null, v));
    }
    
    Assert.assertTrue(!results.isFinished());
    
    int count = 0;
    while(results.hasNext()){
      results.next();
      count++; 
    }
    
    Assert.assertEquals(vals.size(), count);
    
  }

}
