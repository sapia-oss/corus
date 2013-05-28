package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.common.CliUtils;
import org.sapia.ubik.net.TCPAddress;

public class Connect extends CorusCliCommand{

  public static final String OPT_HOST = "h";
  public static final String OPT_PORT = "p";
  
  private static final int 		DEFAULT_PORT = 33000;
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {

    TCPAddress currentAddress = (TCPAddress)ctx.getCorus().getContext().getAddress();
    String host = currentAddress.getHost();
    int port = DEFAULT_PORT;
    
    if(ctx.getCommandLine().containsOption(OPT_HOST, true)){
      host = ctx.getCommandLine().assertOption(OPT_HOST, true).getValue();
    }
    
    if(ctx.getCommandLine().containsOption(OPT_PORT, true)){
      try{
        port = Integer.parseInt(ctx.getCommandLine().assertOption(OPT_PORT, true).getValue());
      }catch(NumberFormatException e){
        throw new InputException(String.format("Expected valid port for option -%s", OPT_PORT));
      }
    }
    ctx.getCorus().getContext().reconnect(host, port);

    // Change the prompt
    ctx.getConsole().setPrompt(CliUtils.getPromptFor(ctx.getCorus().getContext()));
    ctx.getCorus().getContext().getConnectionHistory().push(currentAddress);
  }
}
