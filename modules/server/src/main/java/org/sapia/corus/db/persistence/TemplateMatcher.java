package org.sapia.corus.db.persistence;

import org.sapia.corus.client.services.db.RecordMatcher;
import org.sapia.corus.client.services.db.persistence.Record;

/**
 * This class implements the {@link RecordMatcher} interface over the
 * {@link Template} class.
 * 
 * @author yduchesne
 * 
 */
public class TemplateMatcher<T> implements RecordMatcher<T> {

  private Template<T> template;

  public TemplateMatcher(Template<T> template) {
    this.template = template;
  }

  /**
   * This method delegates matching to its internal {@link Template}.
   * 
   * @see Template
   */
  @Override
  public boolean matches(Record<T> rec) {
    return template.matches(rec);
  }
}
