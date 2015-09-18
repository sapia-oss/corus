package org.sapia.corus.taskmanager;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.junit.runner.RunWith;

@RunWith(MockitoJUnitRunner.class)
public class CorusTaskManagerImplTest {

  @Mock
  private ServerContext        context;
  private CorusTaskManagerImpl tasks;
  
  @Before
  public void setUp() throws Exception {
    tasks = new CorusTaskManagerImpl();
    tasks.setServerContext(context);
    tasks.init();
  }
  
  @After
  public void tearDown() {
    tasks.dispose();
  }

  @Test
  public void testGetBufferedMessages() throws Throwable {
    tasks.executeAndWait(new TestTask(), null).get();
    tasks.executeAndWait(new TestTask(), null).get();
    assertEquals(8, tasks.getBufferedMessages(ProgressMsg.DEBUG, 0).size());
  }
  
  @Test
  public void testGetBufferedMessages_since_timeout() throws Throwable {
    tasks.executeAndWait(new TestTask(), null).get();
    long currentTime = System.nanoTime(); 
    Thread.sleep(100);
    tasks.executeAndWait(new TestTask(), null).get();
    assertEquals(4, tasks.getBufferedMessages(ProgressMsg.DEBUG, currentTime).size());
  }
  
  @Test
  public void testGetBufferedMessages_with_level() throws Throwable {
    tasks.executeAndWait(new TestTask(), null).get();
    tasks.executeAndWait(new TestTask(), null).get();
    assertEquals(2, tasks.getBufferedMessages(ProgressMsg.ERROR, 0).size());
  }
  
  @Test
  public void testGetBufferedMessages_with_level_since_timeout() throws Throwable {
    tasks.executeAndWait(new TestTask(), null).get();
    long currentTime = System.nanoTime(); 
    Thread.sleep(100);
    tasks.executeAndWait(new TestTask(), null).get();
    assertEquals(1, tasks.getBufferedMessages(ProgressMsg.ERROR, currentTime).size());
  }

  @Test
  public void testClearBufferedMessages() throws Throwable {
    tasks.executeAndWait(new TestTask(), null).get();
    tasks.executeAndWait(new TestTask(), null).get();
    assertEquals(8, tasks.clearBufferedMessages(ProgressMsg.DEBUG, 0).size());
    assertTrue(tasks.getBufferedMessages(ProgressMsg.DEBUG, 0).isEmpty());
  }

  @Test
  public void testClearBufferedMessages_since_timeout() throws Throwable {
    tasks.executeAndWait(new TestTask(), null).get();
    long currentTime = System.nanoTime(); 
    Thread.sleep(100);
    tasks.executeAndWait(new TestTask(), null).get();
    assertEquals(4, tasks.clearBufferedMessages(ProgressMsg.DEBUG, currentTime).size());
    assertEquals(4, tasks.getBufferedMessages(ProgressMsg.DEBUG, 0).size());
  }
  
  @Test
  public void testClearBufferedMessages_with_level() throws Throwable {
    tasks.executeAndWait(new TestTask(), null).get();
    tasks.executeAndWait(new TestTask(), null).get();
    assertEquals(2, tasks.clearBufferedMessages(ProgressMsg.ERROR, 0).size());
    assertTrue(tasks.getBufferedMessages(ProgressMsg.DEBUG, 0).isEmpty());
  }

  @Test
  public void testClearBufferedMessages_with_level_since_timeout() throws Throwable {
    tasks.executeAndWait(new TestTask(), null).get();
    long currentTime = System.nanoTime(); 
    Thread.sleep(100);
    tasks.executeAndWait(new TestTask(), null).get();
    assertEquals(1, tasks.clearBufferedMessages(ProgressMsg.ERROR, currentTime).size());
    assertEquals(4, tasks.getBufferedMessages(ProgressMsg.DEBUG, 0).size());
  }
  
  // ==========================================================================
  
  class TestTask extends Task<Void, Void> {
    
    @Override
    public Void execute(TaskExecutionContext ctx, Void param) throws Throwable {
      ctx.debug("debug-msg");
      ctx.info("info-msg");
      ctx.warn("warn-msg");
      ctx.error("error-msg");
      return null;
    }
  }

}
