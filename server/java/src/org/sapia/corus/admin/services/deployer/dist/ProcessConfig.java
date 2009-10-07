package org.sapia.corus.admin.services.deployer.dist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.console.CmdLine;
import org.sapia.corus.exceptions.LogicException;
import org.sapia.util.xml.confix.ConfigurationException;
import org.sapia.util.xml.confix.ObjectHandlerIF;


/**
 * This class corresponds to the <code>process</code> element in the
 * corus.xml file. This element provides process configuration parameters.
 *
 * @author Yanick Duchesne
 */
public class ProcessConfig implements java.io.Serializable, ObjectHandlerIF{
  
  static final long serialVersionUID = 1L;

  public static final int DEFAULT_POLL_INTERVAL   = 10;
  public static final int DEFAULT_STATUS_INTERVAL = 30;
  private boolean         _invoke;
  private List<Starter>     _starters             = new ArrayList<Starter>();
  private List<Port>        _ports                = new ArrayList<Port>();
  private List<Dependency>  _dependencies         = new ArrayList<Dependency>();
  private int             _maxKillRetry           = -1;
  private int             _shutDownTimeout        = -1;
  private String          _name;
  private int             _statusInterval         = DEFAULT_STATUS_INTERVAL;
  private int             _pollInterval           = DEFAULT_POLL_INTERVAL;
  private boolean         _deleteOnKill           = false;
  private String[]          _tags;
  /**
   * Sets this process config's name.
   *
   * @param a name.
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * Returns this process config's name.
   *
   * @return this process config's name.
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets this instance's tags.
   * @param tagList
   */
  public void setTags(String tagList){
    _tags = tagList.split(",");
    for(int i = 0; i < _tags.length; i++){
      _tags[i] = _tags[i].trim();
    }
  }
  
  /**
   * @return the set of tags held by this instance.
   */
  public Set<String> getTagSet(){
    Set<String> set = new HashSet<String>();
    if(_tags != null){
      for(String t:_tags){
        set.add(t);
      }
    }
    return set;
  }
  /**
   * Sets the maximum number of successive times the Corus
   * server should try to kill a process instance corresponding to
   * this object.
   *
   * @param maxRetry a number of maximum successive "kill" attempts.
   */
  public void setMaxKillRetry(int maxRetry) {
    _maxKillRetry = maxRetry;
  }
  
  /**
   * Indicates if the process directory should be deleted after being
   * terminated (defaults to true).
   */
  public void setDeleteOnKill(boolean delete){
    _deleteOnKill = delete;
  }
  
  /**
   * @return <code>true</code> if the process directory should be deleted after being
   * terminated
   */
  public boolean isDeleteOnKill(){
    return _deleteOnKill;
  }

  /**
   * Returns the maximum number of successive times the Corus
   * server should try to kill a process instance corresponding to
   * this object.
   *
   * @return a maximum number of successive "kill" attempts;
   * <code>-1</code> is returned if no value was specified.
   */
  public int getMaxKillRetry() {
    return _maxKillRetry;
  }

  /**
   * Sets the polling interval of the process corresponding to this instance.
   *
   * @param interval the interval (in seconds) at which the corresponding process
   * should poll the corus server - defaults to 10 seconds.
   */
  public void setPollInterval(int interval) {
    _pollInterval = interval;
  }

  /**
   * @return this instance's poll interval.
   * @see #setPollInterval(int)
   */
  public int getPollInterval() {
    return _pollInterval;
  }

  /**
   * Sets the status interval of the process corresponding to this instance.
   *
   * @param interval the interval (in seconds) at which the corresponding process
   * should send a status to the corus server - defaults to 25 seconds.
   */
  public void setStatusInterval(int interval) {
    _statusInterval = interval;
  }

  /**
   * @return this instance's status interval.
   * @see #setStatusInterval(int)
   */
  public int getStatusInterval() {
    return _statusInterval;
  }

