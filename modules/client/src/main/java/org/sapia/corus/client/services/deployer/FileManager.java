package org.sapia.corus.client.services.deployer;

import java.rmi.Remote;
import java.util.List;

import org.sapia.corus.client.Module;
import org.sapia.corus.client.common.ProgressQueue;

/**
 * Provides API for managing files.
 * 
 * @author yduchesne
 * 
 */
public interface FileManager extends Remote, Module {

  String ROLE = FileManager.class.getName();

  /**
   * @return the {@link FileInfo}s corresonding to the files that are kept on
   *         Corus node.
   */
  public List<FileInfo> getFiles();

  /**
   * @param criteria
   *          the {@link FileCriteria} instance to use for selection.
   * @return the {@link FileInfo} instances corresponding to the files whose
   *         name matched the criteria.
   */
  public List<FileInfo> getFiles(FileCriteria criteria);

  /**
   * @param criteria
   *          the {@link FileCriteria} instance to use for selection.
   * @return a {@link ProgressQueue}.
   */
  public ProgressQueue deleteFiles(FileCriteria criteria);

}
