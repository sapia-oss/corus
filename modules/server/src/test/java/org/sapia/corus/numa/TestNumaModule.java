package org.sapia.corus.numa;

import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.configurator.InternalConfigurator;

public class TestNumaModule extends NumaModuleImpl {

  public TestNumaModule(InternalConfigurator aConfigurator, Processor aProcessor) {
    super(aConfigurator, aProcessor);
    setEnabled(false);
    setBindingCpu(true);
    setBindingMemory(true);
    setFirstNumaNodeId(0);
    setNumaNodeCount(2);
  }

}
