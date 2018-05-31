package org.sapia.corus.event;

import java.util.concurrent.ExecutorService;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.ubik.concurrent.ConfigurableExecutor;
import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;
import org.sapia.ubik.concurrent.NamedThreadFactory;
import org.sapia.ubik.rmi.interceptor.MultiDispatcher;
import org.sapia.ubik.util.TimeValue;

/**
 * Implements the {@link EventDispatcher} interface.
 * 
 * @author Yanick Duchesne
 */
@Bind(moduleInterface = EventDispatcher.class)
public class EventDispatcherImpl extends ModuleHelper implements EventDispatcher {

  private static final int CORE_POOL_SIZE = 2;
  private static final int MAX_POOL_SIZE = 5;
  private static final long KEEP_ALIVE_SECONDS = 30;
  private static final int WORK_QUEUE_SIZE = 1000;

  private MultiDispatcher delegate = new MultiDispatcher();
  private ExecutorService executor;

  @Override
  public String getRoleName() {
    return ROLE;
  }

  @Override
  public void init() throws Exception {
    ThreadingConfiguration conf = ThreadingConfiguration.newInstance().setCorePoolSize(CORE_POOL_SIZE).setMaxPoolSize(MAX_POOL_SIZE)
        .setKeepAlive(TimeValue.createSeconds(KEEP_ALIVE_SECONDS)).setQueueSize(WORK_QUEUE_SIZE);

    executor = new ConfigurableExecutor(conf, NamedThreadFactory.createWith("EventDispatcher").setDaemon(true));
  }

  @Override
  public void start() throws Exception {
  }

  @Override
  public void dispose() {
    if (executor != null) {
      executor.shutdownNow();
    }
  }

  @Override
  @SuppressWarnings("rawtypes")
  public void addInterceptor(Class event, Object it) {
    logger().debug("Adding interceptor: " + it + " for event type: " + event);
    delegate.addInterceptor(event, it);
  }

  @Override
  public void dispatch(final Object event) {
    // precaution if this instance is used in unit testing and init()
    // has not been called
    if (executor == null) {
      delegate.dispatch(event);
    } else {
      executor.execute(new Runnable() {
        @Override
        public void run() {
          delegate.dispatch(event);
        }
      });
    }
  }

}
