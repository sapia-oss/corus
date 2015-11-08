package org.sapia.corus.client.common;

import java.io.File;

/**
 * Abstracts file behavior, convenient for unit testing (the {@link File} class can hardly be mocked since
 * it is a concrete class with no public no-arg constructor).
 * 
 * @author yduchesne
 *
 */
public interface FileFacade {

  /**
   * @see File#lastModified()
   */
  public long lastModified();
  
  /**
   * @see {@link File#getName()}
   */
  public String getName();
  
  /**
   * @see File#getAbsolutePath()
   */
  public String getAbsolutePath();
  
  /**
   * @see File#isDirectory()
   */
  public boolean isDirectory();
 
  /**
   * @see File#isFile()
   */
  public boolean isFile();
  
  // --------------------------------------------------------------------------
  // Default impl
  
  public class DefaultFileFacade implements FileFacade {
    
    private File delegate;
    
    public DefaultFileFacade(File delegate) {
      this.delegate = delegate;
    }
    
    @Override
    public String getName() {
      return delegate.getName();
    }
    
    @Override
    public String getAbsolutePath() {
      return delegate.getAbsolutePath();
    }
    
    @Override
    public boolean isDirectory() {
      return delegate.isDirectory();
    }
    
    @Override
    public boolean isFile() {
      return delegate.isFile();
    }
    
    @Override
    public long lastModified() {
      return delegate.lastModified();
    }
    
    public static FileFacade of(File delegate) {
      return new DefaultFileFacade(delegate);
    }
    
  }
}
