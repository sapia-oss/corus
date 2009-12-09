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
import org.sapia.soto.util.TemplateContextMap;
import org.sapia.util.text.MapContext;
import org.sapia.util.text.SystemContext;
import org.sapia.util.text.TemplateContextIF;
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
    
    Map<String, String>  cmdLineVars = new HashMap<String, String>();
    cmdLineVars.put("user.dir", env.getCommonDir());
    MapContext      context = new MapContext(cmdLineVars, new SystemContext(), false);
    
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
    TemplateContextIF envContext = new MapContext(envVars, new SystemContext(), false);
    TemplateFactory fac     = new TemplateFactory();
    
    for (int i = 0; i < props.length; i++) {
      envVars.put(props[i].getName(), props[i].getValue());
    }
    envVars.putAll(cmdLineVars);
    
    String pathSep = System.getProperty("path.separator");
    String baseDir = System.getProperty("corus.home") == null ? System.getProperty("user.dir") : System.getProperty("corus.home");
    String starterLib  = baseDir + File.separator + "lib"  + File.separator + "sapia_corus_starter.jar";
    String starterDist = baseDir + File.separator + "dist" + File.separator + "sapia_corus_starter.jar";
    String starterCp = starterLib + pathSep + starterDist;
    String classpath = starterCp + pathSep + getProcessCp(env.getCommonDir(), envContext) + pathSep + getMainCp();
    
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
  
  private String getProcessCp(String processUserDir, TemplateContextIF env) throws LogicException{
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
      DirectoryScanner ds      = new DirectoryScanner();
      String           currentDir = processUserDir;
      ds.setBasedir(currentDir);
      ds.setIncludes(new String[] { baseDir+"/**/*.jar", baseDir+"/**/*.zip" });
      ds.scan();
      
      String[]     jars = ds.getIncludedFiles();
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
    try{
      TemplateElementIF template = new TemplateFactory().parse(buf.toString());
      Map<String, String> values = new HashMap<String, String>();
      values.put("user.dir", processUserDir);
      MapContext vars = new MapContext(values, env, false);
      return template.render(vars);
    }catch(TemplateException e){
      throw new LogicException("Could not render variables in classpath: " + buf.toString(), e);
    }
  }
  
  private String render(TemplateContextIF context, String value) throws LogicException{
    TemplateFactory fac     = new TemplateFactory();
    try{
      return fac.parse(value).render(context);
    }catch(TemplateException e){
      throw new LogicException("Could not render " + value, e);
    }
  }
}
