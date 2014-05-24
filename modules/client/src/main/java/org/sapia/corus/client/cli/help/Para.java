package org.sapia.corus.client.cli.help;

import org.sapia.console.table.Row;
import org.sapia.console.table.Table;

/**
 * @author Yanick Duchesne
 */
public class Para {
  private String text;
  private int    indent;

  public void setText(String text) {
    this.text = text;
  }
  
  public void setIndent(int indent) {
    this.indent = indent;
  }

  public void display(Table t) {
    Row r = t.newRow();
    if (text == null) {
      text = "";
    }
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < indent; i++) {
      s.append(" ");
    }
    s.append(Para.chopSpaces(text));
    r.getCellAt(0).append(s.toString());
    r.flush();
  }

  private static String chopSpaces(String from) {
    StringBuffer buf = new StringBuffer();
    char last = '.';
    char current;
    for (int i = 0; i < from.length(); i++) {
      current = from.charAt(i);
      if (from.charAt(i) == ' ' && last == ' ') {
        continue;
      } else if (((int) from.charAt(i)) == '\r') {
        if (last == ' ') {
          continue;
        }
        current = ' ';
      }
      last = current;
      buf.append(current);
    }
    return buf.toString();
  }

}
