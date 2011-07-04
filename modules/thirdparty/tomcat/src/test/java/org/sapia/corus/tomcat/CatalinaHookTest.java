package org.sapia.corus.tomcat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sapia.corus.interop.api.InteropLink;

public class CatalinaHookTest {

	// Fixtures
	private static MockCorusInteropLink _corusInterop;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		// Doing it in static because interop link can only be set once
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
			CatalinaHookTest.setUpClass();
			CatalinaHookTest test = new CatalinaHookTest();
			test.testLifeCycle();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
