package org.sapia.corus.deployer.artifact;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Abstracts the underlying artifact repository.
 * 
 * @author yduchesne
 *
 */
public interface ArtifactProvider {

  /**
   * @param url the URL of an artifact.
   * @return <code>true</code> if this instance accepts the given URL.
   */
  public boolean accepts(URI uri);
  
  /**
   * @param url the URL of the artifact to download.
   * @return the {@link InputStream} of the artifact.
   * @throws IOException if an I/O error occurs.
   */
  public InputStream getArtifactStream(URI uri) throws IOException;
  
}