  /**
   * Sets the number of seconds the Corus server should wait
   * for receiving process kill confirmation.
   *
   * @param shutDownTimeout a number in seconds.
   */
  public void setShutdownTimeout(int shutDownTimeout) {
    _shutDownTimeout = shutDownTimeout;
  }

  /**
   * Returns the number of seconds the Corus server should wait
   * for receiving process kill confirmation.
   *
   * @return a number of seconds, or <code>-1</code> if no value
   * was specified.
   */
  public int getShutdownTimeout() {
    return _shutDownTimeout;
  }

  /**
   * Sets if a process corresponding to this instance should be instantiated only if it has been invoked
   * explicitly by its name (in such a case, this method's param must
   * be <code>true</code>
   *
   * @param <code>true</code> if a process corresponding to this param should
   * be invoked explicitly by its name.
   */
  public void setInvoke(boolean invoke) {
    _invoke = invoke;
  }

  /**
   * Returns <code>true</code> if a process corresponding to this param should
   * be invoked explicitly by its name.
   */
  public boolean isInvoke() {
    return _invoke;
  }
  
  /**
   * @return a <code>Port</code> that is added to this instance and should
   * correspond to an existing port range.
   */
  public Port createPort(){
    Port p = new Port();
    _ports.add(p);
    return p;
  }
  
  /**
   * @return creates a {@link Dependency} and returns it.
   */
  public Dependency createDependency(){
    Dependency d = new Dependency();
    _dependencies.add(d);
    return d;
  }
  
  /**
   * @return the list of {@link Dependency} instances containes within this instance.
   */
  public List<Dependency> getDependencies(){
    return Collections.unmodifiableList(_dependencies);
  }
  
  /**
   * @return the <code>List<code> of this instance's <code>Port</code>.
   */
  public List<Port> getPorts(){
    return _ports;
  }

  /**
   * Returns a "command-line" representation of this instance.
   *
   * @return this instance as a <code>CmdLine</code> object, or
   * <code>null</code> if no <code>CmdLine</code> could be generated
   * for the profile contained in the passed in <code>Env</code>
   * instance.
   */
  public CmdLine toCmdLine(Env env) throws LogicException {
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
    for (Starter st:_starters) {
      profiles.add(st.getProfile());
    }

    return profiles;
  }
  
  /**
   * @param profile the name of a profile
   * @return <code>true</code> if this instance has the given profile.
   */
  public boolean containsProfile(String profile){
    for (Starter st:_starters) {
      if(st.getProfile().equals(profile)){
        return true;
      }
    }
    return false;
  }
  
  public void addStarter(Starter starter){
    _starters.add(starter);
  }
  
  List<Dependency> dependencies(){
    return _dependencies;
  }
  
  public void handleObject(String elementName, Object starter)
  throws ConfigurationException {
    if (starter instanceof Starter) {
      addStarter((Starter)starter);
    } else {
    throw new ConfigurationException(starter.getClass().getName() +
                         " does not implement the " +
                         Starter.class.getName() + " interface");
    }
  }
    
  public String toString() {
    return "[ name=" + _name + ", maxKillRetry=" + _maxKillRetry +
           ", shutDownTimeout=" + _shutDownTimeout + " java=" + _starters +
           ", deleteOnKill=" + _deleteOnKill + " ]";
  }
  
  public int hashCode(){
    return _name.hashCode();
  }
  
  public boolean equals(Object other){
    if(other instanceof ProcessConfig){
      return _name.equals(((ProcessConfig)other).getName());
    }
    else{
      return false;
    }
  }
  
  private Starter findFor(String profile) {
    Starter toReturn = null;
    Starter current;

    for (int i = 0; i < _starters.size(); i++) {
      current = (Starter) _starters.get(i);

      if ((current.getProfile() == null) && (toReturn == null)) {
        toReturn = current;
      } else if ((current.getProfile() != null) &&
                   current.getProfile().equals(profile)) {
        toReturn = current;
      }
    }

    return toReturn;
  }

}
