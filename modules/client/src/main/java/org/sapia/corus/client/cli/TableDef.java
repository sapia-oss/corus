package org.sapia.corus.client.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sapia.console.ConsoleOutput;
import org.sapia.console.table.Table;

/**
 * Holds a collection of {@link ColumnDef}s.
 * 
 * @author yduchesne
 * 
 */
public class TableDef {

  private List<ColumnDef> columns = new ArrayList<ColumnDef>();
  private Map<String, ColumnDef> columnsByName = new HashMap<String, ColumnDef>();

  /**
   * Internally creates a {@link ColumnDef}.
   * 
   * @param name
   *          the name of the column to create.
   * @param width
   *          the width of the column to create.
   * @return this instance.
   */
  public TableDef createCol(String name, int width) {
    ColumnDef col = new ColumnDef(name, columns.size(), width);
    columns.add(col);
    columnsByName.put(name, col);
    return this;
  }

  /**
   * @return the list of {@link ColumnDef} instances that this instance holds.
   */
  public List<ColumnDef> col() {
    return columns;
  }

  /**
   * @param name
   *          the name of the {@link ColumnDef} to return.
   * @return the {@link ColumnDef} for the given name.
   */
  public ColumnDef col(String name) {
    ColumnDef def = columnsByName.get(name);
    if (def == null) {
      throw new IllegalArgumentException("No column definition found for "
          + name);
    }
    return def;
  }

  /**
   * @param output
   *          the {@link ConsoleOutput} to use for displaying the data of the
   *          {@link Table} that this method returns.
   * @return a new {@link Table}.
   */
  public Table createTable(ConsoleOutput output) {
    Table table = new Table(output, columns.size(), 15);
    for (ColumnDef def : columns) {
      table.getTableMetaData().getColumnMetaDataAt(def.index()).setWidth(def.width());
    }
    return table;
  }

  /**
   * @return a new instance of this {@link ColumnDef}s.
   */
  public static TableDef newInstance() {
    return new TableDef();
  }
}
