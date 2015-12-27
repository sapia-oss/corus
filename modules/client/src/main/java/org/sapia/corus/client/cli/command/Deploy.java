package org.sapia.corus.client.cli.command;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.console.OptionDef;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.exceptions.deployer.ConcurrentDeploymentException;
import org.sapia.corus.client.exceptions.deployer.DeploymentException;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.deployer.ChecksumPreference;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

/**
 * @author Yanick Duchesne
 */
public class Deploy extends CorusCliCommand {

  private static final String SCRIPT_DESC_UNDEFINED = "no desc.";

  private static final OptionDef OPT_EXEC_CONF      = new OptionDef("e", true);
  private static final OptionDef OPT_FILE           = new OptionDef("f", true);
  private static final OptionDef OPT_SCRIPT         = new OptionDef("s", true);
  private static final OptionDef OPT_DESC_OR_DIR    = new OptionDef("d", true);
  private static final OptionDef OPT_ALIAS          = new OptionDef("a", true);
  private static final OptionDef OPT_DEPLOY_SCRIPTS = new OptionDef("r", false);
  private static final OptionDef OPT_DEPLOY_REV     = new OptionDef("rev", true);
  private static final OptionDef OPT_SEQ            = new OptionDef("seq", false);
  private static final OptionDef OPT_DOCKER         = new OptionDef("docker", false);
  private static final OptionDef OPT_IMAGE          = new OptionDef("img", false);
  private static final OptionDef OPT_CHECKSUM_MD5   = new OptionDef("chkmd5", false);

  
  private static List<OptionDef> AVAIL_OPTIONS = Collects.arrayToList(
    OPT_EXEC_CONF, OPT_FILE, OPT_SCRIPT, OPT_DESC_OR_DIR, OPT_ALIAS, 
    OPT_DEPLOY_SCRIPTS, OPT_DEPLOY_REV, OPT_SEQ, OPT_DOCKER, OPT_IMAGE, 
    OPT_CHECKSUM_MD5,OPT_CLUSTER
  );
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return AVAIL_OPTIONS;
  }
  
  @Override
  protected void doInit(CliContext context) {
  }
    
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    if (ctx.getCommandLine().containsOption(OPT_DEPLOY_REV.getName(), false)) {
      RevId revId = RevId.valueOf(ctx.getCommandLine().assertOption(OPT_DEPLOY_REV.getName(), true).getValue());
      displayProgress(ctx.getCorus().getDeployerFacade().unarchiveDistributions(revId, getClusterInfo(ctx)), ctx);
    } else if (ctx.getCommandLine().containsOption(OPT_DOCKER.getName(), false)) {
      deployDockerImage(
          ctx, 
          ctx.getCommandLine().assertOption(OPT_IMAGE.getName(), true).getValue(), 
          ctx.getCommandLine().assertOption(OPT_FILE.getName(), true).getValue(), 
          DeployPreferences.newInstance()
      );
    } else if (ctx.getCommandLine().containsOption(OPT_EXEC_CONF.getName(), true)) {
      deployExec(ctx, ctx.getCommandLine().assertOption(OPT_EXEC_CONF.getName(), true).getValue());
    } else if (ctx.getCommandLine().containsOption(OPT_FILE.getName(), true)) {
      deployFile(ctx, ctx.getCommandLine().assertOption(OPT_FILE.getName(), true).getValue());
    } else if (ctx.getCommandLine().containsOption(OPT_SCRIPT.getName(), true)) {
      deployScript(
          ctx, ctx.getCommandLine().assertOption(OPT_SCRIPT.getName(), true).getValue(), 
          ctx.getCommandLine().assertOption(OPT_ALIAS.getName(), true).getValue()
      );
    } else {
      DeployPreferences prefs = DeployPreferences.newInstance();
      if (ctx.getCommandLine().containsOption(OPT_DEPLOY_SCRIPTS.getName(), false)) {
        prefs.setExecDeployScripts(true);
      }
      CmdLine cmdLine = ctx.getCommandLine();
      if (cmdLine.hasNext() && cmdLine.isNextArg()) {
        deployDistribution(ctx, cmdLine.assertNextArg().getName(), prefs);
      } else {
        throw new InputException("File name expected as argument");
      }
    }
  }
  
  // --------------------------------------------------------------------------
  // Distribution deployment
  
  private void deployDistribution(final CliContext ctx, final String fileName, final DeployPreferences prefs) throws AbortException, InputException {
    if (fileName.endsWith("xml")) {
      deployExec(ctx, fileName);
    } else if (ctx.getCommandLine().containsOption(OPT_SEQ.getName(), false)) {
      doSequentionDeployment(fileName, ctx, new Func<Void, CorusHost>() {
        @Override
        public Void call(CorusHost target) {
          ClusterInfo info = new ClusterInfo(false);
          try {
            displayProgress(ctx.getCorus().getDeployerFacade().deployDistribution(fileName, assignChecksumPreference(prefs.getCopy(), ctx), info), ctx);
          } catch (Exception e) {
            throw new AbortException("Error caught performing distribution deployment: " + e.getMessage(), e);
          }
          return null;
        }
      });
    } else {
      try {
        displayProgress(ctx.getCorus().getDeployerFacade().deployDistribution(fileName, assignChecksumPreference(prefs.getCopy(), ctx), getClusterInfo(ctx)), ctx);
      } catch (ConcurrentDeploymentException e) {
        CliError err = ctx.createAndAddErrorFor(this, "Distribution file already being deployed", e);
        ctx.getConsole().println(err.getSimpleMessage());
      } catch (Exception e) {
        CliError err = ctx.createAndAddErrorFor(this, "Problem deploying distribution", e);
        ctx.getConsole().println(err.getSimpleMessage());
      }
    }
  }
  
  // --------------------------------------------------------------------------
  // Docker deployment

  private void deployDockerImage(final CliContext ctx, final String imageName, final String fileName, final DeployPreferences prefs) throws AbortException, InputException {
    if (!ctx.getFileSystem().getFile(fileName).exists()) {
      throw new InputException("Docker image file not found: " + fileName);
    }
    if (ctx.getCommandLine().containsOption(OPT_SEQ.getName(), false)) {
      doSequentionDeployment(fileName, ctx, new Func<Void, CorusHost>() {
        @Override
        public Void call(CorusHost target) {
          ClusterInfo info = new ClusterInfo(false);
          try {
            displayProgress(
                ctx.getCorus().getDeployerFacade().deployDockerImage(
                  imageName, 
                  fileName, 
                  assignChecksumPreference(prefs.getCopy(), ctx), info
                ), 
                ctx
            );
          } catch (Exception e) {
            throw new AbortException("Error caught performing Docker image deployment: " + e.getMessage(), e);
          }
          return null;
        }
      });
    } else {
      try {
        displayProgress(
            ctx.getCorus().getDeployerFacade().deployDockerImage(
              imageName, 
              fileName, 
              assignChecksumPreference(prefs.getCopy(), ctx), getClusterInfo(ctx)
            ), 
            ctx
        );
      } catch (Exception e) {
        CliError err = ctx.createAndAddErrorFor(this, "Problem deploying Docker image", e);
        ctx.getConsole().println(err.getSimpleMessage());
      }
    }
    
  }
  
  // --------------------------------------------------------------------------
  // Script deployment
  
  private void deployScript(final CliContext ctx, final String fileName, final String alias) throws AbortException, InputException {
    final String desc;
    if (ctx.getCommandLine().containsOption(OPT_DESC_OR_DIR.getName(), true)) {
      desc = ctx.getCommandLine().assertOption(OPT_DESC_OR_DIR.getName(), true).getValue();
    } else {
      desc = SCRIPT_DESC_UNDEFINED;
    }
    
    if (ctx.getCommandLine().containsOption(OPT_SEQ.getName(), false)) {
      doSequentionDeployment(fileName, ctx, new Func<Void, CorusHost>() {
        @Override
        public Void call(CorusHost target) {
          ClusterInfo info = new ClusterInfo(false);
          try {
            DeployPreferences prefs = DeployPreferences.newInstance();
            displayProgress(ctx.getCorus().getDeployerFacade().deployScript(fileName, alias, desc, assignChecksumPreference(prefs, ctx), info), ctx);
          } catch (Exception e) {
            throw new AbortException("Error caught performing distribution deployment: " + e.getMessage(), e);
          }
          return null;
        }
      });
    } else {
      try {
        DeployPreferences prefs = DeployPreferences.newInstance();
        displayProgress(ctx.getCorus().getDeployerFacade().deployScript(fileName, alias, desc, assignChecksumPreference(prefs, ctx), getClusterInfo(ctx)), ctx);
      } catch (Exception e) {
        CliError err = ctx.createAndAddErrorFor(this, "Problem deploying script", e);
        ctx.getConsole().println(err.getSimpleMessage());
      }
    }
  }
  
  // --------------------------------------------------------------------------
  // File deployment

  private void deployFile(final CliContext ctx, final String fileName) throws AbortException, InputException {
    final String destDir = ctx.getCommandLine().containsOption(OPT_DESC_OR_DIR.getName(), true) ?
        ctx.getCommandLine().assertOption(OPT_DESC_OR_DIR.getName(), true).getValue() : null;
    
    if (ctx.getCommandLine().containsOption(OPT_SEQ.getName(), false)) {
      doSequentionDeployment(fileName, ctx, new Func<Void, CorusHost>() {
        @Override
        public Void call(CorusHost target) {
          ClusterInfo info = new ClusterInfo(false);
          try {
            DeployPreferences prefs = DeployPreferences.newInstance();
            displayProgress(ctx.getCorus().getDeployerFacade().deployFile(fileName, destDir, assignChecksumPreference(prefs, ctx), info), ctx);
          } catch (Exception e) {
            throw new AbortException("Error caught performing distribution deployment: " + e.getMessage(), e);
          }
          return null;
        }
      });
    } else {    
      try {
        DeployPreferences prefs = DeployPreferences.newInstance();
        assignChecksumPreference(prefs, ctx);
        displayProgress(ctx.getCorus().getDeployerFacade().deployFile(fileName, destDir, assignChecksumPreference(prefs, ctx), getClusterInfo(ctx)), ctx);
      } catch (Exception e) {
        CliError err = ctx.createAndAddErrorFor(this, "Problem deploying file", e);
        ctx.getConsole().println(err.getSimpleMessage());
      }
    }
  }

  
  // --------------------------------------------------------------------------
  // Exec config deployment
  
  private void deployExec(final CliContext ctx, String fileName) throws AbortException, InputException {
    final File file = ctx.getFileSystem().getFile(fileName);
    
    if (!file.exists()) {
      CliError err = ctx.createAndAddErrorFor(this, new DeploymentException("File not found: " + fileName));
      ctx.getConsole().println(err.getSimpleMessage());

    } else if (file.isDirectory()) {
      CliError err = ctx.createAndAddErrorFor(this, new DeploymentException("Resource is a directory: " + fileName));
      ctx.getConsole().println(err.getSimpleMessage());

    } else {
      if (ctx.getCommandLine().containsOption(OPT_SEQ.getName(), false)) {
        doSequentionDeployment(fileName, ctx, new Func<Void, CorusHost>() {
          @Override
          public Void call(CorusHost target) {
            ClusterInfo info = new ClusterInfo(false);
            try {
              ctx.getCorus().getProcessorFacade().deployExecConfig(file, info);              
            } catch (Exception e) {
              throw new AbortException("Error caught performing distribution deployment: " + e.getMessage(), e);
            }
            return null;
          }
        });
      } else {
        try {
          ctx.getCorus().getProcessorFacade().deployExecConfig(file, getClusterInfo(ctx));
        } catch (Exception e) {
          CliError err = ctx.createAndAddErrorFor(this, "Could not deploy execution configuration", e);
          ctx.getConsole().println(err.getSimpleMessage());
        }
      }
    }
  }
  
  private void doSequentionDeployment(String fileName, CliContext ctx, Func<Void, CorusHost> deployFunc) {
    ClusterInfo info = getClusterInfo(ctx);
    List<CorusHost> targets = new ArrayList<CorusHost>();
    if (info.isClustered()) {
      // targeting all hosts including this one.
      if (info.isTargetingAllHosts()) {
        targets.addAll(ctx.getCorus().getContext().getOtherHosts());
        targets.add(ctx.getCorus().getContext().getServerHost());
      } else {
        Map<ServerAddress, CorusHost> hostsByAddress = new HashMap<ServerAddress, CorusHost>();
        for (CorusHost h : ctx.getCorus().getContext().getOtherHosts()) {
          hostsByAddress.put(h.getEndpoint().getServerAddress(), h);
        }
        hostsByAddress.put(ctx.getCorus().getContext().getAddress(), ctx.getCorus().getContext().getServerHost());
        for (ServerAddress t : info.getTargets()) {
          CorusHost h = hostsByAddress.get(t);
          if (h == null) {
            throw new InputException("Targeted host unknown: " + t);
          }
          targets.add(h);
        }
      }
    } else {
      targets.add(ctx.getCorus().getContext().getServerHost());
    }
    
    CorusHost current = ctx.getCorus().getContext().getServerHost();
    
    try {
      for (CorusHost t : targets) {
        ctx.getConsole().println(String.format("Deploying %s to %s", fileName, t.getFormattedAddress()));
        ctx.getCorus().getContext().connect(
            t.getEndpoint().getServerTcpAddress().getHost(), 
            t.getEndpoint().getServerTcpAddress().getPort()
        );
        deployFunc.call(t);
      }
    } finally {
      ctx.getCorus().getContext().connect(
          current.getEndpoint().getServerTcpAddress().getHost(), 
          current.getEndpoint().getServerTcpAddress().getPort()
      );
    }
  
  }
  
  private DeployPreferences assignChecksumPreference(DeployPreferences prefs, CliContext ctx) {
    if (ctx.getCommandLine().containsOption(OPT_CHECKSUM_MD5.getName(), false)) {
      Option checksumOpt = ctx.getCommandLine().getOpt(OPT_CHECKSUM_MD5.getName());
      if (checksumOpt.getValue() == null) {
        prefs.setChecksum(ChecksumPreference.forMd5());
      } else {
        prefs.setChecksum(ChecksumPreference.forMd5().assignClientChecksum(checksumOpt.getValue()));
      }
    }
    return prefs;
  }

}
