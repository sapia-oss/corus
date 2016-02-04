package org.sapia.corus.tomcat;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sapia.corus.interop.api.InteropLink;
import org.sapia.corus.interop.api.message.ContextMessagePart;
import org.sapia.corus.interop.api.message.InteropMessageBuilderFactory;
import org.sapia.corus.interop.api.message.ParamMessagePart;
import org.sapia.corus.interop.api.message.StatusMessageCommand;

public class CatalinaHookTest {

	// Fixtures
	private static MockCorusInteropLink _corusInterop;

	private static InteropMessageBuilderFactory  _factory;
	private static StatusMessageCommand.Builder  _statusBuilder;
	private static ContextMessagePart.Builder    _contextBuilder;
	private static ParamMessagePart.Builder      _paramBuilder;
	
	private static StatusMessageCommand _status;
	private static ContextMessagePart   _context;
	private static ParamMessagePart     _param;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		// Doing it in static because interop link can only be set once
		_corusInterop = new MockCorusInteropLink();
		InteropLink.setImpl(_corusInterop);
		
		_factory        = mock(InteropMessageBuilderFactory.class);
		_statusBuilder  = mock(StatusMessageCommand.Builder.class);
		_contextBuilder = mock(ContextMessagePart.Builder.class);
		_paramBuilder   = mock(ParamMessagePart.Builder.class);
		
		_status         = mock(StatusMessageCommand.class);
		_context        = mock(ContextMessagePart.class);
		_param          = mock(ParamMessagePart.class);
		
		when(_factory.newStatusMessageBuilder()).thenReturn(_statusBuilder);
		when(_factory.newContextBuilder()).thenReturn(_contextBuilder);
		when(_factory.newParamBuilder()).thenReturn(_paramBuilder);
		
		when(_statusBuilder.commandId(anyString())).thenReturn(_statusBuilder);
		when(_statusBuilder.context(any(ContextMessagePart.class))).thenReturn(_statusBuilder);
		when(_contextBuilder.name(anyString())).thenReturn(_contextBuilder);
		when(_contextBuilder.param(anyString(), anyString())).thenReturn(_contextBuilder);
		
		when(_paramBuilder.build()).thenReturn(_param);
		when(_contextBuilder.build()).thenReturn(_context);
		when(_statusBuilder.build()).thenReturn(_status);
	}
	
	@Test
	public void testLifeCycle() throws Exception {
		Thread t = new Thread(new CatalinaHookTask(new String[] {"start"}));
		t.start();
		
		Thread.sleep(5000);
		_corusInterop.status(_statusBuilder, _factory);
		
		Thread.sleep(2000);
		_corusInterop.shutdown();
		
		t.join(2000);
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
