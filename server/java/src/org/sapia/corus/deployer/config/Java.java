package org.sapia.corus.deployer.config;

import org.sapia.console.CmdLine;

import org.sapia.corus.LogicException;
import org.sapia.corus.starter.Starter;

import org.sapia.util.text.MapContext;
import org.sapia.util.text.SystemContext;
import org.sapia.util.text.TemplateElementIF;
import org.sapia.util.text.TemplateException;
import org.sapia.util.text.TemplateFactory;

import java.io.File;

import java.util.HashMap;
import java.util.Map;


/**
 * Corresponds to the "java" element of the corus.xml file.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Java extends BaseJavaStarter {
  protected String _mainClass;
  protected String _cp;
  protected String _args;
  protected String _mainArgs;
  
  public void setCorusHome(String home) {
    _corusHome = home;
  }
  
  /**
   * Sets the name of the class to execute - class must have a main()
   * method.
   *
   * @param main the name of the class to execute
   */
  public void setMainClass(String main) {
    _mainClass = main;
  }
  
  /**
   * Sets the classpath to pass to the "java" executable, as would be
   * specified at the command line (but excluding the "-cp" string).
   *
   * @param cp sets the classpath to pass to the "java" executable.
   */
  public void setCp(String cp) {
    _cp = cp;
  }
  
  /**
   * Sets the arguments to pass to the main method of the specified
   * "main class".
   *
   * @see #setMainClass(String)
   * @param mainArgs a string of arguments.
   */
  public void setArgs(String mainArgs) {
    _mainArgs = mainArgs;
  }
  
  public CmdLine toCmdLine(Env env) throws LogicException {
    CmdLine cmd = new CmdLine();
    File javaHome = new File(_javaHome);
    
    if(!javaHome.exists()){
      throw new LogicException("java.home not found");
    }
    
    cmd.addArg(javaHome.getAbsolutePath() + File.separator + "bin" + File.separator + _javaCmd);
    
    if (_vmType != null) {
      cmd.addArg(_vmType);
    }
    
    if (_mainClass == null) {
      throw new LogicException("'mainClass' not specified in corus.xml");
    }
    Property prop = new Property();
    prop.setName(Starter.CORUS_JAVAPROC_MAIN_CLASS);
    prop.setValue(_mainClass);
    _vmProps.add(prop);
    
    for (int i = 0; i < _xoptions.size(); i++) {
      cmd.addElement(((Param) _xoptions.get(i)).convert());
    }
    
    for (int i = 0; i < _options.size(); i++) {
      cmd.addElement(((Param) _options.get(i)).convert());
    }
    
    for (int i = 0; i < _vmProps.size(); i++) {
      cmd.addElement(((Param) _vmProps.get(i)).convert());
    }
    
    Property[]      props = env.getProperties();
    
    Map             vars    = new HashMap();
    MapContext      context = new MapContext(vars, new SystemContext(), false);
    TemplateFactory fac     = new TemplateFactory();
    
    for (int i = 0; i < props.length; i++) {
      vars.put(props[i].getName(), props[i].getValue());
      cmd.addElement(props[i].convert());
    }
    
    if (_cp == null) {
      _cp = "";
    }
    String pathSep = System.getProperty("path.separator");
    String baseDir = System.getProperty("corus.home") == null ? System.getProperty("user.dir") : System.getProperty("corus.home");
    String starterLib  = baseDir + File.separator + "lib"  + File.separator + "sapia_corus_starter.jar";
    String starterDist = baseDir + File.separator + "dist" + File.separator + "sapia_corus_starter.jar";
    String starterCp = starterLib + pathSep + starterDist;
    _cp = starterCp + pathSep + _cp;
    
    try {
      TemplateElementIF template = fac.parse(_cp);
      cmd.addOpt("cp", template.render(context).replace(';', System.getProperty("path.separator").charAt(0)));
    } catch (TemplateException e) {
      throw new LogicException("Could not replace variables in 'cp'", e);
    }
    
    cmd.addArg(Starter.class.getName());
    
    if (_mainArgs != null) {
      TemplateElementIF template = fac.parse(_mainArgs);
      CmdLine           toAppend;
      
      try {
        toAppend = CmdLine.parse(template.render(context));
      } catch (TemplateException e) {
        throw new LogicException("Could not parse main class arguments", e);
      }
      
      while (toAppend.hasNext()) {
        cmd.addElement(toAppend.next());
      }
    }
    
    return cmd;
  }
}
