package org.sapia.corus.taskmanager.core.log;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

@RunWith(MockitoJUnitRunner.class)
public class LogCallbackTaskLogTest {

  @Mock
  private TaskExecutionContext context;
  private LogCallbackTaskLog log;
  
  @Before
  public void setUp() throws Exception {
    log = new LogCallbackTaskLog(context);
  }

  @Test
  public void testDebug() {
    log.debug("test");
    verify(context).debug("test");
  }

  @Test
  public void testInfo() {
    log.info("test");
    verify(context).info("test");
  }

  @Test
  public void testError() {
    log.error("test");
    verify(context).error("test");
  }

}
