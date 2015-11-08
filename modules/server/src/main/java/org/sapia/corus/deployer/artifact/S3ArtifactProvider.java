package org.sapia.corus.deployer.artifact;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.lang.StringUtils;
import org.sapia.corus.client.annotations.Priority;
import org.sapia.corus.client.annotations.PriorityLevel;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

/**
 * Implements the {@link ArtifactProvider} interface over S3.
 * 
 * @author yduchesne
 *
 */
@Priority(PriorityLevel.EXTREME)
public class S3ArtifactProvider implements ArtifactProvider {
 
  private static final String S3_PREFIX = "s3";
  
  @Override
  public boolean accepts(URI uri) {
    return uri.getHost() != null && uri.getHost().startsWith(S3_PREFIX);
  }
  
  @Override
  public InputStream getArtifactStream(URI uri) throws IOException {
    AmazonS3Client s3Client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain());
    s3Client.setEndpoint("https://" + uri.getHost());
    
    String[] path = StringUtils.split(uri.getPath(), "/");
    if (path.length != 2) {
      throw new IllegalArgumentException("Invalid artifact URI, expected: https://<s3-endpoint>/<bucket>/<key>. Got: " + uri);
    }
    String bucket = path[0];
    String key    = path[1];
    GetObjectRequest req = new GetObjectRequest(bucket, key);
    return getObjectStream(s3Client, req);
  }
  
  /**
   * Fetches the {@link S3Object} corresponding to the given request, using the provided client.
   * 
   * @param s3Client the {@link AmazonS3Client} to use.
   * @param req the {@link GetObjectRequest} to process.
   * @return the {@link InputStream} obtained from processing the request.
   */
  protected InputStream getObjectStream(AmazonS3Client s3Client, GetObjectRequest req) {
    return s3Client.getObject(req).getObjectContent();
  }
}
