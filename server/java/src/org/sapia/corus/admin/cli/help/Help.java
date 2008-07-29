package org.sapia.corus.admin.cli.help;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.sapia.console.table.Row;
import org.sapia.console.table.Table;
import org.sapia.corus.CorusException;
import org.sapia.corus.admin.cli.command.Man;
//import org.sapia.corus.admin.cli.command.Cron;
//import org.sapia.corus.admin.cli.command.Deploy;
//import org.sapia.corus.admin.cli.command.Exec;
//import org.sapia.corus.admin.cli.command.Exit;
//import org.sapia.corus.admin.cli.command.Host;
//import org.sapia.corus.admin.cli.command.Hosts;
//import org.sapia.corus.admin.cli.command.Kill;
//import org.sapia.corus.admin.cli.command.Suspend;
//import org.sapia.corus.admin.cli.command.Undeploy;
import org.sapia.util.xml.ProcessingException;
import org.sapia.util.xml.confix.Dom4jProcessor;
import org.sapia.util.xml.confix.ReflectionFactory;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Help {
  private List _sections = new ArrayList();
  
  public Section createSection(){
    Section s = new Section();
    _sections.add(s);
    return s;
  }
  
  public void display(PrintWriter out){
    Table t = new Table(out, 1, 78);
    Row r = t.newRow();
    Section sect;
    for(int i = 0; i < _sections.size(); i++){
      sect = (Section)_sections.get(i);
      sect.display(t);
    }
  }
  
  public static Help newHelpFor(Class caller)
  throws CorusException, NoHelpException{
    String className = caller.getName();
    int idx;
    if((idx = className.lastIndexOf('.')) > -1 ){
      className = className.substring(idx+1);
    }
    className.replace('.', '/');
    InputStream input = caller.getResourceAsStream(className + ".help");
    if(input == null){
      throw new NoHelpException();
    }
    Dom4jProcessor proc =
      new Dom4jProcessor(
      new ReflectionFactory(
      new String[]{"org.sapia.corus.admin.cli.help"}));
      try{
        return (Help)proc.process(input);
      }catch(ProcessingException e){
        throw new CorusException("Could not read command help for", e);
      }
  }
  
  public static void main(String[] args) {
    try {
      Help h = Help.newHelpFor(Man.class);
      h.display(new PrintWriter(System.out, true));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
