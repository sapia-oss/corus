package org.sapia.corus.aws.autoscaling;

import org.sapia.corus.aws.AwsConfiguration;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.configurator.InternalConfigurator;
import org.sapia.corus.core.ServerContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Synchronizes the lifecycle of auto-scaling instances with this corus instance. Namelly will listen
 * on any SNS topic for lifecycle temrination events.
 *  
 * @author jcdesrochers
 */
public class AwsAutoScalingInstanceLifecycleBean {
  
  @Autowired 
  private ServerContext                   serverContext;
  
  @Autowired
  private EventDispatcher                 dispatcher;
  
  @Autowired
  private AwsConfiguration                awsConfiguration;
  
  @Autowired
  private AwsAutoScalingConfigurationBean awsAutoScalingConfig;

  @Autowired
  private InternalConfigurator            configurator;

}
