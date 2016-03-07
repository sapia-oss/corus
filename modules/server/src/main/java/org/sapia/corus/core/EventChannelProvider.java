package org.sapia.corus.core;

import org.sapia.ubik.mcast.EventChannel;

public interface EventChannelProvider {

  public EventChannel getEventChannel();
}
