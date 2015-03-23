package org.sapia.corus.deployer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.exceptions.deployer.DuplicateDistributionException;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;

/**
 * Implements the {@link DistributionDatabase} interface over an in-memory
 * {@link Map}.
 * 
 * @author Yanick Duchesne
 */
public class DistributionDatabaseImpl implements DistributionDatabase {

  private Map<String, Map<String, Distribution>> distsByName = new TreeMap<String, Map<String, Distribution>>();

  public synchronized void addDistribution(Distribution dist) throws DuplicateDistributionException {
    Map<String, Distribution> distsByVersion;

    if ((distsByVersion = distsByName.get(dist.getName())) == null) {
      distsByVersion = new TreeMap<String, Distribution>();
      distsByName.put(dist.getName(), distsByVersion);
    }

    if (distsByVersion.get(dist.getVersion()) != null) {
      throw new DuplicateDistributionException("Deployment already exists for distribution: " + dist.getName() + ", version: " + dist.getVersion());
    }
    
    if (dist.getTimestamp() == 0) {
      dist.setTimestamp(System.currentTimeMillis());
    }

    distsByVersion.put(dist.getVersion(), dist);
  }

  @Override
  public synchronized boolean containsDistribution(DistributionCriteria criteria) {
    return select(criteria).size() > 0;
  }

  @Override
  public synchronized void removeDistribution(DistributionCriteria criteria) {
    Map<String, Distribution> distsByVersion;
    List<Distribution> dists = select(criteria);
    for (int i = 0; i < dists.size(); i++) {
      Distribution dist = (Distribution) dists.get(i);
      if ((distsByVersion = distsByName.get(dist.getName())) != null) {
        if (distsByVersion.get(dist.getVersion()) != null) {
          distsByVersion.remove(dist.getVersion());
          if (distsByVersion.size() == 0) {
            distsByName.remove(dist.getName());
          }
        }
      }
    }
  }

  @Override
  public synchronized List<Distribution> getDistributions(DistributionCriteria criteria) {
    List<Distribution> lst = new ArrayList<Distribution>();
    lst.addAll(select(criteria));
    return lst;
  }

  @Override
  public synchronized Distribution getDistribution(DistributionCriteria criteria) throws DistributionNotFoundException {
    List<Distribution> dists = select(criteria);
    if (dists.size() == 0) {
      throw new DistributionNotFoundException(String.format("No distribution for version %s under %s", criteria.getName(), criteria.getVersion()));
    } else if (dists.size() > 1) {
      throw new DistributionNotFoundException(String.format("More than one distribution for version %s under %s", criteria.getName(),
          criteria.getVersion()));
    } else {
      return (Distribution) dists.get(0);
    }
  }

  private List<Distribution> select(DistributionCriteria criteria) {
    Iterator<String> names = distsByName.keySet().iterator();
    List<Distribution> dists = new ArrayList<Distribution>();
    while (names.hasNext()) {
      String name = (String) names.next();
      if (criteria.getName().matches(name)) {
        Map<String, Distribution> distsByVersion = distsByName.get(name);
        Iterator<String> versions = distsByVersion.keySet().iterator();
        while (versions.hasNext()) {
          String version = (String) versions.next();
          Distribution dist = (Distribution) distsByVersion.get(version);
          if (dist.matches(criteria)) {
            dists.add(dist);
          }
        }
      }
    }
    if (criteria.getBackup() > 0) {
      Map<String, ArrayList<Distribution>> distsByName = new HashMap<>();
      
      Collections.sort(dists);
      
      for(Distribution d : dists) {
        ArrayList<Distribution> distsForName = distsByName.get(d.getName());
        if (distsForName == null) {
          distsForName = new ArrayList<>();
          distsByName.put(d.getName(), distsForName);
        }
        distsForName.add(d);
      }
      
      List<Distribution> toRemove = new ArrayList<>();
      for (Map.Entry<String, ArrayList<Distribution>> entry : distsByName.entrySet()) {
        if (entry.getValue().size() > criteria.getBackup()) {
          List<Distribution> copy = new ArrayList<Distribution>(entry.getValue());
          Collections.sort(copy, new Comparator<Distribution>() {
            @Override
            public int compare(Distribution d1, Distribution d2) {
              int c = 0;
              if (d1.getTimestamp() < d2.getTimestamp()) {
                c = -1;
              } else if (d1.getTimestamp() > d2.getTimestamp()) {
                c = 1;
              }
              if (c == 0) {
                c = d1.compareTo(d2);
              }
              
              return c;
            }
          });
          List<Distribution> sublist = entry.getValue().subList(0, entry.getValue().size() - criteria.getBackup());
          toRemove.addAll(sublist);
        }
      }
      return toRemove;
    }
    return dists;
  }

}
