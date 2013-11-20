package org.sapia.corus.client.facade;

import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.services.deployer.FileCriteria;
import org.sapia.corus.client.services.deployer.FileInfo;

/**
 * Provides the management interface pertaining to the file uploads managed by
 * Corus.
 * 
 * @author yduchesne
 * 
 */
public interface FileManagementFacade {

  /**
   * @param criteria
   *          the {@link FileCriteria} instance to use for selection.
   * @param cluster
   *          a {@link ClusterInfo} indicating if this operation should be
   *          clustered.
   * @return the {@link FileInfo} instances corresponding to the files whose
   *         name matched the criteria.
   */
  public Results<List<FileInfo>> getFiles(FileCriteria criteria, ClusterInfo cluster);

  /**
   * @param criteria
   *          the {@link FileCriteria} instance to use for selection.
   * @param cluster
   *          a {@link ClusterInfo} indicating if this operation should be
   *          clustered.
   * @return a {@link ProgressQueue}.
   */
  public ProgressQueue deleteFiles(FileCriteria criteria, ClusterInfo cluster);

}
