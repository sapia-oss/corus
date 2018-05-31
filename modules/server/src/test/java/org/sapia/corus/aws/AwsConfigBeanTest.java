package org.sapia.corus.aws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.aws.AwsConfiguration.AwsConfigChangeListener;
import org.sapia.corus.configurator.InternalConfigurator;

@RunWith(MockitoJUnitRunner.class)
public class AwsConfigBeanTest {

  @Mock
  private AwsConfigChangeListener listener;
  
  @Mock
  private InternalConfigurator configurator;
  
  private AwsConfigBean bean;
  
  @Before
  public void setUp() throws Exception {
    bean = new AwsConfigBean() {
      @Override
      protected String retrieveInstanceId() throws IOException {
        return "test";
      }
    };
    bean.setConfigurator(configurator);
    bean.init();
  }

  @Test
  public void testIsAwsEnabled_true() {
    bean.addConfigChangeListener(listener);
    bean.getIsAwsEnabled().setValue(true);
    assertTrue(bean.isAwsEnabled());
    verify(listener).onAwsEnabled();
  }
  
  @Test
  public void testIsAwsEnabled_false() {
    bean.getIsAwsEnabled().setValue(true);
    bean.addConfigChangeListener(listener);
    bean.getIsAwsEnabled().setValue(false);
    assertFalse(bean.isAwsEnabled());
    verify(listener).onAwsDisabled();
  }
  
  @Test
  public void testConfigChangeListener_get_instance_id_for_enabled() throws IOException {
    bean.getIsAwsEnabled().setValue(true);
    assertEquals("test", bean.getInstanceId());
  }
  
  @Test(expected = IllegalStateException.class)
  public void testConfigChangeListener_get_instance_id_for_disabled() throws IOException {
    bean.getInstanceId();
  }

}
