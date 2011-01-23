package org.sapia.corus.client.cli.help;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.sapia.console.table.Table;
import org.sapia.corus.client.cli.command.Man;
import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;
import org.sapia.util.xml.ProcessingException;
import org.sapia.util.xml.confix.Dom4jProcessor;
import org.sapia.util.xml.confix.ReflectionFactory;

/**
 * @author Yanick Duchesne
 */
public class Help {
  
  private List<Section> _sections = new ArrayList<Section>();
  
  public Section createSection(){
    Section s = new Section();
    _sections.add(s);
    return s;
  }
  
  public void display(PrintWriter out){
    Table t = new Table(out, 1, 78);
    Section sect;
    for(int i = 0; i < _sections.size(); i++){
      sect = (Section)_sections.get(i);
      sect.display(t);
    }
  }
  
  public static Help newHelpFor(Class<?> caller)
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
      new String[]{"org.sapia.corus.client.cli.help"}));
      try{
        return (Help)proc.process(input);
      }catch(ProcessingException e){
        throw new CorusException("Could not read command help", ExceptionCode.COMMAND_HELP_ERROR.getFullCode(), e);
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
