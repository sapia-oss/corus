package org.sapia.corus.client.services.db.persistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.sapia.corus.client.annotations.Transient;
import org.sapia.corus.client.annotations.Version;

/**
 * An instance of this class holds metadata about a persistent class.
 * 
 * @author yduchesne
 * 
 */
public class ClassDescriptor<T> {

  private static final String GET_PREFIX = "get";
  private static final String IS_PREFIX = "is";

  private Class<T> type;
  private Constructor<T> constructor;
  private Map<String, FieldDescriptor> fieldsByName = new HashMap<String, FieldDescriptor>();
  private List<FieldDescriptor> fieldsByIndex = new ArrayList<FieldDescriptor>();

  /**
   * Creates an instance of this class.
   * 
   * @param type
   *          the persistent class.
   */
  public ClassDescriptor(Class<T> type) {
    this.type = type;
    analyze();
  }

  /**
   * @param name
   *          the name of the {@link FieldDescriptor} to return.
   * @return the {@link FieldDescriptor} corresponding to the given name.
   * @throws NoSuchFieldException
   *           if no matching field was found.
   */
  public FieldDescriptor getFieldForName(String name) throws NoSuchFieldException {
    FieldDescriptor fd = fieldsByName.get(name);
    if (fd == null) {
      throw new NoSuchFieldException(String.format("No field found for: %s", name));
    }
    return fd;
  }

  /**
   * @param index
   *          the index of the {@link FieldDescriptor} to return.
   * @return the {@link FieldDescriptor} corresponding to the given index.
   * @throws NoSuchFieldException
   *           if no matching field was found.
   */
  public FieldDescriptor getFieldForIndex(int index) {
    if (index < 0 || index >= fieldsByIndex.size()) {
      throw new NoSuchFieldException(String.format("Invalid field index for: %s", index));
    }
    return fieldsByIndex.get(index);
  }

  /**
   * @return the number of fields held by this instance.
   */
  public int getFieldCount() {
    return fieldsByIndex.size();
  }

  /**
   * Returns a copy of this instance's field descriptor list.
   * 
   * @return return the {@link FieldDescriptor}s that this instance holds.
   */
  public Collection<FieldDescriptor> getFields() {
    return new ArrayList<FieldDescriptor>(fieldsByIndex);
  }

  /**
   * @return the {@link Class} that this instance describes.
   */
  public Class<T> getType() {
    return type;
  }

  /**
   * @return creates a new instance of this descriptor's class.
   */
  public T newInstance() {
    try {
      return constructor.newInstance(new Object[0]);
    } catch (Exception e) {
      throw new ConstructorAccessException(String.format("Could not create instance of %s", type.getName()), e);
    }
  }

  public String toString() {
    return new ToStringBuilder(this).append("type", type).append("fields", fieldsByName).toString();
  }

  private void analyze() {

    try {
      constructor = type.getDeclaredConstructor(new Class<?>[0]);
      constructor.setAccessible(true);
    } catch (Exception e) {
      throw new ConstructorAccessException(String.format("No-args constructor access problem for %s (make sure class has one)", type.getName()), e);
    }

    Set<AccessorInfo> accessors = new TreeSet<AccessorInfo>();
    Set<AccessorInfo> transientAccessors = new TreeSet<AccessorInfo>();
    for (Method m : type.getMethods()) {
      if (m.getParameterTypes().length == 0 && !m.getReturnType().equals(void.class) && !m.getDeclaringClass().equals(Object.class)) {
        if (m.getName().startsWith(GET_PREFIX)) {
          AccessorInfo ai = new AccessorInfo();
          ai.accessor = m;
          ai.name = m.getName().substring(GET_PREFIX.length());
          if (isTransient(m)) {
            transientAccessors.add(ai);
          } else if (!transientAccessors.contains(ai)) {
            accessors.add(ai);
            ai.isVersion = isVersion(m);
          }
        } else if (m.getName().startsWith(IS_PREFIX)) {
          AccessorInfo ai = new AccessorInfo();
          ai.accessor = m;
          ai.name = m.getName().substring(IS_PREFIX.length());
          if (isTransient(m)) {
            transientAccessors.add(ai);
          } else if (!transientAccessors.contains(ai)) {
            accessors.add(ai);
            ai.isVersion = isVersion(m);
          }
        } else {
          continue;
        }
      }
    }

    List<AccessorInfo> sortedAccessors = new ArrayList<AccessorInfo>(accessors);

    Collections.sort(sortedAccessors);

    int i = 0;
    for (AccessorInfo ai : accessors) {
      if (ai.accessor.getName().startsWith(GET_PREFIX)) {
        analyzeMethod(i, ai);
        i++;
      } else if (ai.accessor.getName().startsWith(IS_PREFIX)) {
        analyzeMethod(i, ai);
        i++;
      }
    }
  }

