package org.sapia.corus.deployer;

import org.sapia.corus.util.ProgressQueue;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * An output stream that is used for deployment.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DeployOutputStreamImpl extends FileOutputStream
  implements DeployOutputStream {
  private DeployerImpl  _deployer;
  private boolean _closed;
  private String        _fName;
  private ProgressQueue _queue;

  /**
   * Constructor for DeployOutputStream.
   * @param fileName
   * @throws FileNotFoundException
   */
  public DeployOutputStreamImpl(String absolutePath, String fileName,
                         DeployerImpl deployer) throws FileNotFoundException {
    super(absolutePath);
    _deployer = deployer;
    _fName    = fileName;
  }

  public void close() throws IOException {
  	if(_closed) return;
    try {
      super.flush();

      if (_deployer != null) {
        _queue = _deployer.unlockDeployFile(_fName);
      }
    } finally {
      super.close();
    }
    _closed = true;
  }
  
  /**
   * @see org.sapia.corus.deployer.DeployOutputStream#getProgressQueue()
   */
  public ProgressQueue getProgressQueue() {
    if (_queue == null) {
      throw new IllegalStateException("progress queue not available");
    }

    return _queue;
  }
}
