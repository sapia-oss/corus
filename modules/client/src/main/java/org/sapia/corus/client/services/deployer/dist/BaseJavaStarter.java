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
import org.sapia.corus.client.common.EnvVariableStrLookup;
import org.sapia.corus.client.common.FileUtil;
import org.sapia.corus.client.common.FileUtil.FileInfo;
import org.sapia.corus.client.common.PathFilter;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.exceptions.misc.MissingDataException;
import org.sapia.ubik.util.Strings;
import org.sapia.util.xml.confix.ConfigurationException;

import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.*;

/**
 * This helper class can be inherited from to implement {@link Starter}s that
 * launch Java processes.
 * 
 * @author Yanick Duchesne
 */
public abstract class BaseJavaStarter implements Starter, Serializable {

  static final long serialVersionUID = 1L;

  protected String javaHome = System.getProperty("java.home");
  protected String javaCmd  = "java";
  protected String vmType;
  protected String profile;
  
  protected String         corusHome    = System.getProperty("corus.home");
  protected List<VmArg>    vmArgs       = new ArrayList<VmArg>();
  protected List<Property> vmProps      = new ArrayList<Property>();
  protected List<Option>   options      = new ArrayList<Option>();
  protected List<XOption>  xoptions     = new ArrayList<XOption>();
  private List<Dependency> dependencies = new ArrayList<Dependency>();

  /**
   * Sets the Corus home.
   * 
   * @param home
   *          the Corus home.
   */
  public void setCorusHome(String home) {
    corusHome = home;
  }

  /**
   * Sets this instance's profile.
   * 
   * @param profile
   *          a profile name.
   */
  public void setProfile(String profile) {
    this.profile = profile;
  }

  /**
   * Returns this instance's profile.
   * 
   * @return a profile name.
   */
  public String getProfile() {
    return profile;
  }

  /**
   * Adds the given {@link VmArg} to this instance.
   * 
   * @param arg
   *          a {@link VmArg}.
   */
  public void addArg(VmArg arg) {
    vmArgs.add(arg);
  }

  /**
   * Adds the given property to this instance.
   * 
   * @param prop
   *          a {@link Property} instance.
   */
  public void addProperty(Property prop) {
    vmProps.add(prop);
  }

  /**
   * Adds the given VM option to this instance.
   * 
   * @param opt
   *          an {@link Option} instance.
   */
  public void addOption(Option opt) {
    options.add(opt);
  }

  /**
   * Adds the given "X" option to this instance.
   * 
   * @param opt
   *          a {@link XOption} instance.
   */
  public void addXoption(XOption opt) {
    xoptions.add(opt);
  }

  /**
   * Sets this instance's JDK home directory.
   * 
   * @param home
   *          the full path to a JDK installation directory
   */
  public void setJavaHome(String home) {
    javaHome = home;
  }

  /**
   * Sets the name of the 'java' executable.
   * 
   * @param cmdName
   *          the name of the 'java' executable
   */
  public void setJavaCmd(String cmdName) {
    javaCmd = cmdName;
  }

  public void setVmType(String aType) {
    vmType = aType;
  }

  /**
   * Adds a dependency to this instance.
   * 
   * @param dep
   *          a {@link Dependency}
   */
  public void addDependency(Dependency dep) {
    if (dep.getProfile() == null) {
      dep.setProfile(profile);
    }
    dependencies.add(dep);
  }

  public Dependency createDependency() {
    Dependency dep = new Dependency();
    dep.setProfile(profile);
    dependencies.add(dep);
    return dep;
  }

  public List<Dependency> getDependencies() {
    return new ArrayList<Dependency>(dependencies);
  }

