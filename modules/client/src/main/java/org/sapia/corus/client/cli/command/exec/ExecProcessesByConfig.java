package org.sapia.corus.client.cli.command.exec;

import java.util.ArrayList;
import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.command.AbstractExecCommand;
import org.sapia.corus.client.cli.command.Exec;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.ProcessDef;

/**
 * Implements execution of processes by configuration (
 * <code>exec -e myExecConfig</code>).
 * 
 * @author yduchesne
 * 
 */
public class ExecProcessesByConfig extends AbstractExecCommand {
  
  @Override
  protected List<OptionDef> getAvailableOptions() {
    return new ArrayList<OptionDef>();
  }
  
  @Override
  protected void doInit(CliContext context) {
  }

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    ClusterInfo cluster = getClusterInfo(ctx);
    String configName = ctx.getCommandLine().assertOption(Exec.OPT_EXEC_CONFIG.getName(), true).getValue();
    displayProgress(ctx.getCorus().getProcessorFacade().execConfig(configName, cluster), ctx);

    // determining which hosts may have the running processes: they must have
    // the
    // distribution, and the tags corresponding to these processes
    Results<List<ExecConfig>> execResults = ctx.getCorus().getProcessorFacade().getExecConfigs(cluster);

    ExecConfig conf = null;
    while (execResults.hasNext()) {
      Result<List<ExecConfig>> execResult = execResults.next();
      for (ExecConfig e : execResult.getData()) {
        if (e.getName().equals(configName)) {
          conf = e;
        }
      }
    }

    Option waitOpt = getOpt(ctx, OPT_WAIT.getName());
    if (waitOpt != null) {
      for (ProcessDef pd : conf.getProcesses()) {
        ProcessCriteria criteria = ProcessCriteria.builder().distribution(pd.getDist()).name(pd.getName()).profile(conf.getProfile())
            .version(pd.getVersion()).build();

        waitForProcessStartup(ctx, criteria, pd.getInstances(), waitOpt.getValue() == null ? Exec.DEFAULT_EXEC_WAIT_TIME_SECONDS : waitOpt.asInt(),
            cluster);
      }
      ctx.getConsole().println("Process startup completed on all nodes");
    }
  }

}
