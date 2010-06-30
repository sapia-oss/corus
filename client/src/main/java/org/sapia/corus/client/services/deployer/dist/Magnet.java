package org.sapia.corus.client.services.deployer.dist;

import java.io.File;

import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.Env;
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

  /**
   * Sets the name of the magnet file that will be used to start the VM.
   *
   * @param file the name of the magnet file that will be used to start the VM.
   */
  public void setMagnetFile(String file) {
    _magnetFile = file;
  }

  public void setMagnetOptions(String options) {
    _magnetOptions = options;
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

    CmdLine cmd = new CmdLine();
    cmd.addArg(_javaHome + File.separator + "bin" + 
               File.separator + _javaCmd);

    if (_vmType != null) {
      cmd.addArg(_vmType);
    }
    
    for (int i = 0; i < _xoptions.size(); i++) {
      cmd.addElement(((Param) _xoptions.get(i)).convert());
    }

    for (int i = 0; i < _options.size(); i++) {
      cmd.addElement(((Param) _options.get(i)).convert());
    }

    for (int i = 0; i < _vmProps.size(); i++) {
      cmd.addElement(((Param) _vmProps.get(i)).convert());
    }

    Property[] props = env.getProperties();

    for (int i = 0; i < props.length; i++) {
      cmd.addElement(props[i].convert());
    }

    cmd.addOpt("cp", getMainCp(env));
    cmd.addArg(APP_STARTER_CLASS_NAME);
    cmd.addOpt("ascp", getAsCp(env));
    cmd.addArg("org.sapia.magnet.MagnetRunner");
    cmd.addOpt("magnetfile", env.getCommonDir() + File.separator + _magnetFile);
    cmd.addOpt("p", _profile);
    if (_magnetOptions != null && _magnetOptions.length() > 0) {
      cmd.addArg(_magnetOptions);
    }

    return cmd;
  }

  public String toString() {
    return "[ profile=" + _profile + ", JDK home=" + _javaHome +
           ", magnet file=" + _magnetFile + ", VM props=" + _vmProps +
           ", options=" + _options + ", x options=" + _xoptions + " ]";
  }

  private String getAsCp(Env env) {
    String           basedir = _corusHome + File.separator + "lib" + File.separator + "magnet";
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
    String           basedir = _corusHome + File.separator + "lib" + File.separator + "vm-boot";
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
