package org.sapia.corus.client.services.processor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.common.CyclicIdGenerator;
import org.sapia.corus.client.common.IDGenerator;
import org.sapia.corus.client.common.Mappable;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.database.persistence.AbstractPersistent;
import org.sapia.corus.client.services.deployer.dist.Starter;
import org.sapia.corus.client.services.deployer.dist.StarterType;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.interop.AbstractCommand;
import org.sapia.corus.interop.ConfigurationEvent;
import org.sapia.corus.interop.ConfigurationEvent.ConfigurationEventBuilder;
import org.sapia.corus.interop.Shutdown;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Strings;

/**
 * This class models an external process. An instance of this class actually
 * corresponds to a process started by a Corus server.
 *
 * @author Yanick Duchesne
 */
public class Process extends AbstractPersistent<String, Process>
  implements Externalizable, Comparable<Process>, JsonStreamable, Matcheable, Mappable {

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

  private static final int VERSION_1 = 1;
  private static final int VERSION_2 = 2;
  private static final int VERSION_3 = 3;
  private static final int VERSION_4 = 4;
  private static final Set<Integer> SUPPORTED_VERSIONS = Collects.arrayToSet(
      VERSION_1, VERSION_2, VERSION_3, VERSION_4
  );
  private static final int CURRENT_VERSION = VERSION_4;

  public static final int DEFAULT_SHUTDOWN_TIMEOUT_SECS = 30;
  public static final int DEFAULT_KILL_RETRY            = 3;

  private DistributionInfo                         distributionInfo;
  private String                                   processID       = IDGenerator.makeDateId();
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
  private ProcessStartupInfo                       startupInfo     = new ProcessStartupInfo();
  private boolean                                  interopEnabled  = true;
  private StarterType                              starterType     = StarterType.UNDEFINED;
  private OptionalValue<Integer>                   numaNode        = OptionalValue.none();
  private final Map<String, String>           nativeProcessOptions = new HashMap<>();

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
      ActivePort port = activePorts.remove(i--);
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
   * @return the assigned numa node of this process.
   */
  public OptionalValue<Integer> getNumaNode() {
    return numaNode;
  }

  /**
   * Changes the numa node assigned to this process.
   *
   * @param nodeId The new numa node value.
   */
  public void setNumaNode(Integer nodeId) {
    numaNode = OptionalValue.of(nodeId);
  }

  /**
   * @return The native process options of this process.
   */
  public Map<String, String> getNativeProcessOptions() {
    return nativeProcessOptions;
  }

  /**
   * Sets a native process option to the given value.
   *
   * @param optionName The name of the native process option.
   * @param optionValue The value of the native process options.
   * @return This {@link Process} instance to chain calls like a builder.
   */
  public Process setNativeProcessOption(String optionName, String optionValue) {
    nativeProcessOptions.put(optionName, optionValue);
    return this;
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
   * @param interopEnabled if <code>true</code>, indicates that interop is enabled (<code>true</code> by default).
   */
  public void setInteropEnabled(boolean interopEnabled) {
    this.interopEnabled = interopEnabled;
  }

  /**
   * @return <code>true</code> if interop is enabled.
   */
  public boolean isInteropEnabled() {
    return interopEnabled;
  }

  /**
   * @param startupInfo the {@link ProcessStartupInfo} to assign to this instance.
   */
  public void setStartupInfo(ProcessStartupInfo startupInfo) {
    this.startupInfo = startupInfo;
  }

  /**
   * @return this instance's {@link ProcessStartupInfo}.
   */
  public ProcessStartupInfo getStartupInfo() {
    return startupInfo;
  }

  /**
   * @param starterType the {@link StarterType} corresponding to the {@link Starter} which generated the command-line
   * to start this process.
   */
  public void setStarterType(StarterType starterType) {
    this.starterType = starterType;
  }

  /**
   * @return the {@link StarterType} corresponding to the {@link Starter} which generated the command-line
   * to start this process.
   */
  public StarterType getStarterType() {
    return starterType;
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
  public synchronized void incrementStaleDetectionCount() {
    staleDeleteCount++;
  }

  /**
   * Called by this instance's corresponding process when the latter polls the
   * corus server.
   *
   * @return the {@link List} of pending commands for the process.
   */
  public synchronized List<AbstractCommand> poll() {
    staleDeleteCount = 0;
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
    lastAccess   = System.currentTimeMillis();
    status       = LifeCycleStatus.ACTIVE;
  }

  @Override
  public Map<String, Object> asMap() {
    Map<String, Object> toReturn = new HashMap<String, Object>();
    toReturn.put("process.distribution.name", distributionInfo.getName());
    toReturn.put("process.distribution.version", distributionInfo.getVersion());
    toReturn.put("process.name", distributionInfo.getProcessName());
    toReturn.put("process.id", processID);
    toReturn.put("process.pid", pid);
    toReturn.put("process.deleteOnKill", deleteOnKill);
    toReturn.put("process.creationTime", creationTime);
    toReturn.put("process.lastAccessTime", lastAccess);
    toReturn.put("process.shutdownTimeout", shutdownTimeout);
    toReturn.put("process.pollTimeout", pollTimeout);
    toReturn.put("process.maxKillRetry", maxKillRetry);
    return toReturn;
  }

  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject()
      .field("classVersion").value(CURRENT_VERSION)
      .field("id").value(processID)
      .field("name").value(distributionInfo.getProcessName())
      .field("pid").value(pid)
      .field("distribution").value(distributionInfo.getName())
      .field("version").value(distributionInfo.getVersion())
      .field("profile").value(distributionInfo.getProfile());

    if (level == ContentLevel.DETAIL) {
      stream.field("creationTimeMillis").value(creationTime)
      .field("creationTimestamp").value(new Date(creationTime))
      .field("lastAccessTimeMillis").value(lastAccess)
      .field("lastAccessTimestamp").value(new Date(lastAccess))
      .field("status").value(status.name())
      .field("maxKillRetry").value(maxKillRetry)
      .field("deleteOnKill").value(deleteOnKill)
      .field("pollTimeout").value(pollTimeout)
      .field("shutdownTimeout").value(shutdownTimeout)
      .field("staleDetectionCount").value(staleDeleteCount)
      .field("processDir").value(processDir)
      .field("interopEnabled").value(interopEnabled)
      .field("starterType").value(starterType.name());
    }

    stream.field("activePorts").beginArray();
    for (ActivePort p : activePorts) {
      stream.beginObject()
        .field("name").value(p.getName())
        .field("port").value(p.getPort())
      .endObject();
    }
    stream.endArray();
    if (level == ContentLevel.DETAIL) {
      stream.field("startupInfo");
      startupInfo.toJson(stream, level);
    }

    // V4
    if (level == ContentLevel.DETAIL) {
      if (numaNode.isSet()) {
        stream.field("numaNode").value(numaNode.get());
      }
      if (nativeProcessOptions.size() > 0) {
        stream.field("nativeProcessOptions").beginArray();
        for (Map.Entry<String, String> entry: nativeProcessOptions.entrySet()) {
          stream.beginObject()
              .field(entry.getKey())
              .value(entry.getValue())
              .endObject();
        }
        stream.endArray();
      }
    }

    stream.endObject();
  }

  public static Process fromJson(JsonInput input) {
    Process p = new Process();
    int inputVersion = input.getInt("classVersion");
    if (SUPPORTED_VERSIONS.contains(inputVersion)) {
      p.lock = new ProcessLock();
      p.processID = input.getString("id");
      p.pid       = input.getString("pid");
      p.distributionInfo = new DistributionInfo(
          input.getString("distribution"),
          input.getString("version"),
          input.getString("profile"),
          input.getString("name")
      );
      p.creationTime     = input.getLong("creationTimeMillis");
      p.lastAccess       = input.getLong("lastAccessTimeMillis");
      p.status           = LifeCycleStatus.valueOf(input.getString("status"));
      p.maxKillRetry     = input.getInt("maxKillRetry");
      p.deleteOnKill     = input.getBoolean("deleteOnKill");
      p.pollTimeout      = input.getInt("pollTimeout");
      p.shutdownTimeout  = input.getInt("shutdownTimeout");
      p.staleDeleteCount = input.getInt("staleDetectionCount");
      p.processDir       = input.getString("processDir");
      for (JsonInput in : input.iterate("activePorts")) {
        ActivePort port = new ActivePort(
            in.getString("name"),
            in.getInt("port")
        );
        p.addActivePort(port);
      }
      if (inputVersion >= VERSION_2) {
        p.startupInfo = ProcessStartupInfo.fromJson(input.getObject("startupInfo"));
      }
      if (inputVersion >= VERSION_3) {
        p.interopEnabled = input.getBoolean("interopEnabled");
        p.starterType = StarterType.valueOf(input.getString("starterType"));
      }
      if (inputVersion >= VERSION_4) {
        if (input.containsField("numaNode")) {
          p.setNumaNode(input.getInt("numaNode"));
        }
        if (input.containsField("nativeProcessOptions")) {
          for (JsonInput in : input.iterate("nativeProcessOptions")) {
            for (String name: in.fields()) {
              p.setNativeProcessOption(name, in.getString(name));
            }
          }
        }
      }

    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }

    return p;
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
        && (pid == null ? criteria.getOsPid().matches("null") : criteria.getOsPid().matches(pid))
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

    int inputVersion = in.readInt();

    if (SUPPORTED_VERSIONS.contains(inputVersion)) {
      distributionInfo = (DistributionInfo) in.readObject();
      processID        = in.readUTF();
      processDir       = in.readUTF();
      pid              = (String) in.readObject();
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
      if (inputVersion >= VERSION_2) {
        startupInfo = ObjectUtil.safeNonNull((ProcessStartupInfo) in.readObject(), ProcessStartupInfo.forSingleProcess());
      }
      if (inputVersion >= VERSION_3) {
        interopEnabled = in.readBoolean();
        starterType = (StarterType) in.readObject();
      }
      if (inputVersion >= VERSION_4) {
        if (in.readBoolean()) {
          numaNode = OptionalValue.of(in.readInt());
        }
        int nativeProcessOptionCount = in.readInt();
        for (int i = 0; i < nativeProcessOptionCount; i++) {
          nativeProcessOptions.put(in.readUTF(), in.readUTF());
        }
      }
    } else {
      throw new IllegalStateException("Version not handled: " + inputVersion);
    }
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {

    out.writeInt(CURRENT_VERSION);
    // V1
    out.writeObject(distributionInfo);
    out.writeUTF(processID);
    out.writeUTF(processDir);
    out.writeObject(pid);
    out.writeBoolean(deleteOnKill);
    out.writeLong(creationTime);
    out.writeLong(lastAccess);
    out.writeInt(shutdownTimeout);
    out.writeInt(pollTimeout);
    out.writeInt(maxKillRetry);
    out.writeObject(status);
    out.writeObject(commands);
    out.writeObject(activePorts);
    // V2
    out.writeObject(startupInfo);
    // V3
    out.writeBoolean(interopEnabled);
    out.writeObject(starterType);
    // V4
    if (numaNode.isSet()) {
      out.writeBoolean(true);
      out.writeInt(numaNode.get());
    } else {
      out.writeBoolean(false);
    }
    out.writeInt(nativeProcessOptions.size());
    for (Map.Entry<String, String> entry: nativeProcessOptions.entrySet()) {
      out.writeUTF(entry.getKey());
      out.writeUTF(entry.getValue());
    }
  }

}
