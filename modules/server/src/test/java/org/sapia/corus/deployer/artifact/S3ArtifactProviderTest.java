package org.sapia.corus.deployer.artifact;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;

public class S3ArtifactProviderTest {
  
  private S3ArtifactProvider provider;
  
  @Before
  public void setUp(){
    provider = new S3ArtifactProvider() {
      @Override
      protected InputStream getObjectStream(AmazonS3Client s3Client,
          GetObjectRequest req) {
        return new ByteArrayInputStream("test".getBytes());
      }
    };
  }

  @Test
  public void testAccepts() {
    URI uri = URI.create("https://s3.amazonaws.com/testbucket/testartifact");
    assertTrue(provider.accepts(uri));
  }
  
  @Test
  public void testAccepts_false() {
    URI uri = URI.create("https://foo.com/testbucket/testartifact");
    assertFalse(provider.accepts(uri));
  }

  @Test
  public void testGetArtifactStream() throws IOException {
    URI uri = URI.create("https://foo.com/testbucket/testartifact");
    InputStream stream = provider.getArtifactStream(uri);
    stream.close();
  }

}
