package org.sapia.corus.client.services.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.common.CyclicIdGenerator;
import org.sapia.corus.client.common.IDGenerator;
import org.sapia.corus.client.services.db.persistence.AbstractPersistent;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.interop.AbstractCommand;
import org.sapia.corus.interop.Shutdown;

/**
 * This class models an external process. An instance of this class actually corresponds to 
 * a process started by a corus server.
 *
 * @author Yanick Duchesne
 */
public class Process extends AbstractPersistent<String, Process> implements java.io.Serializable, Comparable<Process> {

  static final long serialVersionUID = 1L;
  
  public enum ProcessTerminationRequestor{
    
    KILL_REQUESTOR_ADMIN("corus.admin"),
    KILL_REQUESTOR_SERVER("corus.server"),
    KILL_REQUESTOR_PROCESS("corus.process");
    
    private String type;
    
    private ProcessTerminationRequestor(String type) {
      this.type = type;
    }
    public String toString(){
      return type;
    }
    public String getType() {
      return type;
    }
  }
  
  public enum LifeCycleStatus{
  
    /**
     * Corresponds to the "active" status: the process is up and running.
     * @see #getStatus()
     */
     ACTIVE,
    
    /**
     * Corresponds to the "kill requested" status: the process is shutting down.
     * @see #getStatus() 
     */
    KILL_REQUESTED,
  
    /**
     * Corresponds to the "kill confirmed" status: the process has terminated.
     * @see #getStatus() 
     */
    KILL_CONFIRMED,
  
    /**
     * Corresponds to the "restarting" status: the process is in the "restarting" queue.
     * @see #getStatus() 
     */
    RESTARTING,
  
    /**
     * Corresponds to the "suspended" status: the process is in the "suspended" queue.
     * @see #getStatus() 
     */
    SUSPENDED,  
    
    /**
     * Corresponds to the "stale" status: the process is in the active queue still, bug flagged as stale.
     * @see #getStatus()
     */
    STALE
  }
  
  public static final int    DEFAULT_SHUTDOWN_TIMEOUT_SECS = 30;
  public static final int    DEFAULT_KILL_RETRY            = 3;
  private DistributionInfo   _distributionInfo;
  private String             _processID                    = IDGenerator.makeIdFromDate();
  private String             _processDir;
  private String             _pid;
  private boolean            _deleteOnKill                 = false;
  private ProcessLock        _lock                         = new ProcessLock();
  private long               _creationTime                 = System.currentTimeMillis();
  private long               _lastAccess                   = System.currentTimeMillis();
  private int                _shutdownTimeout              = DEFAULT_SHUTDOWN_TIMEOUT_SECS;
  private int                _maxKillRetry                 = DEFAULT_KILL_RETRY;
  private LifeCycleStatus    _status                       = LifeCycleStatus.ACTIVE;
  private transient List<AbstractCommand>  _commands       = new ArrayList<AbstractCommand>();
  private transient int      _staleDetectionCount;
  private List<ActivePort>   _activePorts                  = new ArrayList<ActivePort>();
  private transient org.sapia.corus.interop.Status _processStatus;
  
  Process(){}
  
  /**
   * Creates an instance of this class.
   *
   * @param info a {@link DistributionInfo}.
   */
  public Process(DistributionInfo info) {
    _distributionInfo = info;
  }

  /**
   * Creates an instance of this class.
   *
   * @param info a {@link DistributionInfo}.
   * @param processID the identifier of the suspended process.
   */
  public Process(DistributionInfo info, String processID) {
    _distributionInfo = info;
    _processID = processID;
  }

  /**
   * Creates an instance of this class.
   *
   * @param info a {@link DistributionInfo}.
   * @param shutDownTimeoutSeconds the number of seconds the process corresponding
   * to this instance should be given to acknowledge a "kill".
   */
  public Process(DistributionInfo info, int shutDownTimeoutSeconds) {
    this(info);
    _shutdownTimeout = shutDownTimeoutSeconds;
  }

  /**
   * Creates an instance of this class.
   *
   * @param info a {@link DistributionInfo}.
   * @param shutDownTimeoutSeconds the number of seconds the process corresponding
   * to this instance should be given to acknowledge a "kill".
   * @param maxKillRetry the maximum number of times the Corus server should
   * attempt to kill the process corresponding to this instance before it
   * performs an "OS" kill.
   */
  public Process(DistributionInfo info, int shutDownTimeoutSeconds, int maxKillRetry) {
    this(info);
    _shutdownTimeout = shutDownTimeoutSeconds;
    _maxKillRetry    = maxKillRetry;
  }
  
