package org.sapia.corus.processor;

import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.db.HashDbMap;


/**
 * @author Yanick Duchesne
 */
public class TestProcessRepository extends ProcessRepositoryImpl {
  public TestProcessRepository() {
    super(new ProcessDatabaseImpl(new HashDbMap<String, Process>()), 
          new ProcessDatabaseImpl(new HashDbMap<String, Process>()),
          new ProcessDatabaseImpl(new HashDbMap<String, Process>()));
  }
}
