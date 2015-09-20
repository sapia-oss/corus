package org.sapia.corus.deployer.artifact;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.annotations.PriorityLevel;
import org.sapia.corus.client.services.artifact.ArtifactManager;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.util.Streams;

/**
 * Internally manages artifacts, in part depending on {@link ArtifactProvider}s.
 * 
 * @author yduchesne
 *
 */
@Bind(moduleInterface = { ArtifactManager.class, InternalArtifactManager.class })
@Remote(interfaces = { ArtifactManager.class })
public class ArtifactManagerImpl extends ModuleHelper implements InternalArtifactManager {

  private List<ArtifactProvider> providers;

  // --------------------------------------------------------------------------
  // Module interface
  
  @Override
  public String getRoleName() {
    return ArtifactManager.ROLE;
  }
  
  // --------------------------------------------------------------------------
  // Lifecycle
  
  @Override
  public void init() throws Exception {
    providers = new ArrayList<ArtifactProvider>(appContext.getBeansOfType(ArtifactProvider.class).values());
    PriorityLevel.sort(providers);
    for (ArtifactProvider p : providers) {
      logger().info("Got artifact provider: " + p);
    }
  }
  
  @Override
  public void dispose() throws Exception {
  }
  
  // --------------------------------------------------------------------------
  // InternalArtifactManager interface
  
  @Override
  public void downloadArtifact(URI uri, File targetFile) throws IOException {
    ArtifactProvider p       = selectProvider(uri);
    log.info(String.format("Downloading artifact %s using provider %s", uri, p));
    log.info(String.format("Will download artifact to local file: %s", targetFile));
    FileOutputStream     fos = new FileOutputStream(targetFile);
    BufferedOutputStream bos = new BufferedOutputStream(fos);
    InputStream          as  = p.getArtifactStream(uri);
    try {
      IOUtils.copy(as, bos);
    } finally {
      Streams.closeSilently(bos);
      Streams.closeSilently(as);
    }
  }

  // --------------------------------------------------------------------------
  // Visible for testing

  void setProviders(List<ArtifactProvider> providers) {
    this.providers = providers;
  }
  
  ArtifactProvider selectProvider(URI uri) {
    for (ArtifactProvider p : providers) {
      if (p.accepts(uri)) {
        return p;
      }
    }
    throw new IllegalStateException("No artifact provider configured for URI: " + uri);
  }
}
