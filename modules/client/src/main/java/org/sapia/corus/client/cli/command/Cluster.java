package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.InputException;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.TableDef;
import org.sapia.corus.client.services.cluster.ClusterStatus;
import org.sapia.ubik.net.TCPAddress;

/**
 * @author Yanick Duchesne
 */
public class Cluster extends CorusCliCommand {

  private static final String STATUS = "status";
  private static final String RESYNC = "resync";

  private static final TableDef TBL = TableDef.newInstance().createCol("host", 30).createCol("role", 32);

  // --------------------------------------------------------------------------

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    if (ctx.getCommandLine().hasNext()) {
      Arg arg = ctx.getCommandLine().assertNextArg(new String[] { STATUS, RESYNC });
      if (arg.getName().equals(STATUS)) {
        displayStatus(ctx);
      } else {
        resync(ctx);
      }

    } else {
      displayStatus(ctx);
    }
  }

  private void displayStatus(CliContext ctx) throws InputException {
    displayHeader(ctx);
    Results<ClusterStatus> results = ctx.getCorus().getCluster().getClusterStatus(getClusterInfo(ctx));
    Table table = TBL.createTable(ctx.getConsole().out());

    table.drawLine('=', 0, CONSOLE_WIDTH);

    while (results.hasNext()) {
      Result<ClusterStatus> status = results.next();
      TCPAddress addr = (TCPAddress) status.getOrigin();
      Row row = table.newRow();
      row.getCellAt(TBL.col("host").index()).append(ctx.getCorus().getContext().resolve(addr).getFormattedAddress());
      row.getCellAt(TBL.col("role").index()).append(status.getData().getRole().name());
      row.flush();
    }
  }

  private void resync(CliContext ctx) {
    ctx.getCorus().getCluster().resync();
  }

  private void displayHeader(CliContext ctx) {
    Table table = TBL.createTable(ctx.getConsole().out());
    Row headers = table.newRow();
    headers.getCellAt(TBL.col("host").index()).append("Host");
    headers.getCellAt(TBL.col("role").index()).append("Role");
    headers.flush();
  }
}
