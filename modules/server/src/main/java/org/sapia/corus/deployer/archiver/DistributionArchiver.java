package org.sapia.corus.deployer.archiver;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.deployer.dist.Distribution;

/**
 * Specifies the behavior for archiving/unarchiving. 
 * 
 * @author yduchesne
 *
 */
public interface DistributionArchiver {
  
  public static final String ROLE = DistributionArchiver.class.getName();
  
  /**
   * Holds data about an archived distribution.
   * 
   * @author yduchesne
   *
   */
  public interface DistributionArchive {
    
    /**
     * @return the distribution .zip file of the distribution to which this instance corresponds.
     */
    public File getDistributionZip();
    
  }
  
  // --------------------------------------------------------------------------

  /**
   * @param revId the {@link RevId} corresponding to the revision under which to archive.
   * @param toArchive a {@link List} of {@link Distribution}s to archive.
   * @throws IOException if an I/O error occurs.
   */
  public void archive(RevId revId, List<Distribution> toArchive) throws IOException;

  /**
   * @param revId the {@link RevId} corresponding to the revision unarchive.
   * @return the {@link List} of {@link DistributionArchive}s that were unarchived.
   * @throws IOException if an I/O error occurs.
   */
  public List<DistributionArchive> unarchive(RevId revId) throws IOException;
  
  /**
   * Deletes the distributions archived under the given revision.
   * 
   * @param revId a revision ID.
   * @throws IOException if an I/O error occurs.
   */
  public void clear(RevId revId) throws IOException;
  
  /**
   * Clears the state of all distributions.
   * 
   * @throws IOException if an I/O error occurs.
   */
  public void clear() throws IOException;
}
