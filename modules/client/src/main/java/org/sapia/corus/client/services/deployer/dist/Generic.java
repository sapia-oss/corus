package org.sapia.corus.client.services.deployer.dist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.CompositeStrLookup;
import org.sapia.corus.client.common.Env;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.exceptions.misc.MissingDataException;
import org.sapia.ubik.util.Assertions;

/**
 * A {@link Starter} implementation used to start any type of process (supports specifying the corresponding
 * command to execute, as well as the process' arguments).
 * 
 * @author yduchesne
 *
 */
public class Generic implements Starter, Serializable {
  
  static final long serialVersionUID = 1L;

  private String             profile;
  private List<Dependency>   dependencies = new ArrayList<Dependency>();
  private List<CmdGenerator> args         = new ArrayList<CmdGenerator>();
  
  @Override
  public void setProfile(String profile) {
    this.profile = profile;
  }
  
  @Override
  public String getProfile() {
    return profile;
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

  /**
   * @return a new {@link Dependency} instance.
   */
  public Dependency createDependency() {
    Dependency dep = new Dependency();
    dep.setProfile(profile);
    dependencies.add(dep);
    return dep;
  }
  
  @Override
  public List<Dependency> getDependencies() {
    return dependencies;
  }
  
  /**
   * @return a new {@link GenericArg}.
   */
  public GenericArg createArg() {
    GenericArg arg = new GenericArg();
    this.args.add(arg);
    return arg;
  }
  
  /**
   * @return a new {@link IncludeProcessPropertiesArg}.
   */
  public IncludeProcessPropertiesArg createProcessProperties() {
    IncludeProcessPropertiesArg arg = new IncludeProcessPropertiesArg();
    this.args.add(arg);
    return arg;
  }

  @Override
  public CmdLine toCmdLine(Env env) throws MissingDataException {
    Assertions.notNull(profile, "Profile not set");
    
    Map<String, String> cmdLineVars   = new HashMap<String, String>();
    cmdLineVars.put("user.dir", env.getCommonDir());
    Property[]          envProperties = env.getProperties();
   
    CompositeStrLookup propContext = new CompositeStrLookup()
        .add(StrLookup.mapLookup(cmdLineVars))
        .add(PropertiesStrLookup.getInstance(envProperties))
        .add(PropertiesStrLookup.getSystemInstance())
        .add(StrLookup.mapLookup(env.getEnvironmentVariables()));

    CmdLine cmd = new CmdLine();
    
    // rendering arguments
    for (CmdGenerator arg : args) {
      if (arg instanceof GenericArg) {
        GenericArg copy = new GenericArg();
        String value = render(propContext, ((GenericArg) arg).getValue());
        copy.setValue(value);
        copy.generate(env, cmd);
      } else {
        arg.generate(env, cmd);
      }
    }
    
    return cmd;
  }
  
  private String render(StrLookup context, String value) {
    StrSubstitutor substitutor = new StrSubstitutor(context);
    return substitutor.replace(value);
  }

}
