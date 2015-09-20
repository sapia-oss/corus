package org.sapia.corus.client.cli.command;

import java.util.List;
import java.util.UUID;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.OptionDef;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.TableDef;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.security.ApplicationKeyManager.AppKeyConfig;
import org.sapia.ubik.util.Collects;

/**
 * Manages application key.
 * 
 * @author yduchesne
 *
 */
public class Appkey extends CorusCliCommand {
  
  private final TableDef PERM_TBL  = TableDef.newInstance().createCol("appId", 19).createCol("role", 19).createCol("appKey", 34);
  private final TableDef TITLE_TBL = TableDef.newInstance().createCol("val", 78);
  
  private static final String ARG_ADD = "add";
  private static final String ARG_DEL = "del";
  private static final String ARG_LS  = "ls";
  
  private static final OptionDef OPT_APP  = new OptionDef("a", true);
  private static final OptionDef OPT_KEY  = new OptionDef("k", true);
  private static final OptionDef OPT_ROLE = new OptionDef("r", true);
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return Collects.arrayToList(OPT_APP, OPT_KEY, OPT_ROLE, OPT_CLUSTER);
  }
  
  @Override
  protected void doInit(CliContext context) {
    PERM_TBL.setTableWidth(context.getConsole().getWidth());
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
    
    String cmd = ctx.getCommandLine().assertNextArg(new String[] {ARG_ADD, ARG_DEL, ARG_LS}).getName();
    if (cmd.equals(ARG_ADD)) {
      doAdd(ctx);
    } else if (cmd.equals(ARG_LS)) {
      doList(ctx);
    } else {
      doDelete(ctx);
    }
  }
  
  private void doList(CliContext ctx) {
    ArgMatcher filter;
    if (ctx.getCommandLine().containsOption(OPT_APP.getName(), true)) {
      filter = ArgMatchers.parse(ctx.getCommandLine().getOptNotNull(OPT_APP.getName()).getValue());
    } else {
      filter = ArgMatchers.any();
    }
    Results<List<AppKeyConfig>> results = ctx.getCorus().getApplicationKeyManagementFacade().getAppKeyInfos(filter, getClusterInfo(ctx));
    
    while (results.hasNext()) {
      Result<List<AppKeyConfig>> result = results.next();
      displayHeader(result.getOrigin(), ctx);
      for (AppKeyConfig apk : result.getData()) {
        displayResult(apk, ctx);
      }
    }
  }
  
  private void doAdd(CliContext ctx) throws InputException {
    String appId  = ctx.getCommandLine().assertOption(OPT_APP.getName(), true).getValue();
    String appKey = getOpt(ctx, OPT_KEY.getName(), UUID.randomUUID().toString().toLowerCase().replace("-", "")).getValue();
    String role   = ctx.getCommandLine().assertOption(OPT_ROLE.getName(), true).getValue();
  
    ctx.getCorus().getApplicationKeyManagementFacade().createApplicationKey(appId, appKey, role, getClusterInfo(ctx));
  }
  
  private void doDelete(CliContext ctx) throws InputException {
    String appId  = ctx.getCommandLine().assertOption(OPT_APP.getName(), true).getValue();
    ctx.getCorus().getApplicationKeyManagementFacade().removeAppKey(ArgMatchers.parse(appId), getClusterInfo(ctx));
  }
  
  private void displayHeader(CorusHost addr, CliContext ctx) {
    Table titleTable   = TITLE_TBL.createTable(ctx.getConsole().out());
    Table headersTable = PERM_TBL.createTable(ctx.getConsole().out());

    titleTable.drawLine('=', 0, ctx.getConsole().getWidth());

    Row row = titleTable.newRow();
    row.getCellAt(TITLE_TBL.col("val").index()).append("Host: ").append(addr.getFormattedAddress());
    row.flush();

    titleTable.drawLine(' ', 0, ctx.getConsole().getWidth());

    Row headers = headersTable.newRow();

    headers.getCellAt(PERM_TBL.col("appId").index()).append("Application ID");
    headers.getCellAt(PERM_TBL.col("role").index()).append("Role");
    headers.getCellAt(PERM_TBL.col("appKey").index()).append("Application key");
    headers.flush();
  }

  private void displayResult(AppKeyConfig apk, CliContext ctx) {
    Table propsTable = PERM_TBL.createTable(ctx.getConsole().out());

    propsTable.drawLine('-', 0, ctx.getConsole().getWidth());

    Row row = propsTable.newRow();
    row.getCellAt(PERM_TBL.col("appId").index()).append(apk.getAppId());
    row.getCellAt(PERM_TBL.col("role").index()).append(apk.getRole());
    row.getCellAt(PERM_TBL.col("appKey").index()).append(apk.getApplicationKey());
    row.flush();
  }
}
