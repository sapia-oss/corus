package org.sapia.corus.deployer.config;

import org.sapia.util.xml.confix.CreationStatus;
import org.sapia.util.xml.confix.ObjectCreationException;
import org.sapia.util.xml.confix.ReflectionFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * Implements the Confix object factory that creates the objects pertaining
 * to the content of the corus.xml file.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DeployerObjectFactory extends ReflectionFactory {
  Map _localNamesToClasses = new HashMap();

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

  void register(String localName, Class clazz) {
    _localNamesToClasses.put(localName, clazz);
  }

  public CreationStatus newObjectFor(String prefix, String uri,
                                     String localName, Object parent)
                              throws ObjectCreationException {
    Class clazz = (Class) _localNamesToClasses.get(localName);

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
