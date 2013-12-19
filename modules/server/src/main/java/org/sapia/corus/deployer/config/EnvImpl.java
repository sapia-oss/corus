package org.sapia.corus.deployer.config;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;

import org.sapia.corus.client.common.Env;
import org.sapia.corus.client.common.PathFilter;
import org.sapia.corus.client.services.deployer.dist.Property;

/**
 * A helper class that encapsulates various process startup parameters.
 * 
 * @author Yanick Duchesne
 */
public class EnvImpl implements Env {

  private String corusHome;
  private String distDir;
  private String commonDir;
  private String processDir;
  private String profile;
  private Property[] props;

  /**
   * Constructor for Env.
   * 
   * @param profile
   *          the name of the profile under which a process is to be started.
   * @param distDir
   *          the path to the distribution directory of the process to start.
   * @param commonDir
   *          the path to the "common" directory of the process to start -
   *          corresponds to user.dir.
   * @param processDir
   *          the path to the process directory.
   * @param props
   *          an array of {@link Property} instances that corresponds to the
   *          properties that will be dynamically passed to the started process.
   */
  public EnvImpl(String corusHome, String profile, String distDir, String commonDir, String processDir, Property[] props) {
    this.corusHome = corusHome;
    this.distDir = distDir;
    this.processDir = processDir;
    this.props = props;
    this.profile = profile;
    this.commonDir = commonDir;
  }

  /**
   * @return the distribution directory of the process to start.
   */
  public String getDistDir() {
    return distDir;
  }

  /**
   * @return the "common" directory of the process to start.
   */
  public String getCommonDir() {
    return commonDir;
  }

  /**
   * @return the process directory of the process to start.
   */
  public String getProcessDir() {
    return processDir;
  }

  /**
   * @return the name of the profile under which to start the process.
   */
  public String getProfile() {
    return profile;
  }

  /**
   * @return the properties to pass to the started process.
   */
  public Property[] getProperties() {
    return props;
  }

  @Override
  public String getLibDir() {
    return corusHome + File.separator + "lib";
  }

  @Override
  public String getServerLibDir() {
    return getLibDir() + File.separator + "server";
  }

  @Override
  public String getVmBootLibDir() {
    return getLibDir() + File.separator + "vm-boot";
  }

  @Override
  public String getJavaLibDir() {
    return getLibDir() + File.separator + "java";
  }

  @Override
  public String getMagnetLibDir() {
    return getLibDir() + File.separator + "magnet";
  }

  @Override
  public String getCorusIopLibPath() {
    return findLibPath(getServerLibDir(), "sapia_corus_iop-");
  }

  @Override
  public String getJavaStarterLibPath() {
    return findLibPath(getServerLibDir(), "sapia_corus-starter");
  }

  @Override
  public PathFilter createPathFilter(String basedir) {
    return new PathFilterImpl(basedir);
  }
  
  @Override
  public Map<String, String> getEnvironmentVariables() {
    return System.getenv();
  }

  protected String findLibPath(String basedirName, final String libName) {
    File basedir = new File(basedirName);
    File[] matching = basedir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.startsWith(libName);
      }
    });
    if (matching == null || matching.length == 0) {
      throw new IllegalStateException(String.format("Could not find lib %s under %s", libName, basedirName));
    }
    if (matching.length > 1) {
      throw new IllegalStateException(String.format("More than one match for lib %s under %s", libName, basedirName));
    }
    return matching[0].getAbsolutePath();
  }

}
