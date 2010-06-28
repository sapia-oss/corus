package org.sapia.corus.client.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.command.cron.CronWizard;
import org.sapia.corus.client.services.cron.CronJobInfo;
import org.sapia.ubik.net.ServerAddress;


/**
 * @author Yanick Duchesne
 */
public class Cron extends CorusCliCommand {
  public static final String STAR        = "*";
  public static final String ADD         = "add";
  public static final String LIST        = "list";
  public static final String LS          = "ls";
  public static final String REMOVE      = "delete";
  public static final String JOB_ID      = "i";
  static final int           COL_ID      = 0;
  static final int           COL_DIST    = 1;
  static final int           COL_VERSION = 2;
  static final int           COL_PROFILE = 3;
  static final int           COL_VM      = 4;
  static final int           COL_HOUR    = 5;
  static final int           COL_MINUTE  = 6;
  static final int           COL_DAY     = 7;
  static final int           COL_MONTH   = 8;
  static final int           COL_YEAR    = 9;
  static final String[]      DAYS        = new String[] {
                                             "Su", "Mo", "Tu", "We", "Th", "Fr",
                                             "Sa"
                                           };

  @Override
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    CmdLine    cmd = ctx.getCommandLine();
    CronWizard wiz = new CronWizard();

    if (cmd.hasNext() && cmd.isNextArg()) {
      Arg arg = (Arg) cmd.next();

      if (arg.getName().equals(LIST) || arg.getName().equals(LS)) {
        displayResults(ctx.getCorus().getCronFacade().getCronJobs(getClusterInfo(ctx)),
                       ctx);
      } else if (arg.getName().equals(REMOVE)) {
        ctx.getCorus().getCronFacade().removeCronJob(cmd.assertOption(JOB_ID, true).getValue());
      } else if (arg.getName().equals(ADD)) {
        wiz.execute(cmd, ctx);
      } else {
        throw new InputException("Unrecognized argument: " + arg.getName() +
                                 "; should be: add | list | delete ");
      }
    } else {
      throw new InputException("Missing argument; should be: add | list | delete ");
    }
  }

  private void displayResults(Results<List<CronJobInfo>> res, CliContext ctx) {

    while (res.hasNext()) {
      Result<List<CronJobInfo>> result = res.next();
      displayHeader(result.getOrigin(), ctx);
      for(CronJobInfo job:result.getData()){
        displayJob(job, ctx);
      }
    }
  }

  private void displayJob(CronJobInfo info, CliContext ctx) {
    Table cronTable = new Table(ctx.getConsole().out(), 10, 20);
    Row   row;

    cronTable.getTableMetaData().getColumnMetaDataAt(COL_ID).setWidth(17);
    cronTable.getTableMetaData().getColumnMetaDataAt(COL_DIST).setWidth(7);
    cronTable.getTableMetaData().getColumnMetaDataAt(COL_VERSION).setWidth(7);
    cronTable.getTableMetaData().getColumnMetaDataAt(COL_VM).setWidth(7);
    cronTable.getTableMetaData().getColumnMetaDataAt(COL_PROFILE).setWidth(10);
    cronTable.getTableMetaData().getColumnMetaDataAt(COL_HOUR).setWidth(2);
    cronTable.getTableMetaData().getColumnMetaDataAt(COL_MINUTE).setWidth(2);
    cronTable.getTableMetaData().getColumnMetaDataAt(COL_DAY).setWidth(2);
    cronTable.getTableMetaData().getColumnMetaDataAt(COL_MONTH).setWidth(2);
    cronTable.getTableMetaData().getColumnMetaDataAt(COL_YEAR).setWidth(4);

    cronTable.drawLine('-');

    row = cronTable.newRow();
    row.getCellAt(COL_ID).append(info.getId());
    row.getCellAt(COL_DIST).append(info.getDistribution());
    row.getCellAt(COL_VERSION).append(info.getVersion());
    row.getCellAt(COL_VM).append(info.getProcessName());
    row.getCellAt(COL_PROFILE).append(info.getProfile());
    row.getCellAt(COL_HOUR).append(doAppend(info.getHour()));
    row.getCellAt(COL_MINUTE).append(doAppend(info.getMinute()));

    if (info.getDayOfWeek() == CronJobInfo.UNDEFINED) {
      row.getCellAt(COL_DAY).append(STAR);
    } else {
      row.getCellAt(COL_DAY).append(DAYS[info.getDayOfWeek() - 1]);
    }

    row.getCellAt(COL_MONTH).append(doAppend(info.getMonth()));
    row.getCellAt(COL_YEAR).append(doAppend(info.getYear()));
    row.flush();
  }

  private static String doAppend(int value) {
    if (value == CronJobInfo.UNDEFINED) {
      return STAR;
    } else {
      String toReturn = Integer.toString(value);

      if (toReturn.length() < 2) {
        return "0" + toReturn;
      } else {
        return toReturn;
      }
    }
  }

  private void displayHeader(ServerAddress addr, CliContext ctx) {
    Table hostTable;
    Table infoTable;
    Row   row;
    Row   headers;

    hostTable = new Table(ctx.getConsole().out(), 1, 78);
    hostTable.drawLine('=');
    row = hostTable.newRow();
    row.getCellAt(0).append("Host: ").append(addr.toString());
    row.flush();

    hostTable.drawLine(' ');

    infoTable = new Table(ctx.getConsole().out(), 10, 20);
    infoTable.getTableMetaData().getColumnMetaDataAt(COL_ID).setWidth(17);
    infoTable.getTableMetaData().getColumnMetaDataAt(COL_DIST).setWidth(7);
    infoTable.getTableMetaData().getColumnMetaDataAt(COL_VERSION).setWidth(7);
    infoTable.getTableMetaData().getColumnMetaDataAt(COL_VM).setWidth(7);
    infoTable.getTableMetaData().getColumnMetaDataAt(COL_PROFILE).setWidth(10);
    infoTable.getTableMetaData().getColumnMetaDataAt(COL_HOUR).setWidth(2);
    infoTable.getTableMetaData().getColumnMetaDataAt(COL_MINUTE).setWidth(2);
    infoTable.getTableMetaData().getColumnMetaDataAt(COL_DAY).setWidth(2);
    infoTable.getTableMetaData().getColumnMetaDataAt(COL_MONTH).setWidth(2);
    infoTable.getTableMetaData().getColumnMetaDataAt(COL_YEAR).setWidth(4);

    headers = infoTable.newRow();

    headers.getCellAt(COL_ID).append("ID");
    headers.getCellAt(COL_DIST).append("Dist.");
    headers.getCellAt(COL_VERSION).append("Version");
    headers.getCellAt(COL_VM).append("Process   Name");
    headers.getCellAt(COL_PROFILE).append("Profile");
    headers.getCellAt(COL_HOUR).append("hh");
    headers.getCellAt(COL_MINUTE).append("mm");
    headers.getCellAt(COL_DAY).append("DD");
    headers.getCellAt(COL_MONTH).append("MM");
    headers.getCellAt(COL_YEAR).append("YYYY");
    headers.flush();
  }
}
