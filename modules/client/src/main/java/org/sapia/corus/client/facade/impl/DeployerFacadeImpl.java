package org.sapia.corus.client.facade.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.exceptions.deployer.ConcurrentDeploymentException;
import org.sapia.corus.client.exceptions.deployer.DuplicateDistributionException;
import org.sapia.corus.client.exceptions.deployer.RunningProcessesException;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.DeployerFacade;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.transport.ClientDeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeployOsAdapter;
import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeploymentClientFactory;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;

public class DeployerFacadeImpl extends FacadeHelper<Deployer> implements DeployerFacade{
  
  static final int             BUFSZ = 2048;
  
  public DeployerFacadeImpl(CorusConnectionContext context){
    super(context, Deployer.class);
  }
  
  @Override
  public synchronized ProgressQueue deploy(final String fileName, final ClusterInfo cluster)
  throws IOException,
    ConcurrentDeploymentException,
    DuplicateDistributionException,
    Exception{
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
    DuplicateDistributionException,
    Exception{

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
      DeployOutputStream dos  = new ClientDeployOutputStream(meta, DeploymentClientFactory.newDeploymentClientFor(context.getAddress()));
      
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

  @Override
  public synchronized ProgressQueue undeploy(String distName, String version,
    ClusterInfo cluster) throws RunningProcessesException{
    proxy.undeploy(ArgFactory.parse(distName), ArgFactory.parse(version));
    try{
      return invoker.invoke(ProgressQueue.class, cluster);
    }catch(RunningProcessesException e){
      throw (RunningProcessesException)e;
    }catch(RuntimeException e){
      throw e;
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public synchronized Results<List<Distribution>> getDistributions(ClusterInfo cluster) {
    Results<List<Distribution>>  results = new Results<List<Distribution>>();
    proxy.getDistributions();
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public synchronized Results<List<Distribution>> getDistributions(String name, ClusterInfo cluster) {
    Results<List<Distribution>>  results = new Results<List<Distribution>>();
    proxy.getDistributions(ArgFactory.parse(name));
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public synchronized Results<List<Distribution>> getDistributions(String name, String version, ClusterInfo cluster) {
    Results<List<Distribution>>  results = new Results<List<Distribution>>();
    proxy.getDistributions(ArgFactory.parse(name), ArgFactory.parse(version));
    invoker.invokeLenient(results, cluster);
    return results;    
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
