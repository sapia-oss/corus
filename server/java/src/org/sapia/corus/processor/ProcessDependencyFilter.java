package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.corus.admin.StringArg;
import org.sapia.corus.admin.services.deployer.Deployer;
import org.sapia.corus.admin.services.deployer.dist.Dependency;
import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.admin.services.deployer.dist.ProcessConfig;
import org.sapia.corus.admin.services.processor.Processor;
import org.sapia.corus.exceptions.LogicException;
import org.sapia.corus.util.ProgressQueue;

public class ProcessDependencyFilter {

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
      ProcessConfig conf, String profile) {
    this.rootProcesses.add(new ProcessRef(dist, conf, profile));
    return this;
  }

  public void filterDependencies(Deployer deployer, Processor processor) {
    DependencyGraphNode results = new DependencyGraphNode();
    
    for (ProcessRef rootProcess : rootProcesses) {
      DependencyGraphNode root = new DependencyGraphNode(rootProcess);
      doFilterDependencies(root, rootProcess.getDist()
          .getVersion(), rootProcess.getProfile(), deployer, processor);
      results.add(root);
    }
    Collection<ProcessRef> flattened = results.flatten();
    filteredProcesses.clear();
    filteredProcesses.addAll(flattened);
  }

  private void doFilterDependencies(DependencyGraphNode parentNode,
      String defaultVersion, String defaultProfile, Deployer deployer,
      Processor processor) {

    List<Dependency> deps = parentNode.getProcessRef().getProcessConfig().getDependenciesFor(defaultProfile);
    if (deps.size() > 0) {
      for (Dependency dep:deps) {
        if (dep.getDist() != null) {
          String currentVersion = dep.getVersion() != null ? dep.getVersion() : defaultVersion;
          String currentProfile = dep.getProfile() != null ? dep.getProfile() : defaultProfile;
          try {
            Distribution dist = deployer.getDistribution(new StringArg(
                dep.getDist()), new StringArg(currentVersion));
            if (dist != null) {
              ProcessConfig depProcess = dist.getProcess(dep.getProcess());
              if (depProcess != null) {
                if (depProcess.containsProfile(currentProfile)) {
                  if (processor.getProcesses(
                      new StringArg(dist.getName()),
                      new StringArg(currentVersion), currentProfile,
                      new StringArg(dep.getProcess())).size() == 0) {
                    ProcessRef ref = new ProcessRef(dist, depProcess, defaultProfile);
                    DependencyGraphNode childNode = new DependencyGraphNode(ref);
                    if (parentNode.add(ref)) {
                      doFilterDependencies(childNode, defaultVersion,
                          defaultProfile, deployer, processor);
                    }
                  } else {
                    progress
                        .warning("Process already running for distribution "
                            + dep.getDist() + ", version: " + currentVersion
                            + ", profile: " + currentProfile);
                  }
                } else {
                  progress
                      .warning("No profile found for dependency - distribution"
                          + dist + ", process: " + depProcess);
                }
              } else {
                progress.warning("No process found: " + dep.getProcess()
                    + " in distribution: " + dist);
              }
            } else {
              progress
                  .warning("No distribution found for dependency - distribution"
                      + dep.getDist() + ", version: " + currentVersion);
            }
          } catch (LogicException loe) {
            progress.warning("Could not find distribution for dependency: "
                + loe.getMessage());
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
