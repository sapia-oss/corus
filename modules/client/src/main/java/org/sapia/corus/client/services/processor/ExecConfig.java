package org.sapia.corus.client.services.processor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.common.Mappable;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.services.database.persistence.AbstractPersistent;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.util.xml.ProcessingException;
import org.sapia.util.xml.confix.Dom4jProcessor;
import org.sapia.util.xml.confix.ReflectionFactory;

/**
 * An instance of this class corresponts to a so-called
 * "execution configuration". An execution configuration holds
 * "process definitions", each defining a process to be executed. A process
 * definition refers to a process configured as part of a distribution deployed
 * in Corus.
 * 
 * @see Distribution
 * 
 * @author yduchesne
 * 
 */
public class ExecConfig extends AbstractPersistent<String, ExecConfig> implements Externalizable, Comparable<ExecConfig>, JsonStreamable, Mappable {
  static final long serialVersionUID = 1L;

  static final int VERSION_1 = 1;
  static final int CURRENT_VERSION = VERSION_1;
  
  private List<ProcessDef> processes = new ArrayList<ProcessDef>();
  private String  name, profile;
  private boolean startOnBoot;
  private boolean enabled = true;
  private int     id;
  
  /**
   * Meant for externalization only.
   */ 
  public ExecConfig() {
  }
  
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
   * @return the profile under which the process should be started. If
   *         <code>null</code>, the Corus server will interpret the profile as
   *         corresponding to the one of the depending process, if such a
   *         dependency is configured.
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
   * @return <code>true</code> if this instance is enabled.
   */
  public boolean isEnabled() {
    return enabled;
  }
  
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * @return the unmodifiable {@link List} of {@link ProcessDef}s that this
   *         instance holds.
   */
  public List<ProcessDef> getProcesses() {
    return Collections.unmodifiableList(processes);
  }
  
  /**
   * @return this instance's identifier.
   */
  public int getId() {
    return id;
  }
  
  public void setId(int id) {
    this.id = id;
  }

  /**
   * @param is
   *          an {@link InputStream} holding the XML data corresponding to an
   *          execution configuration.
   * @return the {@link ExecConfig} instance that was built using the given
   *         data.
   * @throws IOException
   *           if an IO problem occurs.
   * @throws ProcessingException
   *           if an XML-processing problem occurs.
   */
  public static ExecConfig newInstance(InputStream is) throws IOException, ProcessingException {
    try {
      ReflectionFactory factory = new ReflectionFactory(new String[] {});
      Dom4jProcessor processor = new Dom4jProcessor(factory);
      RootElement root = new RootElement();
      processor.process(root, is);
      return root.exec;
    } finally {
      is.close();
    }
  }

  /**
   * Creates a {@link ProcessDef}, internally adds it to this instance, and
   * returns it.
   * 
   * @return a new {@link ProcessDef}
   */
  public ProcessDef createProcess() {
    ProcessDef proc = new ProcessDef();
    processes.add(proc);
    return proc;
  }

  /**
   * Removes all process definitions that this instance holds, and which
   * correspond to the given distribution.
   * 
   * @param d
   *          a {@link Distribution}
   */
  public void removeAll(Distribution d) {
    List<ProcessDef> toRemove = new ArrayList<ProcessDef>();
    for (ProcessDef ref : processes) {
      if (ref.getDist().equals(d.getName()) && ref.getVersion().equals(d.getVersion())) {
        toRemove.add(ref);
      }
    }
    List<ProcessDef> toKeep = new ArrayList<ProcessDef>(processes);
    toKeep.removeAll(toRemove);
    processes = toKeep;
  }

  // --------------------------------------------------------------------------
  // Mappeable
  
  @Override
  public Map<String, Object> asMap() {
    Map<String, Object> toReturn = new HashMap<>();
    toReturn.put("exec.name", name);
    toReturn.put("exec.profile", profile);
    toReturn.put("exec.startOnBoot", startOnBoot);
    toReturn.put("exec.enabled", enabled);
    toReturn.put("exec.processes", processes);
    return toReturn;
  }
  
  // --------------------------------------------------------------------------
  // JsonStreamable
  
  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject()
      .field("classVersion").value(CURRENT_VERSION)
      .field("name").value(name)
      .field("profile").value(profile == null ? "N/A" : profile)
      .field("enabled").value(enabled)
      .field("startOnBoot").value(startOnBoot);
    stream.field("processes").beginArray();
    for (ProcessDef d : processes) {
      stream.beginObject()
        .field("distribution").value(d.getDist())
        .field("version").value(d.getVersion())
        .field("process").value(d.getName())
        .field("profile").value(d.getProfile() == null ? (profile == null ? "N/A" : profile) : d.getProfile())
        .field("instances").value(d.getInstances())
      .endObject();
    }
    stream.endArray();
    stream.endObject();
  }
  
  public static ExecConfig fromJson(JsonInput in) {
    int inputVersion = in.getInt("classVersion");
    if (inputVersion == VERSION_1) {
      ExecConfig c = new ExecConfig();
      c.name = in.getString("name");
      c.profile = in.getString("profile");
      if (c.profile.equals("N/A")) {
        c.profile = null;
      }
      c.enabled = in.getBoolean("enabled");
      c.startOnBoot = in.getBoolean("startOnBoot");
      for (JsonInput jsonProcessDef : in.iterate("processes")) {
        ProcessDef def = new ProcessDef();
        def.setDist(jsonProcessDef.getString("distribution"));
        def.setVersion(jsonProcessDef.getString("version"));
        def.setName(jsonProcessDef.getString("process"));
        def.setProfile(jsonProcessDef.getString("profile"));
        if (def.getProfile().equals("N/A")) {
          def.setProfile(null);
        }
        def.setInstances(jsonProcessDef.getInt("instances"));
        c.processes.add(def);
      }
      return c;
    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }
  }

  // --------------------------------------------------------------------------
  // Comparable

  @Override
  public int compareTo(ExecConfig other) {
    return name.compareTo(other.getName());
  }
  
  // --------------------------------------------------------------------------
  // Object overrides

  public String toString() {
    return new StringBuilder("[").append("name=").append(name).append(", ").append("profile=").append(profile).append(", ").append("startOnBoot=")
        .append(startOnBoot).append(", ").append("processes=").append(processes).append(", ").append("]").toString();
  }
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    
    int inputVersion = in.readInt();
    if (inputVersion == VERSION_1) {
      processes   = (List<ProcessDef>) in.readObject();
      name        = in.readUTF();
      profile     = (String) in.readObject();
      startOnBoot = in.readBoolean();
      enabled     = in.readBoolean();
      id          = in.readInt();
    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    
    out.writeInt(CURRENT_VERSION);
    
    out.writeObject(processes);
    out.writeUTF(name);
    out.writeObject(profile);
    out.writeBoolean(startOnBoot);
    out.writeBoolean(enabled);
    out.writeInt(id);
  }
  
  // ==========================================================================
  // Inner classes

  public static final class RootElement {

    ExecConfig exec = new ExecConfig();

    public ExecConfig createExec() {
      return exec;
    }
  }
}
