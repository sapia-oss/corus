package org.sapia.corus.client.cli.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.TableDef;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.corus.client.services.security.SecurityModule.RoleConfig;
import org.sapia.ubik.util.Collects;

/**
 * Manages roles.
 * 
 * @author yduchesne
 *
 */
public class Role extends CorusCliCommand {
  
  private final TableDef PERM_TBL  = TableDef.newInstance().createCol("role", 38).createCol("permissions", 38);
  private final TableDef TITLE_TBL = TableDef.newInstance().createCol("val", 78);
  
  private static final String ARG_ADD = "add";
  private static final String ARG_DEL = "del";
  private static final String ARG_LS  = "ls";
  
  private static final OptionDef OPT_NAME       = new OptionDef("n", true);
  private static final OptionDef OPT_PERMISSION = new OptionDef("p", true);
  private static final OptionDef OPT_LENIENT    = new OptionDef("l", false);
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return Collects.arrayToList(OPT_NAME, OPT_PERMISSION, OPT_LENIENT, OPT_CLUSTER);
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
    Arg filter;
    if (ctx.getCommandLine().containsOption(OPT_NAME.getName(), true)) {
      filter = ArgFactory.parse(ctx.getCommandLine().getOptNotNull(OPT_NAME.getName()).getValue());
    } else {
      filter = ArgFactory.any();
    }
    Results<List<RoleConfig>> results = ctx.getCorus().getSecurityManagementFacade().getRoleConfig(filter, getClusterInfo(ctx));
    
    while (results.hasNext()) {
      Result<List<RoleConfig>> result = results.next();
      displayPermissionsHeader(result.getOrigin(), ctx);
      for (RoleConfig rc : result.getData()) {
        displayPermissions(rc, ctx);
      }
    }
  }
  
  private void doAdd(CliContext ctx) throws InputException {
    String role           = ctx.getCommandLine().assertOption(OPT_NAME.getName(), true).getValue();
    String permissionList = ctx.getCommandLine().assertOption(OPT_PERMISSION.getName(), true).getValue();
    Set<Permission> permissions = new HashSet<>();
    for (int i = 0; i < permissionList.length(); i++) {
      char c = permissionList.charAt(i);
      permissions.add(Permission.forAbbreviation(c));
    }
    ctx.getCorus().getSecurityManagementFacade().addOrUpdateRole(role, permissions, getClusterInfo(ctx));
  }
  
  private void doDelete(CliContext ctx) throws InputException {
    String role = ctx.getCommandLine().assertOption(OPT_NAME.getName(), true).getValue();
    if (ctx.getCommandLine().containsOption(OPT_LENIENT.getName(), false)) {
      try {
       ctx.getCorus().getSecurityManagementFacade().removeRole(role, getClusterInfo(ctx));
      } catch (IllegalArgumentException e) {
        // noop
      }
    } else {
      ctx.getCorus().getSecurityManagementFacade().removeRole(role, getClusterInfo(ctx));
    }
  }
  
  private void displayPermissionsHeader(CorusHost addr, CliContext ctx) {
    Table titleTable   = TITLE_TBL.createTable(ctx.getConsole().out());
    Table headersTable = PERM_TBL.createTable(ctx.getConsole().out());

    titleTable.drawLine('=', 0, ctx.getConsole().getWidth());

    Row row = titleTable.newRow();
    row.getCellAt(TITLE_TBL.col("val").index()).append("Host: ").append(addr.getFormattedAddress());
    row.flush();

    titleTable.drawLine(' ', 0, ctx.getConsole().getWidth());

    Row headers = headersTable.newRow();

    headers.getCellAt(PERM_TBL.col("role").index()).append("Role");
    headers.getCellAt(PERM_TBL.col("permissions").index()).append("Permissions");
    headers.flush();
  }

  private void displayPermissions(RoleConfig role, CliContext ctx) {
    Table propsTable = PERM_TBL.createTable(ctx.getConsole().out());

    propsTable.drawLine('-', 0, ctx.getConsole().getWidth());

    Row row = propsTable.newRow();
    row.getCellAt(PERM_TBL.col("role").index()).append(role.getRole());
    row.getCellAt(PERM_TBL.col("permissions").index()).append(permissionsToString(role.getPermissions()));
    row.flush();
  }
  
  private String permissionsToString(Set<Permission> perms) {
    List<Permission> sorted = new ArrayList<>(perms);
    Collections.sort(sorted);
    StringBuilder sb = new StringBuilder();
    for (Permission p : sorted) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(p.abbreviation());
    }
    return sb.toString();
  }
}
