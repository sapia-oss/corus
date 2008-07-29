package org.sapia.corus.starter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.sapia.corus.interop.client.InteropClient;
import org.sapia.corus.interop.http.HttpProtocol;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Starter {
  
  public static final String CORUS_JAVAPROC_MAIN_CLASS = "corus.process.java.main";
  
  public static void main(String[] args){
    
    try {
      InteropClient.getInstance().setProtocol(new HttpProtocol());
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    String mainClass = System.getProperty(CORUS_JAVAPROC_MAIN_CLASS);
    if(mainClass == null){
      System.err.println(CORUS_JAVAPROC_MAIN_CLASS + " system property not defined");
      return;
    }
    try{
      Class clazz = Class.forName(mainClass);
      Method mainMethod = clazz.getMethod("main", new Class[]{String[].class});
      mainMethod.invoke(null, new Object[]{args});
    }catch(ClassNotFoundException e){
      e.printStackTrace();
    }catch(NoSuchMethodException e){
      System.err.println(mainClass + " does not have a main method");
      e.printStackTrace();
    }catch(IllegalAccessException e){
      System.err.println(mainClass + " must be public and have a public main method");
      e.printStackTrace();
    }catch(InvocationTargetException e){
      System.err.println("Error invoking main method on " + CORUS_JAVAPROC_MAIN_CLASS);
      e.getTargetException().printStackTrace();
    }
    
  }
}
