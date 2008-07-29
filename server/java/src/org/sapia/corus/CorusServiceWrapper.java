package org.sapia.corus;

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

/**
 * This class encapsulate the CorusServer class to provide the functionnality
 * of creating a NT service or a Unix daemon with it. To do that it uses the java service
 * wrapper tool of TanukiSoftware (http://wrapper.tanukisoftware.org/doc/english/index.html).
 *
 * @author Jean-Cedric Desrochers
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>.
 *        All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CorusServiceWrapper implements WrapperListener, Runnable {
  
  private String[] _theArguments;
  private boolean _isStarted;
  
  /**
   * Main method to start the corus service wrapper.
   *
   * @param args The arguments to pass to the CorusServer.
   */
  public static void main(String[] args) {
    System.out.println("Initializing...");
    
    // Start the application.  If the JVM was launched from the native
    //  Wrapper then the application will wait for the native Wrapper to
    //  call the application's start method.  Otherwise the start method
    //  will be called immediately.
    WrapperManager.start(new CorusServiceWrapper(), args);
  }
  
  /**
   * Creates a new CorusServiceWrapper instance.
   */
  public CorusServiceWrapper() {
    _isStarted = false;
  }
  
  
  /**
   * Implements the WrapperListener interface and starts the CorusServer.
   *
   * @see org.tanukisoftware.wrapper.WrapperListener#start(java.lang.String[])
   */
  public Integer start(String[] args) {
    _theArguments = args;
    Thread aThread = new Thread(this, "CorusService");
    aThread.start();
    _isStarted = true;
    
    return null;
  }
  
  /**
   * @see org.tanukisoftware.wrapper.WrapperListener#stop(int)
   */
  public int stop(int anExitCode) {
    // The Corus shutdown hook takes care of the shutdown procedure
    return anExitCode;
  }
  
  /**
   * @see org.tanukisoftware.wrapper.WrapperListener#controlEvent(int)
   */
  public void controlEvent(int anEvent) {
    if ((anEvent == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT) &&
      WrapperManager.isLaunchedAsService()) {
      System.out.println("Ignoring logoff event");
      // Ignore
    } else {
      WrapperManager.stop(0);
    }
  }
  
  /**
   * Implements the Runnable interface and it contains the logic to start a new
   * CorusServer with the passed in parameters.
   *
   * @see java.lang.Runnable#run()
   */
  public void run() {
    try {
      CorusServer.main(_theArguments);
    } catch (RuntimeException re) {
      System.err.println("System error running the activator server from the service wrapper");
      re.printStackTrace();
      _isStarted = false;
    }
  }
}
