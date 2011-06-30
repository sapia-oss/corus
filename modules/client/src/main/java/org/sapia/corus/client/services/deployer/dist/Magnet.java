package org.sapia.corus.client.services.deployer.dist;

import java.io.File;

import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.Env;
import org.sapia.corus.client.common.FileUtils;
import org.sapia.corus.client.common.PathFilter;
import org.sapia.corus.client.exceptions.misc.MissingDataException;

/**
 * This class corresponds to the <code>magnet</code> element in the
 * corus.xml file.
 *
 * @author Yanick Duchesne
 */
public class Magnet extends BaseJavaStarter implements java.io.Serializable {
  
  static final long serialVersionUID = 1L;
  
  private static final String APP_STARTER_CLASS_NAME = "org.sapia.util.ApplicationStarter";
  
  private String _magnetFile;
  private String _magnetOptions;
  private String _libDirs;

  /**
   * Sets the name of the magnet file that will be used to start the VM.
   *
   * @param file the name of the magnet file that will be used to start the VM.
   */
  public void setMagnetFile(String file) {
    _magnetFile = file;
  }

  /**
   * Sets the Magnet-specific options (-debug, etc.)
   * @param options
   */
  public void setMagnetOptions(String options) {
    _magnetOptions = options;
  }
  
  /**
   * Sets the directories where libraries that should be part of
   * the System classloader are stored.
   */
  public void setLibDirs(String dirs) {
    _libDirs = dirs;
  }  
  
  /**
   * Returns a "command-line" representation of this instance.
   *
   * @return a <code>CmdLine</code> instance.
   */
  public CmdLine toCmdLine(Env env) throws MissingDataException {
    if (_magnetFile == null) {
      throw new MissingDataException("'magnetFile' attribute not specified in 'magnet' element of corus.xml");
    } else if (_profile == null) {
      throw new MissingDataException("'profile' attribute not specified in 'magnet' element of corus.xml");
    }

    CmdLineBuildResult result = super.buildCommandLine(env);
   
    String mainCp = getMainCp(env);    
    String optionalCp = _libDirs == null ? null : getOptionalCp(_libDirs, result.variables, env);
    if(optionalCp != null){
      mainCp = mainCp+FileUtils.PATH_SEPARATOR+optionalCp;
    }
    
    result.command.addOpt("cp", mainCp);
    result.command.addArg(APP_STARTER_CLASS_NAME);
    result.command.addOpt("ascp", getAsCp(env));
    result.command.addArg("org.sapia.magnet.MagnetRunner");
    result.command.addOpt("magnetfile", env.getCommonDir() + File.separator + _magnetFile);
    result.command.addOpt("p", _profile);
    if (_magnetOptions != null && _magnetOptions.length() > 0) {
      result.command.addArg(_magnetOptions);
    }

    return result.command;
  }

  public String toString() {
    return "[ profile=" + _profile + ", JDK home=" + _javaHome +
           ", magnet file=" + _magnetFile + ", VM props=" + _vmProps +
           ", options=" + _options + ", x options=" + _xoptions + " ]";
  }

  private String getAsCp(Env env) {
    String           basedir = env.getMagnetLibDir();
    PathFilter filter = env.createPathFilter(basedir);
    filter.setIncludes(new String[] { "**/*.jar", "**/*.zip" });
    
    String[]     jars = filter.filter();
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < jars.length; i++) {
      buf.append(basedir).append(File.separator).append(jars[i]);

      if (i < (jars.length - 1)) {
        buf.append(System.getProperty("path.separator"));
      }
    }

    return buf.toString();
  }

  private String getMainCp(Env env) {
    String           basedir = env.getVmBootLibDir();
    PathFilter filter = env.createPathFilter(basedir);
    filter.setIncludes(new String[] { "**/*.jar" });
    
    String[]     jars = filter.filter();
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < jars.length; i++) {
      buf.append(basedir).append(File.separator).append(jars[i]);

      if (i < (jars.length - 1)) {
        buf.append(System.getProperty("path.separator"));
      }
    }

    return buf.toString();
  }

}
