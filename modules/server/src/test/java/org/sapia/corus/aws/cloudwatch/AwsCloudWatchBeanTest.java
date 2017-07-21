package org.sapia.corus.aws.cloudwatch;

import org.sapia.corus.aws.AwsConfiguration;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.event.CorusEvent;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.processor.event.ProcessStartedEvent;
import org.sapia.corus.client.test.TestCorusObjects;
import org.sapia.corus.configurator.InternalConfigurator;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.core.ServerContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEvents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AwsCloudWatchBeanTest {

  @Mock
  private EventDispatcher       dispatcher;

  @Mock
  private InternalConfigurator  configurator;

  @Mock
  private AwsConfiguration       awsConfiguration;

  @Mock
  private ServerContext          serverContext;

  private CorusHost              host;

  @Mock
  private CorusEvent             event;

  @Mock
  private AmazonCloudWatchEvents client;

  @Captor
  private ArgumentCaptor<AwsConfiguration.AwsConfigChangeListener> awsChangeCaptor;

  private AwsCloudWatchBean      bean;


  @Before
  public void setUp() throws Exception {
    host = TestCorusObjects.createHost();

    bean = new AwsCloudWatchBean() {
      @Override
      protected AmazonCloudWatchEvents doCreateClient(OptionalValue<String> theRegion) {
        return client;
      }
    };

    bean.setDispatcher(dispatcher);
    bean.setConfigurator(configurator);
    bean.setAwsConfiguration(awsConfiguration);
    bean.setServerContext(serverContext);
    bean.setEnabled(true);

    when(awsConfiguration.isAwsEnabled()).thenReturn(true);

  }

  @Test
  public void testInit() {
    bean.init();

    verify(configurator).registerForPropertyChange(CorusConsts.PROPERTY_CORUS_AWS_CLOUDWATCH_ENABLED, bean.getIsEnabled());
    verify(dispatcher).addInterceptor(CorusEvent.class, bean);
    assertThat(bean.getClient()).isNotNull();
  }

  @Test
  public void testInit_aws_disabled() {
    when(awsConfiguration.isAwsEnabled()).thenReturn(false);
    bean.init();

    verify(configurator).registerForPropertyChange(CorusConsts.PROPERTY_CORUS_AWS_CLOUDWATCH_ENABLED, bean.getIsEnabled());
    verify(dispatcher).addInterceptor(CorusEvent.class, bean);
    assertThat(bean.getClient()).isNull();
  }

  @Test
  public void testInit_cloudwatch_disabled() {
    bean.setEnabled(false);
    bean.init();

    verify(configurator).registerForPropertyChange(CorusConsts.PROPERTY_CORUS_AWS_CLOUDWATCH_ENABLED, bean.getIsEnabled());
    verify(dispatcher).addInterceptor(CorusEvent.class, bean);
    assertThat(bean.getClient()).isNull();
  }

  @Test
  public void testDestroy() {
    bean.setEnabled(true);
    bean.init();
    bean.destroy();

    verify(client).shutdown();
  }

  @Test
  public void testDestroy_aws_disabled() {
    when(awsConfiguration.isAwsEnabled()).thenReturn(false);
    bean.init();
    bean.destroy();

    verify(client, never()).shutdown();
  }

  @Test
  public void testDestroy_cloudwatch_disabled() {
    bean.setEnabled(false);
    bean.init();
    bean.destroy();

    verify(client, never()).shutdown();
  }

  @Test
  public void testSetRegion() {
    bean.setRegion("us-east-1");

    assertThat(bean.getRegion().get()).isEqualTo("us-east-1");
  }

  @Test
  public void testSetRegion_null() {
    bean.setRegion(null);

    assertThat(bean.getRegion().isNull()).isTrue();
  }

  @Test
  public void testSetRegion_empty() {
    bean.setRegion("");

    assertThat(bean.getRegion().isNull()).isTrue();
  }

  @Test
  public void testOnCorusEvent() {
    bean.init();
    bean.onCorusEvent(event);

    verify(client).putEvents(any());
  }

  @Test
  public void testOnCorusEvent_aws_disabled() {
    when(awsConfiguration.isAwsEnabled()).thenReturn(false);
    bean.init();
    bean.onCorusEvent(event);

    verify(client, never()).putEvents(any());
  }

  @Test
  public void testOnCorusEvent_cloudwatch_disabled() {
    bean.setEnabled(false);
    bean.init();
    bean.onCorusEvent(event);

    verify(client, never()).putEvents(any());
  }

  @Test
  public void testDisableAwsConfig() {
    bean.init();
    verify(awsConfiguration).addConfigChangeListener(awsChangeCaptor.capture());

    awsChangeCaptor.getValue().onAwsDisabled();
    verify(client).shutdown();
  }

  @Test
  public void testEnableAwsConfig() {
    when(awsConfiguration.isAwsEnabled()).thenReturn(false);
    bean.init();

    verify(awsConfiguration).addConfigChangeListener(awsChangeCaptor.capture());
    when(awsConfiguration.isAwsEnabled()).thenReturn(true);
    awsChangeCaptor.getValue().onAwsEnabled();

    assertThat(bean.getClient()).isNotNull();
  }

  @Test
  public void testDisableEnableAwsConfig() {
    bean.init();

    verify(awsConfiguration).addConfigChangeListener(awsChangeCaptor.capture());

    AwsConfiguration.AwsConfigChangeListener listener = awsChangeCaptor.getValue();
    when(awsConfiguration.isAwsEnabled()).thenReturn(false);
    listener.onAwsDisabled();
    verify(client).shutdown();

    when(awsConfiguration.isAwsEnabled()).thenReturn(true);
    listener.onAwsEnabled();
    assertThat(bean.getClient()).isNotNull();
  }


  @Test
  public void testDisableCloudWatch() {
    bean.init();
    verify(awsConfiguration).addConfigChangeListener(awsChangeCaptor.capture());

    bean.getIsEnabled().setValue(false);
    verify(client).shutdown();
  }

  @Test
  public void testEnableCloudWatch() {
    bean.setEnabled(false);
    bean.init();
    assertThat(bean.getClient()).isNull();

    bean.getIsEnabled().setValue(true);
    assertThat(bean.getClient()).isNotNull();
  }

  @Test
  public void testDisableEnableCloudWatch() {
    bean.init();

    bean.getIsEnabled().setValue(false);
    verify(client).shutdown();

    bean.getIsEnabled().setValue(true);
    assertThat(bean.getClient()).isNotNull();
  }
}
