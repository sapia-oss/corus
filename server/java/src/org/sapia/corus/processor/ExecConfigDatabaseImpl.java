package org.sapia.corus.processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sapia.corus.admin.Arg;
import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.admin.services.processor.ExecConfig;
import org.sapia.corus.db.DbMap;

public class ExecConfigDatabaseImpl implements ExecConfigDatabase{

  private DbMap<String, ExecConfig> _configs;
  
  public ExecConfigDatabaseImpl(DbMap<String, ExecConfig> configs) {
    _configs = configs;
  }
  
  public synchronized List<ExecConfig> getConfigs(){
    Iterator<ExecConfig> configs = _configs.values();
    List<ExecConfig> toReturn = new ArrayList<ExecConfig>();
    while(configs.hasNext()){
      toReturn.add(configs.next());
    }
    return toReturn;
  }
  
  public synchronized List<ExecConfig> getBootstrapConfigs(){
    Iterator<ExecConfig> configs = _configs.values();
    List<ExecConfig> toReturn = new ArrayList<ExecConfig>();
    while(configs.hasNext()){
      ExecConfig ec = configs.next();
      if(ec.isStartOnBoot()){
        toReturn.add(ec);
      }
    }
    return toReturn;
  }
  
  public synchronized List<ExecConfig> getConfigsFor(Arg arg){
    Iterator<ExecConfig> configs = _configs.values();
    List<ExecConfig> toReturn = new ArrayList<ExecConfig>();
    while(configs.hasNext()){
      ExecConfig c = configs.next();
      if(arg != null && arg.matches(c.getName())){
        toReturn.add(c);
      }
    }
    return toReturn;
  }
  
  public synchronized void removeConfigsFor(Arg arg){
    List<ExecConfig> configs = getConfigsFor(arg);
    for(ExecConfig c:configs){
      _configs.remove(c.getName());
    }
  }
  
  public synchronized ExecConfig getConfigFor(String name){
    return _configs.get(name);
  }
  
  public synchronized void removeConfig(String name){
    _configs.remove(name);
  }
  
  public synchronized void addConfig(ExecConfig btc){
    _configs.put(btc.getName(), btc);
  }

  public synchronized void removeProcessesForDistribution(Distribution d){
    Iterator<ExecConfig> configs = _configs.values();
    while(configs.hasNext()){
      ExecConfig c = configs.next();
      c.removeAll(d);
      if(c.getProcesses().size() == 0){
        _configs.remove(c.getName());
      }
    }
  }

}
