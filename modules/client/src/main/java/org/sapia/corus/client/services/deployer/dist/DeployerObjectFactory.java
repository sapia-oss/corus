package org.sapia.corus.client.services.deployer.dist;

import java.util.HashMap;
import java.util.Map;

import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter;
import org.sapia.util.xml.confix.CreationStatus;
import org.sapia.util.xml.confix.ObjectCreationException;
import org.sapia.util.xml.confix.ReflectionFactory;

/**
 * Implements the Confix object factory that creates the objects pertaining to
 * the content of the corus.xml file.
 * 
 * @author Yanick Duchesne
 * 
 */
public class DeployerObjectFactory extends ReflectionFactory {
  private Map<String, Class<?>> localNamesToClasses = new HashMap<String, Class<?>>();

  public DeployerObjectFactory() {
    super(new String[0]);
    register("distribution", Distribution.class);
    register("java", Java.class);
    register("magnet", Magnet.class);
    register("generic", Generic.class);
    register("docker", DockerStarter.class);
    register("option", Option.class);
    register(VmArg.ELEMENT_NAME, VmArg.class);
    register("property", Property.class);
    register("xoption", XOption.class);
    register("process", ProcessConfig.class);
    register("preExec", PreExec.class);
    register("cmd", Cmd.class);
    register(HttpsDiagnosticConfig.ELEMENT_NAME, HttpsDiagnosticConfig.class);
    register(HttpDiagnosticConfig.ELEMENT_NAME, HttpDiagnosticConfig.class);
    register(AwsElbPublisherConfig.ELEMENT_NAME, AwsElbPublisherConfig.class);
    register(ConsulPublisherConfig.ELEMENT_NAME, ConsulPublisherConfig.class);
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
