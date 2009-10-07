package org.sapia.corus.admin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sapia.corus.ClusterInfo;
import org.sapia.corus.admin.services.configurator.Configurator;
import org.sapia.corus.admin.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.admin.services.deployer.Deployer;
import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.admin.services.port.PortManager;
import org.sapia.corus.admin.services.processor.ExecConfig;
import org.sapia.corus.admin.services.processor.ProcStatus;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.admin.services.processor.Processor;
import org.sapia.corus.cluster.ClusterInterceptor;
import org.sapia.corus.cluster.ClusterManager;
import org.sapia.corus.cron.CronJobInfo;
import org.sapia.corus.cron.CronModule;
import org.sapia.corus.cron.InvalidTimeException;
import org.sapia.corus.deployer.ConcurrentDeploymentException;
import org.sapia.corus.deployer.DeployOsAdapter;
import org.sapia.corus.deployer.DeployOutputStream;
import org.sapia.corus.deployer.DeploymentMetadata;
import org.sapia.corus.deployer.transport.ClientDeployOutputStream;
import org.sapia.corus.deployer.transport.DeploymentClientFactory;
import org.sapia.corus.exceptions.CorusException;
import org.sapia.corus.exceptions.CorusRuntimeException;
import org.sapia.corus.exceptions.LogicException;
import org.sapia.corus.exceptions.PortActiveException;
import org.sapia.corus.exceptions.PortRangeConflictException;
import org.sapia.corus.exceptions.PortRangeInvalidException;
import org.sapia.corus.util.ProgressMsg;
import org.sapia.corus.util.ProgressQueue;
import org.sapia.corus.util.ProgressQueueImpl;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.util.xml.ProcessingException;


/**
 * Implements the <code>CorusFacade</code> interface over a direct connection to a
 * Corus server (not meant to be used if client is outside de LAN).
 *
 * @author Yanick Duchesne
 */
public class CorusFacadeImpl implements CorusFacade {
  static final int             BUFSZ = 2048;
  static final long            RECONNECT_INTERVAL = 15000;
  protected long               _lastReconnect = System.currentTimeMillis();
  protected Corus             _dyn;
  protected ServerAddress      _addr;
  protected String             _domain;
  protected Map                _components    = new HashMap();
  protected ClusterInterceptor _interceptor;
  protected Set                _otherHosts    = new HashSet();
  protected Map                _cachedStubs   = Collections.synchronizedMap(new HashMap());
  
  /**
   * Constructor for RemoteCorusFacade.
   */
  public CorusFacadeImpl(String host, int port) throws CorusException {
    _interceptor = new ClusterInterceptor();
    reconnect(host, port);
  }
  
  /**
   * Reconnects to the corus server at the given host/port.
   * @param host the host of the server to reconnect to.
   * @param port the port of the server to reconnect to.
   * @throws CorusException
   */
  public synchronized void reconnect(String host, int port)
  throws CorusException {
    _addr = new TCPAddress(host, port);
    reconnect();
  }
  
  /**
   * Reconnects to the corus server that this instance corresponds to.
   *
   * @throws CorusException
   */
  public synchronized void reconnect() throws CorusException {
    try {
      _dyn = (Corus) Hub.connect(((TCPAddress) _addr).getHost(),
        ((TCPAddress) _addr).getPort());
      _domain = _dyn.getDomain();
      _otherHosts.clear();
      _cachedStubs.clear();
      _components.clear();
      
      ClusterManager mgr = (ClusterManager) _dyn.lookup(ClusterManager.ROLE);
      _otherHosts.addAll(mgr.getHostAddresses());
    } catch (java.rmi.RemoteException e) {
      throw new CorusException(e);
    }
  }
  
  public synchronized ServerAddress getServerAddress() {
    return _addr;
  }
  
  public synchronized Collection getServerAddresses() {
    refresh();
    
    return Collections.unmodifiableSet(_otherHosts);
  }
  
  public synchronized String getDomain() {
    return _domain;
  }
  
