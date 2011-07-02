package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.StringArg;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.dist.Dependency;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.Processor;

public class ProcessDependencyFilter {

  interface FilterCallback{
    
    public Distribution getDistribution(Arg name, Arg version) throws DistributionNotFoundException;
    
    public List<org.sapia.corus.client.services.processor.Process> getProcesses(Arg name, Arg version, String profile, Arg processName);
    
  }
  
  private Set<ProcessRef> rootProcesses = new HashSet<ProcessRef>();
  private ProgressQueue progress;
  private List<ProcessRef> filteredProcesses = new ArrayList<ProcessRef>();

  public ProcessDependencyFilter(ProgressQueue progress) {
    this.progress = progress;
  }

  public List<ProcessRef> getFilteredProcesses() {
    return filteredProcesses;
  }

  public ProcessDependencyFilter addRootProcess(Distribution dist,
      ProcessConfig conf, String profile, int instances) {
    this.rootProcesses.add(new ProcessRef(dist, conf, profile).setRoot(true).setInstanceCount(instances));
    return this;
  }

  public void filterDependencies(final Deployer deployer, final Processor processor) {
    FilterCallback cb = new FilterCallback() {
      
      @Override
      public List<org.sapia.corus.client.services.processor.Process> getProcesses(Arg distName, Arg version, String profile,
          Arg processName){
        return processor.getProcesses(distName, version, profile, processName);
      }
      
      @Override
      public Distribution getDistribution(Arg name, Arg version)
          throws DistributionNotFoundException {
        return deployer.getDistribution(name, version);
      }
    };
    
    filterDependencies(cb);
  }
  public void filterDependencies(FilterCallback callback) {

    DependencyGraphNode results = new DependencyGraphNode();
    
    for (ProcessRef rootProcess : rootProcesses) {
      DependencyGraphNode root = new DependencyGraphNode(rootProcess);
      doFilterDependencies(root, rootProcess.getDist()
          .getVersion(), rootProcess.getProfile(), callback);
      results.add(root);
    }
    Collection<ProcessRef> flattened = results.flatten();
    filteredProcesses.clear();
    filteredProcesses.addAll(flattened);
  }

  private void doFilterDependencies(DependencyGraphNode parentNode,
      String defaultVersion, String defaultProfile, FilterCallback callback) {

    List<Dependency> deps = parentNode.getProcessRef().getProcessConfig().getDependenciesFor(defaultProfile);
    if (deps.size() > 0) {
      for (Dependency dep:deps) {
        if (dep.getDist() != null) {
          String currentVersion = dep.getVersion() != null ? dep.getVersion() : defaultVersion;
          String currentProfile = dep.getProfile() != null ? dep.getProfile() : defaultProfile;
          try {
            Distribution dist = callback.getDistribution(new StringArg(
                dep.getDist()), new StringArg(currentVersion));
            if (dist != null) {
              ProcessConfig depProcess = dist.getProcess(dep.getProcess());
              if (depProcess != null) {
                if (depProcess.containsProfile(currentProfile)) {
                  if (callback.getProcesses(
                      new StringArg(dist.getName()),
                      new StringArg(currentVersion), currentProfile,
                      new StringArg(dep.getProcess())).size() == 0) {
                    ProcessRef ref = new ProcessRef(dist, depProcess, currentProfile);
                    DependencyGraphNode childNode = new DependencyGraphNode(ref);
                    if (parentNode.add(ref)) {
                      doFilterDependencies(childNode, defaultVersion,
                          currentProfile, callback);
                    }
                  } else {
                    progress
                        .warning("Process already running for distribution "
                            + dep.getDist() + ", version: " + currentVersion
                            + ", profile: " + currentProfile);
                  }
                } else {
                  progress
                      .warning("Profile " + currentProfile + " not found for dependency - distribution "
                          + dist + ", process: " + depProcess);
                }
              } else {
                progress.warning("No process found: " + dep.getProcess()
                    + " in distribution: " + dist);
              }
            } else {
              progress
                  .warning("No distribution found for dependency - distribution "
                      + dep.getDist() + ", version: " + currentVersion);
            }
          } catch (DistributionNotFoundException dnfe) {
            progress.warning("Could not find distribution for dependency: "
                + dnfe.getMessage());
          }
        }
        // no distribution specified
        else {
          progress.warning("No distribution specified for dependency "
              + dep + " in " + parentNode.getProcessRef().getDist());
        }
      }
    }
  }

}
