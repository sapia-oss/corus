package org.sapia.corus.client.services.deployer.dist;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import org.sapia.util.xml.confix.ConfigurationException;
import org.sapia.util.xml.confix.ObjectHandlerIF;

/**
 * Holds the {@link ProcessPubConfig}s for a given {@link ProcessConfig}.
 * 
 * @author yduchesne
 *
 */
public class PublishingConfig implements ObjectHandlerIF, Externalizable {
  
  private List<ProcessPubConfig> configs = new ArrayList<ProcessPubConfig>();
  
  public List<ProcessPubConfig> getConfigs() {
    return configs;
  }
  
  @Override
  public void handleObject(String name, Object obj) throws ConfigurationException {
    ConfigAssertions.elementExpectsInstanceOf("publishing", ProcessPubConfig.class, obj);
    configs.add((ProcessPubConfig) obj);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    configs = (List<ProcessPubConfig>) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(configs);
  }

}
