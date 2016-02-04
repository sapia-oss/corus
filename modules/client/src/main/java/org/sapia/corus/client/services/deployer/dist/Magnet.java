package org.sapia.corus.client.services.deployer.dist;

import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.attributeNotNullOrEmpty;

import java.io.File;

import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.Env;
import org.sapia.corus.client.common.FileUtil;
import org.sapia.corus.client.common.PathFilter;
import org.sapia.corus.client.exceptions.misc.MissingDataException;
import org.sapia.util.xml.confix.ConfigurationException;
import org.sapia.util.xml.confix.ObjectCreationCallback;

/**
 * This class corresponds to the <code>magnet</code> element in the corus.xml
 * file.
 * 
 * @author Yanick Duchesne
 */
public class Magnet extends BaseJavaStarter implements java.io.Serializable, ObjectCreationCallback {

  static final long serialVersionUID = 1L;

  private static final String APP_STARTER_CLASS_NAME = "org.sapia.util.ApplicationStarter";

  private String magnetFile;
  private String magnetOptions;
  private String libDirs;
  
  /**
   * Sets the name of the magnet file that will be used to start the VM.
   * 
   * @param file
   *          the name of the magnet file that will be used to start the VM.
   */
  public void setMagnetFile(String file) {
    magnetFile = file;
  }

  /**
   * Sets the Magnet-specific options (-debug, etc.)
   * 
   * @param options
   */
  public void setMagnetOptions(String options) {
    magnetOptions = options;
  }

  /**
   * Sets the directories where libraries that should be part of the System
   * classloader are stored.
   */
  public void setLibDirs(String dirs) {
    libDirs = dirs;
  }
  
  /**
   * Returns a "command-line" representation of this instance.
   * 
   * @return a {@link CmdLine} instance.
   */
  @Override
  public StarterResult toCmdLine(Env env) throws MissingDataException {
    if (magnetFile == null) {
      throw new MissingDataException("'magnetFile' attribute not specified in 'magnet' element of corus.xml");
    } else if (profile == null) {
      throw new MissingDataException("'profile' attribute not specified in 'magnet' element of corus.xml");
    }

    CmdLineBuildResult result = super.buildCommandLine(env);

    String mainCp = getMainCp(env);
    String optionalCp = libDirs == null ? null : getOptionalCp(libDirs, result.variables, env);
    if (optionalCp != null) {
      mainCp = mainCp + File.pathSeparator + optionalCp;
    }

    result.command.addOpt("cp", mainCp);
    result.command.addArg(APP_STARTER_CLASS_NAME);
    result.command.addOpt("ascp", getAsCp(env));
    result.command.addArg("org.sapia.magnet.MagnetRunner");
    result.command.addOpt("magnetfile", FileUtil.toPath(env.getCommonDir(), magnetFile));
    result.command.addOpt("p", profile);
    if (magnetOptions != null && magnetOptions.length() > 0) {
      result.command.addArg(magnetOptions);
    }

    return new StarterResult(StarterType.MAGNET, result.command, isInteropEnabled());
  }
  
  @Override
  public Object onCreate() throws ConfigurationException {
    attributeNotNullOrEmpty("magnet", "magnetFile", magnetFile);
    return this;
  }

  public String toString() {
    return "[ profile=" + profile + ", JDK home=" + javaHome + ", magnet file=" + magnetFile + ", VM props=" + vmProps + ", options=" + options
        + ", x options=" + xoptions + " ]";
  }

  private String getAsCp(Env env) {
    String basedir = env.getMagnetLibDir();
    PathFilter filter = env.createPathFilter(basedir);
    filter.setIncludes(new String[] { "**/*.jar", "**/*.zip" });

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

}
