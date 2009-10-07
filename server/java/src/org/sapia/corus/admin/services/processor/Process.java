package org.sapia.corus.admin.services.processor;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.admin.services.port.PortManager;
import org.sapia.corus.exceptions.LockException;
import org.sapia.corus.interop.AbstractCommand;
import org.sapia.corus.interop.Shutdown;
import org.sapia.corus.interop.client.CyclicIdGenerator;
import org.sapia.corus.util.IDGenerator;


/**
 * This class models an external process. An instance of this class actually corresponds to 
 * a process started by a corus server.
 *
 * @author Yanick Duchesne
 */
public class Process implements java.io.Serializable {

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
    SUSPENDED;  
  }
  
  public static final int    DEFAULT_SHUTDOWN_TIMEOUT_SECS = 30;
  public static final int    DEFAULT_KILL_RETRY            = 3;
  private DistributionInfo   _distInfo;
  private String             _vmId                         = IDGenerator.makeId();
  private String             _vmDir;
  private String             _pid;
  private boolean            _deleteOnKill                 = false;
  private transient Object   _lockOwner;
  private long               _creationTime                 = System.currentTimeMillis();
  private long               _lastAccess                   = System.currentTimeMillis();
  private int                _shutDownTimeout              = DEFAULT_SHUTDOWN_TIMEOUT_SECS;
  private int                _maxKillRetry                 = DEFAULT_KILL_RETRY;
  private LifeCycleStatus             _status                       = LifeCycleStatus.ACTIVE;
  private transient List<AbstractCommand>  _commands       = new ArrayList<AbstractCommand>();
  private List<ActivePort>   _activePorts                  = new ArrayList<ActivePort>();
  private org.sapia.corus.interop.Status             _interopStatus;

  /**
   * Creates an instance of this class.
   *
   * @param info a <code>DistributionInfo</code>.
   */
  public Process(DistributionInfo info) {
    _distInfo = info;
  }

  /**
   * Creates an instance of this class.
   *
   * @param info a <code>DistributionInfo</code>.
   * @param vmId the identifier of the suspended process.
   */
  public Process(DistributionInfo info, String vmId) {
    _distInfo = info;
    _vmId     = vmId;
  }

  /**
   * Creates an instance of this class.
   *
   * @param info a <code>DistributionInfo</code>.
   * @param shutDownTimeoutSeconds the number of seconds the process corresponding
   * to this instance should be given to acknowledge a "kill".
   */
  public Process(DistributionInfo info, int shutDownTimeoutSeconds) {
    this(info);
    _shutDownTimeout = shutDownTimeoutSeconds;
  }

  /**
   * Creates an instance of this class.
   *
   * @param info a <code>DistributionInfo</code>.
   * @param shutDownTimeoutSeconds the number of seconds the process corresponding
   * to this instance should be given to acknowledge a "kill".
   * @param maxKillRetry the maximum number of times the Corus server should
   * attempt to kill the process corresponding to this instance before it
   * performs an "OS" kill.
   */
  public Process(DistributionInfo info, int shutDownTimeoutSeconds, int maxKillRetry) {
    this(info);
    _shutDownTimeout = shutDownTimeoutSeconds;
    _maxKillRetry    = maxKillRetry;
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
   * @return this instance's <code>DistributionInfo</code>.
   */
  public DistributionInfo getDistributionInfo() {
    return _distInfo;
  }

  /**
   * Returns this instance's process identifier.
   *
   * @return this instance's process identifier as a string.
   */
  public String getProcessID() {
    return _vmId;
  }

  /**
   * Returns this instance's process directory.
   *
   * @return this instance's process process private directory.
   */
  public String getProcessDir() {
    return _vmDir;
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
    return _shutDownTimeout;
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
   * @param an <code>ActivePort</code>.
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
   * @return the OS-specific process identifier corresponding to this
   * instance, or <code>null</code> if that identifier is not accessible.
   */
  public String getOsPid() {
    return _pid;
  }

  /**
   * Sets this instance's shutdown timeout.
   *
   * @param a timeout in seconds.
   */
  public void setShutdownTimeout(int timeout) {
    _shutDownTimeout = timeout;
  }

  /**
   * Sets this instance's max number of kill retries.
   * 
   * @param retry a max number of retries.
   */
  public void setMaxKillRetry(int retry) {
    _shutDownTimeout = retry;
  }

  /**
   * Sets this instance's process private directory.
   * 
   * @param dir a valid directory path.
   */
  public void setProcessDir(String dir) {
    _vmDir = dir;
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
  public long lastAccess() {
    return _lastAccess;
  }

  /**
   * Called by this instance's corresponding process when the latter
   * polls the corus server.
   *
   * @return the <code>List</code> of pending commands for the process.
   */
  public synchronized List<AbstractCommand> poll() {
    _lastAccess = System.currentTimeMillis();

    List<AbstractCommand> commands = new ArrayList<AbstractCommand>(commands());
    commands().clear();

    return commands;
  }

  /**
   * Called by this instance's corresponding process when the latter
   * sends its status to the corus server.
   *
   * @return the <code>List</code> of pending commands for the process.
   */
  public synchronized List<AbstractCommand> status(org.sapia.corus.interop.Status stat) {
    _lastAccess    = System.currentTimeMillis();
    _interopStatus = stat;

    List<AbstractCommand> commands  = new ArrayList<AbstractCommand>(commands());
    commands().clear();

    return commands;
  }

  /**
   * This process' "interoperability" status: corresponds to the "status"
   * message defined by the Corus Interop Spec.
   *
   * @return the <code>Status</code> of this process instance.
   */
  public synchronized org.sapia.corus.interop.Status getProcessStatus() {
    return _interopStatus;
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
      commands().add(shutdown);
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
   * Acquires the lock on this instance.
   * 
   * @param leaser the object that attempts to obtain the lock on this instance.
   * @throws LockException if this instance is already locked by another object.
   */
  public synchronized void acquireLock(Object leaser) throws LockException {
    if ((_lockOwner != null) && (_lockOwner != leaser)) {
      throw new LockException("Process is currently locked - probably in shutdown; try again");
    }

    _lockOwner = leaser;
  }
   
  /**
   * Forces the releases of the lock on this instance.
   */
  public synchronized void releaseLock() {
    _lockOwner = null;
  }
  
  /**
   * @return <code>true</code> if this instance is locked.
   */
  public synchronized boolean isLocked() {
    return _lockOwner != null;
  }
  
  /**
   * Releases this instance's lock, ONLY if the passed in instance
   * is the owner of the lock (otherwise, this method has no effect).
   * 
   * @param leaser the object that attempts to release this
   * instance's locked.
   */
  public synchronized void releaseLock(Object leaser) {
    if ((_lockOwner != null) && (_lockOwner == leaser)) {
      _lockOwner = null;
    }
  }

  /**
   * Returns this instance's status.
   * @see #ACTIVE 
   * @see #KILL_CONFIRMED
   * @see #KILL_REQUESTED
   * @see #RESTARTING
   * @see #SUSPENDED
   * @return a status corresponding to one of the status constants of this class.
   */
  public LifeCycleStatus getStatus() {
    return _status;
  }
  
  /**
   * Sets this process' status.
   * @see #ACTIVE 
   * @see #KILL_CONFIRMED
   * @see #KILL_REQUESTED
   * @see #RESTARTING
   * @see #SUSPENDED
   * @param status a status constant value.
   */
  public void setStatus(LifeCycleStatus status){
    _status = status;
  }

  /**
   * Returns <code>true</code> if this process has timed-out (has not polled
   * for the amount of time specified).
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
  public boolean isShutdownTimedOut() {
    return (System.currentTimeMillis() - _lastAccess) > _shutDownTimeout;
  }
  
  private List<AbstractCommand> commands(){
    return _commands == null ? _commands = new ArrayList<AbstractCommand>(1) : _commands;
  }

  public String toString() {
    return "[ id=" + _vmId + ", pid=" + _pid + " " + _distInfo.toString() + " ]@" + Integer.toHexString(hashCode());
  }
}
