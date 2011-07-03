package org.sapia.corus.tomcat;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.interop.api.InteropLink;

public class CatalinaHookTest {

	// Fixtures
	private MockCorusInteropLink _corusInterop;
	
	@Before
	public void setUp() throws Exception {
		_corusInterop = new MockCorusInteropLink();
		InteropLink.setImpl(_corusInterop);
	}
	
	@Test
	public void testLifeCycle() throws Exception {
		Thread t = new Thread(new CatalinaHookTask(new String[] {"start"}));
		t.start();
		
		Thread.sleep(5000);
		
		_corusInterop.shutdown();
		
		t.join(5000);
	}
	
	public class CatalinaHookTask implements Runnable {
		private String[] _args;
		public CatalinaHookTask(String[] someArgs) {
			_args = someArgs;
		}
		
		public void run() {
			CatalinaHook.main(_args);
		}
	}
	
	public static void main(String[] args) {
		try {
			CatalinaHookTest test = new CatalinaHookTest();
			test.setUp();
			test.testLifeCycle();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
