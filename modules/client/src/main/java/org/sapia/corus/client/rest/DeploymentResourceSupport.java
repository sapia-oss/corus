package org.sapia.corus.client.rest;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public abstract class DeploymentResourceSupport extends ResourceSupport {

  private static final int BUFSZ = 8192;
  
  // --------------------------------------------------------------------------
  // Restricted methods
  
  protected File transfer(RequestContext ctx, String extension) throws IOException {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    if (!tmpDir.exists()) {
      throw new IllegalStateException("Directory corresponding to java.io.tmpdir does not exist");
    }
    File                tmpFile = new File(tmpDir, UUID.randomUUID() + extension);
    FileOutputStream    fos     = null;
    BufferedInputStream bis     = null;
    try {
      byte[] buf  = new byte[BUFSZ];
      int    read = 0;
      fos = new FileOutputStream(tmpFile);
      bis = new BufferedInputStream(ctx.getRequest().getContent(), BUFSZ);
      while ((read = bis.read(buf)) > -1) {
        fos.write(buf, 0, read);
      }
      return tmpFile;
    } catch (IOException e) {
      tmpFile.delete();
      throw e;
    } finally {
      if (fos != null) {
        try {
          fos.flush();
          fos.close();
        } catch (Exception e2) {
          // NOOP
        }
      }
      if (bis != null) {
        try {
          bis.close();
        } catch (Exception e2) {
          // NOOP
        }
      }
    }
  }
}
