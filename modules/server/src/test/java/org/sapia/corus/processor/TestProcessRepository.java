package org.sapia.corus.processor;

import org.sapia.corus.client.services.db.persistence.ClassDescriptor;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.db.CachingDbMap;
import org.sapia.corus.db.HashDbMap;


/**
 * @author Yanick Duchesne
 */
public class TestProcessRepository extends ProcessRepositoryImpl {
  public TestProcessRepository() {
    super(new ProcessDatabaseImpl(new CachingDbMap<String, Process>(new HashDbMap<String, Process>(new ClassDescriptor<Process>(Process.class)))), 
          new ProcessDatabaseImpl(new CachingDbMap<String, Process>(new HashDbMap<String, Process>(new ClassDescriptor<Process>(Process.class)))),
          new ProcessDatabaseImpl(new CachingDbMap<String, Process>(new HashDbMap<String, Process>(new ClassDescriptor<Process>(Process.class)))));
  }
}
