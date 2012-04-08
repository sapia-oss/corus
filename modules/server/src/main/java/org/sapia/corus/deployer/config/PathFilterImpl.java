package org.sapia.corus.deployer.config;

import org.apache.tools.ant.DirectoryScanner;
import org.sapia.corus.client.common.PathFilter;

/**
 * Implements the {@link PathFilter} interface over a {@link DirectoryScanner}.
 * 
 * @author yduchesne
 *
 */
public class PathFilterImpl implements PathFilter{
  
  private DirectoryScanner scanner;
  
  public PathFilterImpl(String basedir) {
    scanner = new DirectoryScanner();
    scanner.setBasedir(basedir);
  }

  @Override
  public String getBaseDir() {
    return scanner.getBasedir().getAbsolutePath();
  }
  
  public void setExcludes(String[] excludes) {
    scanner.setExcludes(excludes);
  }
  
  public void setIncludes(String[] includes) {
    scanner.setIncludes(includes);
  }
  
  @Override
  public String[] filter() {
    scanner.scan();
    return scanner.getIncludedFiles();
  }
}
