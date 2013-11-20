package org.sapia.corus.client.cli.help;

import org.sapia.console.table.Row;
import org.sapia.console.table.Table;

/**
 * @author Yanick Duchesne
 *         <dl>
 *         <dt><b>Copyright:</b>
 *         <dd>Copyright &#169; 2002-2003 <a
 *         href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All
 *         Rights Reserved.</dd></dt>
 *         <dt><b>License:</b>
 *         <dd>Read the license.txt file of the jar or visit the <a
 *         href="http://www.sapia-oss.org/license.html">license page</a> at the
 *         Sapia OSS web site</dd></dt>
 *         </dl>
 */
public class Para {
  private String _text;

  public void setText(String text) {
    _text = text;
  }

  public void display(Table t) {
    Row r = t.newRow();
    if (_text == null) {
      _text = "";
    }

    r.getCellAt(0).append(Para.chopSpaces(_text));
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
