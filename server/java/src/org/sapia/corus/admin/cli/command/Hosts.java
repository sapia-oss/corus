package org.sapia.corus.admin.cli.command;

import java.util.Collection;
import java.util.Iterator;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.admin.cli.CliContext;
import org.sapia.corus.admin.cli.CliContextImpl;
import org.sapia.ubik.net.TCPAddress;


/**
 * @author Yanick Duchesne
 */
public class Hosts extends CorusCliCommand {
  static final int COL_HOST = 0;
  static final int COL_PORT = 1;

  @Override
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    Collection others = ctx.getCorus().getServerAddresses();
    displayHeader(ctx);
    displayHosts(others.iterator(), ctx);
  }

  private void displayHosts(Iterator itr, CliContext ctx) {
    Table      distTable = new Table(ctx.getConsole().out(), 2, 20);
    Row        row;
    TCPAddress addr;

    distTable.getTableMetaData().getColumnMetaDataAt(COL_HOST).setWidth(20);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_PORT).setWidth(8);

    distTable.drawLine('=');

    addr = (TCPAddress) ctx.getCorus().getServerAddress();
    row  = distTable.newRow();
    row.getCellAt(COL_HOST).append(addr.getHost());
    row.getCellAt(COL_PORT).append("" + addr.getPort());
    row.flush();

    while (itr.hasNext()) {
      addr = (TCPAddress) itr.next();
      row  = distTable.newRow();
      row.getCellAt(COL_HOST).append(addr.getHost());
      row.getCellAt(COL_PORT).append("" + addr.getPort());
      row.flush();
    }
  }

  private void displayHeader(CliContext ctx) {
    Table distTable;
    Row   headers;

    distTable = new Table(ctx.getConsole().out(), 2, 20);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_HOST).setWidth(20);
    distTable.getTableMetaData().getColumnMetaDataAt(COL_PORT).setWidth(8);

    headers = distTable.newRow();

    headers.getCellAt(COL_HOST).append("Host");
    headers.getCellAt(COL_PORT).append("Port");
    headers.flush();
  }
}
