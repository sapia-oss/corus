package org.sapia.corus.client.common;

import java.util.Properties;

/**
 * Supports tokenizing property literals, according to the following format:
 * <pre>
 * name-0=value-0,name-1=value-1,...name-N=value-N
 * </pre>
 * Note that the comma is used as a delimiter to separate multiple 
 * consecutive properties that are part of the same literal. If such a
 * comma is to be included in a given property value, it should be escaped:
 * <pre>
 * name-0=value-0-0\\,value0-1,name-1=value-1,...name-N=value-N
 * </pre>
 * Escaping is not needed if the property for which a value contains a comma is the last
 * property in the literal:
 * <pre>
 * name-0=value-0,name-1=value-1,name-2=value-2-0,value-2-1
 * </pre>
 * 
 * @author yduchesne
 *
 */

public class PropertiesTokenizer {

  private enum State {
    INITIAL,
    NAME,
    VALUE,
    TERMINATED;
  }
  
  private class PropertyStruct {
    private String name, value;
    
    private void clear() {
      name  = null;
      value = null;
    }
    
    private boolean isComplete() {
      return name != null && value != null;
    }
    
    private PairTuple<String, String> asTuple() {
      return new PairTuple<String, String>(name, value == null ? "" : value);
    }
    
  }
  
  // --------------------------------------------------------------------------
  // Instance state
  
  private State          state = State.INITIAL;
  private int            index;
  private String         toProcess;
  private StringBuilder  buffer = new StringBuilder();
  private PropertyStruct struct = new PropertyStruct();
  
  public PropertiesTokenizer(String toProcess) {
    this.toProcess = toProcess;
  }
  
  public Properties asProperties() {
    Properties toReturn = new Properties();
    while (hasNext()) {
      PairTuple<String, String> p = next();
      toReturn.put(p.getLeft(), p.getRight());
    }
    state = State.INITIAL;
    buffer.delete(0, buffer.length());
    struct.clear();
    return toReturn;
  }
  
  public boolean hasNext() {
    for (; index < toProcess.length() && !struct.isComplete() && state != State.TERMINATED; index++) {
      doProcess();
    }
   
    return struct.isComplete();
  }
  
  private void doProcess() {
    char c = toProcess.charAt(index);
    
    switch (state) {
      case INITIAL:
      case NAME:
        if (c == '=') {
          // end of name
          struct.name = buffer.toString();
          buffer.delete(0,  buffer.length());
          state = State.VALUE;
        } else {
          buffer.append(c);
        }
        break;
      case VALUE:
        if (c == ',') {
          // property delimiter
          if (index - 1 > 0) {
            char previous = toProcess.charAt(index - 1);
            if (previous == '\\') {
              // comma is escaped, so part of value
              buffer.delete(buffer.length() - 1, buffer.length());
              buffer.append(c);
            } else if (!isRemainingContainsPropertyName()) {
              // end of value
              buffer.append(toProcess.substring(index, toProcess.length()));
              struct.value = buffer.toString();
              buffer.delete(0,  buffer.length());
              state = State.TERMINATED;
            } else {
              // end of value
              struct.value = buffer.toString();
              buffer.delete(0,  buffer.length());
              state = State.NAME;
            }
          }
        } else if (c == '=') {
          // introduced to be backward compatible with escaping of '='
          // in property values (which is not required anymore)
          if (index - 1 > 0 && toProcess.charAt(index - 1) == '\\') {
            buffer.delete(buffer.length() - 1, buffer.length());
          }
          buffer.append(c);
        } else {
          buffer.append(c);
        }
        if (index == toProcess.length() - 1) {
          // end of value
          struct.value = buffer.toString();
          buffer.delete(0,  buffer.length());
          state = State.NAME;          
        }
        break;
      default:
        throw new IllegalStateException("State not handled: " + state);
    }
    
  }

  public PairTuple<String, String> next() {
    if (struct.isComplete()) {
      PairTuple<String, String> prop = struct.asTuple();
      struct.clear();
      buffer.delete(0,  buffer.length());
      return prop;
    } 
    throw new IllegalStateException("No more property could be parsed");
  }

  private boolean isRemainingContainsPropertyName() {
    for (int i = index + 1; i < toProcess.length(); i++) {
      if (toProcess.charAt(i) == '=') {
        if (i - 1 > 0) {
          char previous = toProcess.charAt(i - 1);
          if (previous == '\\') {
            continue;
          } else {
            return true;
          }
        } else {
          return true;
        }
      }
    }
    return false;
  }
  
  public static void main(String[] args) {
    System.out.println(System.getProperty("test"));
  }
  
}
