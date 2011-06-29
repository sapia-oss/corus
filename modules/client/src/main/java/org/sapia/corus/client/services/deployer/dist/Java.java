package org.sapia.corus.client.services.deployer.dist;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.CompositeStrLookup;
import org.sapia.corus.client.common.Env;
import org.sapia.corus.client.common.FileUtils;
import org.sapia.corus.client.common.PathFilter;
import org.sapia.corus.client.common.FileUtils.FileInfo;
import org.sapia.corus.client.exceptions.misc.MissingDataException;

/**
 * Corresponds to the "java" element of the corus.xml file.
 *
 * @author Yanick Duchesne
 */
public class Java extends BaseJavaStarter {
  
  public static final String CORUS_JAVAPROC_MAIN_CLASS = "corus.process.java.main";
  public static final String STARTER_CLASS = "org.sapia.corus.starter.Starter";
  
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

    CmdLineBuildResult result = super.buildCommandLine(env);
    
    if (_mainClass == null) {
      throw new MissingDataException("'mainClass' not specified in corus.xml");
    }
    
    Property prop = new Property();
    prop.setName(CORUS_JAVAPROC_MAIN_CLASS);
    prop.setValue(_mainClass);
    result.command.addElement(prop.convert());
    
    String pathSep = System.getProperty("path.separator");

    String classpath = env.getCorusIopLibPath() + pathSep + 
                       env.getJavaStarterLibPath() + pathSep +
                       getProcessCp(env.getCommonDir(), result.variables, env) + pathSep + 
                       getMainCp(env);
        
    result.command.addOpt("cp", render(result.variables, classpath).replace(';', System.getProperty("path.separator").charAt(0)));
    
    result.command.addArg(STARTER_CLASS);
    
    if (_mainArgs != null) {
      CmdLine           toAppend = CmdLine.parse(render(result.variables, _mainArgs));
      while (toAppend.hasNext()) {
        result.command.addElement(toAppend.next());
      }
    }
    
    return result.command;
  }
  
  private String getMainCp(Env env) {
    String           basedir = env.getVmBootLibDir();
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
      baseDirs = _libDirs.split(";");
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
      
      if(dirIndex < jars.length - 1){
        buf.append(FileUtils.PATH_SEPARATOR);
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

  
}
