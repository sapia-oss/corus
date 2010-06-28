package org.sapia.corus.client.common;
/**
 * An instance of this interface is used to filter a certain directory recursively 
 * and return a list of files corresponding to the filtering criteria.
 * 
 * @author yduchesne
 *
 */
public interface PathFilter {

  /**
   * @return the name of the directory form which filtering should be done.
   */
  public String getBaseDir();

  /**
   * @param includes the Ant-like patterns indicating which files/directories to include.
   */
  public void setIncludes(String[] includes);
  
  /**
   * @param excludes the Ant-like patterns indicating which files/directories to exclude.
   */
  public void setExcludes(String[] excludes);
  
  /**
   * @return the names of the files that are finally included, after scanning.
   */
  public String[] filter();
  
}
