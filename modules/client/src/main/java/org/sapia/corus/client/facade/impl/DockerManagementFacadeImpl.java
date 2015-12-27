package org.sapia.corus.client.facade.impl;

import java.io.InputStream;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.encryption.Encryption;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.DockerManagementFacade;
import org.sapia.corus.client.services.audit.AuditInfo;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CurrentAuditInfo;
import org.sapia.corus.client.services.docker.DockerClientException;
import org.sapia.corus.client.services.docker.DockerContainer;
import org.sapia.corus.client.services.docker.DockerHandlerClient;
import org.sapia.corus.client.services.docker.DockerImage;
import org.sapia.corus.client.services.docker.DockerManager;

/**
 * Implements the {@link DockerManagementFacade} interface.
 * 
 * @author yduchesne
 *
 */
public class DockerManagementFacadeImpl extends FacadeHelper<DockerManager> implements DockerManagementFacade {

  @Override
  public Results<List<DockerContainer>> getContainers(ArgMatcher nameMatcher, ClusterInfo cluster)
      throws DockerClientException {
    Results<List<DockerContainer>> results = new Results<List<DockerContainer>>();
    proxy.getContainers(nameMatcher);
    invoker.invokeLenient(results, cluster);
    return results;
  }

  public DockerManagementFacadeImpl(CorusConnectionContext context) {
    super(context, DockerManager.class);
  }

  @Override
  public synchronized Results<List<DockerImage>> getImages(ArgMatcher tagMatcher, ClusterInfo cluster) throws DockerClientException {
    Results<List<DockerImage>> results = new Results<List<DockerImage>>();
    proxy.getImages(tagMatcher);
    invoker.invokeLenient(results, cluster);
    return results;
  }
  
  @Override
  public synchronized ProgressQueue removeImages(ArgMatcher tagMatcher, ClusterInfo cluster) {
    proxy.removeImages(tagMatcher);
    return invoker.invokeLenient(ProgressQueue.class, cluster);
  }
  
  @Override
  public synchronized ProgressQueue pullImage(String imageName, ClusterInfo cluster) {
    proxy.pullImage(imageName);
    return invoker.invokeLenient(ProgressQueue.class, cluster);
  }
   
  @Override
  public synchronized InputStream getImagePayload(String imageName) throws DockerClientException {
    CorusHost           target = getContext().getServerHost();
    DockerHandlerClient client = new DockerHandlerClient(getContext().getServerHost().getEndpoint().getServerAddress());
    AuditInfo           auditInfo;
    if (CurrentAuditInfo.isSet()) {
      auditInfo = CurrentAuditInfo.get().get().getAuditInfo();
    } else {
      auditInfo = AuditInfo.forCurrentUser();
    }
    AuditInfo encrypted = auditInfo.encryptWith(
      Encryption.getDefaultEncryptionContext(target.getPublicKey())
    );
    return client.getImage(imageName, encrypted);
  }
}
