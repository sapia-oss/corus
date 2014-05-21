package org.sapia.corus.client.facade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.facade.impl.ClusterInvoker;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.Uri;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;

@RunWith(MockitoJUnitRunner.class)
public class ClusterInvokerTest {

  private ClusterInvoker<TestInterface> invoker;
  private TestInterface proxy;
  @Mock
  private ServerAddress channelAddress;

  @Before
  public void setUp() throws Exception {
    Endpoint ep = new Endpoint(new HttpAddress(Uri.parse("http://test")), channelAddress);
    CorusHost host = CorusHost.newInstance(ep, "os", "vm");
    TestInvocationDispatcher dispatcher = new TestInvocationDispatcher(host);
    dispatcher.add(TestInterface.class, new TestInterfaceImpl());
    invoker = new ClusterInvoker<TestInterface>(TestInterface.class, dispatcher);
    proxy = invoker.proxy(TestInterface.class);
  }

  @Test
  public void testInvokeLenientException() throws Throwable {
    proxy.throwException();
    try {
      invoker.invokeLenient(void.class, new ClusterInfo(false));
    } catch (RuntimeException e) {
      assertEquals(Exception.class, e.getCause().getClass());
    }
  }

  @Test
  public void testInvokeException() throws Throwable {
    proxy.throwException();
    try {
      invoker.invoke(void.class, new ClusterInfo(false));
    } catch (Exception e) {
      assertEquals(Exception.class, e.getClass());
    }
  }

  @Test
  public void testInvokeWithResults() throws Throwable {
    proxy.getValues();
    Results<String[]> results = new Results<String[]>();
    invoker.invoke(results, new ClusterInfo(false));
    assertTrue("Results is empty", results.hasNext());
  }

  @Test
  public void testInvokeWithResultsThrowException() throws Throwable {
    proxy.getValuesThrowException();
    Results<String[]> results = new Results<String[]>();
    results.setTimeout(1000);
    invoker.invoke(results, new ClusterInfo(false));
    assertTrue("Results is not empty", !results.hasNext());
  }

}