  private void analyzeMethod(int index, AccessorInfo info) {
    String methodName = info.name;
    String fieldName = toLowerCaseFirst(methodName);
    FieldDescriptor fd = null;
    try {
      Method mutator = type.getMethod("set" + methodName, new Class[] { info.accessor.getReturnType() });
      fd = new FieldDescriptor(index, fieldName, info.accessor, new FieldDescriptor.MethodMutator(mutator));
    } catch (Exception e) {
      try {
        Field mutator = type.getDeclaredField(fieldName);
        fd = new FieldDescriptor(index, fieldName, info.accessor, new FieldDescriptor.FieldMutator(mutator));
      } catch (Exception e2) {
        try {
          Field mutator = type.getDeclaredField("_" + fieldName);
          fd = new FieldDescriptor(index, fieldName, info.accessor, new FieldDescriptor.FieldMutator(mutator));
        } catch (Exception e3) {
          throw new FieldAccessException(String.format("Could not determine mutator for field %s in class %s", fieldName, type.getName()), e3);
        }
      }
    }
    fd.setVersion(info.isVersion);
    fieldsByName.put(fieldName, fd);
    fieldsByIndex.add(fd);
  }

  private String toLowerCaseFirst(String s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (i == 0) {
        sb.append(Character.toLowerCase(c));
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  static class AccessorInfo implements Comparable<AccessorInfo> {
    String name;
    Method accessor;
    boolean isVersion;

    @Override
    public int compareTo(AccessorInfo o) {
      return name.compareTo(o.name);
    }
  }

  private boolean isTransient(Method m) {
    return hasAnnotation(Transient.class, m);
  }

  private boolean isVersion(Method m) {
    if (hasAnnotation(Version.class, m)) {
      if (!m.getReturnType().equals(long.class) && !m.getReturnType().equals(Long.class)) {
        throw new IllegalStateException(String.format("Version fields must have type long: method %s has type %s", m.getName(), m.getReturnType()
            .getName()));
      }
      return true;
    }
    return false;
  }

  private <A extends Annotation> boolean hasAnnotation(Class<A> annotationClass, Method m) {
    A result = m.getAnnotation(annotationClass);
    if (result != null) {
      return true;
    } else {
      for (Object element : ClassUtils.getAllInterfaces(m.getDeclaringClass())) {
        Class<?> intf = (Class<?>) element;
        if (hasAnnotationInClass(annotationClass, m, intf)) {
          return true;
        }
      }
      for (Object element : ClassUtils.getAllSuperclasses(m.getDeclaringClass())) {
        Class<?> clazz = (Class<?>) element;
        if (hasAnnotationInClass(annotationClass, m, clazz)) {
          return true;
        }
      }
      return false;
    }
  }

  private <A extends Annotation> boolean hasAnnotationInClass(Class<A> annotationClass, Method m, Class<?> clazz) {
    try {
      m = clazz.getDeclaredMethod(m.getName(), m.getParameterTypes());
      if (m.isAnnotationPresent(annotationClass)) {
        return true;
      }
    } catch (NoSuchMethodException e) {
    }
    return false;
  }
}
