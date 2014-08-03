package org.sapia.corus.client.cli.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.cli.TableDef;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.exceptions.port.PortActiveException;
import org.sapia.corus.client.exceptions.port.PortRangeConflictException;
import org.sapia.corus.client.exceptions.port.PortRangeInvalidException;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.client.sort.Sorting;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.Func;

/**
 * Allows port management.
 * 
 * @author yduchesne
 */
public class Port extends CorusCliCommand {

  private final TableDef RANGE_TBL = TableDef.newInstance()
      .createCol("name", 10).createCol("range", 15)
      .createCol("avail", 24).createCol("active", 24);

  private TableDef TITLE_TBL = TableDef.newInstance()
      .createCol("val", 78);

  // --------------------------------------------------------------------------

  private static final String ADD = "add";
  private static final String DELETE = "del";
  private static final String RELEASE = "release";
  private static final String LIST = "ls";
  private static final OptionDef OPT_NAME  = new OptionDef("n", true);
  private static final OptionDef OPT_PROPS = new OptionDef("p", true);
  private static final OptionDef OPT_FORCE = new OptionDef("f", false);
  private static final OptionDef OPT_CLEAR = new OptionDef("clear", false);
  private static final OptionDef OPT_MIN   = new OptionDef("min", true);
  private static final OptionDef OPT_MAX   = new OptionDef("max", true);
  
  private static final List<OptionDef> AVAIL_OPTIONS = Collects.arrayToList(
      OPT_NAME, OPT_PROPS, OPT_FORCE, OPT_CLEAR, OPT_MIN, OPT_MAX, OPT_CLUSTER
  );

  // --------------------------------------------------------------------------
  
  @Override
  protected List<OptionDef> getAvailableOptions() {
    return AVAIL_OPTIONS;
  }
  
  @Override
  protected void doInit(CliContext context) {
    RANGE_TBL.setTableWidth(context.getConsole().getWidth());
    TITLE_TBL.setTableWidth(context.getConsole().getWidth());
  }

  @Override
  public void doExecute(CliContext ctx) throws InputException {
    CmdLine cmdLine = ctx.getCommandLine();
    Arg arg = cmdLine.assertNextArg(new String[] { ADD, DELETE, LIST, RELEASE });
    if (arg.toString().equals(ADD)) {
      doAdd(ctx, cmdLine);
    } else if (arg.toString().equals(DELETE)) {
      doDelete(ctx, cmdLine);
    } else if (arg.toString().equals(RELEASE)) {
      doRelease(ctx, cmdLine);
    } else {
      doList(ctx, cmdLine);
    }
  }