  @Override
  @Transient
  public String getKey() {
    return _processID;
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
   * Returns this process' distribution information.
   *
   * @return this instance's {@link DistributionInfo}.
   */
  public DistributionInfo getDistributionInfo() {
    return _distributionInfo;
  }

  /**
   * Returns this instance's process identifier.
   *
   * @return this instance's process identifier as a string.
   */
  public String getProcessID() {
    return _processID;
  }

  /**
   * Returns this instance's process directory.
   *
   * @return this instance's process process private directory.
   */
  public String getProcessDir() {
    return _processDir;
  }
  
  /**
   * Sets this instance's process private directory.
   * 
   * @param dir a valid directory path.
   */
  public void setProcessDir(String dir) {
    _processDir = dir;
  }

  /**
   * Returns the time at which this instance was created.
   *
   * @return this instance's creation time as a long.
   */
  public long getCreationTime() {
    return _creationTime;
  }

  /**
   * Returns this instance's shutdown timeout.
   *
   * @return this instance's shutdown timeout, in seconds.
   */
  public int getShutdownTimeout() {
    return _shutdownTimeout;
  }
  
  /**
   * Sets this instance's shutdown timeout.
   *
   * @param timeout a timeout in seconds.
   */
  public void setShutdownTimeout(int timeout) {
    _shutdownTimeout = timeout;
  }
  
  /**
   * @return the <code>List</code> of <code>ActivePort</code>s that this instance 
   * holds.
   */
  public List<ActivePort> getActivePorts(){
    return _activePorts;
  }
  
  /**
   *  @param ports a <code>PortManager</code>.
   */
  public void releasePorts(PortManager ports){
    for(int i = 0; i < _activePorts.size(); i++){
      ActivePort port = (ActivePort)_activePorts.remove(i--);
      ports.releasePort(port.getName(), port.getPort());
    }
  }

  /**
   * @param port an <code>ActivePort</code>.
   */
  public void addActivePort(ActivePort port){
    _activePorts.add(port);
  }

  /**
   * Returns the maximum amount of time the Corus server should
   * attempt killing the process corresponding to this instance before
   * it performs an "OS" kill.
   *
   * @return the maximum amount of time the Corus server should
   * try killing the process corresponding to this instance.
   */
  public int getMaxKillRetry() {
    return _maxKillRetry;
  }
  
  /**
   * Sets this instance's max number of kill retries.
   * 
   * @param retry a max number of retries.
   */
  public void setMaxKillRetry(int retry) {
    _maxKillRetry = retry;
  }

  /**
   * @return the OS-specific process identifier corresponding to this
   * instance, or <code>null</code> if that identifier is not accessible.
   */
  public String getOsPid() {
    return _pid;
  }
  
  /**
   * Sets this instance's OS-specific process identifier.
   * 
   * @param pid an OS-specific identifier.
   */  
  public void setOsPid(String pid) {
    _pid = pid;
  }

  /**
   * Returns the time at which this instance was last accessed
   * by its corresponding process.
   *
   * @return a time in milliseconds.
   *
   */
  public long getLastAccess() {
    return _lastAccess;
  }
  
  /**
   * @return this instance's stale detection count.
   */
  @Transient
  public int getStaleDetectionCount() {
    return _staleDetectionCount;
  }
  
  /**
   * Increments this instance's stale detection count.
   */
  public void incrementStaleDetectionCount() {
    _staleDetectionCount++;
  }

  /**
   * Called by this instance's corresponding process when the latter
   * polls the corus server.
   *
   * @return the <code>List</code> of pending commands for the process.
   */
  public synchronized List<AbstractCommand> poll() {
    touch();
    
    List<AbstractCommand> commands = new ArrayList<AbstractCommand>(getCommands());
    getCommands().clear();

    return commands;
  }

  /**
   * Called by this instance's corresponding process when the latter
   * sends its status to the corus server.
   *
   * @return the <code>List</code> of pending commands for the process.
   */
  public synchronized List<AbstractCommand> status(org.sapia.corus.interop.Status stat) {
    touch();
    _processStatus = stat;

    List<AbstractCommand> commands  = new ArrayList<AbstractCommand>(getCommands());
    getCommands().clear();

    return commands;
  }
  
  /**
   * Clears this instance's {@link List} of {@link AbstractCommand}s.
   */
  public synchronized void clearCommands(){
    getCommands().clear();
  }

  /**
   * This process' "interoperability" status: corresponds to the "status"
   * message defined by the Corus Interop Spec.
   *
   * @return the <code>Status</code> of this process instance.
   */
  @Transient
  public synchronized org.sapia.corus.interop.Status getProcessStatus() {
    return _processStatus;
  }

  /**
   * Asks that this instance notifies its process that it should terminate.
   */
  public synchronized void kill(ProcessTerminationRequestor requestor) {
  	
  	/* Here we add a command to the process' queue if it
  	 * is active, or if a kill request has already been
  	 * emitted. In the second case, we have to add a command
  	 * since the process command queue is emptied at every
  	 * poll/status request. We can thus support multiple
  	 * kill attempts on a given process.
  	 */
    if (_status == LifeCycleStatus.ACTIVE || _status == LifeCycleStatus.KILL_REQUESTED) {
      _status = LifeCycleStatus.KILL_REQUESTED;

      Shutdown shutdown = new Shutdown();
      shutdown.setCommandId(CyclicIdGenerator.newRequestId());
      shutdown.setRequestor(requestor.getType());
      getCommands().add(shutdown);
      _lastAccess = System.currentTimeMillis();
    }
  }

  /**
   * Called by this instance's corresponding process to confirm that
   * its termination was successful (this is called just before
   * the process shuts down).
   */
  public synchronized void confirmKilled() {
    _status = LifeCycleStatus.KILL_CONFIRMED;
  }

  /**
   * Internally sets the "last access" flag of this
   * instance to the current time.
   */
  public void touch() {
    _lastAccess = System.currentTimeMillis();
  }

  /**
   * @return this instance's {@link LifeCycleStatus}.
   */
  public LifeCycleStatus getStatus() {
    return _status;
  }
  
  /**
   * Sets this process' status.
   * @param status a {@link LifeCycleStatus}
   */
  public void setStatus(LifeCycleStatus status){
    _status = status;
  }

  /**
   * Returns <code>true</code> if the process corresponding to this instance
   * has timed-out (has not polled for the amount of time specified).
   *
   * @param timeout a timeout used internally for verification.
   * @return <code>true</code> if this process has timed-out.
   */
  public boolean isTimedOut(long timeout) {
    return (System.currentTimeMillis() - _lastAccess) > timeout;
  }

  /**
   * Returns <code>true</code> if the process corresponding to this instance
   * has not confirmed its shut-down within its assigned shutdown delay.
   *
   * @return <code>true</code> if this instance's process shutdown has timed out.
   */
  @Transient
  public boolean isShutdownTimedOut() {
    return (System.currentTimeMillis() - _lastAccess) > _shutdownTimeout;
  }
  
  @Transient
  public List<AbstractCommand> getCommands(){
    return _commands == null ? _commands = new ArrayList<AbstractCommand>(5) : _commands;
  }
  
  /**
   * Returns the {@link ProcessLock} on this instance.
   * 
   * @return a {@link ProcessLock}.
   */
  @Transient
  public ProcessLock getLock() {
    return _lock;
  }
  
  void setLock(ProcessLock lock){
    _lock = lock;
  }
  
  /**
   * Clears this instance's state:
   * 
   * <ul>
   *  <li>Empties this instance's command queue (see {@link #getCommands()}).
   *  <li>Sets this instance's creation to the current time.
   *  <li>Sets this instance's last access time to the current time.
   *  <li>Sets this instance's status to {@link LifeCycleStatus#ACTIVE}.
   * </ul>
   */
  public void clear(){
    if(_commands != null) _commands.clear();
    _creationTime = System.currentTimeMillis();
    _lastAccess = System.currentTimeMillis();
    _status = LifeCycleStatus.ACTIVE;
  }

  public String toString() {
   return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("pid", _processID)
      .append("OS pid", _pid)
      .append("status", _status)
      .append("isLocked", _lock.isLocked())
      .toString();
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Process){
      Process other = (Process)obj;
      return this.getProcessID().equals(other.getProcessID());
    }
    else{
      return false;
    }
  }
  
  @Override
  public int hashCode() {
    return _processID.hashCode();
  }
  
  @Override
  public int compareTo(Process other) {
    int c = _distributionInfo.compareTo(other.getDistributionInfo());
    if(c == 0){
      c = _processID.compareTo(other.getProcessID());
    }
    return c;
  }
  
}
