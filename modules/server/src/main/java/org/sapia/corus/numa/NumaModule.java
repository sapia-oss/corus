package org.sapia.corus.numa;

import java.util.Map;

public interface NumaModule {

  public boolean isEnabled();

  public boolean isBindingCpu();

  public boolean isBindingMemory();

  public Map<String, Integer> getProcessBindings();

  public int getNextNumaNode();

}
