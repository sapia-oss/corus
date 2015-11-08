package org.sapia.corus.deployer.artifact;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.sapia.corus.client.services.artifact.ArtifactManager;

/**
 * Manages the download of artifacts.
 * 
 * @author yduchesne
 *
 */
public interface InternalArtifactManager extends ArtifactManager{

  /**
   * @param url the {@link URI} of the artifact to download.
   * @param targetFile the file to which to write the downloaded bytes.
   * @throws IOException if an I/O error occurs while performing this operation.
   */
  public void downloadArtifact(URI uri, File targetFile) throws IOException;
}
