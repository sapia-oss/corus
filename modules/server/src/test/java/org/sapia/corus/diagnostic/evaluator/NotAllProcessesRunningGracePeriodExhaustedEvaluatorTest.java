package org.sapia.corus.diagnostic.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NotAllProcessesRunningGracePeriodExhaustedEvaluatorTest extends ProcessConfigDiagnosticEvaluatorTestHelper {
  
  private NotAllProcessesRunningGracePeriodExhaustedEvaluator evaluator;
  
  @Before
  public void setUp() throws Exception {
    evaluator = new NotAllProcessesRunningGracePeriodExhaustedEvaluator();
  }

  @Test
  public void testAccepts() {
    createFixtures(5, 3);
    clock.increaseCurrentTimeMillis(context.getGracePeriod().getValueInMillis() + 1);
    assertTrue(evaluator.accepts(context));
  }

  @Test
  public void testAccepts_within_grace_period() {
    createFixtures(5, 3);
    assertFalse(evaluator.accepts(context));
  }
  
  @Test
  public void testAccepts_all_processes_running() {
    createFixtures(5, 5);
    clock.increaseCurrentTimeMillis(context.getGracePeriod().getValueInMillis() + 1);
    assertFalse(evaluator.accepts(context));
  }

  @Test
  public void testEvaluate() {
    createFixtures(5, 3);
    evaluator.evaluate(context);
    assertEquals(3, results.build(context).getProcessResults().size());
  }


}
