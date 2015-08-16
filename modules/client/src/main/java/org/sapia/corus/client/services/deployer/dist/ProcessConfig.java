package org.sapia.corus.client.services.deployer.dist;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.CompositeStrLookup;
import org.sapia.corus.client.common.Env;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.exceptions.misc.MissingDataException;
import org.sapia.ubik.util.Collects;
import org.sapia.util.xml.confix.ConfigurationException;
import org.sapia.util.xml.confix.ObjectHandlerIF;

/**
 * This class corresponds to the <code>process</code> element in the corus.xml
 * file. This element provides process configuration parameters.
 * 
 * @author Yanick Duchesne
 */
public class ProcessConfig implements Externalizable, ObjectHandlerIF, Matcheable {

  static final long serialVersionUID = 1L;

  public static final int DEFAULT_POLL_INTERVAL        = 10;
  public static final int DEFAULT_STATUS_INTERVAL      = 30;
  public static final int DEFAULT_INTERPOLATION_PASSES = 2;

  private boolean       invoke         = true;
  private List<Starter> starters       = new ArrayList<Starter>();
  private List<Port>    ports          = new ArrayList<Port>();
  private int           maxKillRetry   = -1;
  private int      shutDownTimeout     = -1;
  private int      maxInstances;
  private String   name;
  private int      statusInterval      = DEFAULT_STATUS_INTERVAL;
  private int      pollInterval        = DEFAULT_POLL_INTERVAL;
  private int      pollTimeout         = -1;
  private boolean  deleteOnKill        = false;
  private String[] tags;
  private String[] categories;
  private int      interpolationPasses = DEFAULT_INTERPOLATION_PASSES;
  private PreExec  preExec; 
  
  /**
   * Meant for externalization only.
   */
  public ProcessConfig() {
  }

  public ProcessConfig(String name) {
    this.name = name;
  }

  /**
   * Sets the {@link PreExec} instance.
   */
  public void setPreExec(PreExec preExec) {
    this.preExec = preExec;
  }

  /**
   * Sets this process config's name.
   * 
   * @param name
   *          a name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns this process config's name.
   * 
   * @return this process config's name.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets this instance's tags.
   * 
   * @param tagList a comma-delimited list of tags.
   */
  public void setTags(String tagList) {
    tags = tagList.split(",");
    for (int i = 0; i < tags.length; i++) {
      tags[i] = tags[i].trim();
    }
  }

  /**
   * @return the set of tags held by this instance.
   */
  public Set<String> getTagSet() {
    if (tags == null) {
      return new HashSet<>(0);
    }
    return Collects.arrayToSet(tags);
  }
  
  /**
   * Sets this instance's categories.
   * 
   * @param categoryList a comma-delimited list of categories.
   */
  public void setPropertyCategories(String categoryList) {
    categories = categoryList.split(",");
    for (int i = 0; i < categories.length; i++) {
      categories[i] = categories[i].trim();
    }  
  }
  
  /**
   * @return the list of categories held by this instance.
   */
  public List<String> getPropertyCategories() {
    if (categories == null) {
      return new ArrayList<>(0);
    }
    return Collects.arrayToList(categories);
  }

  /**
   * Sets the maximum number of successive times the Corus server should try to
   * kill a process instance corresponding to this object.
   * 
   * @param maxRetry
   *          a number of maximum successive "kill" attempts.
   */
  public void setMaxKillRetry(int maxRetry) {
    maxKillRetry = maxRetry;
  }

  /**
   * Indicates if the process directory should be deleted after being terminated
   * (defaults to true).
   */
  public void setDeleteOnKill(boolean delete) {
    deleteOnKill = delete;
  }

  /**
   * @return <code>true</code> if the process directory should be deleted after
   *         being terminated
   */
  public boolean isDeleteOnKill() {
    return deleteOnKill;
  }

  /**
   * Returns the maximum number of successive times the Corus server should try
   * to kill a process instance corresponding to this object.
   * 
   * @return a maximum number of successive "kill" attempts; <code>-1</code> is
   *         returned if no value was specified.
   */
  public int getMaxKillRetry() {
    return maxKillRetry;
  }

  /**
   * Sets the polling interval of the process corresponding to this instance.
   * 
   * @param interval
   *          the interval (in seconds) at which the corresponding process
   *          should poll the corus server - defaults to 10 seconds.
   */
  public void setPollInterval(int interval) {
    pollInterval = interval;
  }

  /**
   * @return this instance's poll interval.
   * @see #setPollInterval(int)
   */
  public int getPollInterval() {
    return pollInterval;
  }
 
  /**
   * @param pollTimeout 
   *          the amount of time (in seconds) that the Corus server should give to a process to poll,
   *          beyond which the process should be deemed irresponsive.
   */
  public void setPollTimeout(int pollTimeout) {
    this.pollTimeout = pollTimeout;
  }
  
