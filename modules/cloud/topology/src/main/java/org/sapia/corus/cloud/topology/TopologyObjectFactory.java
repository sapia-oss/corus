package org.sapia.corus.cloud.topology;

import java.util.HashMap;
import java.util.Map;


import org.sapia.util.xml.confix.CreationStatus;
import org.sapia.util.xml.confix.ObjectCreationException;
import org.sapia.util.xml.confix.ReflectionFactory;

/**
 * The object factory used for resolving topology element names to their corresponding Java class.
 * 
 * @author yduchesne
 *
 */
public class TopologyObjectFactory extends ReflectionFactory {
  private Map<String, Class<?>> localNamesToClasses = new HashMap<String, Class<?>>();

  public TopologyObjectFactory() {
    super(new String[0]);
    register("topology", Topology.class);
    register("env-template", EnvTemplate.class);
    register("env", Env.class);
    register("region-template", RegionTemplate.class);
    register("region", Region.class);
    register("zone", Zone.class);
    register("subnet", Subnet.class);
    register("cluster-template", ClusterTemplate.class);
    register("cluster", Cluster.class);
    register("machine-template", MachineTemplate.class);
    register("machine", Machine.class);
    register("load-balancer-attachment", LoadBalancerAttachment.class);
    register("user-data", UserData.class);
    register("artifact", Artifact.class);
    register("property", Property.class);
    register("param", Param.class);
    register("serverTag", ServerTag.class);
  }

  void register(String localName, Class<?> clazz) {
    localNamesToClasses.put(localName, clazz);
  }

  public CreationStatus newObjectFor(String prefix, String uri, String localName, Object parent) throws ObjectCreationException {
    Class<?> clazz = localNamesToClasses.get(localName);

    if (clazz == null) {
      return super.newObjectFor(prefix, uri, localName, parent);
    }

    try {
      return CreationStatus.create(clazz.newInstance());
    } catch (IllegalAccessException e) {
      throw new ObjectCreationException("Could not instantiate " + clazz + "; not accessible");
    } catch (InstantiationException e) {
      throw new ObjectCreationException("Could not instantiate " + clazz, e.getCause());
    }
  }
}