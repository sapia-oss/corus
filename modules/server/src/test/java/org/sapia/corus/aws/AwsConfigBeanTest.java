package org.sapia.corus.aws;

import static org.assertj.core.api.Assertions.assertThat;
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
      protected String retrieveInstanceId() {
        return "test-instance-id";
      }
      
      @Override
      protected String retrieveAvailabilityZone() {
        return "test-az";
      }
      
      @Override
      protected String retrieveRegion() {
        return "test-region";
      }
    };
    bean.setConfigurator(configurator);
    bean.init();
  }

  @Test
  public void testIsAwsEnabled_true() {
    bean.addConfigChangeListener(listener);
    bean.getIsAwsEnabled().setValue(true);
    assertThat(bean.isAwsEnabled()).isTrue();
    verify(listener).onAwsEnabled();
  }
  
  @Test
  public void testIsAwsEnabled_false() {
    bean.getIsAwsEnabled().setValue(true);
    bean.addConfigChangeListener(listener);
    bean.getIsAwsEnabled().setValue(false);
    assertThat(bean.isAwsEnabled()).isFalse();
    verify(listener).onAwsDisabled();
  }
  
  @Test
  public void testAwsEnabled_get_instance_id() throws IOException {
    bean.getIsAwsEnabled().setValue(true);
    assertThat(bean.getInstanceId()).isEqualTo("test-instance-id");
  }
  
  @Test(expected = IllegalStateException.class)
  public void testAwsDisabled_get_instance_id() throws IOException {
    bean.getInstanceId();
  }
  
  @Test
  public void testAwsEnabled_get_az() throws IOException {
    bean.getIsAwsEnabled().setValue(true);
    assertThat(bean.getAvailabilityZone()).isEqualTo("test-az");
  }
  
  @Test(expected = IllegalStateException.class)
  public void testAwsDisabled_get_az() throws IOException {
    bean.getAvailabilityZone();
  }

  @Test
  public void testAwsEnabled_get_region() throws IOException {
    bean.getIsAwsEnabled().setValue(true);
    assertThat(bean.getRegion()).isEqualTo("test-region");
  }
  
  @Test(expected = IllegalStateException.class)
  public void testAwsDisabled_get_region() throws IOException {
    bean.getRegion();
  }
}
