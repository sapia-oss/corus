package org.sapia.corus.publisher;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.tuple.PairTuple;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.Port;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.dist.ProcessPubConfig;
import org.sapia.corus.client.services.deployer.dist.PublishingConfig;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.pub.ProcessPubContext;
import org.sapia.corus.client.services.pub.ProcessPublisher;
import org.sapia.corus.client.services.pub.PublishingCallback;
import org.sapia.corus.client.services.pub.UnpublishingCallback;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Func;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of the {@link ProcessPublisher} interface.
 * 
 * @author yduchesne
 *
 */
@Bind(moduleInterface = ProcessPublisher.class)
@Remote(interfaces = ProcessPublisher.class)
public class ProcessPublisherImpl extends ModuleHelper implements ProcessPublisher {
 
  @Autowired
  private Deployer deployer;
 
  private List<ProcessPublishingProvider> publishers = new ArrayList<ProcessPublishingProvider>();
  
  // --------------------------------------------------------------------------
  // Visible for testing
  
  void setDeployer(Deployer deployer) {
    this.deployer = deployer;
  }
  
  void setPublishers(List<ProcessPublishingProvider> publishers) {
    this.publishers = publishers;
  }

  // --------------------------------------------------------------------------
  // Module interface
  
  @Override
  public String getRoleName() {
    return ProcessPublisher.ROLE;
  }
  
  @Override
  public void init() throws Exception {
    for (ProcessPublishingProvider provider : env().getBeansOfType(ProcessPublishingProvider.class).values()) {
      log.info("Adding publishing provider: " + provider);
      publishers.add(provider);
    }
  }
  
  @Override
  public void dispose() throws Exception {
  }
  
  // --------------------------------------------------------------------------
  // ProcessPublisher interface
  
  @Override
  public void publishProcess(Process process, final PublishingCallback callback) {
    if (process.getActivePorts().isEmpty()) {
      callback.publishingNotApplicable(process);
    } else {
      handlePublisher(
        process, 
        new Func<Void, PairTuple<ProcessPubContext, ProcessPublishingProvider>>() {
          @Override
          public Void call(PairTuple<ProcessPubContext, ProcessPublishingProvider> providerCtx) {
            providerCtx.getRight().publish(providerCtx.getLeft(), callback);
            return null;
          }
        }, 
        new Func<Void, Process>() {
          @Override
          public Void call(Process proc) {
            callback.publishingNotApplicable(proc);
            return null;
          }
        }
      );
    }
  }
  
  @Override
  public void unpublishProcess(Process process, final UnpublishingCallback callback) {
    if (process.getActivePorts().isEmpty()) {
      callback.unpublishingNotApplicable(process);
    } else {
      handlePublisher(
        process, 
        new Func<Void, PairTuple<ProcessPubContext, ProcessPublishingProvider>>() {
          @Override
          public Void call(PairTuple<ProcessPubContext, ProcessPublishingProvider> providerCtx) {
            providerCtx.getRight().unpublish(providerCtx.getLeft(), callback);
            return null;
          }
        }, 
        new Func<Void, Process>() {
          @Override
          public Void call(Process proc) {
            callback.unpublishingNotApplicable(proc);
            return null;
          }
        }
      );
    }    
  }
  
  // --------------------------------------------------------------------------
  // Restricted methods

  private void handlePublisher(
      Process process, 
      Func<Void, PairTuple<ProcessPubContext, ProcessPublishingProvider>> handler, 
      Func<Void, Process> notApplicableHandler) {
    try {
      Distribution  dist        = deployer.getDistribution(process.getDistributionInfo().newDistributionCriteria());
      ProcessConfig processConf = dist.getProcesses(ArgMatchers.parse(process.getDistributionInfo().getProcessName())).get(0);
      for (ActivePort ap: process.getActivePorts()) {
        OptionalValue<Port> port = processConf.getPortByName(ap.getName());
        Assertions.illegalState(port.isNull(), "No port range found for: %s", ap.getName());
        if (port.get().getPublishing().isSet()) {
          PublishingConfig p = port.get().getPublishing().get();
          for (ProcessPubConfig ppc : p.getConfigs()) {
            ProcessPubContext ctx = new ProcessPubContext(process, dist, processConf, ap, ppc);
            OptionalValue<ProcessPublishingProvider> provider = selectPubProvider(ppc);
            if (provider.isSet()) {
              handler.call(new PairTuple<ProcessPubContext, ProcessPublishingProvider>(ctx, provider.get()));
            } else {
              log.warn(String.format("No provider found for publishing config: %s. Publishing will not occur for that configuration", ppc));
              notApplicableHandler.call(process);
            }
          }
        } else {
          notApplicableHandler.call(process);
        }
      }
    } catch (DistributionNotFoundException e) {
      throw new IllegalStateException("Distribution was not found for process: " + ToStringUtil.toString(process), e);
    }    
  }
  
  private OptionalValue<ProcessPublishingProvider> selectPubProvider(ProcessPubConfig conf) {
    OptionalValue<ProcessPublishingProvider> toReturn = OptionalValue.none();
    for (ProcessPublishingProvider p : publishers) {
      if (p.accepts(conf)) {
        toReturn = OptionalValue.of(p);
        break;
      }
    }
    return toReturn;
  }
}
