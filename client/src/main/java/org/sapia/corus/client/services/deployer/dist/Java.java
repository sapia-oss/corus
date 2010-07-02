package org.sapia.corus.client.services.deployer.dist;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.CompositeStrLookup;
import org.sapia.corus.client.common.Env;
import org.sapia.corus.client.common.PathFilter;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.exceptions.misc.MissingDataException;

/**
 * Corresponds to the "java" element of the corus.xml file.
 *
 * @author Yanick Duchesne
 */
public class Java extends BaseJavaStarter {
  
  public static final String CORUS_JAVAPROC_MAIN_CLASS = "corus.process.java.main";
  
  static final long serialVersionUID = 1L;

  protected String _mainClass;
  protected String _args;
  protected String _mainArgs;
  protected String _libDirs;
  
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
  
  /**
   * Sets the directories where libraries that should be part of
   * the classpath are stored.
   */
  public void setLibDirs(String dirs) {
    _libDirs = dirs;
  }
  
  
  public CmdLine toCmdLine(Env env) throws MissingDataException {
    CmdLine cmd = new CmdLine();
    File javaHome = new File(_javaHome);
    
    if(!javaHome.exists()){
      throw new MissingDataException("java.home not found");
    }
    
    cmd.addArg(javaHome.getAbsolutePath() + File.separator + "bin" + File.separator + _javaCmd);
    
    if (_vmType != null) {
      cmd.addArg(_vmType);
    }
    
    if (_mainClass == null) {
      throw new MissingDataException("'mainClass' not specified in corus.xml");
    }
    
    Property prop = new Property();
    prop.setName(CORUS_JAVAPROC_MAIN_CLASS);
    prop.setValue(_mainClass);
    _vmProps.add(prop);
    
    Map<String, String>  cmdLineVars = new HashMap<String, String>();
    cmdLineVars.put("user.dir", env.getCommonDir());
    CompositeStrLookup context = new CompositeStrLookup()
      .add(StrLookup.mapLookup(cmdLineVars))
      .add(PropertiesStrLookup.systemPropertiesLookup());
    
    for (int i = 0; i < _xoptions.size(); i++) {
      XOption opt = _xoptions.get(i);
      String value = render(context, opt.getValue());
      opt.setValue(value);
      cmdLineVars.put(opt.getName(), value);
      cmd.addElement(opt.convert());
    }
    
    for (int i = 0; i < _options.size(); i++) {
      Option opt = _options.get(i);
      String value = render(context, opt.getValue());
      opt.setValue(value);
      cmdLineVars.put(opt.getName(), value);
      cmd.addElement(opt.convert());
    }
    
    for (int i = 0; i < _vmProps.size(); i++) {
      Property p = _vmProps.get(i);
      String value = render(context, p.getValue());
      p.setValue(value);
      cmdLineVars.put(p.getName(), value);
      cmd.addElement(p.convert());
    }
    
    Property[]      props = env.getProperties();
    
    Map<String, String>  envVars    = new HashMap<String, String>();
    CompositeStrLookup   envContext = new CompositeStrLookup()
      .add(StrLookup.mapLookup(envVars))
      .add(StrLookup.systemPropertiesLookup());
    
    for (int i = 0; i < props.length; i++) {
      envVars.put(props[i].getName(), props[i].getValue());
    }
    envVars.putAll(cmdLineVars);
    
    String pathSep = System.getProperty("path.separator");
    String baseDir = System.getProperty("corus.home") == null ? System.getProperty("user.dir") : System.getProperty("corus.home");
    String starterLib  = baseDir + File.separator + "server" + File.separator + "lib"  + File.separator + "sapia_corus-starter.jar";
    String classpath = starterLib + pathSep + getProcessCp(env.getCommonDir(), envContext, env) + pathSep + getMainCp(env);
    
    StrSubstitutor substitutor = new StrSubstitutor(context);
    cmd.addOpt("cp", substitutor.replace(classpath).replace(';', System.getProperty("path.separator").charAt(0)));
    
    cmd.addArg(Starter.class.getName());
    
    if (_mainArgs != null) {
      CmdLine           toAppend = CmdLine.parse(new StrSubstitutor(context).replace(_mainArgs));
      while (toAppend.hasNext()) {
        cmd.addElement(toAppend.next());
      }
    }
    
    return cmd;
  }
  
  private String getMainCp(Env env) {
    String           basedir = (_corusHome == null ? System.getProperty("user.dir") : _corusHome) + File.separator + "lib" + File.separator + "vm-boot";
    PathFilter filter = env.createPathFilter(basedir);
    filter.setIncludes(new String[] { "**/*.jar" });
    String[] jars = filter.filter();
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < jars.length; i++) {
      buf.append(basedir).append(File.separator).append(jars[i]);

      if (i < (jars.length - 1)) {
        buf.append(System.getProperty("path.separator"));
      }
    }

    return buf.toString();
  }
  
  private String getProcessCp(String processUserDir, StrLookup envVars, Env env) {
    if(!new File(processUserDir).exists()){
      processUserDir = System.getProperty("user.dir");
    }
    
    String[] baseDirs;
    if(_libDirs == null || _libDirs.trim().length() == 0){
      baseDirs = new String[]{"lib"};
    }
    else{
      baseDirs = _libDirs.split(",");
    }

    StringBuffer buf = new StringBuffer();

    for(String baseDir:baseDirs){
      String           currentDir = processUserDir;
      PathFilter filter = env.createPathFilter(currentDir);
      filter.setIncludes(new String[] { baseDir+"/**/*.jar", baseDir+"/**/*.zip" });
      String[]     jars = filter.filter();
      Arrays.sort(jars);
      
      List<String> path = new ArrayList<String>();
      // adding classes dir
      path.add("classes"+File.separator);
      for(String jar:jars){
        path.add(jar);
      }
      
      for (int i = 0; i < path.size(); i++) {
        buf.append(currentDir).append(File.separator).append(path.get(i));
        if (i < (path.size() - 1)) {
          buf.append(System.getProperty("path.separator"));
        }
      }
    }
    Map<String, String> values = new HashMap<String, String>();
    values.put("user.dir", processUserDir);
    CompositeStrLookup vars = new CompositeStrLookup()
      .add(StrLookup.mapLookup(values))
      .add(envVars);
    StrSubstitutor substitutor = new StrSubstitutor(vars);
    return substitutor.replace(buf.toString());
  }
  
  private String render(StrLookup context, String value){
    StrSubstitutor substitutor = new StrSubstitutor(context);
    return substitutor.replace(value);
  }
}
