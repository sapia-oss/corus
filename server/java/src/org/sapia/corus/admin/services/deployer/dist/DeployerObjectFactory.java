package org.sapia.corus.admin.services.deployer.dist;

import java.util.HashMap;
import java.util.Map;

import org.sapia.util.xml.confix.CreationStatus;
import org.sapia.util.xml.confix.ObjectCreationException;
import org.sapia.util.xml.confix.ReflectionFactory;


/**
 * Implements the Confix object factory that creates the objects pertaining
 * to the content of the corus.xml file.
 * 
 * @author Yanick Duchesne
 *
 */
public class DeployerObjectFactory extends ReflectionFactory {
  private Map<String, Class<?>> _localNamesToClasses = new HashMap<String, Class<?>>();

  public DeployerObjectFactory() {
    super(new String[0]);
    register("distribution", Distribution.class);
    register("java", Java.class);
    register("magnet", Magnet.class);
    register("option", Option.class);
    register("property", Property.class);
    register("xoption", XOption.class);
    register("process", ProcessConfig.class);
  }

  void register(String localName, Class<?> clazz) {
    _localNamesToClasses.put(localName, clazz);
  }

  public CreationStatus newObjectFor(String prefix, String uri,
                                     String localName, Object parent)
                              throws ObjectCreationException {
    Class<?> clazz = _localNamesToClasses.get(localName);

    if (clazz == null) {
      return super.newObjectFor(prefix, uri, localName, parent);
    }

    try {
      return CreationStatus.create(clazz.newInstance());
    } catch (IllegalAccessException e) {
      throw new ObjectCreationException("Could not instantiate " + clazz +
                                        "; not accessible");
    } catch (InstantiationException e) {
      throw new ObjectCreationException("Could not instantiate " + clazz,
                                        e.getCause());
    }
  }
}
