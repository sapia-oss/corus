package org.sapia.corus.tomcat;


import java.lang.reflect.Method;

import org.sapia.corus.interop.api.InteropLink;
import org.sapia.corus.interop.api.ShutdownListener;


public class CatalinaHook { 

	public static String PROPERTY_CATALINA_STOP_METHOD = "sapia.corus.tomcat.catalinaStopMethod";
	public static String DEFAULT_CATALINA_STOP_METHOD = "stopd";

	public static String PROPERTY_CATALINA_MAIN_CLASS = "sapia.corus.tomcat.catalinaMainClass";
	public static String DEFAULT_CATALINA_MAIN_CLASS = "org.apache.catalina.startup.Bootstrap";

	public static String PROPERTY_MONITOR_CATALINA_MBEANS = "sapia.corus.tomcat.monitorCatalinaMbeans";
	public static String DEFAULT_MONITOR_CATALINA_MBEANS = "true";
	
	
	/**
	 * Main method to start.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String exportValue = System.getProperty(PROPERTY_MONITOR_CATALINA_MBEANS, DEFAULT_MONITOR_CATALINA_MBEANS);
			if (exportValue.equalsIgnoreCase("true")) {
				JmxMonitorAdapter monitor = new JmxMonitorAdapter();
				monitor.setAppendMbeanInfo(false);
				monitor.setDomain("Catalina");
				monitor.addInclude("*j2eeType=Servlet*");
				monitor.addInclude("*type=Deployer*");
				monitor.addInclude("*type=Engine*");
				monitor.addInclude("*type=GlobalRequestProcessor*");
				monitor.addInclude("*type=Host*");
				monitor.addInclude("*type=JkHandler*");
				monitor.addInclude("*type=Manager*");
				monitor.addInclude("*type=Mapper*");
				monitor.addInclude("*type=ProtocolHandler*");
				monitor.addInclude("*type=Resource*");
				monitor.addInclude("*type=Server*");
				monitor.addInclude("*type=ThreadPool*");
				
				monitor.init();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			new ShutdownDelegate().registerWithCorusInterop();
			String className = System.getProperty(PROPERTY_CATALINA_MAIN_CLASS, DEFAULT_CATALINA_MAIN_CLASS);
			doCallStaticMethod(className, "main", new Object[] { args });
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Internal utility method to perform a dynamic call using reflection.
	 * 
	 * @param aClassName The class name
	 * @param aMethodName The method name
	 * @param someArgs The method argument.
	 * @throws Exception If an error occurs calling the method.
	 */
	protected static Object doCallStaticMethod(String aClassName, String aMethodName, Object[] someArgs) throws Exception {
		// 1. Get the target class object
        Class<?> targetClass = Thread.currentThread().getContextClassLoader().loadClass(aClassName);

        // 2. Get the target method
        Class<?>[] paramTypes = new Class[someArgs.length];
        for (int i = 0; i < someArgs.length; i++) {
        	paramTypes[i] = someArgs[i].getClass();
        }
        Method targetMethod = targetClass.getMethod(aMethodName, paramTypes);
        
        // 3. Invoke the method
        Object result = targetMethod.invoke(targetClass, someArgs);
        
        return result;
	}
	
	/**
	 * Simple class that implements a delegate that listens on Corus Interop for shutdown call.
	 * 
	 * @author JC
	 */
	public static class ShutdownDelegate implements ShutdownListener {

		private boolean _isRegistered;
		
		/**
		 * Creates a new instance of {@link ShutdownDelegate}.
		 */
		public ShutdownDelegate() {
			_isRegistered = false;
		}

		/**
		 * Registers this delegate with the Corus interop link.
		 */
		public synchronized void registerWithCorusInterop() {
			if (!_isRegistered && InteropLink.getImpl().isDynamic()) {
				InteropLink.getImpl().addShutdownListener(this);
				_isRegistered = true;
			}
		}
		
		/* (non-Javadoc)
		 * @see org.sapia.corus.interop.api.ShutdownListener#onShutdown()
		 */
		@Override
		public void onShutdown() {
			try {
				String className = System.getProperty(PROPERTY_CATALINA_MAIN_CLASS, DEFAULT_CATALINA_MAIN_CLASS);
				String argument = System.getProperty(PROPERTY_CATALINA_STOP_METHOD, DEFAULT_CATALINA_STOP_METHOD);
				
				doCallStaticMethod(className, "main", new Object[] { new String[] {argument} });
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
