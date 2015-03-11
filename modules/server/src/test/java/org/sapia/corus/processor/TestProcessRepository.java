package org.sapia.corus.processor;

import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.database.CachingDbMap;
import org.sapia.corus.database.InMemoryDbMap;

public class TestProcessRepository extends ProcessRepositoryImpl {
  public TestProcessRepository() {
    super(
        new ProcessDatabaseImpl(
            new CachingDbMap<String, Process>(
                new InMemoryDbMap<String, Process>(new ClassDescriptor<Process>(Process.class))
            )
        ) 
    );
  }
}
