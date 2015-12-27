package org.sapia.corus.client.services.deployer.dist;

import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.attributeNotNull;

import java.io.File;

import org.apache.commons.lang.text.StrLookup;
import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.Env;
import org.sapia.corus.client.common.FileUtil;
import org.sapia.corus.client.common.PathFilter;
import org.sapia.corus.client.exceptions.misc.MissingDataException;
import org.sapia.util.xml.confix.ConfigurationException;
import org.sapia.util.xml.confix.ObjectCreationCallback;

/**
 * Corresponds to the "java" element of the corus.xml file.
 * 
 * @author Yanick Duchesne
 */
public class Java extends BaseJavaStarter implements ObjectCreationCallback {

  public static final String CORUS_JAVAPROC_MAIN_CLASS = "corus.process.java.main";
  public static final String STARTER_CLASS             = "org.sapia.corus.starter.Starter";

  static final long serialVersionUID = 1L;

  protected String mainClass;
  protected String args;
  protected String mainArgs;
  protected String libDirs;
  private boolean interopEnabled = true;

  /**
   * Sets the name of the class to execute - class must have a main() method.
   * 
   * @param main
   *          the name of the class to execute
   */
  public void setMainClass(String main) {
    mainClass = main;
  }

  /**
   * Sets the arguments to pass to the main method of the specified
   * "main class".
   * 
   * @see #setMainClass(String)
   * @param mainArgs
   *          a string of arguments.
   */
  public void setArgs(String mainArgs) {
    this.mainArgs = mainArgs;
  }

  /**
   * Sets the directories where libraries that should be part of the classpath
   * are stored.
   */
  public void setLibDirs(String dirs) {
    libDirs = dirs;
  }
  
  /**
   * @param interopEnabled if <code>true</code>, indicates that interop is enabled (<code>true</code> by default).
   */
  public void setInteropEnabled(boolean interopEnabled) {
    this.interopEnabled = interopEnabled;
  }
  
  public boolean isInteropEnabled() {
    return interopEnabled;
  }

  @Override
  public StarterResult toCmdLine(Env env) throws MissingDataException {

    CmdLineBuildResult result = super.buildCommandLine(env);

    if (mainClass == null) {
      throw new MissingDataException("'mainClass' not specified in corus.xml");
    }

    Property prop = new Property();
    prop.setName(CORUS_JAVAPROC_MAIN_CLASS);
    prop.setValue(mainClass);
    result.command.addElement(prop.convert());
    
    String classpath = FileUtil.mergeFilePaths(
        env.getCorusIopLibPath(), 
        getCp(env, env.getJavaLibDir()),
        env.getJavaStarterLibPath(),
        getProcessCp(result.variables, env),
        getMainCp(env)
    );

    result.command.addOpt("cp", render(result.variables, classpath).replace(';', System.getProperty("path.separator").charAt(0)));

    result.command.addArg(STARTER_CLASS);

    if (mainArgs != null) {
      CmdLine toAppend = CmdLine.parse(render(result.variables, mainArgs));
      while (toAppend.hasNext()) {
        result.command.addElement(toAppend.next());
      }
    }

    return new StarterResult(StarterType.JAVA, result.command, interopEnabled);
  }
  
  @Override
  public Object onCreate() throws ConfigurationException {
    super.doValidate("java");
    attributeNotNull("java", "mainClass", mainClass);
    return this;
  }

  private String getMainCp(Env env) {
    String basedir = env.getVmBootLibDir();
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

  private String getProcessCp(StrLookup envVars, Env env) {
    if (libDirs == null || libDirs.trim().length() == 0) {
      return super.getOptionalCp("lib", envVars, env);
    } else {
      return super.getOptionalCp(libDirs, envVars, env);
    }
  }
}
