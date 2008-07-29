package org.sapia.corus.deployer.config;

import org.apache.tools.ant.DirectoryScanner;

import org.sapia.console.CmdLine;

import org.sapia.corus.LogicException;

import org.sapia.util.ApplicationStarter;

import java.io.File;


/**
 * This class corresponds to the <code>magnet</code> element in the
 * corus.xml file.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Magnet extends BaseJavaStarter implements java.io.Serializable {
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
  public CmdLine toCmdLine(Env env) throws LogicException {
    if (_magnetFile == null) {
      throw new LogicException("'magnetFile' attribute not specified in 'magnet' element of corus.xml");
    } else if (_profile == null) {
      throw new LogicException("'profile' attribute not specified in 'magnet' element of corus.xml");
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

    cmd.addOpt("cp", getMainCp());
    cmd.addArg(ApplicationStarter.class.getName());
    cmd.addOpt("ascp", getAsCp());
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

  private String getAsCp() {
    DirectoryScanner ds      = new DirectoryScanner();
    String           basedir = _corusHome + File.separator + "magnetlib";
    ds.setBasedir(basedir);
    ds.setIncludes(new String[] { "**/*.jar", "**/*.zip" });
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

  private String getMainCp() {
    DirectoryScanner ds      = new DirectoryScanner();
    String           basedir = _corusHome + File.separator + "vm-boot-lib";
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
}
