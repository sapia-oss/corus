package org.sapia.corus.processor.task;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.processor.ProcessorThrottleKeys;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskParams;
import org.sapia.corus.taskmanager.core.ThrottleKey;
import org.sapia.corus.taskmanager.core.Throttleable;

/**
 * This task pushes out to the manage processes any configuration update/deletion
 * that occurs in the corus server. The propertie update that are published are
 * limited to the ones in the process scope.  
 * 
 * @author jcdesrochers
 */
public class PublishConfigurationChangeTask extends Task<Void, TaskParams<List<Property>, List<Property>, Void, Void>> implements Throttleable {

  @Override
  public ThrottleKey getThrottleKey() {
    return ProcessorThrottleKeys.PROCESS_EXEC;
  }

  @Override
  public Void execute(TaskExecutionContext ctx, TaskParams<List<Property>, List<Property>, Void, Void> params) throws Throwable {
    ctx.info("Publishing configuration change to active processes...");

    ProcessRepository processRepo = ctx.getServerContext().getServices().getProcesses();
    Deployer deployer = ctx.getServerContext().getServices().getDeployer();

    List<Property> updatedProperties = params.getParam1();
    List<Property> deletedProperties = params.getParam2();

    if (CollectionUtils.isEmpty(updatedProperties) && CollectionUtils.isEmpty(deletedProperties)) {
      ctx.debug("No property change to publish");
      return null;
    }

    // 1. Query for active processes
    ProcessCriteria criteria = ProcessCriteria.builder()
        .lifecycles(LifeCycleStatus.ACTIVE, LifeCycleStatus.STALE)
        .build();
    List<Process> toNotify = processRepo.getProcesses(criteria);

    if (CollectionUtils.isEmpty(toNotify)) {
      ctx.debug("No running process found for process configuration update");
      return null;
    }
    
    // 2. Process each process to notify
    for (Process process: toNotify) {
      // 2a. Gather all categories related to the process
      Distribution distributionDef = deployer.getDistribution(DistributionCriteria.builder()
          .name(process.getDistributionInfo().getName())
          .version(process.getDistributionInfo().getVersion())
          .build());
      ProcessConfig processDef = distributionDef.getProcess(process.getDistributionInfo().getProcessName());
      
      Set<String> processCategories = new HashSet<>();
      processCategories.addAll(distributionDef.getPropertyCategories());
      processCategories.addAll(processDef.getPropertyCategories());

      // 2b. Process updated properties
      Map<String, Property> appliedUpdatedProperties = new LinkedHashMap<>(updatedProperties.size());
      for (Property property: updatedProperties) {
        if (!appliedUpdatedProperties.containsKey(property.getName())
            && (property.getCategory().isNull() ||  processCategories.contains(property.getCategory().get()))) {
          appliedUpdatedProperties.put(property.getName(), property);
        }
      }
      
      if (MapUtils.isNotEmpty(appliedUpdatedProperties)) {
        process.configurationUpdated(appliedUpdatedProperties.values());
        ctx.info(String.format("Added configuration update event for process: %s", process));
      }
      
      // 2c. Process deleted properties
      Map<String, Property> appliedDeletedProperties = new LinkedHashMap<>(deletedProperties.size());
      for (Property property: deletedProperties) {
        if (!appliedDeletedProperties.containsKey(property.getName())
            && (property.getCategory().isNull() ||  processCategories.contains(property.getCategory().get()))) {
          appliedDeletedProperties.put(property.getName(), property);
        }
      }

      if (MapUtils.isNotEmpty(appliedDeletedProperties)) {
        process.configurationDeleted(appliedDeletedProperties.values());
        ctx.info(String.format("Added configuration delete event for process: %s", process));
      }
    }

    return null;
  }
  
}
