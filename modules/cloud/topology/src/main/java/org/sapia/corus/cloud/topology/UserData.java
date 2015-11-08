package org.sapia.corus.cloud.topology;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows adding lines of user data.
 * 
 * @author yduchesne
 *
 */
public class UserData {

  private List<String> lines = new ArrayList<String>();
  
  public void addLine(String line) {
    lines.add(line);
  }
  
  public List<String> getLines() {
    return lines;
  }
  
  public void copyFrom(UserData other) {
    lines.addAll(other.lines);
  }
}
