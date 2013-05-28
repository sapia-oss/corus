package org.sapia.corus.repository.task;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.repository.Repository;

public class ForcePullTaskTest extends AbstractRepoTaskTest {
  
  private Repository    repo;
  private ForcePullTask task;

  @Before
  public void setUp() {
    super.doSetUp();
    repo = mock(Repository.class);
    task = new ForcePullTask(repo);
  }
  
  @Test
  public void testExecute() throws Throwable {
    task.execute(taskContext, null);
    verify(repo).pull();
  }

}
