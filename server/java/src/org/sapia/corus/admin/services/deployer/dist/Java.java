package org.sapia.corus.admin.services.deployer.dist;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.DirectoryScanner;
import org.sapia.console.CmdLine;
import org.sapia.corus.exceptions.LogicException;
import org.sapia.corus.starter.Starter;
import org.sapia.util.text.MapContext;
import org.sapia.util.text.SystemContext;
import org.sapia.util.text.TemplateElementIF;
import org.sapia.util.text.TemplateException;
import org.sapia.util.text.TemplateFactory;


/**
 * Corresponds to the "java" element of the corus.xml file.
 *
 * @author Yanick Duchesne
 */
public class Java extends BaseJavaStarter {
  
  static final long serialVersionUID = 1L;

  protected String _mainClass;
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
    
    Map<String, String>             vars    = new HashMap<String, String>();
    MapContext      context = new MapContext(vars, new SystemContext(), false);
    TemplateFactory fac     = new TemplateFactory();
    
    for (int i = 0; i < props.length; i++) {
      vars.put(props[i].getName(), props[i].getValue());
      cmd.addElement(props[i].convert());
    }
    
    String pathSep = System.getProperty("path.separator");
    String baseDir = System.getProperty("corus.home") == null ? System.getProperty("user.dir") : System.getProperty("corus.home");
    String starterLib  = baseDir + File.separator + "lib"  + File.separator + "sapia_corus_starter.jar";
    String starterDist = baseDir + File.separator + "dist" + File.separator + "sapia_corus_starter.jar";
    String starterCp = starterLib + pathSep + starterDist;
    String classpath = starterCp + pathSep + getProcessCp(env.getCommonDir()) + pathSep + getMainCp();
    
    try {
      TemplateElementIF template = fac.parse(classpath);
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
  
  private String getMainCp() {
    DirectoryScanner ds      = new DirectoryScanner();
    String           basedir = (_corusHome == null ? System.getProperty("user.dir") : _corusHome) + File.separator + "vm-boot-lib";
    ds.setBasedir(basedir);
    ds.setIncludes(new String[] { "**/*.jar" });
    ds.scan();

    String[]     jars = ds.getIncludedFiles();
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < jars.length; i++) {
      buf.append(basedir).append(File.separator).append(jars[i]);

      if (i < (jars.length - 1)) {
        buf.append(System.getProperty("path.separator"));
      }
    }

    return buf.toString();
  }
  
  private String getProcessCp(String processUserDir) {
    if(!new File(processUserDir).exists()){
      processUserDir = System.getProperty("user.dir");
    }
    DirectoryScanner ds      = new DirectoryScanner();
    String           basedir = processUserDir;
    ds.setBasedir(basedir);
    ds.setIncludes(new String[] { "lib/**/*.jar", "lib/**/*.zip" });
    ds.scan();
    
    String[]     jars = ds.getIncludedFiles();
    Arrays.sort(jars);
    
    List<String> path = new ArrayList<String>();
    // adding classes dir
    path.add("classes"+File.separator);
    for(String jar:jars){
      path.add(jar);
    }
    StringBuffer buf = new StringBuffer();
    
    for (int i = 0; i < path.size(); i++) {
      buf.append(basedir).append(File.separator).append(path.get(i));
      if (i < (path.size() - 1)) {
        buf.append(System.getProperty("path.separator"));
      }
    }

    return buf.toString();
  }
}
