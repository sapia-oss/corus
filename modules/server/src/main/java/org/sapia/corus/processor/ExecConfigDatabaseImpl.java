package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.processor.ExecConfig;

public class ExecConfigDatabaseImpl implements ExecConfigDatabase {

  private DbMap<String, ExecConfig> configs;
  
  public ExecConfigDatabaseImpl(DbMap<String, ExecConfig> configs) {
    this.configs = configs;
  }
  
  public synchronized List<ExecConfig> getConfigs(){
    Iterator<ExecConfig> itr 	= configs.values();
    List<ExecConfig> toReturn = new ArrayList<ExecConfig>();
    while(itr.hasNext()){
      toReturn.add(itr.next());
    }
    Collections.sort(toReturn);
    return toReturn;
  }
  
  public synchronized List<ExecConfig> getBootstrapConfigs(){
    Iterator<ExecConfig> itr 	= configs.values();
    List<ExecConfig> toReturn = new ArrayList<ExecConfig>();
    while(itr.hasNext()){
      ExecConfig ec = itr.next();
      if(ec.isStartOnBoot()){
        toReturn.add(ec);
      }
    }
    Collections.sort(toReturn);
    return toReturn;
  }
  
  public synchronized List<ExecConfig> getConfigsFor(Arg arg){
    Iterator<ExecConfig> itr = configs.values();
    List<ExecConfig> toReturn 	 = new ArrayList<ExecConfig>();
    while(itr.hasNext()){
      ExecConfig c = itr.next();
      if(arg != null && arg.matches(c.getName())){
        toReturn.add(c);
      }
    }
    Collections.sort(toReturn);
    return toReturn;
  }
  
  public synchronized void removeConfigsFor(Arg arg){
    List<ExecConfig> list = getConfigsFor(arg);
    for(ExecConfig c : list){
      configs.remove(c.getName());
    }
  }
  
  public synchronized ExecConfig getConfigFor(String name){
    return configs.get(name);
  }
  
  public synchronized void removeConfig(String name){
    configs.remove(name);
  }
  
  public synchronized void addConfig(ExecConfig btc){
    configs.put(btc.getName(), btc);
  }

  public synchronized void removeProcessesForDistribution(Distribution d){
    Iterator<ExecConfig> itr = configs.values();
    while(itr.hasNext()){
      ExecConfig c = itr.next();
      c.removeAll(d);
      if(c.getProcesses().size() == 0){
        configs.remove(c.getName());
      }
    }
  }

}
