package org.sapia.corus.client.cli.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.sapia.corus.client.sort.Sorting;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Condition;

/**
 * Lists the Corus server nodes in the domain.
 * 
 * @author Yanick Duchesne
 */
public class Hosts extends NoOptionCommand {

  private final TableDef TBL = TableDef.newInstance()
      .createCol("host", 10)
      .createCol("addr", 15)
      .createCol("os", 15)
      .createCol("java", 20)
      .createCol("repo", 10);

  // --------------------------------------------------------------------------

  protected void doInit(CliContext context) {
    TBL.setTableWidth(context.getConsole().getWidth());
  }
  
  @Override
  protected void doExecute(final CliContext ctx) throws AbortException, InputException {
    Collection<CorusHost> others = ctx.getCorus().getContext().getOtherHosts();
    List<CorusHost> hosts = new ArrayList<CorusHost>();
    hosts.add(ctx.getCorus().getContext().getServerHost());
    hosts.addAll(others);
    hosts = Collects.filterAsList(hosts, new Condition<CorusHost>() {
      @Override
      public boolean apply(CorusHost item) {
        return item.matches(ctx.getCorus().getContext().getResultFilter());
      }
    });
    Collections.sort(hosts, Sorting.getHostComparatorFor(ctx.getSortSwitches()));
    displayHeader(ctx);
    displayHosts(hosts, ctx);
    FacadeInvocationContext.set(hosts);
  }

  private void displayHosts(Collection<CorusHost> hosts, CliContext ctx) {
    Table hostTable = TBL.createTable(ctx.getConsole().out());

    hostTable.drawLine('=', 0, ctx.getConsole().getWidth());

    for (CorusHost other : hosts) {
      Row row = hostTable.newRow();
      row.getCellAt(TBL.col("host").index()).append(other.getHostName());
      TCPAddress addr = other.getEndpoint().getServerTcpAddress();
      row.getCellAt(TBL.col("addr").index()).append(addr.getHost() + ":" + addr.getPort());
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
    headers.getCellAt(TBL.col("addr").index()).append("Addr");
    headers.getCellAt(TBL.col("os").index()).append("OS");
    headers.getCellAt(TBL.col("java").index()).append("JVM");
    headers.getCellAt(TBL.col("repo").index()).append("Repo");

    headers.flush();
  }
}
