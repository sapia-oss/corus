package org.sapia.corus.client.services.deployer.dist;

import java.io.File;
import java.io.Serializable;
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
import org.sapia.corus.client.common.FileUtils;
import org.sapia.corus.client.common.PathFilter;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.common.FileUtils.FileInfo;
import org.sapia.corus.client.exceptions.misc.MissingDataException;


/**
 * This helper class can be inherited from to implement <code>Starter</code>s that
 * launch Java processes.
 * 
 * @author Yanick Duchesne
 */
public abstract class BaseJavaStarter implements Starter, Serializable {
  
  static final long serialVersionUID = 1L;

  protected String _javaHome   = System.getProperty("java.home");
  protected String _javaCmd    = "java";
  protected String _vmType; 
  protected String _profile;
  protected String _corusHome = System.getProperty("corus.home");
  protected List<Property>   _vmProps   = new ArrayList<Property>();
  protected List<Option>     _options    = new ArrayList<Option>();
  protected List<XOption>    _xoptions   = new ArrayList<XOption>();
  private   List<Dependency> _dependencies = new ArrayList<Dependency>();
  
  /**
   * Sets the Corus home.
   *
   * @param home the Corus home.
   */
  public void setCorusHome(String home) {
    _corusHome = home;
  }

  /**
   * Sets this instance's profile.
   *
   * @param profile a profile name.
   */
  public void setProfile(String profile) {
    _profile = profile;
  }

  /**
   * Returns this instance's profile.
   *
   * @return a profile name.
   */
  public String getProfile() {
    return _profile;
  }

  /**
   * Adds the given property to this instance.
   *
   * @param prop a <code>Property</code> instance.
   */
  public void addProperty(Property prop) {
    _vmProps.add(prop);
  }

  /**
   * Adds the given VM option to this instance.
   *
   * @param opt an <code>Option</code> instance.
   */
  public void addOption(Option opt) {
    _options.add(opt);
  }

  /**
   * Adds the given "X" option to this instance.
   *
   * @param opt a <code>XOption</code> instance.
   */
  public void addXoption(XOption opt) {
    _xoptions.add(opt);
  }

  /**
   * Sets this instance's JDK home directory.
   *
   * @param home the full path to a JDK installation directory
   */
  public void setJavaHome(String home) {
    _javaHome = home;
  }

  /**
   * Sets the name of the 'java' executable.
   *
   * @param cmdName the name of the 'java' executable
   */
  public void setJavaCmd(String cmdName) {
    _javaCmd = cmdName;
  }
  
  public void setVmType(String aType) {
    _vmType = aType;
  }
  
  /**
   * Adds a dependency to this instance.
   * 
   * @param dep a {@link Dependency}
   */
  public void addDependency(Dependency dep){
    if(dep.getProfile() == null){
      dep.setProfile(_profile);
    }
    _dependencies.add(dep);
  }
  
  public Dependency createDependency(){
    Dependency dep = new Dependency();
    dep.setProfile(_profile);
    _dependencies.add(dep);
    return dep;
  }
  
  public List<Dependency> getDependencies() {
    return new ArrayList<Dependency>(_dependencies);
  }
  
  
  protected CmdLineBuildResult buildCommandLine(Env env){
    Map<String, String> cmdLineVars = new HashMap<String, String>();
    cmdLineVars.put("user.dir", env.getCommonDir());
    Property[] envProperties = env.getProperties();
        
    CompositeStrLookup propContext = new CompositeStrLookup()
      .add(StrLookup.mapLookup(cmdLineVars))
      .add(PropertiesStrLookup.getInstance(envProperties))    
      .add(PropertiesStrLookup.getSystemInstance());
    
    CmdLine cmd = new CmdLine();
        
    File javaHome = new File(_javaHome);
    if(!javaHome.exists()){
      throw new MissingDataException("java.home not found");
    }        
    cmd.addArg(javaHome.getAbsolutePath() + File.separator + "bin" + File.separator + _javaCmd);
    
    if (_vmType != null) {
      if(!_vmType.startsWith("-")){
        cmd.addArg("-"+_vmType);        
      }
      else{
        cmd.addArg(_vmType);
      }
    }

    
    for (int i = 0; i < _xoptions.size(); i++) {
      XOption opt = _xoptions.get(i);
      String value = render(propContext, opt.getValue());
      opt.setValue(value);
      cmdLineVars.put(opt.getName(), value);
      cmd.addElement(opt.convert());
    }
  
    for (int i = 0; i < _options.size(); i++) {
      Option opt = _options.get(i);
      String value = render(propContext, opt.getValue());
      opt.setValue(value);
      cmdLineVars.put(opt.getName(), value);
      cmd.addElement(opt.convert());
    }
  
    for (int i = 0; i < _vmProps.size(); i++) {
      Property p = _vmProps.get(i);
      String value = render(propContext, p.getValue());
      p.setValue(value);
      cmdLineVars.put(p.getName(), value);
      cmd.addElement(p.convert());
    }
    
    for (int i = 0; i < envProperties.length; i++) {
      if(propContext.lookup(envProperties[i].getName()) != null){
        cmd.addElement(envProperties[i].convert());
      }
    }
    
    CmdLineBuildResult ctx = new CmdLineBuildResult();
    ctx.command = cmd;
    ctx.variables = propContext;
    return ctx;
  }
  
  protected String getOptionalCp(String libDirs, StrLookup envVars, Env env) {
    String processUserDir;
    if((processUserDir = env.getCommonDir()) == null || !new File(env.getCommonDir()).exists()){
      processUserDir = System.getProperty("user.dir");
    }
    
    String[] baseDirs;
    if(libDirs == null){
      return "";
    }
    else{
      baseDirs = libDirs.split(";");
    }

    StringBuffer buf = new StringBuffer();

    for(int dirIndex = 0; dirIndex < baseDirs.length; dirIndex++){
      String baseDir = render(envVars, baseDirs[dirIndex]);
      String currentDir;
      if(FileUtils.isAbsolute(baseDir)){
        currentDir = baseDir;        
      }
      else{
        currentDir = processUserDir + FileUtils.FILE_SEPARATOR + baseDir;
      }
      
      FileInfo fileInfo = FileUtils.getFileInfo(currentDir);        
      PathFilter filter = env.createPathFilter(fileInfo.directory);
      if(fileInfo.isClasses){
        buf.append(fileInfo.directory);
      }
      else{
        if(fileInfo.fileName == null){
          filter.setIncludes(new String[] { "**/*.jar", "**/*.zip" });
        }
        else{
          filter.setIncludes(new String[] { fileInfo.fileName });
        }
        
        String[]     jars = filter.filter();
        Arrays.sort(jars);
        for (int i = 0; i < jars.length; i++) {
          buf.append(fileInfo.directory).append(FileUtils.FILE_SEPARATOR).append(jars[i]);
          if (i < (jars.length - 1)) {
            buf.append(FileUtils.PATH_SEPARATOR);
          }
        }
        
        if(dirIndex < baseDirs.length - 1){
          buf.append(FileUtils.PATH_SEPARATOR);
        }
      }
    }
    Map<String, String> values = new HashMap<String, String>();
    values.put("user.dir", processUserDir);
    CompositeStrLookup vars = new CompositeStrLookup()
      .add(StrLookup.mapLookup(values))
      .add(envVars);
    return render(vars, buf.toString());
  }    
  
  protected String render(StrLookup context, String value){
    StrSubstitutor substitutor = new StrSubstitutor(context);
    return substitutor.replace(value);
  }
  
  static final class CmdLineBuildResult{
    CmdLine command;
    StrLookup variables;
  }
}
