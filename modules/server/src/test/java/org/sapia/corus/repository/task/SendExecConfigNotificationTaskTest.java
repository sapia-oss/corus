package org.sapia.corus.repository.task;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.cluster.ClusterNotification;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.processor.ExecConfig;

public class SendExecConfigNotificationTaskTest extends AbstractRepoTaskTest {
  
  private List<ExecConfig>               execConfigs;
  private SendExecConfigNotificationTask task;

  @Before
  public void setUp() {
    super.doSetUp();
    execConfigs = mock(List.class);
    task = new SendExecConfigNotificationTask(execConfigs, new HashSet<Endpoint>());
  }
  
  @Test
  public void testNonEmptyConfigList() throws Throwable {
    when(execConfigs.isEmpty()).thenReturn(false);
    task.execute(taskContext, null);
    verify(cluster).dispatch(any(ClusterNotification.class));
  }
  
  @Test
  public void testWithEmptyConfigList() throws Throwable {
    when(execConfigs.isEmpty()).thenReturn(true);
    task.execute(taskContext, null);
    verify(cluster, never()).dispatch(any(ClusterNotification.class));
  }
}
