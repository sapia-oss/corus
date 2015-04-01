package org.sapia.corus.cloud.topology;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Stack;

public class XmlStreamWriter implements XmlStream {
 
  private static class State {
    
    private String elementName;
    private int    childCount     = 0;
    
    private State(String elementName) {
      this.elementName = elementName;
    }
    
    private void child() {
      childCount += 1;
    }
  }
  
  // --------------------------------------------------------------------------
 
  private int          indentation = 0;
  private boolean      prettyPrint;
  private Stack<State> state = new Stack<>();
  private PrintWriter  writer;

  public XmlStreamWriter(Writer  writer, boolean prettyPrint) {
    this.writer = new PrintWriter(writer, true);
    this.prettyPrint = prettyPrint;
  }
  
  @Override
  public void beginRootElement(String name) {
    State s = new State(name);
    state.push(s);
    indent("<" + name);
    indentation++;
  }
  
  @Override
  public void beginElement(String name) {
    State current = state.peek();
    current.child();
    println(">");
    
    State s = new State(name);
    state.push(s);
    indent("<" + name);
    indentation++;
  }
  
  @Override
  public void attribute(String name, int value) {
    writer.print(" " + name + "=\"" + value + "\"");
  }
  
  public void attribute(String name, String value) {
    writer.print(" " + name + "=\"" + value + "\"");
  }
  
  @Override
  public void cdata(String content) {
    indent(">" + content);
  }
  
  @Override
  public void endElement(String name) {
    indentation--;
    State s = state.pop();
    if (s == null) {
      throw new IllegalStateException("Invalid XML structure: end element </" + name + "> does not have matching start element");
    }
    if (!s.elementName.equals(name)) {
      throw new IllegalStateException("Invalid XML structure: end element </" + name + "> does not match start element <" + s.elementName + ">");
    }
    if (s.childCount == 0) {
      println(">");
    }
    indent("</" + name + ">");
    println();
  }

  @Override
  public void endRootElement(String name) {
    indentation--;
    State s = state.pop();
    if (s == null) {
      throw new IllegalStateException("Invalid XML structure: end element </" + name + "> does not have matching start element");
    }
    if (!s.elementName.equals(name)) {
      throw new IllegalStateException("Invalid XML structure: end element </" + name + "> does not match start element <" + s.elementName + ">");
    }
    if (s.childCount == 0) {
      println(">");
    }
    println("</" + name + ">");
  }
  
  // --------------------------------------------------------------------------
  // Private methods

  private void indent(String content) {
    if (prettyPrint) {
      for (int i = 0; i < indentation; i++) {
        writer.print("  ");
      }
    }
    writer.print(content);
  }
  
  private void println(String content) {
    if (prettyPrint) {
      writer.println(content);
    } else {
      writer.print(content);
    }
  }
  
  private void println() {
    if (prettyPrint) {
      writer.println();
    }
  }
}
