package org.sapia.corus.cloud.platform.cli;

import org.sapia.console.ConsoleOutput;
import org.sapia.corus.cloud.platform.workflow.DefaultWorkflowLog.Level;
import org.sapia.corus.cloud.platform.workflow.DefaultWorkflowLog.LogOutput;

/**
 * Implements the {@link LogOutput} interface over the {@link ConsoleOutput} interface.
 * 
 * @author yduchesne
 *
 */
public class CliWorkflowLogOutputAdapter implements LogOutput {
  
  private ConsoleOutput delegate;
  
  public CliWorkflowLogOutputAdapter(ConsoleOutput delegate) {
    this.delegate = delegate;
  }
  
  @Override
  public void log(Level l, String msg) {
    delegate.println(msg);
    delegate.flush();
  }

}
