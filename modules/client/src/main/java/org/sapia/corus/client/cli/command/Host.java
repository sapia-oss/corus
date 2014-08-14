package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.TableDef;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.ubik.net.TCPAddress;

/**
 * Displays info about the host to which the CLI is currently connected.
 * 
 * @author Yanick Duchesne
 */
public class Host extends NoOptionCommand {
  
  private final TableDef TBL = TableDef.newInstance()
      .createCol("host", 10)
      .createCol("addr", 15)
      .createCol("os", 15)
      .createCol("java", 20)
      .createCol("repo", 10);
  
  @Override
  protected void doInit(CliContext context) {
    TBL.setTableWidth(context.getConsole().getWidth());
  }

  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    Table hostTable = TBL.createTable(ctx.getConsole().out());

    hostTable.drawLine('=', 0, ctx.getConsole().getWidth());
    
    CorusHost host = ctx.getCorus().getContext().getServerHost();
    Row row = hostTable.newRow();
    row.getCellAt(TBL.col("host").index()).append(host.getHostName());
    TCPAddress addr = host.getEndpoint().getServerTcpAddress();
    row.getCellAt(TBL.col("addr").index()).append(addr.getHost() + ":" + addr.getPort());
    row.getCellAt(TBL.col("os").index()).append(host.getOsInfo());
    row.getCellAt(TBL.col("java").index()).append(host.getJavaVmInfo());
    if (host.getRepoRole() == RepoRole.NONE) {
      row.getCellAt(TBL.col("repo").index()).append("n/a");
    } else {
      row.getCellAt(TBL.col("repo").index()).append(host.getRepoRole().name().toLowerCase());
    }
    row.flush();
  }
}
