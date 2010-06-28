package org.sapia.corus.client.facade;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.facade.impl.ClusterInvoker;
import org.sapia.ubik.net.TCPAddress;

public class ClusterInvokerTest {

  private ClusterInvoker<TestInterface> invoker; 
  private TestInterface proxy;
  
  @Before
  public void setUp() throws Exception {
    TestInvocationDispatcher dispatcher = new TestInvocationDispatcher(new TCPAddress("localhost", 8888));
    dispatcher.add(TestInterface.class, new TestInterfaceImpl());
    invoker = new ClusterInvoker<TestInterface>(TestInterface.class, dispatcher);
    proxy = invoker.proxy(TestInterface.class);
  }

  @Test
  public void testInvokeLenientException() throws Throwable{
    proxy.throwException();
    try{
      invoker.invokeLenient(void.class, new ClusterInfo(false));
    }catch(RuntimeException e){
      assertEquals(Exception.class, e.getCause().getClass());
    }
  }
  
  @Test
  public void testInvokeException() throws Throwable{
    proxy.throwException();
    try{
      invoker.invoke(void.class, new ClusterInfo(false));
    }catch(Exception e){
      assertEquals(Exception.class, e.getClass());
    }
  }

  @Test
  public void testInvokeWithResults() throws Throwable{
    proxy.getValues();
    Results<String[]> results = new Results<String[]>();
    invoker.invoke(results, new ClusterInfo(false));
    assertTrue("Results is empty", results.hasNext());
  }
  
  @Test
  public void testInvokeWithResultsThrowException() throws Throwable{
    proxy.getValuesThrowException();
    Results<String[]> results = new Results<String[]>();
    invoker.invoke(results, new ClusterInfo(false));
    assertTrue("Results is not empty", !results.hasNext());
  }


}
