package org.sapia.corus.cloud.platform.cli;

import org.sapia.console.Console;
import org.sapia.corus.cloud.platform.workflow.DefaultWorkflowLog;
import org.sapia.corus.cloud.platform.workflow.WorkflowLog;

/**
 * An instance of this class is used by a {@link CliModule} instance, in the context
 * of an interaction with an end-user.
 * 
 * @author yduchesne
 *
 */
public class CliModuleContext {

  private DefaultWorkflowLog.Level logLevel;
  private Console                  console;
  
  public CliModuleContext(DefaultWorkflowLog.Level level, Console console) {
    this.logLevel = level;
    this.console  = console;
  }
  
  public Console getConsole() {
    return console;
  }
  
  public WorkflowLog getWorflowLog() {
    return DefaultWorkflowLog.forOutput(logLevel, new CliWorkflowLogOutputAdapter(console.out()));
  }
  
}
