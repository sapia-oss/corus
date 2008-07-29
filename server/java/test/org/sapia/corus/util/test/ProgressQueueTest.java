package org.sapia.corus.util.test;

import junit.framework.TestCase;

import org.sapia.corus.util.ProgressMsg;
import org.sapia.corus.util.ProgressQueueImpl;

import java.util.List;


/**
 * @author Yanick Duchesne
 * 2002-02-23
 */
public class ProgressQueueTest extends TestCase {
  /**
   * Constructor for ProgressQueueTest.
   * @param arg0
   */
  public ProgressQueueTest(String arg0) {
    super(arg0);
  }

  public void testHasNext() {
    ProgressQueueImpl qu = new ProgressQueueImpl();

    for (int i = 0; i < 5; i++) {
      qu.addMsg(new ProgressMsg("" + i));
    }

    qu.close();

    while (qu.hasNext()) {
      super.assertEquals(qu.next().size(), 5);
    }
  }

  public void testMultiThread() throws Exception {
    ProgressQueueImpl qu  = new ProgressQueueImpl();
    Adder             add = new Adder(qu);
    add.start();

    int total = 0;

    while (qu.hasNext()) {
      List lst = qu.next();
      total = total + lst.size();
    }

    super.assertEquals(total, 10);
  }

  static class Adder extends Thread {
    ProgressQueueImpl _queue;

    Adder(ProgressQueueImpl q) {
      _queue = q;
    }

    public void run() {
      for (int i = 0; i < 10; i++) {
        _queue.addMsg(new ProgressMsg("" + i));
      }

      _queue.close();
    }
  }
}
