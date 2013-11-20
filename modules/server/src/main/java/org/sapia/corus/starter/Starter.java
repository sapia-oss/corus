package org.sapia.corus.starter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.sapia.corus.client.services.deployer.dist.Java;
import org.sapia.corus.interop.client.InteropClient;
import org.sapia.corus.interop.http.HttpProtocol;

/**
 * This class implements the logic for bootstrapping the Corus interop client on
 * the client-side.
 * 
 * @author Yanick Duchesne
 */
public class Starter {

  public static void main(String[] args) {

    try {
      InteropClient.getInstance().setProtocol(new HttpProtocol());
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    String mainClass = System.getProperty(Java.CORUS_JAVAPROC_MAIN_CLASS);
    if (mainClass == null) {
      System.err.println(Java.CORUS_JAVAPROC_MAIN_CLASS + " system property not defined");
      return;
    }
    try {
      Class<?> clazz = Class.forName(mainClass);
      Method mainMethod = clazz.getMethod("main", new Class[] { String[].class });
      mainMethod.invoke(null, new Object[] { args });
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      System.err.println(mainClass + " does not have a main method");
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      System.err.println(mainClass + " must be public and have a public main method");
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      System.err.println("Error invoking main method on " + Java.CORUS_JAVAPROC_MAIN_CLASS);
      e.getTargetException().printStackTrace();
    }

  }
}
