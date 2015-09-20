package org.sapia.corus.client.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.OptionDef;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.common.CliUtils;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Collects;

public class Connect extends CorusCliCommand {

  private static final OptionDef OPT_HOST = new OptionDef("h", true);
  private static final OptionDef OPT_PORT = new OptionDef("p", true);
  private static final List<OptionDef> AVAIL_OPTIONS = Collects.arrayToList(OPT_HOST, OPT_PORT);
  
  private static final int DEFAULT_PORT = 33000;
  
  @Override
  protected void doInit(CliContext context) {
  }

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    TCPAddress currentAddress = (TCPAddress) ctx.getCorus().getContext().getAddress();
    String host = currentAddress.getHost();
    int port = DEFAULT_PORT;

    if (ctx.getCommandLine().containsOption(OPT_HOST.getName(), true)) {
      host = ctx.getCommandLine().assertOption(OPT_HOST.getName(), true).getValue();
    }

    if (ctx.getCommandLine().containsOption(OPT_PORT.getName(), true)) {
      try {
        port = Integer.parseInt(ctx.getCommandLine().assertOption(OPT_PORT.getName(), true).getValue());
      } catch (NumberFormatException e) {
        throw new InputException(String.format("Expected valid port for option -%s", OPT_PORT));
      }
    }
    ctx.getCorus().getContext().reconnect(host, port);

    // Change the prompt
    ctx.getConsole().setPrompt(CliUtils.getPromptFor(ctx.getCorus().getContext()));
    ctx.getCorus().getContext().getConnectionHistory().push(currentAddress);
  }
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return AVAIL_OPTIONS;
  }
}
