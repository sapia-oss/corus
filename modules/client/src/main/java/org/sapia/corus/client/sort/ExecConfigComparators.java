package org.sapia.corus.client.sort;

import java.util.Comparator;

import org.sapia.corus.client.services.processor.ExecConfig;

public class ExecConfigComparators {

  private ExecConfigComparators() {
  }
  
  public static Comparator<ExecConfig> forName() {
    return new Comparator<ExecConfig>() {
      @Override
      public int compare(ExecConfig e1, ExecConfig e2) {
        return e1.getName().compareTo(e2.getName());
      }
    };
  }
  
  public static Comparator<ExecConfig> forProfile() {
    return new Comparator<ExecConfig>() {
      @Override
      public int compare(ExecConfig e1, ExecConfig e2) {
        return e1.getProfile().compareTo(e2.getProfile());
      }
    };
  }
}
