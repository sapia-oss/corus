package org.sapia.corus.repository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.deployer.FileInfo;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.repository.RepoDistribution;
import org.sapia.ubik.net.ServerAddress;

/**
 * Keep the state of a given Pull process, mainly to synchronize execution when
 * multiple repo server are available to perform deployments.
 * 
 * The methods of this class are not thread safe. It is the responsibility of the client class to properly
 * acquire and release the lock on this state object to perform any mutation safelly. 
 * 
 * @author jcdesrochers
 */
public class PullProcessState {

  final private Semaphore lock = new Semaphore(1, true);
  final private Set<CorusHost> contactedRepoServers = new LinkedHashSet<>();
  final private Map<RepoDistribution, ServerAddress> discoveredDistributions = new LinkedHashMap<>();
  final private Map<ShellScript, ServerAddress> discoveredShellScripts = new LinkedHashMap<>(); 
  final private Map<FileInfo, ServerAddress> discoveredFiles = new LinkedHashMap<>(); 
  
  public void acquireLock() {
    lock.acquireUninterruptibly();
  }
  
  public void releaseLock() {
    lock.release();
  }
  
  public void reset() {
    contactedRepoServers.clear();
    discoveredDistributions.clear();
    discoveredShellScripts.clear();
    discoveredFiles.clear();
  }

  public boolean addContactedRepoServer(CorusHost repoServer) {
    return contactedRepoServers.add(repoServer);
  }
  
  public Collection<CorusHost> getContactedRepoServer() {
    return contactedRepoServers;
  }

  public boolean addDiscoveredDistributionFromHostIfAbsent(RepoDistribution dist, ServerAddress host) {
    return (discoveredDistributions.putIfAbsent(dist, host) == null);
  }
  
  public Collection<RepoDistribution> getDiscoveredDistributionsFromHost(ServerAddress host) {
    return discoveredDistributions.entrySet().stream().
        filter(entry -> entry.getValue().equals(host)).
        map(Map.Entry::getKey).
        collect(Collectors.toList());
  }

  public boolean addDiscoveredScriptFromHostIfAbsent(ShellScript script, ServerAddress host) {
    return (discoveredShellScripts.putIfAbsent(script, host) == null);
  }
  
  public Collection<ShellScript> getDiscoveredScriptsFromHost(ServerAddress host) {
    return discoveredShellScripts.entrySet().stream().
        filter(entry -> entry.getValue().equals(host)).
        map(Map.Entry::getKey).
        collect(Collectors.toList());
  }

  public boolean addDiscoveredFileFromHostIfAbsent(FileInfo file, ServerAddress host) {
    return (discoveredFiles.putIfAbsent(file, host) == null);
  }
  
  public Collection<FileInfo> getDiscoveredFilesFromHost(ServerAddress host) {
    return discoveredFiles.entrySet().stream().
        filter(entry -> entry.getValue().equals(host)).
        map(Map.Entry::getKey).
        collect(Collectors.toList());
  }
  
}
