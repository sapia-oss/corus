package org.sapia.corus.port;

import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.database.InMemoryArchiver;
import org.sapia.corus.database.InMemoryDbMap;
import org.sapia.ubik.util.Func;

/**
 *
 * @author yduchesne
 */
public class TestPortManager extends PortManagerImpl {
  
  public TestPortManager(Processor processor) {
    super(new PortRangeStore(new InMemoryDbMap<String, PortRangeDefinition>(new ClassDescriptor<PortRangeDefinition>(PortRangeDefinition.class),
      new InMemoryArchiver<String, PortRangeDefinition>(),
      new Func<PortRangeDefinition, JsonInput>() {
        public PortRangeDefinition call(JsonInput arg0) {
          throw new UnsupportedOperationException();
        }
    })), processor);
  }

}
