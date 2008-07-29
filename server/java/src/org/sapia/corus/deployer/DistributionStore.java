package org.sapia.corus.deployer;

import org.sapia.corus.LogicException;
import org.sapia.corus.admin.CommandArg;
import org.sapia.corus.admin.CommandArgParser;
import org.sapia.corus.deployer.config.Distribution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * Holds <code>Distribution</code> instances.
 * 
 * @see org.sapia.corus.deployer.config.Distribution
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DistributionStore {
  private Map _distsByName = new TreeMap();

  /**
   * Adds a distribution to this instance.
   * 
   * @param dist a <code>Distribution</code>.
   * @throws DuplicateDistributionException
   */
  public synchronized void addDistribution(Distribution dist)
                                    throws DuplicateDistributionException {
    Map distsByVersion;

    if ((distsByVersion = (Map) _distsByName.get(dist.getName())) == null) {
      distsByVersion = new TreeMap();
      _distsByName.put(dist.getName(), distsByVersion);
    }

    if (distsByVersion.get(dist.getVersion()) != null) {
      throw new DuplicateDistributionException("Deployment already exists for distribution: " +
                                               dist.getName() + ", version: " +
                                               dist.getVersion());
    }

    distsByVersion.put(dist.getVersion(), dist);
  }
  
  /**
   * Tests for the presence of a distribution within this instance.
   * 
   * @see Distribution
   * 
   * @param name the name of a distribution.
   * @param version the version of a distribution.
   * @return <code>true</code> if this instance contains the distribution correponding
   * to the given name and version.
   */
  public synchronized boolean containsDistribution(CommandArg name, CommandArg version) {
    return select(name, version).size() > 0;
  }

  /**
   * @param name a distribution name.
   * @param version a distribution version.
   * @return
   */
  public synchronized boolean containsDistribution(String name, String version) {
    Map distsByVersion = (Map)_distsByName.get(name);
    if(distsByVersion == null){
      return false;
    }
    else{
      return distsByVersion.get(version) != null;
    }
  }  

  /**
   * Removes the distribution that matches the given parameters from this instance.
   * 
   * @see Distribution
   * 
   * @param name the name of a distribution.
   * @param version the version of a distribution.
   * @return <code>true</code> if this instance contains the distribution correponding
   * to the given name and version.
   */
  public synchronized void removeDistribution(CommandArg name, CommandArg version) {
    Map distsByVersion;
    List dists = select(name, version);
    for(int i = 0; i < dists.size(); i++){
      Distribution dist = (Distribution)dists.get(i);
      if ((distsByVersion = (Map) _distsByName.get(dist.getName())) != null) {
        if (distsByVersion.get(dist.getVersion()) != null) {
          distsByVersion.remove(dist.getVersion());
          if (distsByVersion.size() == 0) {
            _distsByName.remove(dist.getName());
          }
        }
      }      
    }

  }
  
  /**
   * Returns a list of distributions.
   * 
   * @return a <code>List</code> of <code>Distribution</code>s.
   */
  public synchronized List getDistributions() {
    List   dists = new ArrayList();
    String name;

    for (Iterator iter = _distsByName.keySet().iterator(); iter.hasNext();) {
      name = (String) iter.next();
      dists.addAll(getDistributions(CommandArgParser.parse(name)));
    }

    return dists;
  }

  /**
   * Returns a list of distributions that match the given distribution
   * name.
   * 
   * @param name the name of the distribution.
   * @return a <code>List</code> of <code>Distribution</code>s.
   */  
  public synchronized List getDistributions(CommandArg name) {
    List lst            = new ArrayList();
    lst.addAll(select(name, null));
    return lst;
  }

  /**
   * Returns a list of distributions that match the given distribution
   * name and version.
   * 
   * @param name the name of the distribution.
   * @param version the version of the distribution.
   * @return a <code>List</code> of <code>Distribution</code>s.
   */    
  public synchronized List getDistributions(CommandArg name, CommandArg version) {
    List lst            = new ArrayList();
    lst.addAll(select(name, version));
    return lst;
  }

  /**
   * Returns the distribution that matches the given distribution
   * name and version.
   * 
   * @param name the name of the distribution.
   * @param version the version of the distribution.
   * 
   * @return a <code>Distribution</code>.
   */   
  public synchronized Distribution getDistribution(CommandArg name, CommandArg version)
                                            throws LogicException {
    List dists = select(name, version);
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
  
  private List select(CommandArg nameToken, CommandArg versionToken){
    Iterator names = _distsByName.keySet().iterator();
    List dists = new ArrayList();
    while(names.hasNext()){
      String name = (String)names.next();
      if(nameToken.matches(name)){
        Map distsByVersion = (Map)_distsByName.get(name);
        Iterator versions = distsByVersion.keySet().iterator();
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
