package org.sapia.corus.deployer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.sapia.corus.admin.Arg;
import org.sapia.corus.admin.ArgFactory;
import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.exceptions.DuplicateDistributionException;
import org.sapia.corus.exceptions.LogicException;


/**
 * Holds <code>Distribution</code> instances.
 * 
 * @see org.sapia.corus.admin.services.deployer.dist.Distribution
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DistributionDatabaseImpl implements DistributionDatabase {
  private Map<String, Map<String, Distribution>> _distsByName = new TreeMap<String, Map<String, Distribution>>();

  /* (non-Javadoc)
   * @see org.sapia.corus.deployer.DistributionDatabase#addDistribution(org.sapia.corus.admin.services.deployer.dist.Distribution)
   */
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
  
  /* (non-Javadoc)
   * @see org.sapia.corus.deployer.DistributionDatabase#containsDistribution(org.sapia.corus.admin.CommandArg, org.sapia.corus.admin.CommandArg)
   */
  public synchronized boolean containsDistribution(Arg name, Arg version) {
    return select(name, version).size() > 0;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.deployer.DistributionDatabase#containsDistribution(java.lang.String, java.lang.String)
   */
  public synchronized boolean containsDistribution(String name, String version) {
    Map<String, Distribution> distsByVersion = (Map<String, Distribution>)_distsByName.get(name);
    if(distsByVersion == null){
      return false;
    }
    else{
      return distsByVersion.get(version) != null;
    }
  }  

  /* (non-Javadoc)
   * @see org.sapia.corus.deployer.DistributionDatabase#removeDistribution(org.sapia.corus.admin.CommandArg, org.sapia.corus.admin.CommandArg)
   */
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
  
  /* (non-Javadoc)
   * @see org.sapia.corus.deployer.DistributionDatabase#getDistributions()
   */
  public synchronized List<Distribution> getDistributions() {
    List<Distribution>   dists = new ArrayList<Distribution>();
    String name;

    for (Iterator<String> iter = _distsByName.keySet().iterator(); iter.hasNext();) {
      name = iter.next();
      dists.addAll(getDistributions(ArgFactory.parse(name)));
    }

    return dists;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.deployer.DistributionDatabase#getDistributions(org.sapia.corus.admin.CommandArg)
   */  
  public synchronized List<Distribution> getDistributions(Arg name) {
    List<Distribution> lst            = new ArrayList<Distribution>();
    lst.addAll(select(name, null));
    return lst;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.deployer.DistributionDatabase#getDistributions(org.sapia.corus.admin.CommandArg, org.sapia.corus.admin.CommandArg)
   */    
  public synchronized List<Distribution> getDistributions(Arg name, Arg version) {
    List<Distribution> lst            = new ArrayList<Distribution>();
    lst.addAll(select(name, version));
    return lst;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.deployer.DistributionDatabase#getDistribution(org.sapia.corus.admin.CommandArg, org.sapia.corus.admin.CommandArg)
   */   
  public synchronized Distribution getDistribution(Arg name, Arg version)
                                            throws LogicException {
    List<Distribution> dists = select(name, version);
    if(dists.size() == 0){
      throw new LogicException("No distribution for version " + version +
          " under " + name);
    }
    else if(dists.size() > 1){
      throw new LogicException("More than one distribution for version " + version +
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
