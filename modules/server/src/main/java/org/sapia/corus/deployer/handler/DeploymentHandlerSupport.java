package org.sapia.corus.deployer.handler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.services.deployer.ChecksumPreference;
import org.sapia.corus.deployer.DeploymentHandler;

/**
 * Implements common {@link DeploymentHandler} functionality.
 * 
 * @author yduchesne
 *
 */
abstract class DeploymentHandlerSupport implements DeploymentHandler {
  
  private static final int BUFSZ = 8000;
  
  /**
   * 
   * @param progress the {@link ProgressQueue} to feed the checksum outcome to.
   * @param cs the {@link ChecksumPreference} to check against.
   * @param toCheck the {@link File} to check.
   * @return <code>true</code> if the checksum was successful, <code>false</code> otherwise.
   */
  protected boolean computeChecksum(ProgressQueue progress, ChecksumPreference cs, File toCheck) {
    progress.info("Validating checksum for file: " + toCheck.getName());
    switch (cs.getAlgo()) {
    case MD5:
      try (InputStream is = new BufferedInputStream(new FileInputStream(toCheck), BUFSZ)) {
        String md5 = DigestUtils.md5Hex(is);
        if (!md5.equalsIgnoreCase(cs.getClientChecksum().get())) {
          progress.error(String.format("Checksum validation failed on file %s. Received checksum %s, computed: %s", 
              toCheck.getName(), cs.getClientChecksum().get(), md5));
          progress.close();
          return false;
        } else {
          progress.info("Checksum validation was successful");
        }
      } catch (IOException e) {
        progress.error(new IllegalStateException("I/O error occurred attempting to compute checksum on file: " + toCheck.getName(), e));
        progress.close();
        return false;
      }
      break;
    default:
      progress.error("Checksum algorithm not handled: " + cs.getAlgo());
      progress.close();
      return false;
    }
    return true;
  }
}
