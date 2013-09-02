package org.sapia.corus.client.cli;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sapia.console.AbortException;
import org.sapia.console.ConsoleOutput.DefaultConsoleOutput;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.facade.ConfiguratorFacade;
import org.sapia.corus.client.facade.CorusConnectorImpl;
import org.sapia.corus.client.facade.ProcessorFacade;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.processor.ProcessCriteria;

public class InterpreterTest {
  
  private CorusConnectorImpl     connector;
  private Interpreter console;
  
  @Before
  public void setUp() {
    this.connector = mock(CorusConnectorImpl.class);
    this.console   = new Interpreter(DefaultConsoleOutput.newInstance(), connector);
  }

  @Test
  public void testInterpret() throws Throwable {
    ProcessorFacade processor = mock(ProcessorFacade.class);
    when(connector.getProcessorFacade()).thenReturn(processor);

    ProcessCriteria expectedCriteria = new ProcessCriteria();
    expectedCriteria.setDistribution(ArgFactory.exact("test"));
    expectedCriteria.setVersion(ArgFactory.exact("1.0"));
    expectedCriteria.setName(ArgFactory.exact("proc"));
    expectedCriteria.setProfile("dev");
    
    final AtomicReference<ClusterInfo>     inputClusterInfo = new AtomicReference<ClusterInfo>();
    final AtomicReference<ProcessCriteria> inputCriteria    = new AtomicReference<ProcessCriteria>();
    
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        inputCriteria.set((ProcessCriteria) invocation.getArguments()[0]);
        inputClusterInfo.set((ClusterInfo) invocation.getArguments()[1]);
        return null;
      }
    }).when(processor).kill(any(ProcessCriteria.class), any(ClusterInfo.class));
    
    console.eval("kill -d test -v 1.0 -n proc -p dev -cluster");    
    
    assertEquals(expectedCriteria.getDistribution(), inputCriteria.get().getDistribution());
    assertEquals(expectedCriteria.getVersion(), inputCriteria.get().getVersion());
    assertEquals(expectedCriteria.getName(), inputCriteria.get().getName());
    assertEquals(expectedCriteria.getProfile(), inputCriteria.get().getProfile());    
    assertTrue("Expected clustered", inputClusterInfo.get().isClustered());
  }

  @Test
  public void testInterpret_confCommand() throws Throwable {
    ConfiguratorFacade config = mock(ConfiguratorFacade.class);
    when(connector.getConfigFacade()).thenReturn(config);

    console.eval("conf add -p name=value\\=123");    
    
    verify(config).addProperty(eq(PropertyScope.PROCESS), eq("name"), eq("value\\=123"), any(ClusterInfo.class));
  }
  
  @Test (expected = AbortException.class)
  public void testAbort() throws Throwable {
    console.eval("quit");    
  }
  
  @Test
  public void testInterpretCommandReader() throws Throwable {
    ProcessorFacade processor = mock(ProcessorFacade.class);
    when(connector.getProcessorFacade()).thenReturn(processor);

    ProcessCriteria expectedCriteria = new ProcessCriteria();
    expectedCriteria.setDistribution(ArgFactory.exact("test"));
    expectedCriteria.setVersion(ArgFactory.exact("1.0"));
    expectedCriteria.setName(ArgFactory.exact("proc"));
    expectedCriteria.setProfile("dev");
    
    final AtomicReference<ClusterInfo>     inputClusterInfo = new AtomicReference<ClusterInfo>();
    final AtomicReference<ProcessCriteria> inputCriteria    = new AtomicReference<ProcessCriteria>();
    
    doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        inputCriteria.set((ProcessCriteria) invocation.getArguments()[0]);
        inputClusterInfo.set((ClusterInfo) invocation.getArguments()[1]);
        return null;
      }
    }).when(processor).kill(any(ProcessCriteria.class), any(ClusterInfo.class));
    
    
    StringWriter writer = new StringWriter();
    PrintWriter pw = new PrintWriter(writer);
    pw.println("kill -d test -v 1.0 -n ${proc.name} -p dev -cluster");
    pw.println("# this is a comment");
    pw.println("echo \"process killed\"");
    
    StringReader reader = new StringReader(writer.toString());
    Map<String, String> vars = new HashMap<String, String>();
    vars.put("proc.name", "proc");
    console.interpret(reader, vars);
    
    assertEquals(expectedCriteria.getDistribution(), inputCriteria.get().getDistribution());
    assertEquals(expectedCriteria.getVersion(), inputCriteria.get().getVersion());
    assertEquals(expectedCriteria.getName(), inputCriteria.get().getName());
    assertEquals(expectedCriteria.getProfile(), inputCriteria.get().getProfile());    
    assertTrue("Expected clustered", inputClusterInfo.get().isClustered());    
  }

}