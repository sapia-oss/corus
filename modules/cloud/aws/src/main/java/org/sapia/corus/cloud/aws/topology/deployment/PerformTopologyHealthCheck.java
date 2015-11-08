package org.sapia.corus.cloud.aws.topology.deployment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.sapia.corus.cloud.aws.client.InstanceStatusCode;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;
import org.sapia.corus.cloud.topology.Cluster;
import org.sapia.corus.cloud.topology.Env;
import org.sapia.corus.cloud.topology.Region;
import org.sapia.corus.cloud.topology.Topology;
import org.sapia.corus.cloud.topology.Zone;

import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;

/**
 * Performs topology diagnostic.
 * 
 * @author yduchesne
 *
 */
public class PerformTopologyHealthCheck implements WorkflowStep<AwsTopologyDeploymentContext> {
  
  private static final String DESC = "performing topology diagnostic";
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  public void execute(AwsTopologyDeploymentContext context) throws Exception {
    Topology topology = context.getSettings().getNotNull("topology").get(Topology.class);
    String   envName  = context.getSettings().getNotNull("environment").get(String.class);
    Env      env      = topology.getEnvByName(envName);
   
    for (Region region : env.getRegions()) {
      for (Zone zone : region.getZones()) {
        for (Cluster cluster : env.getClusters()) {
          List<Filter> filters = new ArrayList<Filter>();
          filters.add(new Filter("corus.environment", Arrays.asList(env.getName())));
          filters.add(new Filter("corus.region", Arrays.asList(region.getName())));
          filters.add(new Filter("corus.zone", Arrays.asList(zone.getName())));
          filters.add(new Filter("corus.domain", Arrays.asList(cluster.getName())));
          filters.add(new Filter("corus.topology.org", Arrays.asList(topology.getOrg())));
          filters.add(new Filter("corus.topology.application", Arrays.asList(topology.getApplication())));
          context.getLog().info(
              "Will perform diagnostic on cluster % in region/zone %s/%s of environment %s", 
              cluster.getName(), region.getName(), zone.getName(), env.getName()
          );
          doPerformClusterDiagnostic(context, filters);
        }
      }
    }
  }
  
  private void doPerformClusterDiagnostic(AwsTopologyDeploymentContext context, List<Filter> filters) throws Exception {

    DescribeInstancesResult descRes = context.getEc2Client().describeInstances(
        new DescribeInstancesRequest().withFilters(filters)
    );
    
    List<Instance> instances = new ArrayList<Instance>();
    
    for (Reservation reservation : descRes.getReservations()) {
      for (Instance instance : reservation.getInstances()) {
        if (instance.getState().getCode().equals(InstanceStatusCode.RUNNING.value())) {
          instances.add(instance);
        } 
      }
    }
   
    Preconditions.checkState(!instances.isEmpty(), "No instance(s) found running");
    
    context.getLog().info("Attempting to find Corus host to connect to in order to perform diagnostic");

    Collection<Instance> corusHosts = filterPublicRepoServerInstances(instances);
    
    if (corusHosts.isEmpty()) {
      corusHosts = filterPublicInstances(instances);
    }
    if (corusHosts.isEmpty()) {
      context.getLog().warning(
          "Did not find instance with public IP to connect to (your topology has " 
          + "no <machine> element with the publicIpEnabled attribute set to true). " 
          + "Will resort to try to connect to instance with private IP. "
          + "This will fail if your network setup does not support such connectivity with AWS.");
      corusHosts = filterRepoServerInstances(instances);
    }
    if (corusHosts.isEmpty()) {
      corusHosts = instances;
    }

    Instance toCheck = instances.get(0);
    String address = 
        Strings.isNullOrEmpty(toCheck.getPublicIpAddress()) 
        ? toCheck.getPrivateIpAddress() 
        : toCheck.getPublicIpAddress();
    int corusPort = context.getSettings().getNotNull("corusPort").get(Integer.class);
    String corusUrl = "https://" + address + ":" + corusPort + "/rest";
        
    do {
      
    } while (true);
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  private Collection<Instance> filterPublicRepoServerInstances(List<Instance> instances) {
    return Collections2.filter(instances, new Predicate<Instance>() {
      @Override
      public boolean apply(Instance input) {
        boolean repoMatch = false;
        for (Tag t : input.getTags()) {
          if (t.getValue().equals("corus.repo.role") && t.getValue().equals("server")) {
            repoMatch = true;
          }
        }
        return repoMatch && !Strings.isNullOrEmpty(input.getPublicIpAddress());
      }
    });    
  }
  
  private Collection<Instance> filterRepoServerInstances(List<Instance> instances) {
    return Collections2.filter(instances, new Predicate<Instance>() {
      @Override
      public boolean apply(Instance input) {
        boolean repoMatch = false;
        for (Tag t : input.getTags()) {
          if (t.getValue().equals("corus.repo.role") && t.getValue().equals("server")) {
            repoMatch = true;
          }
        }
        return repoMatch;
      }
    });    
  }
  
  private Collection<Instance> filterPublicInstances(List<Instance> instances) {
    return Collections2.filter(instances, new Predicate<Instance>() {
      @Override
      public boolean apply(Instance input) {
        return !Strings.isNullOrEmpty(input.getPublicIpAddress());
      }
    });    
  }
  
}
