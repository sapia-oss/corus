package org.sapia.corus.numa;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.configurator.InternalConfigurator;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.util.DynamicProperty;
import org.sapia.corus.util.DynamicProperty.DynamicPropertyListener;
import org.sapia.ubik.util.Assertions;

/**
 * Implementation of the NUMA module.
 *
 * @author jcdesrochers
 */
@Bind(moduleInterface = NumaModule.class)
public class NumaModuleImpl implements NumaModule {

  private final Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

  private final DynamicProperty<Boolean> enabled         = new DynamicProperty<Boolean>(false);
  private final DynamicProperty<Boolean> bindCpu         = new DynamicProperty<Boolean>(true);
  private final DynamicProperty<Boolean> bindMemory      = new DynamicProperty<Boolean>(true);
  private final DynamicProperty<Integer> firstNumaNodeId = new DynamicProperty<Integer>(new Integer(0));
  private final DynamicProperty<Integer> numaNodeCount   = new DynamicProperty<Integer>(new Integer(0));

  private final InternalConfigurator configurator;

  private final Processor processor;

  /**
   * Creates a new {@link NumaModuleImpl} instance.
   *
   * @param aConfigurator The configurator instance.
   * @para aStore The store of numa node bindings.
   */
  public NumaModuleImpl(InternalConfigurator aConfigurator, Processor aProcessor) {
    configurator = aConfigurator;
    processor = aProcessor;
  }

  @PostConstruct
  public void init() {
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_NUMA_ENABLED, enabled);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_NUMA_BIND_CPU, bindCpu);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_NUMA_BIND_MEMORY, bindMemory);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_NUMA_FIRST_NODE_ID, firstNumaNodeId);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_NUMA_NODE_COUNT, numaNodeCount);

    enabled.addListener(new DynamicPropertyListener<Boolean>() {
      @Override
      public void onModified(DynamicProperty<Boolean> property) {
        if (property.getValueNotNull()) {
          log.info("NUMA integration enabled");
        } else {
          log.info("NUMA integration disabled");
        }
      }
    });

    if (enabled.getValueNotNull()) {
      log.info("NUMA integration enabled");
    } else {
      log.info("NUMA integration disabled");
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled.getValue();
  }

  @Override
  public boolean isBindingCpu() {
    return bindCpu.getValue();
  }

  @Override
  public boolean isBindingMemory() {
    return bindMemory.getValue();
  }

  /**
   * Changes the value of the enabled attributes.
   *
   * @param isEnabled The new value.
   */
  public void setEnabled(boolean isEnabled) {
    enabled.setValue(isEnabled);
  }

  public void setBindingCpu(boolean isEnabled) {
    bindCpu.setValue(isEnabled);
  }

  public void setBindingMemory(boolean isEnabled) {
    bindMemory.setValue(isEnabled);
  }

  public void setFirstNumaNodeId(int nodeId) {
    firstNumaNodeId.setValue(nodeId);
  }

  public void setNumaNodeCount(int nodeCount) {
    numaNodeCount.setValue(nodeCount);
  }

  protected void doStartNuma() {
    // check is numa is supported

    // get number of numa cores

    // get first numa core id

  }

  @Override
  public Map<String, Integer> getProcessBindings() {
    Map<String, Integer> numaBindings = new LinkedHashMap<>();
    for (org.sapia.corus.client.services.processor.Process p: processor.getProcesses(ProcessCriteria.builder().all())) {
      if (p.getNumaNode() != null) {
        numaBindings.put(p.getProcessID(), p.getNumaNode());
      }
    }

    return numaBindings;
  }

  @Override
  public int getNextNumaNode() {
    Assertions.illegalState(!enabled.getValue(), "Can't bind process - the NUMA module is disabled");

    int numaNodeSelected;

    // 1. Find process count assigned per numa node
    int[] processByCoreId = new int[numaNodeCount.getValue()];
    for (org.sapia.corus.client.services.processor.Process p: processor.getProcesses(ProcessCriteria.builder().all())) {
      if (p.getNumaNode() != null) {
        processByCoreId[p.getNumaNode()] = 1 + processByCoreId[p.getNumaNode()];
      }
    }

    // 2. Find least used numa node
    int leastUsedCoreId = -1;
    int leastUsedCoreProcessCount = Integer.MAX_VALUE;
    for (int i = firstNumaNodeId.getValue(); i < processByCoreId.length; i++) {
      if (processByCoreId[i] < leastUsedCoreProcessCount) {
        leastUsedCoreId = i;
        leastUsedCoreProcessCount = processByCoreId[i];
      }
    }

    // 3. Assign numa node to process
    numaNodeSelected = leastUsedCoreId;

    return numaNodeSelected;
  }

}
