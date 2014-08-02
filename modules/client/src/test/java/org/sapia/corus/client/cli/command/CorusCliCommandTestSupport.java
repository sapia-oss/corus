package org.sapia.corus.client.cli.command;

import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.sapia.console.CmdLine;
import org.sapia.corus.client.cli.CliContext;

public class CorusCliCommandTestSupport {
  
  @Mock
  private CliContext context;
  
  protected CliContext context() {
    return context;
  }
  protected void doSetup(CmdLine cmdLine) {
    when(context.getCommandLine()).thenReturn(cmdLine);
  }
  
}
