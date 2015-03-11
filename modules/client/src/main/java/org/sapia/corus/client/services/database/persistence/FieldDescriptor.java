package org.sapia.corus.client.services.database.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Holds metadata about a given field/property of a class.
 * 
 * @author yduchesne
 * 
 */
public class FieldDescriptor {

  static final Object[] EMPTY_ARGS = new Object[] {};

  private int index;
  private String name;
  private Method accessor;
  private Mutator mutator;
  private boolean isVersion;

  /**
   * 
   * @param index
   *          the index of this instance in it's parent {@link ClassDescriptor}
   * @param name
   *          the name of the field descriptor.
   * @param accessor
   *          the accessor {@link Method} of the corresponding field/property.
   * @param mutator
   *          the {@link Mutator} of the corresponding field/property.
   */
  FieldDescriptor(int index, String name, Method accessor, Mutator mutator) {
    this.name = name;
    this.accessor = accessor;
    this.index = index;
    this.mutator = mutator;
  }

  /**
   * @return this instance's name.
   */
  public String getName() {
    return name;
  }

  /**
   * @param isVersion
   *          if <code>true</code>, indicates that this descriptor corresponds
   *          to a version field.
   */
  public void setVersion(boolean isVersion) {
    this.isVersion = isVersion;
  }

  /**
   * @return <code>true</code> if this instance corresponds to a version field.
   */
  public boolean isVersion() {
    return isVersion;
  }

  /**
   * @return this instance's index in its parent's {@link ClassDescriptor}
   */
  public int getIndex() {
    return index;
  }

  /**
   * @return the accessor {@link Method} corresponding to this instance.
   */
  public Method getAccessor() {
    return accessor;
  }

  /**
   * @param instance
   *          the instance on which to perform the invocation.
   * @return the value returned by the accessor's invocation.
   * @throws FieldAccessException
   */
  public Object invokeAccessor(Object instance) throws FieldAccessException {
    try {
      return accessor.invoke(instance, EMPTY_ARGS);
    } catch (InvocationTargetException e) {
      throw new FieldAccessException(String.format("Could not access field %s", name), e.getTargetException());
    } catch (Exception e) {
      throw new FieldAccessException(String.format("Could not access field %s", name), e);
    }
  }

  /**
   * @param instance
   *          the instance on which to perform the invocation.
   * @param value
   *          the value with which to do the update.
   * @throws FieldAccessException
   */
  public void invokeMutator(Object instance, Object value) throws FieldAccessException {
    mutator.invoke(instance, value);
  }

  // ///////////////////////// INNER CLASSES ///////////////////////////

  public interface Mutator {
    public void invoke(Object instance, Object value);
  }

  public static class FieldMutator implements Mutator {

    private Field field;

    FieldMutator(Field field) {
      this.field = field;
      field.setAccessible(true);
    }

    @Override
    public void invoke(Object instance, Object value) {
      try {
        field.set(instance, value);
      } catch (Exception e) {
        throw new FieldAccessException(String.format("Could not modify field %s", field.getName()), e);
      }
    }
  }

  public static class MethodMutator implements Mutator {

    private Method method;

    MethodMutator(Method method) {
      this.method = method;
    }

    @Override
    public void invoke(Object instance, Object value) {
      try {
        method.invoke(instance, value);
      } catch (InvocationTargetException e) {
        throw new FieldAccessException(String.format("Could not invoke method %s", method.getName()), e.getTargetException());
      } catch (Exception e) {
        throw new FieldAccessException(String.format("Could not invoke method %s", method.getName()), e);
      }

    }
  }

}
