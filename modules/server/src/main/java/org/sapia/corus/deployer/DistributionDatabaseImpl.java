package org.sapia.corus.deployer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.exceptions.deployer.DuplicateDistributionException;
import org.sapia.corus.client.services.deployer.dist.Distribution;


/**
 * Holds {@link Distribution} instances.
 *  
 * @author Yanick Duchesne
 */
public class DistributionDatabaseImpl implements DistributionDatabase {
  private Map<String, Map<String, Distribution>> _distsByName = new TreeMap<String, Map<String, Distribution>>();

  public synchronized void addDistribution(Distribution dist)
                                    throws DuplicateDistributionException {
    Map<String, Distribution> distsByVersion;

    if ((distsByVersion = _distsByName.get(dist.getName())) == null) {
      distsByVersion = new TreeMap<String, Distribution>();
      _distsByName.put(dist.getName(), distsByVersion);
    }

    if (distsByVersion.get(dist.getVersion()) != null) {
      throw new DuplicateDistributionException("Deployment already exists for distribution: " +
                                               dist.getName() + ", version: " +
                                               dist.getVersion());
    }

    distsByVersion.put(dist.getVersion(), dist);
  }
  
  public synchronized boolean containsDistribution(Arg name, Arg version) {
    return select(name, version).size() > 0;
  }

  public synchronized boolean containsDistribution(String name, String version) {
    Map<String, Distribution> distsByVersion = (Map<String, Distribution>)_distsByName.get(name);
    if(distsByVersion == null){
      return false;
    }
    else{
      return distsByVersion.get(version) != null;
    }
  }  

  public synchronized void removeDistribution(Arg name, Arg version) {
    Map<String, Distribution> distsByVersion;
    List<Distribution> dists = select(name, version);
    for(int i = 0; i < dists.size(); i++){
      Distribution dist = (Distribution)dists.get(i);
      if ((distsByVersion = _distsByName.get(dist.getName())) != null) {
        if (distsByVersion.get(dist.getVersion()) != null) {
          distsByVersion.remove(dist.getVersion());
          if (distsByVersion.size() == 0) {
            _distsByName.remove(dist.getName());
          }
        }
      }      
    }
  }
  
  public synchronized List<Distribution> getDistributions() {
    List<Distribution>   dists = new ArrayList<Distribution>();
    String name;

    for (Iterator<String> iter = _distsByName.keySet().iterator(); iter.hasNext();) {
      name = iter.next();
      dists.addAll(getDistributions(ArgFactory.parse(name)));
    }

    return dists;
  }

  public synchronized List<Distribution> getDistributions(Arg name) {
    List<Distribution> lst            = new ArrayList<Distribution>();
    lst.addAll(select(name, null));
    return lst;
  }

  public synchronized List<Distribution> getDistributions(Arg name, Arg version) {
    List<Distribution> lst            = new ArrayList<Distribution>();
    lst.addAll(select(name, version));
    return lst;
  }

  public synchronized Distribution getDistribution(Arg name, Arg version)
                                            throws DistributionNotFoundException {
    List<Distribution> dists = select(name, version);
    if(dists.size() == 0){
      throw new DistributionNotFoundException("No distribution for version " + version +
          " under " + name);
    }
    else if(dists.size() > 1){
      throw new DistributionNotFoundException("More than one distribution for version " + version +
          " under " + name);      
    }
    else{
      return (Distribution)dists.get(0);
    }
  }
  
  private List<Distribution> select(Arg nameToken, Arg versionToken){
    Iterator<String> names = _distsByName.keySet().iterator();
    List<Distribution> dists = new ArrayList<Distribution>();
    while(names.hasNext()){
      String name = (String)names.next();
      if(nameToken.matches(name)){
        Map<String, Distribution> distsByVersion = _distsByName.get(name);
        Iterator<String> versions = distsByVersion.keySet().iterator();
        while(versions.hasNext()){
          String version = (String)versions.next();
          if(versionToken == null || versionToken.matches(version)){
            Distribution dist = (Distribution)distsByVersion.get(version);
            dists.add(dist);
          }
        }
      }
    }
    return dists;
  }
  
}
