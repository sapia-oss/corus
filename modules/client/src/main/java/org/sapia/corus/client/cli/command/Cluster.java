package org.sapia.corus.client.cli.command;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.InputException;
import org.sapia.console.OptionDef;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Result.Type;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.TableDef;
import org.sapia.corus.client.common.CliUtil;
import org.sapia.corus.client.services.cluster.ClusterStatus;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.sort.Sorting;
import org.sapia.ubik.net.ThreadInterruptedException;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.threads.Threads;
import org.sapia.ubik.util.Collects;

/**
 * @author yduchesne
 */
public class Cluster extends CorusCliCommand {

  private static final int CLUSTER_CHECK_BATCH_SIZE = 10;
  
  private static final String STATUS = "status";
  private static final String CHECK  = "check";
  private static final String RESYNC = "resync";
  private static final String DOMAIN = "domain";
  private static final String REPO   = "repo";
  
  private static final OptionDef OPT_ASSERT     = new OptionDef("a", true);
  private static final OptionDef OPT_SEQUENTIAL = new OptionDef("seq", false);

  
  private final TableDef STATUS_TBL = TableDef.newInstance()
      .createCol("host", 70)
      .createCol("nodeCount", 6);
  
  private final TableDef CHECK_TBL = TableDef.newInstance()
      .createCol("host", 68)
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
      Arg arg = ctx.getCommandLine().assertNextArg(new String[] { STATUS, RESYNC, CHECK, DOMAIN, REPO });
      if (arg.getName().equals(STATUS)) {
        status(ctx);
      } else if (arg.getName().equals(CHECK)) {
        check(ctx);
      } else if (arg.getName().equals(DOMAIN)) {
        if (ctx.getCommandLine().hasNext() && ctx.getCommandLine().isNextArg()) {
          cluster(ctx, ctx.getCommandLine().assertNextArg().getName());
        } else {
          throw new InputException("New domain expected");
        }
      } else if (arg.getName().equals(REPO)) {
        if (ctx.getCommandLine().hasNext() && ctx.getCommandLine().isNextArg()) {
          String roleName = ctx.getCommandLine().assertNextArg().getName();
          if (roleName.equalsIgnoreCase(RepoRole.CLIENT.name())) {
            repo(ctx, RepoRole.CLIENT);
          } else if (roleName.equalsIgnoreCase(RepoRole.SERVER.name())) {
            repo(ctx, RepoRole.SERVER);
          } else if (roleName.equalsIgnoreCase(RepoRole.NONE.name())) {
            repo(ctx, RepoRole.NONE);
          } else {
            throw new InputException("Invalid repository role: " + roleName);
          } 
        } else {
          throw new InputException("New repository role expected");
        }
      } else {
        resync(ctx);
      }

    } else {
      status(ctx);
    }
  }
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return Collects.arrayToList(OPT_ASSERT, OPT_CLUSTER, OPT_SEQUENTIAL);
  }

  private void status(CliContext ctx) throws InputException {
    displayStatusHeader(ctx);
    
    Results<ClusterStatus> results;
    if (ctx.getCommandLine().containsOption(OPT_SEQUENTIAL.getName(), false)) {
      results = doSequentialExecution(ctx, host -> ctx.getCorus().getCluster().getClusterStatus(new ClusterInfo(false)).next());      
    } else {
      results = ctx.getCorus().getCluster().getClusterStatus(getClusterInfo(ctx));
    }
    
    results = Sorting.sortSingle(results, ClusterStatus.class, ctx.getSortSwitches());
    Table table = STATUS_TBL.createTable(ctx.getConsole().out());

    table.drawLine('=', 0, ctx.getConsole().getWidth());

    while (results.hasNext()) {
      Result<ClusterStatus> status = results.next();
      if (!status.isError()) {
        Row row = table.newRow();
        row.getCellAt(STATUS_TBL.col("host").index()).append(status.getOrigin().getFormattedAddress());
        row.getCellAt(STATUS_TBL.col("nodeCount").index()).append(Integer.toString(status.getData().getNodeCount()));
        row.flush();
      }
    }
  }
  
  private void cluster(CliContext ctx, String newClusterName) throws InputException {
    ctx.getCorus().getCluster().changeCluster(newClusterName, getClusterInfo(ctx));
    ctx.getCorus().getContext().reconnect();
    ctx.getConsole().setPrompt(CliUtil.getPromptFor(ctx.getCorus().getContext()));
  }
  
  private void repo(CliContext ctx, RepoRole newRole) throws InputException {
    ctx.getCorus().getRepoFacade().changeRole(newRole, getClusterInfo(ctx));
    ctx.getCorus().getContext().reconnect();
    ctx.getConsole().setPrompt(CliUtil.getPromptFor(ctx.getCorus().getContext()));
  }

  private void check(final CliContext ctx) throws InputException {
    Collection<CorusHost> otherHosts = ctx.getCorus().getContext().getOtherHosts();
    final CountDownLatch  countdown  = new CountDownLatch(otherHosts.size());
    final List<CorusHost> downHosts  = Collections.synchronizedList(new ArrayList<CorusHost>());
    final List<CorusHost> upHosts    = Collections.synchronizedList(new ArrayList<CorusHost>());

    for (final List<CorusHost> hosts: Collects.splitAsLists(otherHosts, CLUSTER_CHECK_BATCH_SIZE)) {
      Threads.getGlobalIoOutboundPool().submit(new Runnable() {
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
      row.getCellAt(CHECK_TBL.col("status").index()).append("UP");
      row.flush(); 
    }
    
    Collections.sort(downHosts, Sorting.getHostComparatorFor(ctx.getSortSwitches()));
    for (CorusHost h : downHosts) {
      Row row = table.newRow();
      row.getCellAt(CHECK_TBL.col("host").index()).append(h.getFormattedAddress());
      row.getCellAt(CHECK_TBL.col("status").index()).append("DOWN");
      row.flush(); 
    }
    
  }
  
  private void displayStatusHeader(CliContext ctx) {
    Table table = STATUS_TBL.createTable(ctx.getConsole().out());
    Row headers = table.newRow();
    headers.getCellAt(STATUS_TBL.col("host").index()).append("Host");
    headers.getCellAt(STATUS_TBL.col("nodeCount").index()).append("View");
    headers.flush();
  }
  
  private void displayCheckHeader(CliContext ctx) {
    Table table = CHECK_TBL.createTable(ctx.getConsole().out());
    Row headers = table.newRow();
    headers.getCellAt(CHECK_TBL.col("host").index()).append("Host");
    headers.getCellAt(CHECK_TBL.col("status").index()).append("Status");
    headers.flush();
  }
  
  private void resync(CliContext ctx) {
    if (ctx.getCommandLine().containsOption(OPT_SEQUENTIAL.getName(), false)) {
      doSequentialExecution(ctx, host -> { 
        ctx.getConsole().println(String.format("Resyncing cluster for host %s", host.getFormattedAddress()));
        ctx.getCorus().getCluster().resync();
        return null;
      });
    } else {
      ctx.getCorus().getCluster().resync();
    }
    
  }
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private <T> Results<T> doSequentialExecution(CliContext ctx, Function<CorusHost, Result<T>> execFunc) {
    ClusterInfo info = getClusterInfo(ctx);
    CorusHost current = ctx.getCorus().getContext().getServerHost();

    List<CorusHost> targets = new ArrayList<CorusHost>();
    targets.add(current);
    if (info.isClustered()) {
      // targeting all other hosts
      targets.addAll(ctx.getCorus().getContext().getOtherHosts());
    }
    
    Results<T> result = new Results<>();
    try {
      for (CorusHost host : targets) {
        try {
          ctx.getCorus().getContext().connect(
              host.getEndpoint().getServerTcpAddress().getHost(), 
              host.getEndpoint().getServerTcpAddress().getPort()
          );
          Result<T> r = execFunc.apply(host);
          if (result != null) {
            result.addResult(r);
          }
        } catch (Exception e) {
          result.addResult(new Result(host, e, Type.ELEMENT));
        }
      }
    } finally {
      ctx.getCorus().getContext().connect(
          current.getEndpoint().getServerTcpAddress().getHost(), 
          current.getEndpoint().getServerTcpAddress().getPort()
      );
    }
  
    return result;
  }

}