  public synchronized ProgressQueue deploy(final String fileName, final ClusterInfo cluster)
  throws IOException,
    ConcurrentDeploymentException,
    CorusException {
    refresh();
    final ProgressQueueImpl queue = new ProgressQueueImpl();
    
    if(ArgFactory.isPattern(fileName)){
      Thread deployer = new Thread(new Runnable(){
          public void run() {
            try{
              Object[] baseDirAndFilePattern = split(fileName);
              File baseDir = (File)baseDirAndFilePattern[0];
              Arg pattern = ArgFactory.parse((String)baseDirAndFilePattern[1]);
              File[] files = baseDir.listFiles();
              ProgressQueue tmp = null;
              int fileCount = 0;
              for(int i = 0; i < files.length; i++){
                if(!files[i].isDirectory() && pattern.matches(files[i].getName())){
                  queue.info("Deploying: " + files[i].getName());
                  try{
                    fileCount++;
                    tmp = doDeploy(files[i].getAbsolutePath(), cluster);
                    while(tmp.hasNext()){
                      List<ProgressMsg> lst = tmp.fetchNext();
                      for(int j = 0; j < lst.size(); j++){
                        ProgressMsg msg = (ProgressMsg)lst.get(j);
                        queue.addMsg(msg);
                      }
                    }
                  }catch(Exception e){
                    queue.error(e);
                  }
                }
              }
              if(fileCount == 0){
                queue.warning("No file found to deploy for: " + fileName);
              }
              else{
                queue.info("Batch deployment completed");
              }
            }finally{
              queue.close();
            }
          }
        });
      deployer.start();
      return queue;
    } else{
      return doDeploy(fileName, cluster);
    }
    
  }
  
  private ProgressQueue doDeploy(String fileName, ClusterInfo cluster)   
  throws IOException,
    ConcurrentDeploymentException,
    CorusException {

    OutputStream        os     = null;
    BufferedInputStream bis    = null;
    boolean             closed = false;
    
    try {
      File toDeploy = new File(fileName);
      
      if (!toDeploy.exists()) {
        throw new IOException(toDeploy.getAbsolutePath() + " does not exist");
      }
      
      if (toDeploy.isDirectory()) {
        throw new IOException(toDeploy.getAbsolutePath() + " is a directory");
      }
      
      
      DeploymentMetadata meta = new DeploymentMetadata(toDeploy.getName(), toDeploy.length(), cluster.getTargets(), cluster.isClustered());
      DeployOutputStream dos  = new ClientDeployOutputStream(meta, DeploymentClientFactory.newDeploymentClientFor(_addr));
      
      /*getDeployer().getDeployOutputStream(new File(fileName).getName(),
                                                                   cluster);*/
      os = new DeployOsAdapter(dos);
      
      bis = new BufferedInputStream(new FileInputStream(fileName));
      
      byte[] b    = new byte[BUFSZ];
      int    read;
      
      while ((read = bis.read(b)) > -1) {
        os.write(b, 0, read);
      }
      
      os.flush();
      os.close();
      closed = true;
      
      return dos.getProgressQueue();
    } finally {
      if ((os != null) && !closed) {
        try {
          os.close();
        } catch (IOException e) {
        }
      }
      
      if (bis != null) {
        try {
          bis.close();
        } catch (IOException e) {
        }
      }
    }
  }
  
  public synchronized ProgressQueue undeploy(String distName, String version,
    ClusterInfo cluster) {
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    
    return getDeployer().undeploy(ArgFactory.parse(distName), 
        ArgFactory.parse(version));
  }
  
  public synchronized void deployExecConfig(String fileName, ClusterInfo cluster) 
    throws IOException, CorusException{
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    FileInputStream fis = new FileInputStream(fileName); 
    try{
      ExecConfig conf = ExecConfig.newInstance(fis);
      getProcessor().addExecConfig(conf);
    }catch(ProcessingException e){
      e.printStackTrace();
      throw new CorusException(e);
    }
  }
  
  public synchronized void undeployExecConfig(String fileName, ClusterInfo cluster){
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    getProcessor().removeExecConfig(ArgFactory.parse(fileName));
  }

