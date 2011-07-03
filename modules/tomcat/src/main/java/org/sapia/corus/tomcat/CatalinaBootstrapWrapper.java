package org.sapia.corus.tomcat;

import java.lang.reflect.Method;

import org.sapia.corus.interop.api.InteropLink;
import org.sapia.corus.interop.api.ShutdownListener;

public class CatalinaBootstrapWrapper { 

	public static String PROPERTY_CATALINA_STOP_METHOD = "sapia.corus.tomcat.catalinaStopMethod";
	public static String DEFAULT_CATALINA_STOP_METHOD = "stopd";

	public static String PROPERTY_CATALINA_MAIN_CLASS = "sapia.corus.tomcat.catalinaMainClass";
	public static String DEFAULT_CATALINA_MAIN_CLASS = "org.apache.catalina.startup.Bootstrap";
	
	
	/**
	 * Main method to start.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
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
		public void registerWithCorusInterop() {
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
