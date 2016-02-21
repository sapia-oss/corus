package org.sapia.corus.client.common.log;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PrefixLogCallbackTest {
  
  @Mock
  private LogCallback delegate;
  private PrefixedLogCallback callback;
  
  @Before
  public void setUp() {
    callback = new PrefixedLogCallback("testPrefix >>", delegate);
  }
  
  @Test
  public void testDebug() {
    callback.debug("test");
    verify(delegate).debug("testPrefix >> test");
  }
  
  @Test
  public void testInfo() {
    callback.info("test");
    verify(delegate).info("testPrefix >> test");
  }
  
  @Test
  public void testError() {
    callback.error("test");
    verify(delegate).error("testPrefix >> test");
  }
}
