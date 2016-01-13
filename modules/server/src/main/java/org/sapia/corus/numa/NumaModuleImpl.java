package org.sapia.corus.numa;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.console.CmdLine;
import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.os.OsModule;
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

  private final DynamicProperty<Boolean> enabled               = new DynamicProperty<Boolean>(false);
  private final DynamicProperty<Boolean> bindCpu               = new DynamicProperty<Boolean>(true);
  private final DynamicProperty<Boolean> bindMemory            = new DynamicProperty<Boolean>(true);
  private final DynamicProperty<Integer> firstNumaNodeId       = new DynamicProperty<Integer>(0);
  private final DynamicProperty<Integer> numaNodeCountOverride = new DynamicProperty<Integer>(0);
  private final DynamicProperty<Boolean> autoDetectionEnabled  = new DynamicProperty<Boolean>(true);

  private final InternalConfigurator configurator;
  private final Processor processor;
  private final OsModule osModule;

  private OptionalValue<Integer> detectedNumaNodeCount = OptionalValue.none();

  /**
   * Creates a new {@link NumaModuleImpl} instance.
   *
   * @param aConfigurator The configurator instance.
   * @param aProcessor The internal processor.
   * @param anOsModule The OS module.
   */
  public NumaModuleImpl(InternalConfigurator aConfigurator, Processor aProcessor, OsModule anOsModule) {
    configurator = aConfigurator;
    processor = aProcessor;
    osModule = anOsModule;
  }

  @PostConstruct
  public void init() {
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_NUMA_ENABLED, enabled);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_NUMA_BIND_CPU, bindCpu);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_NUMA_BIND_MEMORY, bindMemory);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_NUMA_FIRST_NODE_ID, firstNumaNodeId);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_NUMA_NODE_COUNT, numaNodeCountOverride);
    configurator.registerForPropertyChange(CorusConsts.PROPERTY_CORUS_NUMA_AUTO_DETECT_ENABLED, autoDetectionEnabled);

    enabled.addListener(new DynamicPropertyListener<Boolean>() {
      @Override
      public void onModified(DynamicProperty<Boolean> property) {
        doHandleEnabledChange();
      }
    });

    autoDetectionEnabled.addListener(new DynamicPropertyListener<Boolean>() {
      @Override
      public void onModified(DynamicProperty<Boolean> property) {
        doHandleAutoDetectionEnabledChange();
      }
    });

    if (enabled.getValueNotNull()) {
      if (autoDetectionEnabled.getValueNotNull()) {
        log.info("NUMA integration enabled");
        doDetectNuma();
      } else {
        log.info("NUMA integration enabled with auto detection disabled");
      }
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

  public OptionalValue<Integer> getDetectedNumaNodeCount() {
    return detectedNumaNodeCount;
  }

  public void setEnabled(boolean isEnabled) {
    enabled.setValue(isEnabled);
  }

  public void setAutoDetectionEnabled(boolean isEnabled) {
    autoDetectionEnabled.setValue(isEnabled);
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
    numaNodeCountOverride.setValue(nodeCount);
  }

  protected void doHandleEnabledChange() {
    if (enabled.getValueNotNull()) {
      log.info("NUMA integration enabled");
      if (autoDetectionEnabled.getValueNotNull()) {
        doDetectNuma();
      }
    } else {
      log.info("NUMA integration disabled");
    }
  }

  protected void doHandleAutoDetectionEnabledChange() {
    if (autoDetectionEnabled.getValueNotNull()) {
      log.info("NUMA auto-detection enabled");
      if (enabled.getValueNotNull()) {
        doDetectNuma();
      }
    } else {
      log.info("NUMA auto-detection disabled");
      detectedNumaNodeCount = OptionalValue.none();
    }
  }

  protected void doDetectNuma() {
    log.info("Detecting NUMA support on this host...");

    // check is numa is supported
    CmdLine numaCmd = new CmdLine().addArg("numactl").addArg("--hardware");
    final StringBuilder numaOutput = new StringBuilder();
    final StringBuilder errorOutput = new StringBuilder();
    try {
      LogCallback cmdCallback = new LogCallback() {
          @Override
          public void info(String msg) {
            numaOutput.append(msg);
          }
          @Override
          public void error(String msg) {
            errorOutput.append(msg);
          }
          @Override
          public void debug(String msg) {
            numaOutput.append(msg);
          }
      };
      osModule.executeScript(cmdCallback, new File(System.getProperty("user.dir")), numaCmd);
    } catch (IOException e) {
      log.error("System error detecting NUMA support on this host", e);
    }

    if (StringUtils.isNotBlank(errorOutput.toString())) {
      log.warn("Unable to detect NUMA support on this host: error executing command\n" + errorOutput.toString());

    } else if (StringUtils.isBlank(numaOutput.toString())) {
      log.warn("Unable to detect NUMA support on this host: no output returned by the command");

    } else {
      Pattern pattern = Pattern.compile("available:(.+?)node");
      Matcher matcher = pattern.matcher(numaOutput.toString());
      if (matcher.find()) {
        String extractedValue = matcher.group(1);
        try {
          int numaNodeCount = Integer.parseInt(extractedValue.trim());
          detectedNumaNodeCount = OptionalValue.of(numaNodeCount);
          log.info("NUMA detected on this host with " + numaNodeCount + " nodes\n" + numaOutput.toString());

        } catch (NumberFormatException nfe) {
          log.warn("Unable to detect NUMA support on this host: unable to parse the number of available nodes\n" + numaOutput.toString());
        }

      } else {
        log.warn("Unable to detect NUMA support on this host: unable to find the number of available nodes\n" + numaOutput.toString());
      }
    }
  }

  @Override
  public Map<String, Integer> getProcessBindings() {
    Map<String, Integer> numaBindings = new LinkedHashMap<>();
    for (org.sapia.corus.client.services.processor.Process p: processor.getProcesses(ProcessCriteria.builder().all())) {
      if (p.getNumaNode().isSet()) {
        numaBindings.put(p.getProcessID(), p.getNumaNode().get());
      }
    }

    return numaBindings;
  }

  @Override
  public int getNextNumaNode() {
    Assertions.illegalState(!enabled.getValue(), "Can't bind process - the NUMA module is disabled");

    int numaNodeSelected;

    // 1. Find process count assigned per numa node
    int[] processByCoreId;
    if (numaNodeCountOverride.getValue() > 0) {
      processByCoreId = new int[numaNodeCountOverride.getValue()];
    } else if (detectedNumaNodeCount.isSet()) {
      processByCoreId = new int[detectedNumaNodeCount.get()];
    } else {
      throw new IllegalStateException("Can't bind process - NUMA support is not detected on this host and no manual override was set");
    }

    for (org.sapia.corus.client.services.processor.Process p: processor.getProcesses(ProcessCriteria.builder().all())) {
      if (p.getNumaNode().isSet()) {
        processByCoreId[p.getNumaNode().get()] = 1 + processByCoreId[p.getNumaNode().get()];
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
