package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.StringArg;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Dependency;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.Processor;

/**
 * 
 * @author yduchesne
 * 
 */
public class ProcessDependencyFilter {

  /**
   * An interface that abstracts a {@link ProcessDependencyFilter} from
   * processes and distributions.
   */
  interface FilterCallback {

    /**
     * @param name
     *          an {@link ArgMatcher} corresponding to a distribution name.
     * @param version
     *          an {@link ArgMatcher} corresponding to a distribution version.
     * @return the {@link Distribution} corresponding to the given name and
     *         version.
     * @throws DistributionNotFoundException
     *           if no distribution is found for the given name and version.
     */
    public Distribution getDistribution(ArgMatcher name, ArgMatcher version) throws DistributionNotFoundException;

    /**
     * @param name
     *          an {@link ArgMatcher} corresponding to a distribution name.
     * @param version
     *          an {@link ArgMatcher} corresponding to a distribution version.
     * @param profile
     *          a profile.
     * @param processName
     *          an {@link ArgMatcher} corresponding to a process name.
     * @return the {@link List} of {@link Process} corresponding to the given
     *         parameters.
     */
    public List<org.sapia.corus.client.services.processor.Process> getProcesses(ArgMatcher name, ArgMatcher version, String profile, ArgMatcher processName);

  }

  private Set<ProcessRef> rootProcesses = new HashSet<ProcessRef>();
  private ProgressQueue progress;
  private List<ProcessRef> filteredProcesses = new ArrayList<ProcessRef>();

  /**
   * Creates an instance of this class wrapping the given {@link ProgressQueue},
   * which is internally used to notify about the filtering progress.
   * 
   * @param progress
   *          a {@link ProgressQueue}.
   */
  public ProcessDependencyFilter(ProgressQueue progress) {
    this.progress = progress;
  }

  /**
   * @return the {@link List} of filtered {@link ProcessRef}s.
   */
  public List<ProcessRef> getFilteredProcesses() {
    return filteredProcesses;
  }

  /**
   * Adds the root process to evaluate dependencies from.
   * 
   * @param dist
   *          a {@link Distribution}.
   * @param conf
   *          a {@link ProcessConfig}.
   * @param profile
   *          a profile.
   * @param instances
   *          the number of process instances.
   * @return this instance.
   */
  public ProcessDependencyFilter addRootProcess(Distribution dist, ProcessConfig conf, String profile, int instances) {
    this.rootProcesses.add(new ProcessRef(dist, conf, profile).setRoot(true).setInstanceCount(instances));
    return this;
  }

  /**
   * Performs dependency filtering using the given processor and deployer.
   * 
   * @param deployer
   *          a {@link Deployer} instance.
   * @param processor
   *          a {@link Processor} instance.
   */
  public void filterDependencies(final Deployer deployer, final Processor processor) {
    FilterCallback cb = new FilterCallback() {

      @Override
      public List<org.sapia.corus.client.services.processor.Process> getProcesses(ArgMatcher distName, ArgMatcher version, String profile, ArgMatcher processName) {
        ProcessCriteria criteria = ProcessCriteria.builder().distribution(distName).version(version).profile(profile).name(processName).build();
        return processor.getProcesses(criteria);
      }

      @Override
      public Distribution getDistribution(ArgMatcher name, ArgMatcher version) throws DistributionNotFoundException {
        return deployer.getDistribution(DistributionCriteria.builder().name(name).version(version).build());
      }
    };

    filterDependencies(cb);
  }

  /**
   * Performs dependency filtering using the given callback.
   * 
   * @param callback
   *          a {@link FilterCallback}
   */
  public void filterDependencies(FilterCallback callback) {

    DependencyGraphNode results = new DependencyGraphNode();

    for (ProcessRef rootProcess : rootProcesses) {
      DependencyGraphNode root = new DependencyGraphNode(rootProcess);
      doFilterDependencies(root, rootProcess.getDist().getVersion(), rootProcess.getProfile(), callback);
      results.add(root);
    }
    Collection<ProcessRef> flattened = results.flatten();
    filteredProcesses.clear();
    filteredProcesses.addAll(flattened);
  }

  private void doFilterDependencies(DependencyGraphNode parentNode, String defaultVersion, String defaultProfile, FilterCallback callback) {

    List<Dependency> deps = parentNode.getProcessRef().getProcessConfig().getDependenciesFor(defaultProfile);
    if (deps.size() > 0) {
      for (Dependency dep : deps) {
        if (dep.getDist() != null) {
          String currentVersion = dep.getVersion() != null ? dep.getVersion() : defaultVersion;
          String currentProfile = dep.getProfile() != null ? dep.getProfile() : defaultProfile;
          try {
            Distribution dist = callback.getDistribution(new StringArg(dep.getDist()), new StringArg(currentVersion));
            if (dist != null) {
              ProcessConfig depProcess = dist.getProcess(dep.getProcess());
              if (depProcess != null) {
                if (depProcess.containsProfile(currentProfile)) {
                  if (callback.getProcesses(new StringArg(dist.getName()), new StringArg(currentVersion), currentProfile,
                      new StringArg(dep.getProcess())).size() == 0) {
                    ProcessRef ref = new ProcessRef(dist, depProcess, currentProfile);
                    DependencyGraphNode childNode = new DependencyGraphNode(ref);
                    if (parentNode.add(ref)) {
                      doFilterDependencies(childNode, defaultVersion, currentProfile, callback);
                    }
                  } else {
                    progress.warning("Process already running for distribution " + dep.getDist() + ", version: " + currentVersion + ", profile: "
                        + currentProfile);
                  }
                } else {
                  progress.warning("Profile " + currentProfile + " not found for dependency - distribution " + dist + ", process: " + depProcess);
                }
              } else {
                progress.warning("No process found: " + dep.getProcess() + " in distribution: " + dist);
              }
            } else {
              progress.warning("No distribution found for dependency - distribution " + dep.getDist() + ", version: " + currentVersion);
            }
          } catch (DistributionNotFoundException dnfe) {
            progress.warning("Could not find distribution for dependency: " + dnfe.getMessage());
          }
        } else { // no distribution specified
          progress.warning("No distribution specified for dependency " + dep + " in " + parentNode.getProcessRef().getDist());
        }
      }
    }
  }

}
