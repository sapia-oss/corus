package org.sapia.corus.repository.task;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.IDGenerator;
import org.sapia.corus.client.common.IOUtil;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.range.IntRange;
import org.sapia.corus.client.common.tuple.PairTuple;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.DistributionDeploymentMetadata;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Func;

@RunWith(MockitoJUnitRunner.class)
public class ArtifactRequestHandlerTaskSupportTest {
  
  @Mock
  private ServerContext serverContext;
  
  
  @Mock
  private TaskExecutionContext taskContext;
  
  @Mock
  private DeployOutputStream deployStream;
  
  @Mock
  private ProgressQueue progress;
  
  @Mock
  Func<DeploymentMetadata, Boolean> metadataFunc;
  
  private CorusHost currentHost;
  private File artifactFile;
  private List<Endpoint> targets;
  private ArtifactRequestHandlerTaskSupport support;
  private DeploymentMetadata meta;
  
 
  @Before
  public void setUp() throws Exception {
    currentHost = CorusHost.newInstance(
        "test-node", 
        new Endpoint(
            mock(ServerAddress.class), 
            mock(ServerAddress.class)
        ), 
        "test-os", 
        "test-jvm", 
        mock(PublicKey.class)
    );
    
    artifactFile = FilePath.forJvmTempDir()
        .setRelativeFile(getClass().getSimpleName() + "-" + IDGenerator.makeBase62Id(5) + ".txt")
        .createFile();
    
    when(metadataFunc.call(anyBoolean())).thenReturn(
        meta = new DistributionDeploymentMetadata("test", 1000, DeployPreferences.newInstance(), new ClusterInfo(true))
    );

    
    try (FileOutputStream os = new FileOutputStream(artifactFile)) {
      IOUtil.transfer(new ByteArrayInputStream("TEST".getBytes()), os, 2048);
    }
    targets = new ArrayList<>();
    IntRange.forLength(5).asList(new Func<Endpoint, Integer>() {
      @Override
      public Endpoint call(Integer val) {
        targets.add(new Endpoint(new TCPAddress("test", "test-host", val), mock(ServerAddress.class)));
        return null;
      }
    });
    
    support = new ArtifactRequestHandlerTaskSupport(artifactFile, targets, metadataFunc) {
    };
    
    support.setDeployOutputStreamFunc(new Func<DeployOutputStream, PairTuple<DeploymentMetadata,ServerAddress>>() {
      public DeployOutputStream call(PairTuple<DeploymentMetadata,ServerAddress> targetInfo) {
       return deployStream;
      }
    });
   
    when(taskContext.getServerContext()).thenReturn(serverContext);
    when(serverContext.getCorusHost()).thenReturn(currentHost);
    when(deployStream.commit()).thenReturn(progress);
  }

  @Test
  public void testRun() throws Throwable {
    support.execute(taskContext, null);

    assertNotNull(meta);
    assertTrue(meta.getVisited().contains(currentHost.getEndpoint().getServerAddress()));
    verify(taskContext, never()).error(anyString(), isA(Throwable.class));
    verify(deployStream).commit();
    verify(progress).hasNext();
    verify(deployStream).write(any(byte[].class), anyInt(), anyInt());
  }

}
