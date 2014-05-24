package org.sapia.corus.client.cli.help;

import java.util.ArrayList;
import java.util.List;

import org.sapia.console.table.Row;
import org.sapia.console.table.Table;

/**
 * @author Yanick Duchesne
 */
public class Section {
  private String title;
  private List<Para> paras = new ArrayList<Para>();

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public Para createP() {
    Para p = new Para();
    paras.add(p);
    return p;
  }

  public void display(Table t) {
    t.newRow().flush();
    Row r = t.newRow();
    r.getCellAt(0).append(title);
    r.flush();
    t.drawLine('=', 1, title.length());
    t.newRow().flush();
    Para p;
    for (int i = 0; i < paras.size(); i++) {
      p = (Para) paras.get(i);
      p.display(t);
    }
  }
}