  protected CmdLineBuildResult buildCommandLine(Env env) {
    Map<String, String> cmdLineVars = new HashMap<String, String>();
    cmdLineVars.put("user.dir", env.getCommonDir());
    cmdLineVars.put("java.home", javaHome);
    Property[] envProperties = env.getProperties();

    CompositeStrLookup propContext = new CompositeStrLookup()
        .add(StrLookup.mapLookup(cmdLineVars))
        .add(PropertiesStrLookup.getInstance(envProperties))
        .add(PropertiesStrLookup.getSystemInstance())
        .add(new EnvVariableStrLookup());

    CmdLine cmd = new CmdLine();

    File javaHomeDir = env.getFileSystem().getFile(javaHome);
    if (!javaHomeDir.exists()) {
      throw new MissingDataException("java.home not found");
    }
    cmd.addArg(FileUtil.toPath(javaHomeDir.getAbsolutePath(), "bin", javaCmd));

    if (vmType != null) {
      if (!vmType.startsWith("-")) {
        cmd.addArg("-" + vmType);
      } else {
        cmd.addArg(vmType);
      }
    }

    for (VmArg arg : vmArgs) {
      String value = render(propContext, arg.getValue());
      VmArg copy = new VmArg();
      copy.setValue(value);
      cmd.addElement(copy.convert());
    }

    for (XOption opt : xoptions) {
      String value = render(propContext, opt.getValue());
      XOption copy = new XOption();
      copy.setName(opt.getName());
      copy.setValue(value);
      if (!Strings.isBlank(copy.getName())) {
        cmdLineVars.put(copy.getName(), value);
      }
      cmd.addElement(copy.convert());
    }

    for (Option opt : options) {
      String value = render(propContext, opt.getValue());
      Option copy = new Option();
      copy.setName(opt.getName());
      copy.setValue(value);
      if (!Strings.isBlank(copy.getName())) {
        cmdLineVars.put(copy.getName(), value);
      }
      cmd.addElement(copy.convert());
    }

    for (Property prop : vmProps) {
      String value = render(propContext, prop.getValue());
      Property copy = new Property();
      copy.setName(prop.getName());
      copy.setValue(value);
      cmdLineVars.put(copy.getName(), value);
      cmd.addElement(copy.convert());
    }

    for (Property prop : envProperties) {
      if (propContext.lookup(prop.getName()) != null) {
        cmd.addElement(prop.convert());
      }
    }

    CmdLineBuildResult ctx = new CmdLineBuildResult();
    ctx.command = cmd;
    ctx.variables = propContext;
    return ctx;
  }

  protected String getOptionalCp(String libDirs, StrLookup envVars, Env env) {
    String processUserDir;
    if ((processUserDir = env.getCommonDir()) == null 
        || !env.getFileSystem().getFile(env.getCommonDir()).exists()) {
      processUserDir = System.getProperty("user.dir");
    }

    String[] baseDirs;
    if (libDirs == null) {
      return "";
    } else {
      baseDirs = FileUtil.splitFilePaths(render(envVars, libDirs));
    }

    StringBuffer buf = new StringBuffer();

    for (int dirIndex = 0; dirIndex < baseDirs.length; dirIndex++) {
      String baseDir = baseDirs[dirIndex];
      String currentDir;
      if (FileUtil.isAbsolute(baseDir)) {
        currentDir = baseDir;
      } else {
        currentDir = FileUtil.toPath(processUserDir, baseDir);
      }

      FileInfo fileInfo = FileUtil.getFileInfo(currentDir);
      PathFilter filter = env.createPathFilter(fileInfo.directory);
      if (fileInfo.isClasses) {
        if (buf.length() > 0) {
          buf.append(File.pathSeparator);
        }
        buf.append(fileInfo.directory);
      } else {
        if (fileInfo.fileName == null) {
          filter.setIncludes(new String[] { "**/*.jar", "**/*.zip" });
        } else {
          filter.setIncludes(new String[] { fileInfo.fileName });
        }

        if (buf.length() > 0) {
          buf.append(File.pathSeparator);
        }
        
        String[] jars = filter.filter();
        Arrays.sort(jars);
        for (int i = 0; i < jars.length; i++) {
          buf.append(fileInfo.directory).append(File.separator).append(jars[i]);
          if (i < (jars.length - 1)) {
            buf.append(File.pathSeparator);
          }
        }
      }
    }
    return render(envVars, buf.toString());
  }

  protected String render(StrLookup context, String value) {
    StrSubstitutor substitutor = new StrSubstitutor(context);
    return substitutor.replace(value);
  }

  protected String getCp(Env env, String basedir) {
    PathFilter filter = env.createPathFilter(basedir);
    filter.setIncludes(new String[] { "**/*.jar" });

    String[] jars = filter.filter();
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < jars.length; i++) {
      buf.append(basedir).append(File.separator).append(jars[i]);

      if (i < (jars.length - 1)) {
        buf.append(File.pathSeparator);
      }
    }

    return buf.toString();
  }
  
  protected void doValidate(String elementName) throws ConfigurationException {
    attributeNotNullOrEmpty(elementName, "corusHome", corusHome);
    attributeNotNullOrEmpty(elementName, "javaCmd", javaCmd);
    attributeNotNullOrEmpty(elementName, "javaHome", javaHome);
    attributeNotNullOrEmpty(elementName, "profile", profile);
  }

  static final class CmdLineBuildResult {
    CmdLine command;
    StrLookup variables;
  }
}
