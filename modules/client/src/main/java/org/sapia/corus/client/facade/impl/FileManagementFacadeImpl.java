package org.sapia.corus.client.facade.impl;

import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.FileManagementFacade;
import org.sapia.corus.client.services.deployer.FileCriteria;
import org.sapia.corus.client.services.deployer.FileInfo;
import org.sapia.corus.client.services.deployer.FileManager;

/**
 * Implements the {@link FileManagementFacade} interface.
 * 
 * @author yduchesne
 * 
 */
public class FileManagementFacadeImpl extends FacadeHelper<FileManager> implements FileManagementFacade {

  public FileManagementFacadeImpl(CorusConnectionContext context) {
    super(context, FileManager.class);
  }

  @Override
  public synchronized ProgressQueue deleteFiles(FileCriteria criteria, ClusterInfo cluster) {
    proxy.deleteFiles(criteria);
    return invoker.invokeLenient(ProgressQueue.class, cluster);
  }

  @Override
  public synchronized Results<List<FileInfo>> getFiles(FileCriteria criteria, ClusterInfo cluster) {
    Results<List<FileInfo>> results = new Results<List<FileInfo>>();
    proxy.getFiles(criteria);
    invoker.invokeLenient(results, cluster);
    return results;
  }

}