  @SuppressWarnings("unchecked")
  void doAdd(CliContext ctx, CmdLine cmd) throws InputException {

    try {
      if (cmd.containsOption(OPT_PROPS.getName(), true)) {

        String fileName = cmd.assertOption(OPT_PROPS.getName(), true).getValue();
        File propFile = new File(fileName);
        if (!propFile.exists()) {
          throw new InputException("File does not exist: " + fileName);
        }
        if (propFile.isDirectory()) {
          throw new InputException("File is a directory: " + fileName);
        }

        Properties props = new Properties();
        InputStream input = null;
        try {
          input = new FileInputStream(propFile);
          props.load(input);
          boolean clearExisting = ctx.getCommandLine().containsOption(OPT_CLEAR.getName(), false);

          List<PortRange> ranges = new ArrayList<PortRange>();
          Enumeration<String> names = (Enumeration<String>) props.propertyNames();
          while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String minMax = props.getProperty(name);
            if (minMax != null) {
              String[] minMaxArray = minMax.split(",");
              if (minMaxArray.length == 1) {
                minMaxArray = new String[] { minMaxArray[0], minMaxArray[0] };
              }
              if (minMaxArray.length != 2) {
                throw new InputException(String.format("Invalid min/max for port range %s, expected <min>,<max>, got %", name, minMax));
              }
              PortRange range = new PortRange(name, Integer.parseInt(minMaxArray[0]), Integer.parseInt(minMaxArray[1]));
              ranges.add(range);
            } else {
              throw new InputException(String.format("Min/max not specified for port range %s", name));
            }
          }
          ctx.getCorus().getPortManagementFacade().addPortRanges(ranges, clearExisting, getClusterInfo(ctx));
        } finally {
          try {
            input.close();
          } catch (IOException e) {
          }
        }
      } else {
        String name = cmd.assertOption(OPT_NAME.getName(), true).getValue();
        int min = cmd.assertOption(OPT_MIN.getName(), true).asInt();
        int max = cmd.assertOption(OPT_MAX.getName(), true).asInt();
        ctx.getCorus().getPortManagementFacade().addPortRange(name, min, max, getClusterInfo(ctx));
      }

    } catch (PortRangeConflictException e) {
      CliError err = ctx.createAndAddErrorFor(this, "Unable to add a port range", e);
      ctx.getConsole().println(err.getSimpleMessage());

    } catch (PortRangeInvalidException e) {
      CliError err = ctx.createAndAddErrorFor(this, "Unable to add a port range", e);
      ctx.getConsole().println(err.getSimpleMessage());
    } catch (IOException e) {
      CliError err = ctx.createAndAddErrorFor(this, e);
      ctx.getConsole().println(err.getSimpleMessage());
    }
  }

  void doDelete(CliContext ctx, CmdLine cmd) throws InputException {
    String name = cmd.assertOption(OPT_NAME.getName(), true).getValue();
    boolean force = false;
    if (cmd.containsOption(OPT_FORCE.getName(), false)) {
      force = true;
    }

    try {
      ctx.getCorus().getPortManagementFacade().removePortRange(name, force, getClusterInfo(ctx));

    } catch (PortActiveException e) {
      CliError err = ctx.createAndAddErrorFor(this, "Unable to delete a port range", e);
      ctx.getConsole().println(err.getSimpleMessage());
    }
  }

  void doRelease(CliContext ctx, CmdLine cmd) throws InputException {
    String name = cmd.assertOption(OPT_NAME.getName(), true).getValue();
    ctx.getCorus().getPortManagementFacade().releasePortRange(name, getClusterInfo(ctx));
  }

  void doList(CliContext ctx, CmdLine cmd) throws InputException {
    Results<List<PortRange>> res = ctx.getCorus().getPortManagementFacade().getPortRanges(getClusterInfo(ctx));
    res = Sorting.sortList(res, PortRange.class, ctx.getSortSwitches());
    displayResults(res, ctx);
  }

  private void displayResults(Results<List<PortRange>> res, CliContext ctx) throws InputException {
    String nameFilter = getOptValue(ctx, OPT_NAME.getName());
    if (nameFilter != null) {
      final org.sapia.corus.client.common.Arg pattern = ArgFactory.parse(nameFilter);
      res = res.filter(new Func<List<PortRange>, List<PortRange>>() {
        @Override
        public List<PortRange> call(List<PortRange> toFilter) {
          return Collects.filterAsList(toFilter, new Condition<PortRange>() {
            @Override
            public boolean apply(PortRange item) {
              return pattern.matches(item.getName());
            }
          });
        }
      });
    }
    while (res.hasNext()) {
      Result<List<PortRange>> result = res.next();
      displayHeader(result.getOrigin(), ctx);
      for (PortRange range : result.getData()) {
        displayPorts(range, ctx);
      }
    }
  }

  private void displayPorts(PortRange range, CliContext ctx) {
    Table table = RANGE_TBL.createTable(ctx.getConsole().out());

    table.drawLine('-', 0, ctx.getConsole().getWidth());

    Row row = table.newRow();
    row.getCellAt(RANGE_TBL.col("name").index()).append(range.getName());
    row.getCellAt(RANGE_TBL.col("range").index()).append(
        new StringBuffer("[").append(range.getMin()).append(" - ").append(range.getMax()).append("]").toString());
    row.getCellAt(RANGE_TBL.col("avail").index()).append(range.getAvailable().toString());
    row.getCellAt(RANGE_TBL.col("active").index()).append(range.getActive().toString());
    row.flush();
  }

  private void displayHeader(CorusHost addr, CliContext ctx) {
    Table ranges = RANGE_TBL.createTable(ctx.getConsole().out());
    Table title = TITLE_TBL.createTable(ctx.getConsole().out());

    title.drawLine('=', 0, ctx.getConsole().getWidth());

    Row row = title.newRow();
    row.getCellAt(TITLE_TBL.col("val").index()).append("Host: ").append(addr.getFormattedAddress());
    row.flush();

    title.drawLine(' ', 0, ctx.getConsole().getWidth());

    Row headers = ranges.newRow();
    headers.getCellAt(RANGE_TBL.col("name").index()).append("Name");
    headers.getCellAt(RANGE_TBL.col("range").index()).append("Range");
    headers.getCellAt(RANGE_TBL.col("avail").index()).append("Available");
    headers.getCellAt(RANGE_TBL.col("active").index()).append("Active");

    headers.flush();
  }

}
