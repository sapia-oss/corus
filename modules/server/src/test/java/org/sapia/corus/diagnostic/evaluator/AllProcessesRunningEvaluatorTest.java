package org.sapia.corus.diagnostic.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AllProcessesRunningEvaluatorTest extends ProcessConfigDiagnosticEvaluatorTestHelper{
  
  private AllProcessesRunningEvaluator evaluator;
  
  @Before
  public void setUp() throws Exception {
    evaluator = new AllProcessesRunningEvaluator();
  }

  @Test
  public void testAccepts() {
    createFixtures(5, 5);
    assertTrue(evaluator.accepts(context));
  }
  
  @Test
  public void testAccepts_not_all_processes_running() {
    createFixtures(5, 3);
    assertFalse(evaluator.accepts(context));
  }

  @Test
  public void testEvaluate() {
    createFixtures(5, 5);
    evaluator.evaluate(context);
    assertEquals(5, results.build(context).getProcessResults().size());
  }

}
