package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.services.cluster.ClusterStatus;
import org.sapia.ubik.net.TCPAddress;


/**
 * @author Yanick Duchesne
 */
public class Cluster extends CorusCliCommand {

	static final int COL_HOST 		 = 0;
  static final int COL_PORT 		 = 1;
  static final int COL_ROLE   	 = 2;

  @Override
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
  	
  	if(ctx.getCommandLine().hasNext()) {
  		ctx.getCommandLine().assertNextArg(new String[]{"status"});
  		displayStatus(ctx);
  	} else {
  		displayStatus(ctx);
  	}
  }

  private void displayStatus(CliContext ctx) {
    displayHeader(ctx);
    Results<ClusterStatus> results = ctx.getCorus().getCluster().getClusterStatus(getClusterInfo(ctx)); 
    Table      distTable = new Table(ctx.getConsole().out(), 4, 20);
    Row        row;
    TCPAddress addr;

    distTable.getTableMetaData().getColumnMetaDataAt(COL_HOST).setWidth(14);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_PORT).setWidth(8);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_ROLE).setWidth(30);

    distTable.drawLine('=');

    while(results.hasNext()) {
    	Result<ClusterStatus> status = results.next();
      addr = (TCPAddress) status.getOrigin();
      row  = distTable.newRow();
      row.getCellAt(COL_HOST).append(addr.getHost());
      row.getCellAt(COL_PORT).append("" + addr.getPort());
      row.getCellAt(COL_ROLE).append(status.getData().getRole().name());
      row.flush();
    }
  }

  private void displayHeader(CliContext ctx) {
    Table distTable;
    Row   headers;

    distTable = new Table(ctx.getConsole().out(), 4, 20);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_HOST).setWidth(14);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_PORT).setWidth(8);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_ROLE).setWidth(30);

    headers = distTable.newRow();

    headers.getCellAt(COL_HOST).append("Host");
    headers.getCellAt(COL_PORT).append("Port");
    headers.getCellAt(COL_ROLE).append("Role");
    headers.flush();
  }
}
