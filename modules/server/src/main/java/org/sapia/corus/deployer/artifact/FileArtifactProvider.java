package org.sapia.corus.deployer.artifact;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Implements the {@link ArtifactProvider} interface for URIs with the <code>file</code> scheme.
 * 
 * @author yduchesne
 */
public class FileArtifactProvider implements ArtifactProvider {

  private static final String FILE_SCHEME = "file";
  
  @Override
  public boolean accepts(URI uri) {
    return uri.getScheme().startsWith(FILE_SCHEME);
  }
  
  @Override
  public InputStream getArtifactStream(URI uri) throws IOException {
    return new FileInputStream(new File(uri.getPath()));
  }
}