  public synchronized Results getExecConfigs(ClusterInfo cluster) 
  throws IOException, CorusException{
    refresh();
    
    Results  res = new Results();
    HostList lst = new HostList(_addr);

    lst.addAll(getProcessor().getExecConfigs());
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, Processor.class, "getExecConfigs", new Object[0],
        new Class[0], cluster);
    } else {
      res.complete();
    }
    
    return res;
  }

  
  public synchronized Results getDistributions(ClusterInfo cluster) {
    refresh();
    
    Results  res = new Results();
    HostList lst = new HostList(_addr);
    
    lst.addAll(getDeployer().getDistributions());
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, Deployer.class, "getDistributions", new Object[0],
        new Class[0], cluster);
    } else {
      res.complete();
    }
    
    return res;
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#getDistributions(String, ClusterInfo)
   */
  public synchronized Results getDistributions(String name, ClusterInfo cluster) {
    refresh();
    
    Results  res = new Results();
    HostList lst = new HostList(_addr);
    lst.addAll(getDeployer().getDistributions(ArgFactory.parse(name)));
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, Deployer.class, "getDistributions",
        new Object[] { name }, new Class[] { String.class }, cluster);
    } else {
      res.complete();
    }
    
    return res;
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#getDistributions(String, String, ClusterInfo)
   */
  public synchronized Results getDistributions(String name, String version,
    ClusterInfo cluster) {
    refresh();
    
    Results      res  = new Results();
    HostList lst = new HostList(_addr);    
    Distribution dist;
    
    try {
      dist = getDeployer().getDistribution(ArgFactory.parse(name),
          ArgFactory.parse(version));

      lst.add(dist);
      res.addResult(lst);
      
      if (cluster.isClustered() && (_otherHosts.size() > 0)) {
        applyToCluster(res, Deployer.class, "getDistributions",
          new Object[] { name, version },
          new Class[] { String.class, String.class }, cluster);
      } else {
        res.complete();
      }
    } catch (LogicException e) {
      res.complete();
      
      // noop
    }
    
    return res;
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#getProcess(String)
   */
  public synchronized Process getProcess(String vmId) throws LogicException {
    refresh();
    
    return getProcessor().getProcess(vmId);
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#getProcesses(ClusterInfo)
   */
  public synchronized Results getProcesses(ClusterInfo cluster) {
    refresh();
    
    Results  res = new Results();
    HostList lst = new HostList(_addr);
    lst.addAll(getProcessor().getProcesses());
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, Processor.class, "getProcesses", new Object[0],
        new Class[0], cluster);
    } else {
      res.complete();
    }
    
    return res;
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#getProcesses(String, ClusterInfo)
   */
  public synchronized Results getProcesses(String distName, ClusterInfo cluster) {
    refresh();
    
    Results  res = new Results();
    HostList lst = new HostList(_addr);
    lst.addAll(getProcessor().getProcesses(ArgFactory.parse(distName)));
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, Processor.class, "getProcesses",
        new Object[] { distName }, new Class[] { String.class }, cluster);
    } else {
      res.complete();
    }
    
    return res;
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#getProcesses(String, String, ClusterInfo)
   */
  public synchronized Results getProcesses(String distName, String version,
    ClusterInfo cluster) {
    refresh();
    
    Results  res = new Results();
    HostList lst = new HostList(_addr);
    lst.addAll(getProcessor().getProcesses(ArgFactory.parse(distName), 
        ArgFactory.parse(version)));
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, Processor.class, "getProcesses",
        new Object[] { distName, version },
        new Class[] { String.class, String.class }, cluster);
    } else {
      res.complete();
    }
    
    return res;
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#getProcesses(String, String, String, ClusterInfo)
   */
  public synchronized Results getProcesses(String distName, String version,
    String profile, ClusterInfo cluster) {
    refresh();
    
    Results  res = new Results();
    HostList lst = new HostList(_addr);
    lst.addAll(getProcessor().getProcesses(ArgFactory.parse(distName), 
        ArgFactory.parse(version), profile));
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, Processor.class, "getProcesses",
        new Object[] { distName, version, profile },
        new Class[] { String.class, String.class, String.class }, cluster);
    } else {
      res.complete();
    }
    
    return res;
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#getProcesses(String, String, String, String, ClusterInfo)
   */
  public synchronized Results getProcesses(String distName, String version,
    String profile, String vmName,
    ClusterInfo cluster) {
    refresh();
    
    Results  res = new Results();
    HostList lst = new HostList(_addr);
    lst.addAll(getProcessor().getProcesses(ArgFactory.parse(distName), 
        ArgFactory.parse(version), profile, ArgFactory.parse(vmName)));
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, Processor.class, "getProcesses",
        new Object[] { distName, version, profile, vmName },
        new Class[] {
          String.class, String.class, String.class, String.class
        }, cluster);
    } else {
      res.complete();
    }
    
    return res;
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#getStatusFor(String)
   */
  public synchronized ProcStatus getStatusFor(String vmId) throws LogicException {
    refresh();
    
    return getProcessor().getStatusFor(vmId);
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#getStatus(ClusterInfo)
   */
  public synchronized Results getStatus(ClusterInfo cluster) {
    refresh();
    
    Results  res = new Results();
    HostList lst = new HostList(_addr);
    lst.addAll(getProcessor().getStatus());
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, Processor.class, "getStatus", new Object[0],
        new Class[0], cluster);
    } else {
      res.complete();
    }
    
    return res;
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#getProcesses(String, ClusterInfo)
   */
  public synchronized Results getStatus(String distName, ClusterInfo cluster) {
    refresh();
    
    Results  res = new Results();
    HostList lst = new HostList(_addr);
    lst.addAll(getProcessor().getStatus(ArgFactory.parse(distName)));
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, Processor.class, "getStatus",
        new Object[] { distName }, new Class[] { String.class }, cluster);
    } else {
      res.complete();
    }
    
    return res;
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#getStatus(String, String, boolean)
   */
  public synchronized Results getStatus(String distName, String version,
    ClusterInfo cluster) {
    refresh();
    
    Results  res = new Results();
    HostList lst = new HostList(_addr);
    lst.addAll(getProcessor().getStatus(ArgFactory.parse(distName), 
        ArgFactory.parse(version)));
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, Processor.class, "getStatus",
        new Object[] { distName, version },
        new Class[] { String.class, String.class }, cluster);
    } else {
      res.complete();
    }
    
    return res;
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#getProcesses(String, String, String, ClusterInfo)
   */
  public synchronized Results getStatus(String distName, String version,
    String profile, ClusterInfo cluster) {
    refresh();
    
    Results  res = new Results();
    HostList lst = new HostList(_addr);
    lst.addAll(getProcessor().getStatus(ArgFactory.parse(distName), 
        ArgFactory.parse(version), profile));
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, Processor.class, "getStatus",
        new Object[] { distName, version, profile },
        new Class[] { String.class, String.class, String.class }, cluster);
    } else {
      res.complete();
    }
    
    return res;
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#getStatus(String, String, String, String, ClusterInfo)
   */
  public synchronized Results getStatus(String distName, String version,
    String profile, String vmName,
    ClusterInfo cluster) {
    refresh();
    
    Results  res = new Results();
    HostList lst = new HostList(_addr);
    lst.addAll(getProcessor().getStatus(ArgFactory.parse(distName), 
        ArgFactory.parse(version), profile, ArgFactory.parse(vmName)));
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, Processor.class, "getStatus",
        new Object[] { distName, version, profile, vmName },
        new Class[] {
          String.class, String.class, String.class, String.class
        }, cluster);
    } else {
      res.complete();
    }
    
    return res;
  }

  public synchronized void addTag(String tag, ClusterInfo cluster) {
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    getConfigurator().addTag(tag);
  }
  
  public synchronized void addTags(Set<String> tags, ClusterInfo cluster) {
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    getConfigurator().addTags(tags);
  }
  
  public synchronized void removeTag(Arg tag, ClusterInfo cluster) {
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    getConfigurator().removeTag(tag);
  }

  public synchronized Results getTags(ClusterInfo cluster) {
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    refresh();
    Results  res = new Results();
    HostList lst = new HostList(_addr);
    lst.addAll(getConfigurator().getTags());
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, 
          Configurator.class, 
          "getTags", 
          new Object[0],
          new Class[0], 
          cluster);
    } else {
      res.complete();
    }
    
    return res;
  }
  
  public synchronized void addProperty(PropertyScope scope, String name, String value,
      ClusterInfo cluster) {
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    getConfigurator().addProperty(scope, name, value);
  }
  
  public synchronized void removeProperty(PropertyScope scope, Arg name,
      ClusterInfo cluster) {
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    getConfigurator().removeProperty(scope, name);    
  }
  
  public synchronized Results getProperties(PropertyScope scope, ClusterInfo cluster) {
    refresh();
    Results  res = new Results();
    HostList lst = new HostList(_addr);
    lst.addAll(getConfigurator().getPropertiesAsNameValuePairs(scope));
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, 
          Configurator.class, 
          "getPropertiesAsNameValuePairs", 
          new Object[]{scope},
          new Class[]{PropertyScope.class}, 
          cluster);
    } else {
      res.complete();
    }
    
    return res;
  }
  
  public synchronized ProgressQueue exec(String distName, ClusterInfo cluster){
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    return getProcessor().exec(distName);
  }
  
  public synchronized ProgressQueue exec(String distName, String version,
    String profile, int instances,
    ClusterInfo cluster) {
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    
    return getProcessor().exec(ArgFactory.parse(distName), 
        ArgFactory.parse(version), profile, instances);
  }
  
  public synchronized ProgressQueue exec(String distName, String version,
    String profile, String vmName,
    int instances, ClusterInfo cluster) {
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    
    return getProcessor().exec(
        ArgFactory.parse(distName), 
        ArgFactory.parse(version), profile, 
        ArgFactory.parse(vmName), instances);
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#restart(ClusterInfo)
   */
  public ProgressQueue restart(ClusterInfo cluster) {
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    
    return getProcessor().resume();
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#kill(String, String, String, ClusterInfo)
   */
  public synchronized void kill(String distName, String version,
    String profile, ClusterInfo cluster) {
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    
    try {
      getProcessor().kill(ArgFactory.parse(distName), ArgFactory.parse(version), profile, false);
    } catch (CorusException e) {
      // noop
    }
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#kill(String, String, String, String, ClusterInfo)
   */
  public synchronized void kill(String distName, String version,
    String profile, String vmName, ClusterInfo cluster) {
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    
    try {
      getProcessor().kill(ArgFactory.parse(distName), 
          ArgFactory.parse(version), profile, ArgFactory.parse(vmName), false);
    } catch (CorusException e) {
      // noop
    }
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#kill(String)
   */
  public synchronized void kill(String vmId) throws LogicException {
    refresh();
    
    try {
      getProcessor().kill(vmId, false);
    } catch (CorusException e) {
      if (e instanceof LogicException) {
        throw (LogicException) e;
      }
    }
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#suspend(String, String, String, ClusterInfo)
   */
  public void suspend(String distName, String version, String profile,
    ClusterInfo cluster) {
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    
    try {
      getProcessor().kill(ArgFactory.parse(distName), ArgFactory.parse(version), profile, true);
    } catch (CorusException e) {
      // noop
    }
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#suspend(String, String, String, String, ClusterInfo)
   */
  public void suspend(String distName, String version, String profile,
    String vmName, ClusterInfo cluster) {
    refresh();
    ClusterInterceptor.clusterCurrentThread(cluster);
    
    try {
      getProcessor().kill(ArgFactory.parse(distName), ArgFactory.parse(version), 
          profile, ArgFactory.parse(vmName), true);
    } catch (CorusException e) {
      // noop
    }
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#suspend(String)
   */
  public void suspend(String vmId) {
    refresh();
    
    try {
      getProcessor().kill(vmId, true);
    } catch (CorusException e) {
      // noop
    }
  }
  
  public void restart(String pid) throws LogicException {
    refresh();
    try{
      getProcessor().restartByAdmin(pid);
    }catch(CorusException e){
      if(e instanceof LogicException){
        throw (LogicException)e;
      }
    }
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#getCronJobs(ClusterInfo)
   */
  public synchronized Results getCronJobs(ClusterInfo cluster) {
    refresh();
    
    Results  res = new Results();
    HostList lst = new HostList(_addr);
    lst.addAll(getCron().listCronJobs());
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, CronModule.class, "listCronJobs", new Object[0],
        new Class[0], cluster);
    } else {
      res.complete();
    }
    
    return res;
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#addCronJon(CronJobInfo)
   */
  public synchronized void addCronJon(CronJobInfo info)
  throws InvalidTimeException {
    refresh();
    
    try {
      getCron().addCronJob(info);
    } catch (CorusException e) {
      throw new CorusRuntimeException(e);
    }
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#removeCronJob(String)
   */
  public synchronized void removeCronJob(String id) {
    refresh();
    getCron().removeCronJob(id);
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#getPortRanges(ClusterInfo)
   */
  public synchronized Results getPortRanges(ClusterInfo cluster) {
    refresh();
    
    Results  res = new Results();
    HostList lst = new HostList(_addr);
    lst.addAll(getPorts().getPortRanges());
    res.addResult(lst);
    
    if (cluster.isClustered() && (_otherHosts.size() > 0)) {
      applyToCluster(res, PortManager.class, "getPortRanges", new Object[0],
        new Class[0], cluster);
    } else {
      res.complete();
    }
    
    return res;
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#addPortRange(String, int, int, ClusterInfo)
   */
  public synchronized void addPortRange(String name, int min, int max, ClusterInfo cluster)
    throws PortRangeConflictException, PortRangeInvalidException {
    refresh();

    ClusterInterceptor.clusterCurrentThread(cluster);
    
    getPorts().addPortRange(name, min, max);
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#removePortRange(String, boolean, ClusterInfo)
   */
  public synchronized void removePortRange(String name, boolean force, ClusterInfo cluster) 
    throws PortActiveException{
    refresh();
    
    ClusterInterceptor.clusterCurrentThread(cluster);
    
    getPorts().removePortRange(name, force);
  }
  
  /**
   * @see org.sapia.corus.admin.CorusFacade#releasePortRange(String, ClusterInfo)
   */
  public synchronized void releasePortRange(String name, ClusterInfo cluster) {
    refresh();
    
    ClusterInterceptor.clusterCurrentThread(cluster);
    
    getPorts().releasePortRange(name);
  }  
  
  protected void refresh() {
    if ((System.currentTimeMillis() - _lastReconnect) > RECONNECT_INTERVAL) {
      try {
        reconnect();
        _lastReconnect = System.currentTimeMillis();
      } catch (CorusException e) {
        throw new CorusRuntimeException(e);
      }
    }
  }  
  
  protected Object lookup(String role) {
    Object toReturn = _components.get(role);
    
    if (toReturn == null) {
      try {
        toReturn = _dyn.lookup(role);
      } catch (CorusException e) {
        throw new CorusRuntimeException(e);
      }
      
      _components.put(role, toReturn);
    }
    
    return toReturn;
  }
  
  protected Deployer getDeployer() {
    return (Deployer) lookup(Deployer.ROLE);
  }
  
  protected Processor getProcessor() {
    return (Processor) lookup(Processor.ROLE);
  }
  
  protected CronModule getCron() {
    return (CronModule) lookup(CronModule.ROLE);
  }
  
  protected PortManager getPorts(){
    return (PortManager) lookup(PortManager.ROLE);
  }
  
  protected Configurator getConfigurator(){
    return (Configurator) lookup(Configurator.ROLE);
  }
  
  protected void applyToCluster(final Results res, final Class moduleInterface,
    final String methodName, final Object[] params,
    final Class[] sig, final ClusterInfo cluster) {
    Thread t = new Thread(new Runnable() {
      public void run() {
        Set otherHosts;
        
        if(cluster.getTargets() != null){
          otherHosts = new HashSet(_otherHosts);
          otherHosts.retainAll(cluster.getTargets());
        } else{
          otherHosts = _otherHosts;
        }
        Iterator   itr    = _otherHosts.iterator();
        TCPAddress addr;
        Corus     dyn;
        Object     module;
        Object     result;
        HostList   lst;
        //HostItem   item;
        
        while (itr.hasNext()) {
          addr = (TCPAddress) itr.next();
          dyn  = (Corus) _cachedStubs.get(addr);
          
          if (dyn == null) {
            try {
              dyn = (Corus) Hub.connect(addr.getHost(), addr.getPort());
              _cachedStubs.put(addr, dyn);
            } catch (java.rmi.RemoteException e) {
              continue;
            }
          }
          
          try {
            module = dyn.lookup(moduleInterface.getName());
            
            Method m = module.getClass().getMethod(methodName, sig);
            result = m.invoke(module, params);
            
            if (result instanceof Collection) {
              lst = new HostList(addr);
              lst.addAll((Collection) result);
              res.addResult(lst);
            } else {
              lst = new HostList(addr);
              lst.add(result);
              res.addResult(lst);              
              /*
              item = new HostItem(addr, result);
              res.addResult(item);*/
            }
            
            continue;
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        
        res.complete();
      }
    });
    t.setDaemon(true);
    t.start();
  }
  
  static Object[] split(String fileName){
    if(fileName.startsWith(ArgFactory.PATTERN)){
      return new Object[]{new File(System.getProperty("user.dir")), fileName};
    }
    else{
      String baseDirName = fileName.substring(0, fileName.indexOf(ArgFactory.PATTERN));
      int idx;
      if((idx = baseDirName.lastIndexOf("/")) > 0 || (idx = baseDirName.lastIndexOf("\\")) > 0){
        baseDirName = fileName.substring(0, idx);
        idx = idx+1;
      }
      else{
        idx = fileName.indexOf(ArgFactory.PATTERN);
      }
      File baseDir = new File(baseDirName);
     
      if(baseDir.exists()){
        String pattern = fileName.substring(idx);
        return new Object[]{baseDir, pattern};
      }
      else{
        String pattern = fileName.substring(idx);
        return new Object[]{new File(System.getProperty("user.dir")), pattern};
      }
      
    }
  }
}
