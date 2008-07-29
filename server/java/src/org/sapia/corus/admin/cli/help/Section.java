package org.sapia.corus.admin.cli.help;

import java.util.ArrayList;
import java.util.List;

import org.sapia.console.table.Row;
import org.sapia.console.table.Table;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Section {
  private String _title;
  private List   _paras = new ArrayList();
  
  public void setTitle(String title){
    _title = title;
  }
  
  public String getTitle(){
    return _title;
  }  
  
  public Para createP(){
    Para p = new Para();
    _paras.add(p);
    return p;
  }
  
  public void display(Table t){
    t.newRow().flush();
    Row r = t.newRow();
    r.getCellAt(0).append(_title);
    r.flush();
    t.drawLine('=', 1, _title.length());
    t.newRow().flush();
    Para p;
    for(int i = 0; i < _paras.size(); i++){
      p = (Para)_paras.get(i);
      p.display(t);
    }
  }
}
