package org.sapia.corus.cloud.platform.cli;

import java.util.ArrayList;

import org.sapia.console.CmdLine;
import org.sapia.corus.cloud.platform.util.TimeMeasure;
import org.sapia.corus.cloud.platform.workflow.WorkflowResult;
import org.sapia.corus.cloud.platform.workflow.WorkflowResult.Outcome;
import org.sapia.corus.cloud.platform.workflow.WorkflowStepResult;

public class TestCliModule implements CliModule {

  @Override
  public void displayHelp(CliModuleContext context) {   
  }
  
  @Override
  public WorkflowResult interact(CliModuleContext context, CmdLine initialCommand) {
    return new WorkflowResult(Outcome.SUCCESS, new ArrayList<WorkflowStepResult>(), TimeMeasure.forMillis(1000));
  }

}
