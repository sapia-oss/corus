package org.sapia.corus.client.cli.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.sapia.console.AbortException;
import org.sapia.console.CommandNotFoundException;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.cli.Interpreter;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;
import org.sapia.ubik.util.Collections2;

/**
 * This command executes a script against the Corus instances in the cluster, a given number of instances
 * at time. The name of this command comes from the "ripple" effect that it has.
 *  
 * @author yduchesne
 *
 */
public class Ripple extends CorusCliCommand {  
  
  private static final String SCRIPT_OPT       = "s";
  private static final String COMMAND_OPT      = "c";
  private static final String MIN_HOST_OPT     = "m";
  private static final String BATCH_POLICY_OPT = "b";
  private static final String PIPE             = "|";
  
  private static final int    DEFAULT_BATCH_SIZE = 1;
  private static final int    DEFAULT_MIN_HOSTS  = 1;
  private static final String TARGETS_VAR_NAME   = "cluster.targets";
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    
    int    minHosts       = DEFAULT_MIN_HOSTS;
    
    if (ctx.getCommandLine().containsOption(MIN_HOST_OPT, true)) {
      minHosts = ctx.getCommandLine().assertOption(MIN_HOST_OPT, true).asInt();
    } 
    if (minHosts < DEFAULT_BATCH_SIZE) {
      throw new InputException("-m option value should be larger than 1");
    }
        
    String policy   = ctx.getCommandLine().assertOption(BATCH_POLICY_OPT, true).getValue();
    
    List<CorusHost> allHosts = new ArrayList<CorusHost>();
    allHosts.add(ctx.getCorus().getContext().getServerHost());
    allHosts.addAll(ctx.getCorus().getContext().getOtherHosts());

    List<List<CorusHost>> hostBatches = new ArrayList<List<CorusHost>>();

    int batchSize;
    if (policy.endsWith("%")) {
      float percentage = Integer.parseInt(policy.substring(0, policy.indexOf("%")));
      if (percentage <= 0) {
        throw new InputException("Invalid value for -b option: must be greater than 0; got: " + percentage);
      }
      batchSize = (int) (allHosts.size() * percentage / 100);
      if (batchSize <= 0) {
        batchSize = DEFAULT_BATCH_SIZE;
      }
    } else {
      batchSize = Integer.parseInt(policy);
      if (batchSize <= 0) {
        throw new InputException("Invalid value for -b option: must be greater than 0; got: " + batchSize);
      }
    }
    
    if (allHosts.size() <= minHosts) {
      batchSize = DEFAULT_BATCH_SIZE;
    }
    
    hostBatches = Collections2.splitAsLists(allHosts, batchSize);
    for (List<CorusHost> batch : hostBatches) {
      try {
        Map<String, String> vars = new HashMap<String, String>();
        if (ctx.getCommandLine().containsOption(COMMAND_OPT, true)) {
          StringTokenizer tk = new StringTokenizer(ctx.getCommandLine().assertOption(COMMAND_OPT, true).getValue().trim(), PIPE);
          while (tk.hasMoreTokens()) {
            processCommand(batch, tk.nextToken(), ctx);
          }
        } else {
          String scriptFilePath = ctx.getCommandLine().assertOption(SCRIPT_OPT, true).getValue();
          File scriptFile = ctx.getFileSystem().getFile(scriptFilePath);
          if (!ctx.getCommandLine().hasNext()) {
            throw new InputException("Path to script file expected");
          }
          String targetString = getTargetString(batch);
          vars.put(TARGETS_VAR_NAME, targetString);
          ctx.getConsole().println("Rippling execution of script " + scriptFile + " against targets: " + targetString);          
          processScript(scriptFile, ctx, vars);
        }
      } catch (FileNotFoundException e) {
        throw new InputException(e.getMessage());
      } catch (Throwable e) {
        CliError err = ctx.createAndAddErrorFor(this, "Unable to perform ripple operation", e);
        ctx.getConsole().println(err.getSimpleMessage());      
        break;
      }
    }
  }
  
  private String getTargetString(List<CorusHost> batch) {
    StringBuilder targets = new StringBuilder();
    for (CorusHost h : batch) {
      if (targets.length() > 0) {
        targets.append(",");
      }
      TCPAddress address = (HttpAddress) h.getEndpoint().getServerTcpAddress();
      targets.append(address.getHost()).append(":").append(address.getPort());
    }
    return targets.toString();
  }
    
  private void processScript(File scriptFile, CliContext ctx, Map<String, String> vars) throws IOException, CommandNotFoundException, Throwable {
    if (scriptFile.exists()) {
      Interpreter interpreter = new Interpreter(ctx.getCorus());
      interpreter.interpret(new FileReader(scriptFile), vars);
    } else {
      throw new FileNotFoundException("File not found: " + scriptFile.getAbsolutePath());
    }
  }
  
  private void processCommand(List<CorusHost> batch, String cmdLine, CliContext ctx) throws IOException, CommandNotFoundException, Throwable {
    Interpreter interpreter = new Interpreter(ctx.getCorus());
    if (cmdLine.contains("-cluster")) {
      throw new InputException("Rippled command must not be invoked with -cluster option");
    }
    String toExecute = cmdLine + " -cluster " + getTargetString(batch);
    ctx.getConsole().println("Rippling command: " + toExecute);        
    interpreter.eval(toExecute);
  }  
  
}