  /**
   * Returns a polling timeout. If it timeout <= 0, the value configured on the server side
   * should be used.
   * 
   * @return the amount of time (in seconds) that the Corus server should give to a process to poll,
   *          beyond which the process should be deemed irresponsive.
   */
  public int getPollTimeout() {
    return pollTimeout;
  }

  /**
   * Sets the status interval of the process corresponding to this instance.
   * 
   * @param interval
   *          the interval (in seconds) at which the corresponding process
   *          should send a status to the corus server - defaults to 25 seconds.
   */
  public void setStatusInterval(int interval) {
    statusInterval = interval;
  }

  /**
   * @return this instance's status interval.
   * @see #setStatusInterval(int)
   */
  public int getStatusInterval() {
    return statusInterval;
  }

  /**
   * Sets the number of seconds the Corus server should wait for receiving
   * process kill confirmation.
   * 
   * @param shutDownTimeout
   *          a number in seconds.
   */
  public void setShutdownTimeout(int shutDownTimeout) {
    this.shutDownTimeout = shutDownTimeout;
  }

  /**
   * Returns the number of seconds the Corus server should wait for receiving
   * process kill confirmation.
   * 
   * @return a number of seconds, or <code>-1</code> if no value was specified.
   */
  public int getShutdownTimeout() {
    return shutDownTimeout;
  }

  /**
   * Sets if a process corresponding to this instance should be instantiated
   * only if it has been invoked explicitly by its name (in such a case, this
   * method's param must be <code>true</code>).
   * 
   * @param invoke
   *          <code>true</code> if a process corresponding to this param should
   *          be invoked explicitly by its name (defaults to <code>true</code>).
   */
  public void setInvoke(boolean invoke) {
    this.invoke = invoke;
  }

  /**
   * Returns <code>true</code> if a process corresponding to this param should
   * be invoked explicitly by its name.
   */
  public boolean isInvoke() {
    return invoke;
  }

  /**
   * @return a <code>Port</code> that is added to this instance and should
   *         correspond to an existing port range.
   */
  public Port createPort() {
    Port p = new Port();
    ports.add(p);
    return p;
  }

  /**
   * @return the list of {@link Dependency} instances containes within this
   *         instance.
   */
  public List<Dependency> getDependenciesFor(String profile) {
    List<Dependency> toReturn = new ArrayList<Dependency>();

    for (Starter st : starters) {
      if (st.getProfile().equals(profile)) {
        for (Dependency dep : st.getDependencies()) {
          toReturn.add(dep);
        }
      }
    }

    return toReturn;
  }

  /**
   * @return the <code>List<code> of this instance's <code>Port</code>.
   */
  public List<Port> getPorts() {
    return ports;
  }
  
  /**
   * @param portRangeName the name of the port range.
   * @return the {@link OptionalValue} holding the {@link Port} range corresponding
   * to the given name, or {@link OptionalValue#none()} is no such port range is found.
   */
  public OptionalValue<Port> getPortByName(String portRangeName) {
    for (Port p : ports) {
      if (p.getName().equals(portRangeName)) {
        return OptionalValue.of(p);
      }
    }
    return OptionalValue.none();
  }

  /**
   * @param maxInstances
   *          the maximum number of process instances allowed.
   */
  public void setMaxInstances(int maxInstances) {
    this.maxInstances = maxInstances;
  }

  /**
   * @return the maximum number of process instances allowed (if <= 0, means no
   *         maximum).
   */
  public int getMaxInstances() {
    return maxInstances;
  }
  
  /**
   * Sets the number of interpolation passes to do when interpolating
   * property variables in process configuration.
   * 
   * @param interpolationPasses a number of interpolation passes.
   */
  public void setInterpolationPasses(int interpolationPasses) {
    this.interpolationPasses = interpolationPasses;
  }
  
  /**
   * @return the number of interpolation passes to do when interpolating
   * property variables in process configuration (defaults to 2).
   */
  public int getInterpolationPasses() {
    return interpolationPasses;
  }

  /**
   * @param env the {@link Env} instance.
   * @throws Throwable if an error occurs while executing the script.
   */
  public void preExec(Env env) throws Throwable {
    if (preExec != null && !preExec.getCommands().isEmpty()) {
      Properties          props = new Properties();
      for (Property p : env.getProperties()) {
        props.setProperty(p.getName(), p.getValue());
      }

      CompositeStrLookup vars   = new CompositeStrLookup()
        .add(new PropertiesStrLookup(props))
        .add(StrLookup.systemPropertiesLookup())
        .add(StrLookup.mapLookup(env.getEnvironmentVariables()));

      StrSubstitutor subs = new StrSubstitutor(vars);
      for (Cmd c : preExec.getCommands()) {
        String value = c.getValue();
        if (value != null) {
          value = subs.replace(value);
          env.getInterpreter().eval(value, vars);          
        }
      }
    }
  }

