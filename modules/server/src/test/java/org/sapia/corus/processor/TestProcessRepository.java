package org.sapia.corus.processor;

import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.database.CachingDbMap;
import org.sapia.corus.database.InMemoryDbMap;
import org.sapia.ubik.util.Func;

public class TestProcessRepository extends ProcessRepositoryImpl {
  public TestProcessRepository() {
    super(
        new ProcessDatabaseImpl(
            new CachingDbMap<String, Process>(
                new InMemoryDbMap<String, Process>(new ClassDescriptor<Process>(Process.class), new Func<Process, JsonInput>() {
                  public Process call(JsonInput arg0) {
                    throw new UnsupportedOperationException();
                  }
                })
            )
        ) 
    );
  }
}
