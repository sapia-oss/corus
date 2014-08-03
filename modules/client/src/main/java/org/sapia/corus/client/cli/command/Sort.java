package org.sapia.corus.client.cli.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.sapia.corus.client.sort.SortSwitchInfo;
import org.sapia.corus.client.sort.Sorting.SortSwitch;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

/**
 * Sets sort switches, allows pushing them to the server.
 * 
 * @author yduchesne
 *
 */
public class Sort extends NoOptionCommand { 
  
  private static final String ON    = "on";
  private static final String PUSH  = "push";
  private static final String OFF   = "off";
  private static final String CLEAR = "clear";
  private static final String LS    = "ls";
  
  private static final String SORT_DESCENDING = "d";
  
  private static final TableDef TBL = TableDef.newInstance()
        .createCol("value", 6)
        .createCol("sort",  6)
        .createCol("description", 45);
  
  @Override
  protected void doInit(CliContext context) {
    TBL.setTableWidth(context.getConsole().getWidth());
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
    
    Arg arg = ctx.getCommandLine().assertNextArg(new String[] {ON, PUSH, OFF, CLEAR, LS});
    
    if (arg.getName().equals(ON)) {
      Arg switchArg = ctx.getCommandLine().assertNextArg();
      List<SortSwitchInfo> sortedSwitches = processSwitches(ctx, getSwitches(switchArg.getName()));
      ctx.setSortSwitches(sortedSwitches.toArray(new SortSwitchInfo[sortedSwitches.size()]));
      
    } else if (arg.getName().equals(PUSH)) {
      Arg switchArg = ctx.getCommandLine().assertNextArg();
      List<SortSwitchInfo> sortedSwitches = processSwitches(ctx, getSwitches(switchArg.getName()));
      ctx.setSortSwitches(sortedSwitches.toArray(new SortSwitchInfo[sortedSwitches.size()]));
      
      StringBuilder toSave = new StringBuilder();
      for (int i = 0; i < sortedSwitches.size(); i++) {
        if (i > 0) {
          toSave.append(",");
        }
        toSave.append(sortedSwitches.get(i).toLiteral());
      }
      
      ctx.getCorus().getConfigFacade().addProperty(
          PropertyScope.SERVER, 
          CliPropertyKeys.SORT_SWITCHES, 
          toSave.toString(), 
          getClusterInfo(ctx)
      );
      
    } else if (arg.getName().equals(CLEAR)) {
      Set<SortSwitchInfo> switches = new HashSet<SortSwitchInfo>();
      ctx.setSortSwitches(switches.toArray(new SortSwitchInfo[switches.size()]));
      ctx.getCorus().getConfigFacade().removeProperty(
          PropertyScope.SERVER, 
          CliPropertyKeys.SORT_SWITCHES, 
          getClusterInfo(ctx)
      );
      
    } else if (arg.getName().equals(OFF)) {
      Arg switchArg = ctx.getCommandLine().assertNextArg();
      List<SortSwitchInfo> toTurnOff = getSwitches(switchArg.getName());
      List<SortSwitchInfo> currentlySet = Collects.arrayToList(ctx.getSortSwitches());
      currentlySet.removeAll(toTurnOff);
      ctx.setSortSwitches(currentlySet.toArray(new SortSwitchInfo[currentlySet.size()]));
      
    } else if (arg.getName().equals(LS)) {
      Table propsTable = TBL.createTable(ctx.getConsole().out());

      Row headers = propsTable.newRow();

      headers.getCellAt(TBL.col("value").index()).append("Value");
      headers.getCellAt(TBL.col("sort").index()).append("Sort");
      headers.getCellAt(TBL.col("description").index()).append("Description");
      headers.flush();      
      
      propsTable.drawLine('-', 0, ctx.getConsole().getWidth());

      for (SortSwitchInfo s : ctx.getSortSwitches()) {
        Row row = propsTable.newRow();
        row.getCellAt(TBL.col("value").index()).append(s.getSwitch().value());
        row.getCellAt(TBL.col("sort").index()).append(s.isAscending() ? "asc" : "desc");
        row.getCellAt(TBL.col("description").index()).append(s.getSwitch().description());
        row.flush();
      }
    }
    
  }
  
  private List<SortSwitchInfo> processSwitches(CliContext ctx, List<SortSwitchInfo> newSwitches) {
    Map<SortSwitchInfo, Integer> switchIndexes = new HashMap<SortSwitchInfo, Integer>();
    int index = 0;
    for (SortSwitchInfo s : ctx.getSortSwitches()) {
      switchIndexes.put(s, index++);
    }
    for (SortSwitchInfo s : newSwitches) {
      if (switchIndexes.containsKey(s)) {
        int currentIndex = switchIndexes.get(s);
        switchIndexes.remove(s);
        switchIndexes.put(s, currentIndex);
      } else {
        switchIndexes.put(s, index++);
      }
    }
    List<Map.Entry<SortSwitchInfo, Integer>> entries = new ArrayList<Map.Entry<SortSwitchInfo,Integer>>(switchIndexes.entrySet());
    Collections.sort(entries, new Comparator<Map.Entry<SortSwitchInfo, Integer>>() {
      @Override
      public int compare(Entry<SortSwitchInfo, Integer> o1, Entry<SortSwitchInfo, Integer> o2) {
        return o1.getValue() - o2.getValue();
      }
    });
    
    List<SortSwitchInfo> sortedSwitches = Collects.convertAsList(entries, new Func<SortSwitchInfo, Map.Entry<SortSwitchInfo, Integer>>() {
      @Override
      public SortSwitchInfo call(Entry<SortSwitchInfo, Integer> arg) {
        return arg.getKey();
      }
    });
    
    return sortedSwitches;
  }
  
  public static List<SortSwitchInfo> getSwitches(String switchString) {
    String[] valuesArr = switchString.split(",");
    List<SortSwitchInfo> switches = new ArrayList<SortSwitchInfo>();
    for (String v : valuesArr) {
      v = v.trim();
      int i = v.indexOf(":");
      if (i > 0 && v.length() >= 3) {
        SortSwitch s = SortSwitch.forValue(v.substring(0, i));
        if (s != null) {
          boolean ascending = !v.substring(i + 1).equals(SORT_DESCENDING);
          switches.add(new SortSwitchInfo(s, ascending));
        }
      } else {
        SortSwitch s = SortSwitch.forValue(v);
        if (s != null) {
          switches.add(new SortSwitchInfo(s, true));
        }
      }
    }
    return switches;
  }

}
