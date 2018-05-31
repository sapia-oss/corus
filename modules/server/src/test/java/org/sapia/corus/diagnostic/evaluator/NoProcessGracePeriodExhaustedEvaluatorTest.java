package org.sapia.corus.diagnostic.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.services.diagnostic.ProcessConfigDiagnosticStatus;

@RunWith(MockitoJUnitRunner.class)
public class NoProcessGracePeriodExhaustedEvaluatorTest extends ProcessConfigDiagnosticEvaluatorTestHelper {

  private NoProcessGracePeriodExhaustedEvaluator evaluator;
  
  @Before
  public void setUp() throws Exception {
    evaluator = new NoProcessGracePeriodExhaustedEvaluator();
  }

  @Test
  public void testAccepts() {
    createFixtures(5, 0);
    clock.increaseCurrentTimeMillis(context.getGracePeriod().getValueInMillis() + 1);
    processes.clear();
    assertTrue(evaluator.accepts(context));
  }
  
  @Test
  public void testAccepts_within_grace_period() {
    createFixtures(5, 0);
    processes.clear();
    assertFalse(evaluator.accepts(context));
  }

  @Test
  public void testEvaluate() {
    createFixtures(5, 0);
    clock.increaseCurrentTimeMillis(context.getGracePeriod().getValueInMillis() + 1);
    evaluator.evaluate(context);
    assertEquals(ProcessConfigDiagnosticStatus.MISSING_PROCESS_INSTANCES, context.getResultsBuilder().build(context).getStatus());
  }

}
