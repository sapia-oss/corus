package org.sapia.corus.admin.services.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.admin.services.deployer.dist.ProcessConfig;
import org.sapia.util.xml.ProcessingException;
import org.sapia.util.xml.confix.Dom4jProcessor;
import org.sapia.util.xml.confix.ReflectionFactory;


public class ExecConfig implements Serializable{
  
  static final long serialVersionUID = 1L;
  
  private List<ProcessDef> processes = new ArrayList<ProcessDef>();
  private String name, profile;
  private boolean startOnBoot;
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getProfile() {
    return profile;
  }
  
  public void setProfile(String profile) {
    this.profile = profile;
  }
  
  public boolean isStartOnBoot() {
    return startOnBoot;
  }
  
  public void setStartOnBoot(boolean startOnBoot) {
    this.startOnBoot = startOnBoot;
  }
  
  public ProcessDef createProcess(){
    ProcessDef proc = new ProcessDef();
    processes.add(proc);
    return proc;
  }
  
  public List<ProcessDef> getProcesses() {
    return Collections.unmodifiableList(processes);
  }
  
  public static ExecConfig newInstance(InputStream is) 
    throws IOException, ProcessingException{
    try{
      ReflectionFactory factory = new ReflectionFactory(new String[]{});
      Dom4jProcessor processor = new Dom4jProcessor(factory);
      RootElement root = new RootElement();
      processor.process(root, is);
      return root.exec;
    }finally{
      is.close();
    }
  }
  
  public void removeAll(Distribution d){
    List<ProcessDef> toRemove = new ArrayList<ProcessDef>();
    for(ProcessConfig pc : d.getProcesses()){
      for(ProcessDef ref:processes){
        if(ref.getDist().equals(d.getName()) &&
           ref.getVersion().equals(d.getVersion()) &&
           ref.getName().equals(pc.getName())){
           toRemove.add(ref);
        }
      }
    } 
    processes.removeAll(toRemove);
  }
 
  
  public String toString(){
    return new StringBuilder("[")
      .append("name=").append(name).append(", ")
      .append("profile=").append(profile).append(", ")
      .append("startOnBoot=").append(startOnBoot).append(", ")
      .append("processes=").append(processes).append(", ")
      .append("]")
      .toString();
  }

  public static final class RootElement{
    
    ExecConfig exec = new ExecConfig();
    
    public ExecConfig createExec(){
      return exec;
    }
  }
}
