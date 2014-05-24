package org.sapia.corus.client.cli.help;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.sapia.console.ConsoleOutput;
import org.sapia.console.table.Table;
import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;
import org.sapia.util.xml.ProcessingException;
import org.sapia.util.xml.confix.Dom4jProcessor;
import org.sapia.util.xml.confix.ReflectionFactory;

/**
 * @author Yanick Duchesne
 */
public class Help {

  private List<Section> sections = new ArrayList<Section>();

  public Section createSection() {
    Section s = new Section();
    sections.add(s);
    return s;
  }

  public void display(ConsoleOutput out) {
    Table t = new Table(out, 1, 78);
    Section sect;
    for (int i = 0; i < sections.size(); i++) {
      sect = (Section) sections.get(i);
      sect.display(t);
    }
  }

  public static Help newHelpFor(Class<?> caller) throws CorusException, NoHelpException {
    String className = caller.getName();
    int idx;
    if ((idx = className.lastIndexOf('.')) > -1) {
      className = className.substring(idx + 1);
    }
    className.replace('.', '/');
    InputStream input = caller.getResourceAsStream(className + ".help");
    if (input == null) {
      throw new NoHelpException();
    }
    Dom4jProcessor proc = new Dom4jProcessor(new ReflectionFactory(new String[] { "org.sapia.corus.client.cli.help" }));
    try {
      return (Help) proc.process(input);
    } catch (ProcessingException e) {
      throw new CorusException("Could not read command help", ExceptionCode.COMMAND_HELP_ERROR.getFullCode(), e);
    }
  }

}