  /**
   * Returns a "command-line" representation of this instance.
   * 
   * @return this instance as a {@link CmdLine} object, or <code>null</code> if
   *         no {@link CmdLine} could be generated for the profile contained in
   *         the passed in {@link Env} instance.
   */
  public CmdLine toCmdLine(Env env) throws MissingDataException {
    Starter st = findFor(env.getProfile());

    if (st == null) {
      return null;
    }

    return st.toCmdLine(env);
  }

  /**
   * Returns the profiles that this instance contains.
   * 
   * @return a <code>List</code> of profile names.
   */
  public List<String> getProfiles() {
    List<String> profiles = new ArrayList<String>();
    for (Starter st : starters) {
      profiles.add(st.getProfile());
    }

    return profiles;
  }

  /**
   * @param profile
   *          the name of a profile
   * @return <code>true</code> if this instance has the given profile.
   */
  public boolean containsProfile(String profile) {
    for (Starter st : starters) {
      if (st.getProfile().equals(profile)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Adds the given starter to this instance.
   * 
   * @param starter
   *          a {@link Starter}.
   */
  public void addStarter(Starter starter) {
    starters.add(starter);
  }
  
  // --------------------------------------------------------------------------
  // Matcheable
  
  @Override
  public boolean matches(Pattern pattern) {
    return pattern.matches(name) || matchesProfile(pattern);
  }

  // --------------------------------------------------------------------------
  // ObjectHandlerIF
  
  public void handleObject(String elementName, Object starter) throws ConfigurationException {
    if (starter instanceof Starter) {
      addStarter((Starter) starter);
    } else {
      throw new ConfigurationException(starter.getClass().getName() + " does not implement the " + Starter.class.getName() + " interface");
    }
  }
  
  // --------------------------------------------------------------------------
  // Object overriddes

  public String toString() {
    return "[ name=" + name + ", maxKillRetry=" + maxKillRetry + ", shutDownTimeout=" + shutDownTimeout + " java=" + starters + ", deleteOnKill="
        + deleteOnKill + " ]";
  }

  public int hashCode() {
    return name.hashCode();
  }

  public boolean equals(Object other) {
    if (other instanceof ProcessConfig) {
      return name.equals(((ProcessConfig) other).getName());
    } else {
      return false;
    }
  }
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    invoke              = in.readBoolean();
    starters            = (List<Starter>) in.readObject();
    ports               = (List<Port>) in.readObject();
    maxKillRetry        = in.readInt();
    shutDownTimeout     = in.readInt();
    maxInstances        = in.readInt();
    name                = in.readUTF();
    statusInterval      = in.readInt();
    pollInterval        = in.readInt();
    pollTimeout         = in.readInt();
    deleteOnKill        = in.readBoolean();
    tags                = (String[]) in.readObject();
    categories          = (String[]) in.readObject();
    interpolationPasses = in.readInt();
    preExec             = (PreExec) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeBoolean(invoke);
    out.writeObject(starters);
    out.writeObject(ports);
    out.writeInt(maxKillRetry);
    out.writeInt(shutDownTimeout);
    out.writeInt(maxInstances);
    out.writeUTF(name);
    out.writeInt(statusInterval);
    out.writeInt(pollInterval);
    out.writeInt(pollTimeout);
    out.writeBoolean(deleteOnKill);
    out.writeObject(tags);
    out.writeObject(categories);
    out.writeInt(interpolationPasses);
    out.writeObject(preExec);
  }
  
  // --------------------------------------------------------------------------
  // Restricted methods

  void init(String distName, String version) {
    for (Starter st : starters) {
      for (Dependency dep : st.getDependencies()) {
        if (dep.getDist() == null)
          dep.setDist(distName);
        if (dep.getVersion() == null)
          dep.getVersion();
        if (dep.getProfile() == null)
          dep.setProfile(st.getProfile());
      }
    }

  }

  private Starter findFor(String profile) {
    Starter toReturn = null;
    Starter current;

    for (int i = 0; i < starters.size(); i++) {
      current = (Starter) starters.get(i);

      if ((current.getProfile() == null) && (toReturn == null)) {
        toReturn = current;
      } else if ((current.getProfile() != null) && current.getProfile().equals(profile)) {
        toReturn = current;
      }
    }

    return toReturn;
  }
  
  private boolean matchesProfile(Pattern pattern) {
    for (String p : this.getProfiles()) {
      if (pattern.matches(p)) {
        return true;
      }
    }
    return false;
  }  

}
