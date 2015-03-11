package org.sapia.corus.database.persistence;

import org.sapia.corus.client.services.database.PatternRecordMatcher;
import org.sapia.corus.client.services.database.PatternRecordMatcher.FieldMatcher;
import org.sapia.corus.client.services.database.persistence.ClassDescriptor;
import org.sapia.corus.client.services.database.persistence.FieldDescriptor;
import org.sapia.corus.client.services.database.persistence.Record;

/**
 * An instance of this class is used to perform matching operations dynamically,
 * in a query-by-example fashion.
 * 
 * @author yduchesne
 * 
 */
public class Template<T> {

  private ClassDescriptor<T> descriptor;
  private Object instance;
  private PatternRecordMatcher<T> matcher;

  /**
   * @param desc
   *          the {@link ClassDescriptor} that provides meta data about the
   *          given object.
   * @param instance
   *          the {@link Object} to use to build this template.
   */
  public Template(ClassDescriptor<T> desc, Object instance) {
    this.descriptor = desc;
    this.instance = instance;
    analyze();
  }

  /**
   * @param record
   *          a {@link Record} to match.
   * @return <code>true</code> if this instance's state matches the given
   *         record.
   */
  public boolean matches(Record<T> record) {
    return matcher.matches(record);
  }

  /**
   * @param o
   *          an {@link Object} to match.
   * @return <code>true</code> if this instance's state matches the given
   *         object.
   */
  public boolean matches(Object o) {
    return matcher.matches(o);
  }

  private void analyze() {
    PatternRecordMatcher<T> matcher = new PatternRecordMatcher<T>(this.descriptor);
    for (FieldDescriptor fd : descriptor.getFields()) {
      Object value = fd.invokeAccessor(instance);
      FieldMatcher fm = new FieldMatcherImpl(value);
      matcher.add(fd.getName(), fm);
    }
    this.matcher = matcher;
  }

  private class FieldMatcherImpl implements FieldMatcher {

    private Object criterion;

    public FieldMatcherImpl(Object criterion) {
      this.criterion = criterion;
    }

    @Override
    public boolean matches(Object value) {
      if (criterion == null) {
        return true;
      } else if (value == null) {
        return false;
      } else
        return criterion.equals(value);
    }

  }
}
