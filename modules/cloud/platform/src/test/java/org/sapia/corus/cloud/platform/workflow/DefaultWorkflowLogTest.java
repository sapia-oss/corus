package org.sapia.corus.cloud.platform.workflow;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.cloud.platform.workflow.DefaultWorkflowLog;
import org.sapia.corus.cloud.platform.workflow.DefaultWorkflowLog.Level;
import org.sapia.corus.cloud.platform.workflow.DefaultWorkflowLog.LogOutput;

@RunWith(MockitoJUnitRunner.class)
public class DefaultWorkflowLogTest {
  
  @Mock
  private LogOutput          output;
  private DefaultWorkflowLog log;
  private Throwable          err;
  
  @Before
  public void setUp() {
    log = new DefaultWorkflowLog(Level.VERBOSE, output);
    err = new Exception("ERROR");
  }

  @Test
  public void testVerbose_message() {
    log.verbose("test");
    verify(output).log(Level.VERBOSE, "test");
  }

  @Test
  public void testVerbose_args() {
    log.verbose("test %s", "args");
    verify(output).log(Level.VERBOSE, "test args");
  }

  @Test
  public void testVerbose_throwable() {
    log.verbose(err);
    verify(output).log(Level.VERBOSE, DefaultWorkflowLog.errToString(err));
  }


  @Test
  public void testInfo_message() {
    log.info("test");
    verify(output).log(Level.INFO, "test");
  }

  @Test
  public void testInfo_args() {
    log.info("test %s", "args");
    verify(output).log(Level.INFO, "test args");
  }

  @Test
  public void testInfo_throwable() {
    log.info(err);
    verify(output).log(Level.INFO,  DefaultWorkflowLog.errToString(err));
  }

  @Test
  public void testWarning_message() {
    log.warning("test");
    verify(output).log(Level.WARNING, "test");
  }

  @Test
  public void testWarning_args() {
    log.warning("test %s", "args");
    verify(output).log(Level.WARNING, "test args");
  }

  @Test
  public void testWarning_throwable() {
    log.warning(err);
    verify(output).log(Level.WARNING,  DefaultWorkflowLog.errToString(err));
  }

  @Test
  public void testError_message() {
    log.error("test");
    verify(output).log(Level.ERROR, "test");
  }

  @Test
  public void testError_args() {
    log.error("test %s", "args");
    verify(output).log(Level.ERROR, "test args");
  }

  @Test
  public void testError_throwable() {
    log.error(err);
    verify(output).log(Level.ERROR,  DefaultWorkflowLog.errToString(err));
  }

}
