package org.sapia.corus.client.cli.command;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.InputException;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.CliPropertyKeys;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.TableDef;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.sort.Sorting.SortSwitch;
import org.sapia.corus.client.sort.SortSwitchInfo;
import org.sapia.ubik.util.Collects;

/**
 * Sets sort switches, allows pushing them to the server.
 * 
 * @author yduchesne
 *
 */
public class Sort extends CorusCliCommand{ 
  
  private static final String ON    = "on";
  private static final String PUSH  = "push";
  private static final String OFF   = "off";
  private static final String CLEAR = "clear";
  private static final String LS    = "ls";
  
  private static final TableDef TBL = TableDef.newInstance()
        .createCol("value", 4)
        .createCol("sort",  5)
        .createCol("description", 45);
  
   
  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
    
    Arg arg = ctx.getCommandLine().assertNextArg(new String[] {ON, PUSH, OFF});
    
    if (arg.getName().equals(ON)) {
      Arg switchArg = ctx.getCommandLine().assertNextArg();
      Set<SortSwitchInfo> currentSwitches = Collects.arrayToSet(ctx.getSortSwitches());
      Set<SortSwitchInfo> switches = getSwitches(switchArg.getName());
      switches.addAll(currentSwitches);
      ctx.setSortSwitches(switches.toArray(new SortSwitchInfo[switches.size()]));
      
    } else if (arg.getName().equals(PUSH)) {
      Arg switchArg = ctx.getCommandLine().assertNextArg();
      Set<SortSwitchInfo> switches = getSwitches(switchArg.getName());
      ctx.setSortSwitches(switches.toArray(new SortSwitchInfo[switches.size()]));
      ctx.getCorus().getConfigFacade().addProperty(
          PropertyScope.SERVER, 
          CliPropertyKeys.SORT_SWITCHES, 
          switchArg.getName(), 
          getClusterInfo(ctx)
      );
      
    } else if (arg.getName().equals(CLEAR)) {
      Set<SortSwitchInfo> switches = new HashSet<SortSwitchInfo>();
      ctx.setSortSwitches(switches.toArray(new SortSwitchInfo[switches.size()]));
      
    } else if (arg.getName().equals(OFF)) {
      Arg switchArg = ctx.getCommandLine().assertNextArg();
      Set<SortSwitchInfo> toTurnOff = getSwitches(switchArg.getName());
      List<SortSwitchInfo> currentlySet = Arrays.asList(ctx.getSortSwitches());
      currentlySet.removeAll(toTurnOff);
      ctx.setSortSwitches(currentlySet.toArray(new SortSwitchInfo[currentlySet.size()]));
      
    } else if (arg.getName().equals(LS)) {
      Table propsTable = TBL.createTable(ctx.getConsole().out());

      Row headers = propsTable.newRow();

      headers.getCellAt(TBL.col("value").index()).append("Value");
      headers.getCellAt(TBL.col("sort").index()).append("Sort");
      headers.getCellAt(TBL.col("description").index()).append("Description");
      headers.flush();      
      
      propsTable.drawLine('-', 0, CONSOLE_WIDTH);

      for (SortSwitchInfo s : ctx.getSortSwitches()) {
        Row row = propsTable.newRow();
        row.getCellAt(TBL.col("value").index()).append(s.getSwitch().value());
        row.getCellAt(TBL.col("sort").index()).append(s.isAscending() ? "asc" : "desc");
        row.getCellAt(TBL.col("description").index()).append(s.getSwitch().description());
        row.flush();
      }
    }
    
  }
  
  public static Set<SortSwitchInfo> getSwitches(String switchString) {
    String[]         valuesArr = switchString.split(",");
    Set<SortSwitchInfo> switches  = new HashSet<SortSwitchInfo>();
    for (String v : valuesArr) {
      v = v.trim();
      int i = v.indexOf(":");
      if (i > 0 && v.length() == 3) {
        SortSwitch s = SortSwitch.forValue(new StringBuilder().append(v.charAt(0)).toString());
        if (s != null) {
          boolean ascending = v.charAt(2) != 'd';
          switches.add(new SortSwitchInfo(s, ascending));
        }
      } else {
        SortSwitch s = SortSwitch.forValue(v);
        switches.add(new SortSwitchInfo(s, true));
      }
    }
    return switches;
  }

}
