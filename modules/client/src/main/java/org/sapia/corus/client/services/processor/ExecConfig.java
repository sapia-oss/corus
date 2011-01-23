package org.sapia.corus.client.services.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.services.db.persistence.AbstractPersistent;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.util.xml.ProcessingException;
import org.sapia.util.xml.confix.Dom4jProcessor;
import org.sapia.util.xml.confix.ReflectionFactory;

/**
 * An instance of this class corresponts to a so-called "execution configuration". An
 * execution configuration holds "process definitions", each defining a process to be
 * executed. A process definition refers to a process configured as part of a distribution
 * deployed in Corus.
 * 
 * @see Distribution
 * 
 * @author yduchesne
 *
 */
public class ExecConfig extends AbstractPersistent<String, ExecConfig> implements Serializable, Comparable<ExecConfig>{
  
  static final long serialVersionUID = 1L;
  
  private List<ProcessDef> processes = new ArrayList<ProcessDef>();
  private String name, profile;
  private boolean startOnBoot;
  
  
  @Override
  @Transient
  public String getKey() {
    return name;
  }
  
  /**
   * @return the name of the process to start.
   * @see ProcessConfig#getName()
   */
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * @return the profile under which the process should
   * be started. If <code>null</code>, the Corus server will
   * interpret the profile as corresponding to the one of the depending process,
   * if such a dependency is configured.
   */
  public String getProfile() {
    return profile;
  }
  
  public void setProfile(String profile) {
    this.profile = profile;
  }
  
  /**
   * @return <code>true</code> if the process is to be started at Corus startup.
   */
  public boolean isStartOnBoot() {
    return startOnBoot;
  }
  
  public void setStartOnBoot(boolean startOnBoot) {
    this.startOnBoot = startOnBoot;
  }
  
  /**
   * @return the unmodifiable {@link List} of {@link ProcessDef}s that this
   * instance holds.
   */
  public List<ProcessDef> getProcesses() {
    return Collections.unmodifiableList(processes);
  }
  
  /**
   * @param is an {@link InputStream} holding the XML data corresponding
   * to an execution configuration.
   * @return the {@link ExecConfig} instance that was built using the given
   * data.
   * @throws IOException if an IO problem occurs.
   * @throws ProcessingException if an XML-processing problem occurs.
   */
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

  /**
   * Creates a {@link ProcessDef}, internally adds it to this instance, and
   * returns it.
   * 
   * @return a new {@link ProcessDef}
   */
  public ProcessDef createProcess(){
    ProcessDef proc = new ProcessDef();
    processes.add(proc);
    return proc;
  }

  
  /**
   * Removes all process definitions that this instance holds, and which correspond to
   * the given distribution.
   * 
   * @param d a {@link Distribution}
   */
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
  
  @Override
  public int compareTo(ExecConfig other) {
    return name.compareTo(other.getName());
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
