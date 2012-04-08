package org.sapia.corus.taskmanager;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;

/**
 * @author Yanick Duchesne
 *
 */
public class ProgressQueues {
	
	private List<SoftReference<Subscription>> queues = Collections.synchronizedList(
	    new ArrayList<SoftReference<Subscription>>()
	);
	
	public void addProgressQueue(ProgressQueue queue, int level){
		queues.add(new SoftReference<Subscription>(new Subscription(level, queue)));
	}
	
  public void notify(ProgressMsg msg){
  	Subscription subs;
  	SoftReference<Subscription> ref;
		synchronized(queues){
			for (int i = 0; i < queues.size(); i++) {
        ref = (SoftReference<Subscription>)queues.get(i);
        subs = (Subscription)ref.get();
        if(subs == null || subs.queue.isClosed()){
        	queues.remove(i);
        	--i;
        	continue;
        } else {
          subs.queue.addMsg(msg);
        }
      }
		}
	}

	static final class Subscription{
		int level;
		ProgressQueue queue;
		Subscription(int level, ProgressQueue queue){
			this.level = level;
			this.queue = queue;
		}
	}

}
