package org.sapia.corus.client.cli.command;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

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
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.sort.Sorting;
import org.sapia.ubik.concurrent.Spawn;
import org.sapia.ubik.net.ThreadInterruptedException;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.util.Collects;

/**
 * @author Yanick Duchesne
 */
public class Cluster extends CorusCliCommand {

  private static final int CLUSTER_CHECK_BATCH_SIZE = 10;
  
  private static final String STATUS = "status";
  private static final String CHECK  = "check";
  private static final String RESYNC = "resync";
  
  private static final OptionDef OPT_ASSERT = new OptionDef("assert", true);

  private static final TableDef STATUS_TBL = TableDef.newInstance()
      .createCol("host", 30)
      .createCol("role", 32);
  
  private static final TableDef CHECK_TBL = TableDef.newInstance()
      .createCol("host", 30)
      .createCol("role", 32)
      .createCol("status", 8);
  
  // --------------------------------------------------------------------------

  @Override
  protected void doInit(CliContext context) {
    STATUS_TBL.setTableWidth(context.getConsole().getWidth());
    CHECK_TBL.setTableWidth(context.getConsole().getWidth());
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    if (ctx.getCommandLine().hasNext()) {
      Arg arg = ctx.getCommandLine().assertNextArg(new String[] { STATUS, RESYNC, CHECK });
      if (arg.getName().equals(STATUS)) {
        status(ctx);
      } else if (arg.getName().equals(CHECK)) {
        check(ctx);
      } else {
        resync(ctx);
      }

    } else {
      status(ctx);
    }
  }
  
  @Override
  protected List<OptionDef> getAvailableOptions() {
    return Collects.arrayToList(OPT_ASSERT);
  }

  private void status(CliContext ctx) throws InputException {
    displayStatusHeader(ctx);
    Results<ClusterStatus> results = ctx.getCorus().getCluster().getClusterStatus(getClusterInfo(ctx));
    results = Sorting.sortSingle(results, ClusterStatus.class, ctx.getSortSwitches());
    Table table = STATUS_TBL.createTable(ctx.getConsole().out());

    table.drawLine('=', 0, ctx.getConsole().getWidth());

    while (results.hasNext()) {
      Result<ClusterStatus> status = results.next();
      Row row = table.newRow();
      row.getCellAt(STATUS_TBL.col("host").index()).append(status.getOrigin().getFormattedAddress());
      row.getCellAt(STATUS_TBL.col("role").index()).append(status.getData().getRole().name());
      row.flush();
    }
  }

  private void check(final CliContext ctx) throws InputException {
    Collection<CorusHost> otherHosts = ctx.getCorus().getContext().getOtherHosts();
    final CountDownLatch  countdown  = new CountDownLatch(otherHosts.size());
    final List<CorusHost> downHosts  = Collections.synchronizedList(new ArrayList<CorusHost>());
    final List<CorusHost> upHosts    = Collections.synchronizedList(new ArrayList<CorusHost>());

    for (final List<CorusHost> hosts: Collects.splitAsLists(otherHosts, CLUSTER_CHECK_BATCH_SIZE)) {
      Spawn.run(new Runnable() {
        @Override
        public void run() {
          for (CorusHost h : hosts) {
            try {
              Hub.connect(h.getEndpoint().getServerAddress());
              upHosts.add(h);
            } catch (RemoteException e) {
              downHosts.add(h);
            } finally {
              countdown.countDown();
            }
          }
        }
      });
    }
    
    try {
      countdown.await();
    } catch (InterruptedException e) {
      throw new ThreadInterruptedException();
    }
    
    if (ctx.getCommandLine().containsOption(OPT_ASSERT.getName(), true)) {
      int expectedUp = ctx.getCommandLine().assertOption(OPT_ASSERT.getName(), true).asInt();
      if (upHosts.size() != expectedUp) {
        throw new AbortException(String.format("Expected %s Corus nodes, got %s", expectedUp, upHosts.size()));
      }
    }
    
    displayCheckHeader(ctx);
    Table table = CHECK_TBL.createTable(ctx.getConsole().out());
    table.drawLine('=', 0, ctx.getConsole().getWidth());
    
    Collections.sort(upHosts, Sorting.getHostComparatorFor(ctx.getSortSwitches()));
    for (CorusHost h : upHosts) {
      Row row = table.newRow();
      row.getCellAt(CHECK_TBL.col("host").index()).append(h.getFormattedAddress());
      row.getCellAt(CHECK_TBL.col("role").index()).append(h.getRepoRole().name());
      row.getCellAt(CHECK_TBL.col("status").index()).append("UP");
      row.flush(); 
    }
    
    Collections.sort(downHosts, Sorting.getHostComparatorFor(ctx.getSortSwitches()));
    for (CorusHost h : downHosts) {
      Row row = table.newRow();
      row.getCellAt(CHECK_TBL.col("host").index()).append(h.getFormattedAddress());
      row.getCellAt(CHECK_TBL.col("role").index()).append(h.getRepoRole().name());
      row.getCellAt(CHECK_TBL.col("status").index()).append("DOWN");
      row.flush(); 
    }
    
  }
  
  private void displayStatusHeader(CliContext ctx) {
    Table table = STATUS_TBL.createTable(ctx.getConsole().out());
    Row headers = table.newRow();
    headers.getCellAt(STATUS_TBL.col("host").index()).append("Host");
    headers.getCellAt(STATUS_TBL.col("role").index()).append("Role");
    headers.flush();
  }
  
  private void displayCheckHeader(CliContext ctx) {
    Table table = CHECK_TBL.createTable(ctx.getConsole().out());
    Row headers = table.newRow();
    headers.getCellAt(CHECK_TBL.col("host").index()).append("Host");
    headers.getCellAt(CHECK_TBL.col("role").index()).append("Role");
    headers.getCellAt(CHECK_TBL.col("status").index()).append("Status");
    headers.flush();
  }
  
  private void resync(CliContext ctx) {
    ctx.getCorus().getCluster().resync();
  }
  
}
