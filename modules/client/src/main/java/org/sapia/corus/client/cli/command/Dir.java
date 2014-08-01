package org.sapia.corus.client.cli.command;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.TableDef;

/**
 * Lists the files in the CLI's current directory.
 * 
 * @author yduchesne
 * 
 */
public class Dir extends NoOptionCommand {

  private static TableDef FILE_TBL = TableDef.newInstance().createCol("file", 25).createCol("type", 20).createCol("date", 31);

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    File[] files = ctx.getFileSystem().getBaseDir().listFiles();
    if (files != null && files.length > 0) {

      Table title = FILE_TBL.createTable(ctx.getConsole().out());
      Table content = FILE_TBL.createTable(ctx.getConsole().out());

      Row row = title.newRow();
      row.getCellAt(FILE_TBL.col("file").index()).append("Name");
      row.getCellAt(FILE_TBL.col("type").index()).append("Type");
      row.getCellAt(FILE_TBL.col("date").index()).append("Last Modified");
      row.flush();

      title.drawLine('-', 0, CONSOLE_WIDTH);

      List<File> toDisplay = Arrays.asList(files);
      Collections.sort(toDisplay, new FileComparator());
      for (File f : toDisplay) {
        row = content.newRow();
        row.getCellAt(FILE_TBL.col("file").index()).append(f.getName());
        row.getCellAt(FILE_TBL.col("type").index()).append(f.isDirectory() ? "directory" : "file");
        row.getCellAt(FILE_TBL.col("date").index()).append(new Date(f.lastModified()).toString());
        row.flush();
      }
    }
  }

  private static class FileComparator implements Comparator<File> {

    @Override
    public int compare(File o1, File o2) {
      if (o1.isDirectory() && !o2.isDirectory()) {
        return -1;
      } else if (!o1.isDirectory() && o2.isDirectory()) {
        return 1;
      } else {
        return o1.getName().compareTo(o2.getName());
      }
    }

  }
}
