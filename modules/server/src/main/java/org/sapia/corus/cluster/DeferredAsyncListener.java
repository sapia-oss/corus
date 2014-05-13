package org.sapia.corus.cluster;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.sapia.corus.util.Queue;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannelStateListener;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.util.Func;

/**
 * An instance of this class is used to keep {@link RemoteEvent}s until the
 * Corus server has started.
 * 
 * @author yduchesne
 * 
 */
class DeferredAsyncListener implements AsyncEventListener, EventChannelStateListener {

  private class AsyncEventListenerWrapper {
    private AsyncEventListener delegate;
    private Queue<RemoteEvent> pending = new Queue<RemoteEvent>();

    public AsyncEventListenerWrapper(AsyncEventListener delegate) {
      this.delegate = delegate;
    }

    private void flush() {
      for (RemoteEvent evt : pending.removeAll()) {
        delegate.onAsyncEvent(evt);
      }
    }
  }

  private class EventChannelStateListenerWrapper {
    private EventChannelStateListener delegate;
    private Queue<Func<Void, EventChannelStateListener>> pending = new Queue<Func<Void, EventChannelStateListener>>();

    public EventChannelStateListenerWrapper(EventChannelStateListener delegate) {
      this.delegate = delegate;
    }

    private void flush() {
      for (Func<Void, EventChannelStateListener> func : pending.removeAll()) {
        func.call(delegate);
      }
    }
  }

  // --------------------------------------------------------------------------

  private Map<String, AsyncEventListenerWrapper> ayncListenersByEventType = new ConcurrentHashMap<String, AsyncEventListenerWrapper>();

  private Set<EventChannelStateListenerWrapper> eventChannelListeners = new HashSet<EventChannelStateListenerWrapper>();

  private volatile boolean ready;

  DeferredAsyncListener() {
  }

  // --------------------------------------------------------------------------
  // AsyncEventListener interface

  @Override
  public synchronized void onAsyncEvent(RemoteEvent evt) {
    AsyncEventListenerWrapper wrapper = ayncListenersByEventType.get(evt.getType());
    if (wrapper != null) {
      if (ready) {
        wrapper.delegate.onAsyncEvent(evt);
      } else {
        wrapper.pending.add(evt);
      }
    }
  }

  // --------------------------------------------------------------------------
  // EventChannelStateListener interface

  @Override
  public synchronized void onDown(final EventChannelEvent event) {
    for (EventChannelStateListenerWrapper wrapper : eventChannelListeners) {
      if (ready) {
        wrapper.delegate.onDown(event);
      } else {
        wrapper.pending.add(new Func<Void, EventChannelStateListener>() {
          @Override
          public Void call(EventChannelStateListener arg) {
            arg.onDown(event);
            return null;
          }
        });
      }
    }
  }

  @Override
  public synchronized void onUp(final EventChannelEvent event) {
    for (EventChannelStateListenerWrapper wrapper : eventChannelListeners) {
      if (ready) {
        wrapper.delegate.onUp(event);
      } else {
        wrapper.pending.add(new Func<Void, EventChannelStateListener>() {
          @Override
          public Void call(EventChannelStateListener arg) {
            arg.onUp(event);
            return null;
          }
        });
      }
    }
  }

  // --------------------------------------------------------------------------
  // Specific instance methods.

  /**
   * @param eventType
   *          an event type.
   * @param delegate
   *          the {@link AsyncEventListener} to register with this instance.
   * @return this instance.
   */
  DeferredAsyncListener add(String eventType, AsyncEventListener delegate) {
    this.ayncListenersByEventType.put(eventType, new AsyncEventListenerWrapper(delegate));
    return this;
  }

  /**
   * 
   * @param listener
   *          an {@link EventChannelStateListener} to add to this instance.
   * @return this instance.
   */
  DeferredAsyncListener add(EventChannelStateListener listener) {
    this.eventChannelListeners.add(new EventChannelStateListenerWrapper(listener));
    return this;
  }

  /**
   * Flushes the events that have been received so far to the appropriate
   * listeners that this instance holds.
   */
  synchronized void ready() {
    for (Map.Entry<String, AsyncEventListenerWrapper> w : ayncListenersByEventType.entrySet()) {
      w.getValue().flush();
    }
    for (EventChannelStateListenerWrapper w : eventChannelListeners) {
      w.flush();
    }
    ready = true;
  }

}
