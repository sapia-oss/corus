package org.sapia.corus.client.cli.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.TableDef;
import org.sapia.corus.client.facade.FacadeInvocationContext;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.ubik.net.TCPAddress;

/**
 * Lists the Corus server nodes in the domain.
 * 
 * @author Yanick Duchesne
 */
public class Hosts extends CorusCliCommand {
  
  private static final TableDef TBL = TableDef.newInstance()
      .createCol("host", 14)
      .createCol("port", 8)
      .createCol("os", 15)
      .createCol("java", 25)
      .createCol("repo", 10);
  
  // --------------------------------------------------------------------------
  
  @Override
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    Collection<CorusHost> others = ctx.getCorus().getContext().getOtherHosts();
    displayHeader(ctx);
    displayHosts(others, ctx);
    List<CorusHost> hosts = new ArrayList<CorusHost>();
    hosts.add(ctx.getCorus().getContext().getServerHost());
    hosts.addAll(others);
    FacadeInvocationContext.set(hosts);
  }

  private void displayHosts(Collection<CorusHost> others, CliContext ctx) {
    Table      hostTable = TBL.createTable(ctx.getConsole().out());

    hostTable.drawLine('=', 0, CONSOLE_WIDTH);

    TCPAddress addr = (TCPAddress) ctx.getCorus().getContext().getAddress();
    Row row  = hostTable.newRow();
    row.getCellAt(TBL.col("host").index()).append(addr.getHost());
    row.getCellAt(TBL.col("port").index()).append("" + addr.getPort());
    row.getCellAt(TBL.col("os").index()).append(ctx.getCorus().getContext().getServerHost().getOsInfo());
    row.getCellAt(TBL.col("java").index()).append(ctx.getCorus().getContext().getServerHost().getJavaVmInfo());
    if (ctx.getCorus().getContext().getServerHost().getRepoRole() == RepoRole.NONE) {
      row.getCellAt(TBL.col("repo").index()).append("n/a");        
    } else {
      row.getCellAt(TBL.col("repo").index()).append(ctx.getCorus().getContext().getServerHost().getRepoRole().name().toLowerCase());        
    }    

    row.flush();

    for(CorusHost other:others) {
      addr = (TCPAddress) other.getEndpoint().getServerAddress();
      row  = hostTable.newRow();
      row.getCellAt(TBL.col("host").index()).append(addr.getHost());
      row.getCellAt(TBL.col("port").index()).append("" + addr.getPort());
      row.getCellAt(TBL.col("os").index()).append(other.getOsInfo());
      row.getCellAt(TBL.col("java").index()).append(other.getJavaVmInfo());
      if (other.getRepoRole() == RepoRole.NONE) {
        row.getCellAt(TBL.col("repo").index()).append("n/a");        
      } else {
        row.getCellAt(TBL.col("repo").index()).append(other.getRepoRole().name().toLowerCase());        
      } 
      row.flush();
    }
  }

  private void displayHeader(CliContext ctx) {
    Table distTable = TBL.createTable(ctx.getConsole().out());
    
    Row headers = distTable.newRow();
    headers.getCellAt(TBL.col("host").index()).append("Host");
    headers.getCellAt(TBL.col("port").index()).append("Port");
    headers.getCellAt(TBL.col("os").index()).append("OS");
    headers.getCellAt(TBL.col("java").index()).append("JVM");
    headers.getCellAt(TBL.col("repo").index()).append("Repo");

    headers.flush();
  }
}
