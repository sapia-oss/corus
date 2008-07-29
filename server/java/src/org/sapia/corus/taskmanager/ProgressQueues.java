package org.sapia.corus.taskmanager;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.util.ProgressMsg;
import org.sapia.corus.util.ProgressQueue;

/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ProgressQueues {
	
	private List _queues = Collections.synchronizedList(new ArrayList());
	
	public void addProgressQueue(ProgressQueue queue, int level){
		_queues.add(new SoftReference(new Subscription(level, queue)));
	}
	
  public void notify(ProgressMsg msg){
  	Subscription subs;
  	SoftReference ref;
		synchronized(_queues){
			for (int i = 0; i < _queues.size(); i++) {
        ref = (SoftReference)_queues.get(i);
        subs = (Subscription)ref.get();
        if(subs == null || subs.queue.isClosed()){
        	_queues.remove(i);
        	--i;
        	continue;
        }
        else{
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
