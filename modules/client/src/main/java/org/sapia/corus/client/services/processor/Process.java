package org.sapia.corus.client.services.processor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.common.CyclicIdGenerator;
import org.sapia.corus.client.common.IDGenerator;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.database.persistence.AbstractPersistent;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.interop.AbstractCommand;
import org.sapia.corus.interop.ConfigurationEvent;
import org.sapia.corus.interop.ConfigurationEvent.ConfigurationEventBuilder;
import org.sapia.corus.interop.Shutdown;
import org.sapia.ubik.util.Strings;

/**
 * This class models an external process. An instance of this class actually
 * corresponds to a process started by a Corus server.
 * 
 * @author Yanick Duchesne
 */
public class Process extends AbstractPersistent<String, Process> 
  implements Externalizable, Comparable<Process>, JsonStreamable, Matcheable {

  static final long serialVersionUID = 1L;
  
  // --------------------------------------------------------------------------
  // Identifies the different types kill "requestors".

  public enum ProcessTerminationRequestor {

    KILL_REQUESTOR_ADMIN("corus.admin", "Process termination has been requested by a Corus end user"), 
    KILL_REQUESTOR_SERVER("corus.server", "Process termination has been requested by the Corus instance"), 
    KILL_REQUESTOR_PROCESS("corus.process", "Process termination has been requested by the process itself");

    private String type;
    private String description;

    private ProcessTerminationRequestor(String type, String description) {
      this.type        = type;
      this.description = description;
    }
    
    /**
     * @return the actual requestor type.
     */
    public String getType() {
      return type;
    }
    
    /**
     * @return this instance's description.
     */
    public String description() {
      return description;
    }

    @Override
    public String toString() {
      return type;
    }
  }
  
  // --------------------------------------------------------------------------
  // Holds the constants corresponding to the different process lifecycles.

  public enum LifeCycleStatus {

    /**
     * Corresponds to the "active" status: the process is up and running.
     * 
     * @see #getStatus()
     */
    ACTIVE("act.", "Indicates that a process is running"),

    /**
     * Corresponds to the "kill requested" status: the process is shutting down.
     * 
     * @see #getStatus()
     */
    KILL_REQUESTED("shutd.", "Indicates that the shutdown of a process has been requested"),

    /**
     * Corresponds to the "kill confirmed" status: the process has terminated.
     * 
     * @see #getStatus()
     */
    KILL_CONFIRMED("shutd.", "Indicates that a process has confirmed its termination"),

    /**
     * Corresponds to the "restarting" status: the process is in the
     * "restarting" queue.
     * 
     * @see #getStatus()
     */
    RESTARTING("rest.", "Indicates that a process is restarting"),

    /**
     * Corresponds to the "suspended" status: the process is in the "suspended"
     * queue.
     * 
     * @see #getStatus()
     */
    SUSPENDED("susp.", "Indicates that a process is currently suspended"),

    /**
     * Corresponds to the "stale" status: the process is in the active queue
     * still, bug flagged as stale.
     * 
     * @see #getStatus()
     */
    STALE("stale", "Indicates that a process is stale (was deemed unresponsived and killed, and not restarted)");
    
    private String abbr;
    private String description;
    
    private LifeCycleStatus(String abbr, String description) {
      this.abbr        = abbr;
      this.description = description;
    }
    
    /**
     * @return this instance's corresponding abbreviation (used for display).
     */
    public String abbreviation() {
      return abbr;
    }
    
    /**
     * @return
     */
    public String description() {
      return description;
    }
  }
  
  // --------------------------------------------------------------------------

  public static final int DEFAULT_SHUTDOWN_TIMEOUT_SECS = 30;
  public static final int DEFAULT_KILL_RETRY            = 3;
  
  private DistributionInfo                         distributionInfo;
  private String                                   processID       = IDGenerator.makeIdFromDate();
  private String                                   processDir;
  private String                                   pid;
  private boolean                                  deleteOnKill    = false;
  private transient ProcessLock                    lock            = new ProcessLock();
  private long                                     creationTime    = System.currentTimeMillis();
  private long                                     lastAccess      = System.currentTimeMillis();
  private int                                      shutdownTimeout = DEFAULT_SHUTDOWN_TIMEOUT_SECS;
  private int                                      pollTimeout     = -1;
  private int                                      maxKillRetry    = DEFAULT_KILL_RETRY;
  private LifeCycleStatus                          status          = LifeCycleStatus.ACTIVE;
  private transient List<AbstractCommand>          commands        = new ArrayList<AbstractCommand>();
  private transient int                            staleDeleteCount;
  private List<ActivePort>                         activePorts     = new ArrayList<ActivePort>();
  private transient org.sapia.corus.interop.Status processStatus;

  /**
   * Meant for externalization only.
   */
  public Process() {
  }
  

  /**
   * Creates an instance of this class.
   * 
   * @param info
   *          a {@link DistributionInfo}.
   */
  public Process(DistributionInfo info) {
    distributionInfo = info;
  }

  /**
   * Creates an instance of this class.
   * 
   * @param info
   *          a {@link DistributionInfo}.
   * @param processID
   *          the identifier of the suspended process.
   */
  public Process(DistributionInfo info, String processID) {
    this(info);
    this.processID = processID;
  }

  /**
   * Creates an instance of this class.
   * 
   * @param info
   *          a {@link DistributionInfo}.
   * @param shutDownTimeoutSeconds
   *          the number of seconds the process corresponding to this instance
   *          should be given to acknowledge a "kill".
   */
  public Process(DistributionInfo info, int shutDownTimeoutSeconds) {
    this(info);
    shutdownTimeout = shutDownTimeoutSeconds;
  }

  /**
   * Creates an instance of this class.
   * 
   * @param info
   *          a {@link DistributionInfo}.
   * @param shutDownTimeoutSeconds
   *          the number of seconds the process corresponding to this instance
   *          should be given to acknowledge a "kill".
   * @param maxKillRetry
   *          the maximum number of times the Corus server should attempt to
   *          kill the process corresponding to this instance before it performs
   *          an "OS" kill.
   */
  public Process(DistributionInfo info, int shutDownTimeoutSeconds, int maxKillRetry) {
    this(info);
    this.shutdownTimeout = shutDownTimeoutSeconds;
    this.maxKillRetry = maxKillRetry;
  }

  @Override
  @Transient
  public String getKey() {
    return processID;
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
   * Returns this process' distribution information.
   * 
   * @return this instance's {@link DistributionInfo}.
   */
  public DistributionInfo getDistributionInfo() {
    return distributionInfo;
  }

  /**
   * Returns this instance's process identifier.
   * 
   * @return this instance's process identifier as a string.
   */
  public String getProcessID() {
    return processID;
  }

  /**
   * Returns this instance's process directory.
   * 
   * @return this instance's process process private directory.
   */
  public String getProcessDir() {
    return processDir;
  }

  /**
   * Sets this instance's process private directory.
   * 
   * @param dir
   *          a valid directory path.
   */
  public void setProcessDir(String dir) {
    processDir = dir;
  }

  /**
   * Returns the time at which this instance was created.
   * 
   * @return this instance's creation time as a long.
   */
  public long getCreationTime() {
    return creationTime;
  }

  /**
   * Returns this instance's shutdown timeout.
   * 
   * @return this instance's shutdown timeout, in seconds.
   */
  public int getShutdownTimeout() {
    return shutdownTimeout;
  }

  /**
   * Sets this instance's shutdown timeout.
   * 
   * @param timeout
   *          a timeout in seconds.
   */
  public void setShutdownTimeout(int timeout) {
    shutdownTimeout = timeout;
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
   * @return the <code>List</code> of <code>ActivePort</code>s that this
   *         instance holds.
   */
  public List<ActivePort> getActivePorts() {
    return activePorts;
  }

  /**
   * @param ports
   *          a <code>PortManager</code>.
   */
  public void releasePorts(PortManager ports) {
    for (int i = 0; i < activePorts.size(); i++) {
      ActivePort port = (ActivePort) activePorts.remove(i--);
      ports.releasePort(port.getName(), port.getPort());
    }
  }

  /**
   * @param port
   *          an <code>ActivePort</code>.
   */
  public void addActivePort(ActivePort port) {
    activePorts.add(port);
  }

  /**
   * Returns the maximum amount of time the Corus server should attempt killing
   * the process corresponding to this instance before it performs an "OS" kill.
   * 
   * @return the maximum amount of time the Corus server should try killing the
   *         process corresponding to this instance.
   */
  public int getMaxKillRetry() {
    return maxKillRetry;
  }

  /**
   * Sets this instance's max number of kill retries.
   * 
   * @param retry
   *          a max number of retries.
   */
  public void setMaxKillRetry(int retry) {
    maxKillRetry = retry;
  }

  /**
   * @return the OS-specific process identifier corresponding to this instance,
   *         or <code>null</code> if that identifier is not accessible.
   */
  public String getOsPid() {
    return pid;
  }

  /**
   * Sets this instance's OS-specific process identifier.
   * 
   * @param pid
   *          an OS-specific identifier.
   */
  public void setOsPid(String pid) {
    this.pid = pid;
  }

  /**
   * Returns the time at which this instance was last accessed by its
   * corresponding process.
   * 
   * @return a time in milliseconds.
   * 
   */
  public long getLastAccess() {
    return lastAccess;
  }

  /**
   * @return this instance's stale detection count.
   */
  @Transient
  public int getStaleDetectionCount() {
    return staleDeleteCount;
  }

  /**
   * Increments this instance's stale detection count.
   */
  public void incrementStaleDetectionCount() {
    staleDeleteCount++;
  }

  /**
   * Called by this instance's corresponding process when the latter polls the
   * corus server.
   * 
   * @return the {@link List} of pending commands for the process.
   */
  public synchronized List<AbstractCommand> poll() {
    touch();

    List<AbstractCommand> commands = new ArrayList<AbstractCommand>(getCommands());
    getCommands().clear();

    return commands;
  }

  /**
   * Called by this instance's corresponding process when the latter sends its
   * status to the corus server.
   * 
   * @return the <code>List</code> of pending commands for the process.
   */
  public synchronized List<AbstractCommand> status(org.sapia.corus.interop.Status stat) {
    touch();
    processStatus = stat;

    List<AbstractCommand> commands = new ArrayList<AbstractCommand>(getCommands());
    getCommands().clear();

    return commands;
  }

  /**
   * Clears this instance's {@link List} of {@link AbstractCommand}s.
   */
  public synchronized void clearCommands() {
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
    return processStatus;
  }

  /**
   * Creates a configuration event of updated configuration properties to be published
   * to the managed process the next time it polls back the server.
   * 
   * @param updatedProperties The collection of updated properties.
   */
  public synchronized void configurationUpdated(Collection<Property> updatedProperties) {
    // Propagation of config change on appropriate process statuses
    if (status == LifeCycleStatus.ACTIVE || status == LifeCycleStatus.STALE) {
      ConfigurationEventBuilder builder = ConfigurationEvent.builder()
          .commandId(CyclicIdGenerator.newCommandId())
          .type(ConfigurationEvent.TYPE_UPDATE);
      for (Property property: updatedProperties) {
        builder.param(property.getName(), property.getValue());
      }
      
      getCommands().add(builder.build());
    }
  }

  /**
   * Creates a configuration event of deleted configuration properties to be published
   * to the managed process the next time it polls back the server.
   * 
   * @param deletedProperties The collection of the deleted properties
   */
  public synchronized void configurationDeleted(Collection<Property> deletedProperties) {
    // Propagation of config change on appropriate process statuses
    if (status == LifeCycleStatus.ACTIVE || status == LifeCycleStatus.STALE) {
      ConfigurationEventBuilder builder = ConfigurationEvent.builder()
          .commandId(CyclicIdGenerator.newCommandId())
          .type(ConfigurationEvent.TYPE_DELETE);
      for (Property property: deletedProperties) {
        builder.param(property.getName(), "");
      }
      
      getCommands().add(builder.build());
    }
  }
  
  /**
   * Asks that this instance notifies its process that it should terminate.
   */
  public synchronized void kill(ProcessTerminationRequestor requestor) {

    /*
     * Here we add a command to the process' queue if it is active, or if a kill
     * request has already been emitted. In the second case, we have to add a
     * command since the process command queue is emptied at every poll/status
     * request. We can thus support multiple kill attempts on a given process.
     */
    if (status == LifeCycleStatus.ACTIVE || status == LifeCycleStatus.STALE) {
      status = LifeCycleStatus.KILL_REQUESTED;

      Shutdown shutdown = new Shutdown();
      shutdown.setCommandId(CyclicIdGenerator.newRequestId());
      shutdown.setRequestor(requestor.getType());
      getCommands().add(shutdown);
      lastAccess = System.currentTimeMillis();
    }
  }

  /**
   * Called by this instance's corresponding process to confirm that its
   * termination was successful (this is called just before the process shuts
   * down).
   */
  public synchronized void confirmKilled() {
    status = LifeCycleStatus.KILL_CONFIRMED;
  }

  /**
   * Internally sets the "last access" flag of this instance to the current
   * time.
   */
  public void touch() {
    lastAccess = System.currentTimeMillis();
  }
  
  @Override
  public void recycle() {
    touch();
    creationTime = System.currentTimeMillis();
    super.recycle();
  }

  /**
   * @return this instance's {@link LifeCycleStatus}.
   */
  public LifeCycleStatus getStatus() {
    return status;
  }

  /**
   * Sets this process' status.
   * 
   * @param status
   *          a {@link LifeCycleStatus}
   */
  public void setStatus(LifeCycleStatus status) {
    this.status = status;
  }

  /**
   * Returns <code>true</code> if the process corresponding to this instance has
   * timed-out (has not polled for the amount of time specified).
   * 
   * @param timeout
   *          a timeout used internally for verification.
   * @return <code>true</code> if this process has timed-out.
   */
  public boolean isTimedOut(long timeout) {
    return (System.currentTimeMillis() - lastAccess) > timeout;
  }

  /**
   * Returns <code>true</code> if the process corresponding to this instance has
   * not confirmed its shut-down within its assigned shutdown delay.
   * 
   * @return <code>true</code> if this instance's process shutdown has timed
   *         out.
   */
  @Transient
  public boolean isShutdownTimedOut() {
    return (System.currentTimeMillis() - lastAccess) > shutdownTimeout;
  }

  @Transient
  public List<AbstractCommand> getCommands() {
    return commands == null ? commands = new ArrayList<AbstractCommand>(5) : commands;
  }

  /**
   * Returns the {@link ProcessLock} on this instance.
   * 
   * @return a {@link ProcessLock}.
   */
  @Transient
  public ProcessLock getLock() {
    return lock;
  }

  void setLock(ProcessLock lock) {
    this.lock = lock;
  }

  /**
   * Clears this instance's state:
   * 
   * <ul>
   * <li>Empties this instance's command queue (see {@link #getCommands()}).
   * <li>Sets this instance's creation to the current time.
   * <li>Sets this instance's last access time to the current time.
   * <li>Sets this instance's status to {@link LifeCycleStatus#ACTIVE}.
   * </ul>
   */
  public void clear() {
    if (commands != null)
      commands.clear();
    creationTime = System.currentTimeMillis();
    lastAccess = System.currentTimeMillis();
    status = LifeCycleStatus.ACTIVE;
  }
  
  @Override
  public void toJson(JsonStream stream) {
    stream.beginObject()
      .field("id").value(processID)
      .field("name").value(distributionInfo.getProcessName())
      .field("pid").value(pid)
      .field("distribution").value(distributionInfo.getName())
      .field("version").value(distributionInfo.getVersion())
      .field("profile").value(distributionInfo.getProfile())
      .field("creationTimeMillis").value(creationTime)
      .field("creationTimestamp").value(new Date(creationTime))
      .field("lastAccessTimeMillis").value(lastAccess)
      .field("lastAccessTimestamp").value(new Date(lastAccess))
      .field("status").value(status.name())
      .field("maxKillRetry").value(maxKillRetry)
      .field("deleteOnKill").value(deleteOnKill)
      .field("shutdownTimeout").value(shutdownTimeout)
      .field("staleDetectionCount").value(staleDeleteCount)
      .field("activePorts").beginArray();
    for (ActivePort p : activePorts) {
      stream.beginObject()
        .field("name").value(p.getName())
        .field("port").value(p.getPort())
      .endObject();
    }
    stream.endArray();
    stream.endObject();
  }
  
  @Override
  public boolean matches(Pattern pattern) {
    return (pid != null && pattern.matches(pid)) || 
        pattern.matches(processID) ||
        pattern.matches(distributionInfo.getName()) ||
        pattern.matches(distributionInfo.getProcessName()) ||
        pattern.matches(distributionInfo.getProfile()) ||
        pattern.matches(distributionInfo.getVersion()) ||
        pattern.matches(status.name().toLowerCase()) ||
        pattern.matches(status.abbreviation());
  }
  
  public boolean matches(ProcessCriteria criteria) {
    return criteria.getDistribution().matches(distributionInfo.getName())
        && criteria.getVersion().matches(distributionInfo.getVersion())
        && criteria.getName().matches(distributionInfo.getProcessName())
        && criteria.getPid().matches(processID)
        && (criteria.getLifeCycles().isEmpty() || criteria.getLifeCycles().contains(status))
        && (criteria.getProfile().isNull() || criteria.getProfile().get().equals(distributionInfo.getProfile()))
        && matches(criteria.getPortCriteria());
  }
  
  private boolean matches(OptionalValue<PortCriteria> criteria) {
    if (criteria.isSet()) {
      for (ActivePort p : activePorts) {
        if (p.matches(criteria.get())) {
          return true;
        }
      }
      return false;
    }
    return true;
  }
 

  // --------------------------------------------------------------------------
  // Comparable
  
  @Override
  public int compareTo(Process other) {
    int c = distributionInfo.compareTo(other.getDistributionInfo());
    if (c == 0) {
      c = processID.compareTo(other.getProcessID());
    }
    return c;
  }
  
  // --------------------------------------------------------------------------
  // Object overrides

  @Override
  public String toString() {
    return Strings.toStringFor(
        this, "id", processID, 
        "pid", pid, 
        "status", status, 
        "lock", lock,
        "deleted", isDeleted()
    );
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Process) {
      Process other = (Process) obj;
      return this.getProcessID().equals(other.getProcessID());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return processID.hashCode();
  }

  // --------------------------------------------------------------------------
  // Externalizable
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    distributionInfo = (DistributionInfo) in.readObject();
    processID        = in.readUTF();
    processDir       = in.readUTF();
    pid              = in.readUTF();
    deleteOnKill     = in.readBoolean();
    lock             = new ProcessLock();
    creationTime     = in.readLong();
    lastAccess       = in.readLong();
    shutdownTimeout  = in.readInt();
    pollTimeout      = in.readInt();
    maxKillRetry     = in.readInt();
    status           = (LifeCycleStatus) in.readObject();
    commands         = (List<AbstractCommand>) in.readObject();
    activePorts      = (List<ActivePort>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(distributionInfo);
    out.writeUTF(processID);
    out.writeUTF(processDir);
    out.writeUTF(pid);
    out.writeBoolean(deleteOnKill);
    out.writeLong(creationTime);
    out.writeLong(lastAccess);
    out.writeInt(shutdownTimeout);
    out.writeInt(pollTimeout);
    out.writeInt(maxKillRetry);
    out.writeObject(status);
    out.writeObject(commands);
    out.writeObject(activePorts);
  }

}
