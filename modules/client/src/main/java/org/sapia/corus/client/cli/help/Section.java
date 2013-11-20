package org.sapia.corus.client.cli.help;

import java.util.ArrayList;
import java.util.List;

import org.sapia.console.table.Row;
import org.sapia.console.table.Table;

/**
 * @author Yanick Duchesne
 */
public class Section {
  private String _title;
  private List<Para> _paras = new ArrayList<Para>();

  public void setTitle(String title) {
    _title = title;
  }

  public String getTitle() {
    return _title;
  }

  public Para createP() {
    Para p = new Para();
    _paras.add(p);
    return p;
  }

  public void display(Table t) {
    t.newRow().flush();
    Row r = t.newRow();
    r.getCellAt(0).append(_title);
    r.flush();
    t.drawLine('=', 1, _title.length());
    t.newRow().flush();
    Para p;
    for (int i = 0; i < _paras.size(); i++) {
      p = (Para) _paras.get(i);
      p.display(t);
    }
  }
}
