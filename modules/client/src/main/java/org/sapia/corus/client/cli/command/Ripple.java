package org.sapia.corus.client.cli.command;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.sapia.console.AbortException;
import org.sapia.console.CommandNotFoundException;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.Interpreter;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;
import org.sapia.ubik.util.Collects;

/**
 * This command executes a script against the Corus instances in the cluster, a
 * given number of instances at time. The name of this command comes from the
 * "ripple" effect thus created.
 * 
 * @author yduchesne
 * 
 */
public class Ripple extends CorusCliCommand {

  private static final OptionDef OPT_SCRIPT          = new OptionDef("s", true);
  private static final OptionDef OPT_COMMAND         = new OptionDef("c", true);
  private static final OptionDef OPT_MIN_HOST        = new OptionDef("m", true);
  private static final OptionDef OPT_BATCH_POLICY    = new OptionDef("b", true);
  private static final List<OptionDef> AVAIL_OPTIONS = Collects.arrayToList(
      OPT_SCRIPT, 
      OPT_COMMAND, 
      OPT_MIN_HOST, 
      OPT_BATCH_POLICY
  );
  
  private static final String PIPE = "|";

  private static final int DEFAULT_BATCH_SIZE = 1;
  private static final int DEFAULT_MIN_HOSTS = 1;
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return AVAIL_OPTIONS;
  }
  
  @Override
  protected void doInit(CliContext context) {
  }

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    int minHosts = DEFAULT_MIN_HOSTS;

    if (ctx.getCommandLine().containsOption(OPT_MIN_HOST.getName(), true)) {
      minHosts = ctx.getCommandLine().assertOption(OPT_MIN_HOST.getName(), true).asInt();
    }
    if (minHosts < DEFAULT_BATCH_SIZE) {
      throw new InputException("-m option value should be larger than 1");
    }

    String policy = null;
    Option policyOpt = ctx.getCommandLine().getOpt(OPT_BATCH_POLICY.getName());
    if (policyOpt == null) {
      policy = Integer.toString(DEFAULT_BATCH_SIZE);
    } else if (policyOpt.getValue() == null || policyOpt.getValue().trim().length() == 0) {
      throw new InputException("Value not specified for -b option");
    } else {
      policy = policyOpt.getValue();
    }
  
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

    hostBatches = Collects.splitAsLists(allHosts, batchSize);
    for (List<CorusHost> batch : hostBatches) {
      try {
        if (ctx.getCommandLine().containsOption(OPT_COMMAND.getName(), true)) {
          StringTokenizer tk = new StringTokenizer(ctx.getCommandLine().assertOption(OPT_COMMAND.getName(), true).getValue().trim(), PIPE);
          while (tk.hasMoreTokens()) {
            processCommand(batch, tk.nextToken(), ctx);
          }
        } else {
          String scriptFilePath = ctx.getCommandLine().assertOption(OPT_SCRIPT.getName(), true).getValue();
          File scriptFile = ctx.getFileSystem().getFile(scriptFilePath);
          if (!ctx.getCommandLine().hasNext()) {
            throw new InputException("Path to script file expected");
          }
          String targetString = getTargetString(batch);
          ctx.getConsole().println("Rippling execution of script " + scriptFile + " against targets: " + targetString);
          processScript(scriptFile, ctx, batch);
        }
      } catch (FileNotFoundException e) {
        throw new InputException(e.getMessage());
      } catch (Throwable e) {
        ctx.getConsole().println("Unable to perform ripple operation:");
        e.printStackTrace();
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

  private void processScript(File scriptFile, CliContext ctx, List<CorusHost> batch) throws IOException, CommandNotFoundException, Throwable {
    if (scriptFile.exists()) {
      Interpreter interpreter = new Interpreter(ctx.getConsole(), ctx.getCorus());
      ClusterInfo cluster = new ClusterInfo(true);
      for (CorusHost h : batch) {
        cluster.addTarget(h.getEndpoint().getServerAddress());
      }
      interpreter.setAutoCluster(cluster);
      interpreter.interpret(new FileReader(scriptFile), ctx.getVars());
    } else {
      throw new FileNotFoundException("File not found: " + scriptFile.getAbsolutePath());
    }
  }

  private void processCommand(List<CorusHost> batch, String cmdLine, CliContext ctx) throws IOException, CommandNotFoundException, Throwable {
    Interpreter interpreter = new Interpreter(ctx.getConsole(), ctx.getCorus());
    ClusterInfo cluster = new ClusterInfo(true);
    for (CorusHost h : batch) {
      cluster.addTarget(h.getEndpoint().getServerAddress());
    }
    interpreter.setAutoCluster(cluster);
    ctx.getConsole().println(String.format("Rippling command to %s: %s", cluster.toString(), cmdLine));
    interpreter.eval(cmdLine, ctx.getVars());
  }

}
