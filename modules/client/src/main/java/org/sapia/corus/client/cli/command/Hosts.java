package org.sapia.corus.client.cli.command;

import java.util.Collection;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.services.cluster.ServerHost;
import org.sapia.ubik.net.TCPAddress;


/**
 * @author Yanick Duchesne
 */
public class Hosts extends CorusCliCommand {
  static final int COL_HOST 		 = 0;
  static final int COL_PORT 		 = 1;
  static final int COL_OS_INFO 	 = 2;
  static final int COL_JAVA_INFO = 3;

  @Override
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    Collection<ServerHost> others = ctx.getCorus().getContext().getOtherHosts();
    displayHeader(ctx);
    displayHosts(others, ctx);
  }

  private void displayHosts(Collection<ServerHost> others, CliContext ctx) {
    Table      distTable = new Table(ctx.getConsole().out(), 4, 20);
    Row        row;
    TCPAddress addr;

    distTable.getTableMetaData().getColumnMetaDataAt(COL_HOST).setWidth(14);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_PORT).setWidth(8);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_OS_INFO).setWidth(20);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_JAVA_INFO).setWidth(30);

    distTable.drawLine('=');

    addr = (TCPAddress) ctx.getCorus().getContext().getAddress();
    row  = distTable.newRow();
    row.getCellAt(COL_HOST).append(addr.getHost());
    row.getCellAt(COL_PORT).append("" + addr.getPort());
    row.getCellAt(COL_OS_INFO).append(ctx.getCorus().getContext().getServerHost().getOsInfo());
    row.getCellAt(COL_JAVA_INFO).append(ctx.getCorus().getContext().getServerHost().getJavaVmInfo());
    row.flush();

    for(ServerHost other:others) {
      addr = (TCPAddress) other.getServerAddress();
      row  = distTable.newRow();
      row.getCellAt(COL_HOST).append(addr.getHost());
      row.getCellAt(COL_PORT).append("" + addr.getPort());
      row.getCellAt(COL_OS_INFO).append(other.getOsInfo());
      row.getCellAt(COL_JAVA_INFO).append(other.getJavaVmInfo());
      row.flush();
    }
  }

  private void displayHeader(CliContext ctx) {
    Table distTable;
    Row   headers;

    distTable = new Table(ctx.getConsole().out(), 4, 20);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_HOST).setWidth(14);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_PORT).setWidth(8);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_OS_INFO).setWidth(20);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_JAVA_INFO).setWidth(30);

    headers = distTable.newRow();

    headers.getCellAt(COL_HOST).append("Host");
    headers.getCellAt(COL_PORT).append("Port");
    headers.getCellAt(COL_OS_INFO).append("Operating System");
    headers.getCellAt(COL_JAVA_INFO).append("Java VM");
    headers.flush();
  }
}
