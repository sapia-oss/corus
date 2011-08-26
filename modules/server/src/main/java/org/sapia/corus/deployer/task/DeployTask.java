package org.sapia.corus.deployer.task;

import java.io.File;
import java.io.IOException;

import org.sapia.corus.client.exceptions.deployer.DeploymentException;
import org.sapia.corus.client.exceptions.deployer.DuplicateDistributionException;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.event.DeploymentEvent;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.deployer.DeployerThrottleKeys;
import org.sapia.corus.deployer.DistributionDatabase;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.ThrottleKey;
import org.sapia.corus.taskmanager.core.Throttleable;


/**
 * This task handles the extraction of deployment jars from the temporary
 * file (where they have been uploaded) to the deployment directory.  This
 * task ensures that a distribution will not overwrite an existing one,
 * and cleans up the temporary jar. Distributions are stored under the
 * deployment directory, according to the following pattern:
 * <p>
 * <code>distribution_name/version</code>
 * <p>
 * Each distribution directory has two additional directories:
 * <code>common</code> (where the jar is actually extracted) and,
 * eventually, <code>processes</code>, where each process instance has a specific
 * directory that it owns exclusively.
 *
 *
 * @author Yanick Duchesne
 */
public class DeployTask extends Task<Void, String> implements Throttleable{
  
  @Override
  public ThrottleKey getThrottleKey() {
    return DeployerThrottleKeys.DEPLOY_DISTRIBUTION;
  }

  @Override
  public Void execute(TaskExecutionContext ctx, String fileName) throws Throwable {
    
    DistributionDatabase dists    = ctx.getServerContext().lookup(DistributionDatabase.class);
    Deployer             deployer = ctx.getServerContext().getServices().getDeployer();
    FileSystemModule     fs       = ctx.getServerContext().getServices().getFileSystem();
    File                 src      = new File(deployer.getConfiguration().getTempDir() + File.separator + fileName);               
    
    try {
    	
      ctx.info(String.format("Deploying: %s", fileName));

      // extracting corus.xml from archive and checking if already exists...
      Distribution dist    = Distribution.newInstance(src, fs);
      String       baseDir = deployer.getConfiguration().getDeployDir() + File.separator + dist.getName() +
                             File.separator + dist.getVersion();
      dist.setBaseDir(baseDir);

      synchronized (dists) {
        File commonDir  = new File(baseDir + File.separator + "common");
        File processDir = new File(baseDir + File.separator + "processes");

        DistributionCriteria criteria = DistributionCriteria.builder()
          .name(dist.getName())
          .version(dist.getVersion())
          .build();
        if (dists.containsDistribution(criteria)) {
          ctx.error(new DuplicateDistributionException("Distribution already exists for: " +
                                         dist.getName() + " version: " +
                                         dist.getVersion()));

          return null;
        }

        if (fs.exists(commonDir)) {
          ctx.error(new DuplicateDistributionException("Distribution already exists for: " +
                                         dist.getName() + " version: " +
                                         dist.getVersion()));

          return null;
        }

        // making distribution directories...
        try{
          fs.createDirectory(commonDir);
        }catch(IOException e){
          ctx.error(String.format("Could not make directory: %s", commonDir.getAbsolutePath()));
        }
        try{
          fs.createDirectory(processDir);
        }catch(IOException e){
          ctx.error(String.format("Could not make directory: %s", processDir.getAbsolutePath()));
        }        

        try {
          fs.unzip(src, commonDir);
          dists.addDistribution(dist);
          ctx.info("Distribution added to Corus");
          ctx.getServerContext().getServices().getEventDispatcher().dispatch(new DeploymentEvent(dist));
        } catch (DuplicateDistributionException e) {
          ctx.error("Distribution already exists", e);
        } catch(IOException e){
          ctx.error("Could not unzip distribution", e);
        }
      }
    } catch (DeploymentException e) {
      ctx.error(e);
    } finally {
      fs.deleteFile(src);
    }
    return null;
  }
}
