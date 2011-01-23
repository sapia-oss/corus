package org.sapia.corus.taskmanager.core;

import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.apache.log.Hierarchy;
import org.sapia.corus.TestServerContext;
import org.sapia.corus.taskmanager.core.log.LoggerTaskLog;

public class TaskManagerImplTest extends TestCase {

  private TaskManagerImpl tm;
  
  protected void setUp() throws Exception {
    super.setUp();
    tm = new TaskManagerImpl(
        new LoggerTaskLog(Hierarchy.getDefaultHierarchy().getLoggerFor("taskmanager")), 
        TestServerContext.create());
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    tm.shutdown();
  }

  public void testExecute() throws Exception{
    TestTask t = new TestTask(getName());
    tm.execute(t);
    t.waitFor();
    assertTrue("Not completed", t.completed);
  }

  public void testExecuteAndWait() throws Exception{
    TestTask t = new TestTask(getName());
    String result = (String)tm.executeAndWait(t).get();
    assertEquals("Result invalid", "TEST", result);
  }
  
  public void testExecuteAndWaitError() throws Exception{
    ErrorTask t = new ErrorTask(getName());
    try{
      tm.executeAndWait(t).get();
      fail("Expected InvocationTargetException");
    }catch(InvocationTargetException e){
      //ok
    }
  }

  public void testExecuteAndWaitTaskWithLog() throws Exception{
    TestTask t = new TestTask(getName());
    TestTaskLog log = new TestTaskLog();

    tm.executeAndWait(t, new TaskConfig().setLog(log)).get();
    assertTrue("Parent log was not called", log.logged);
  }

  public void testExecuteBackground() throws Exception{
    TestTask t = new TestTask(getName());
    tm.executeBackground(t, BackgroundTaskConfig.create().setExecDelay(200).setExecInterval(200));
    Thread.sleep(1000);
    t.abort();
    assertTrue("Task executed only once or less", t.getExecutionCount() > 1);
  }
  
  public void testExecuteBackgroundWithMax() throws Exception{
    TestTask t = new TestTask(getName());
    t.setMaxExecution(3);
    tm.executeBackground(t, BackgroundTaskConfig.create().setExecDelay(200).setExecInterval(200));
    Thread.sleep(1000);
    assertTrue("Task executed more than max", t.getExecutionCount() > 0 && t.getExecutionCount() <= 3);
  }
  
  
  /*
  public void testNestedTask() throws Exception{
    ContainerTask container = new ContainerTask();
    tm.execute(container);
    Thread.sleep(2000000);
  }*/
  

  
  class TestTask extends Task{
    boolean completed;
    
    public TestTask(String name) {
      super(name);
    }

    @Override
    public synchronized Object execute(TaskExecutionContext ctx) throws Throwable {
      try{
        ctx.getLog().debug(this, "executing... " + getExecutionCount());
        return "TEST";
      }finally{
        completed = true;
        notifyAll();
      }
    }
    
    public synchronized void waitFor() throws InterruptedException{
      while(!completed){
        wait(); 
      }
    }
  }

  class ErrorTask extends TestTask{
    
    public ErrorTask(String name) {
      super(name);
    }
    
    @Override    
    public Object execute(TaskExecutionContext ctx) throws Throwable {
      try{
        throw new Exception();
      }finally{
        completed = true;
        notifyAll();
      }
    }
  }
  
  class TestTaskLog implements TaskLog{
    boolean logged;
    
    public boolean isAdditive() {
      return false;
    }
    public void debug(Task task, String msg) {
      this.logged = true;
    }
    public void info(Task task, String msg) {
      this.logged = true;
    }
    public void warn(Task task, String msg) {
      this.logged = true;
    }
    public void warn(Task task, String msg, Throwable err) {
      this.logged = true;
    }
    public void error(Task task, String msg) {
      this.logged = true;
    }
    public void error(Task task, String msg, Throwable err) {
      this.logged = true;
    }
    
    public void close() {
    }
  }
  
  class TestTaskListener implements TaskListener{

    private volatile boolean completed = false;
    private volatile boolean failed, succeeded;

    public synchronized void executionFailed(Task task, Throwable err) {
      failed    = true;
      completed = true;
      notifyAll();
    }
    
    public synchronized void executionSucceeded(Task task, Object result) {
      succeeded = true;
      completed = true;
      notifyAll();
    }
    
    public synchronized void waitFor() throws InterruptedException{
      while(!completed){
        wait();
      }
    }
    
  }
  
  class ContainerTask extends Task{
    
    @Override
    public Object execute(TaskExecutionContext ctx) throws Throwable {
      ctx.debug("Executing container task");
      ctx.getTaskManager().execute(new NestedTask());
      return null;
    }
    
  }
  
  class NestedTask extends Task{
    
    @Override
    public Object execute(TaskExecutionContext ctx) throws Throwable {
      ctx.debug("Executing nested task");
      return null;
    }
  }
}
