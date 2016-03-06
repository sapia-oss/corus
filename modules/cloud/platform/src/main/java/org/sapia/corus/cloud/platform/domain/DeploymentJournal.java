package org.sapia.corus.cloud.platform.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Logs {@link DeploymentJournalEntry} instances. As a whole, an instance provides high-level
 * information about deployment outcome, on one or more Corus clusters in an environment.
 * 
 * @author yduchesne
 *
 */
public class DeploymentJournal {
  
  private List<DeploymentJournalEntry> entries = new ArrayList<>();
  
  /**
   * @return the unmodifiable {@link List} of {@link DeploymentJournalEntry} instances
   * that this instance holds.
   */
  public List<DeploymentJournalEntry> getEntries() {
    return Collections.unmodifiableList(entries);
  }
  
  /**
   * @param entry a {@link DeploymentJournalEntry}.
   */
  public void addEntry(DeploymentJournalEntry entry) {
    entries.add(entry);
  }

}
