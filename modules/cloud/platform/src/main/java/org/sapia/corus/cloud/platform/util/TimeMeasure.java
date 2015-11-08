package org.sapia.corus.cloud.platform.util;

import java.util.concurrent.TimeUnit;

/**
 * Encapsulates an time value, and the {@link TimeUnit} in which it is expressed.
 * 
 * @author yduchesne
 *
 */
public class TimeMeasure implements Comparable<TimeMeasure> {

  private TimeUnit     unit;
  private long         value;
  private TimeSupplier supplier = TimeSupplier.SystemTime.getInstance();
 
  /**
   * @param unit the {@link TimeUnit} in which the given time value is expressed.
   * @param value a time value.
   */
  public TimeMeasure(TimeUnit unit, long value) {
    this.unit  = unit;
    this.value = value;
  }
  
  /**
   * @return this instance's {@link TimeUnit}.
   */
  public TimeUnit getUnit() {
    return unit;
  }
  
  /**
   * @return this instance's value.
   */
  public long getValue() {
    return value;
  }
  
  /**
   * @return this instance's value, in millis.
   */
  public long getMillis() {
    return unit.toMillis(value);
  }
  
  /**
   * @return this instance {@link TimeSupplier}.
   */
  public TimeSupplier getSupplier() {
    return supplier;
  }
  
  /**
   * @param other another {@link TimeMeasure} instance.
   * @return a new {@link TimeMeasure} instance corresponding to the difference between this instance's 
   * time value and the one of the instance passed in (returns a positive value of this time is 
   * larger than the one given, a negative value if it is smaller, and 0 if it is the same).
   */
  public TimeMeasure diff(TimeMeasure other) {
    return new TimeMeasure(TimeUnit.MILLISECONDS, getMillis() - other.getMillis());
  }
  
  /**
   * 
   * @param unit the {@link TimeUnit} that the returned {@link TimeMeasure} should be in (and which should be used
   * to perform this method's time difference computation).
   * @return as a {@link TimeMeasure} the time difference (in the given time unit) between the current time 
   * and this instance (this instance is assumed to be a "start" time).
   */
  public TimeMeasure elapsed(TimeUnit unit) {
    return new TimeMeasure(unit, forCurrentTime(supplier).convertTo(unit).getValue() - this.convertTo(unit).getValue());
  }
  
  /**
   * @param a {@link TimeUnit} to convert to.
   * @return a new {@link TimeMeasure} expressed in the given unit, or this instance if the given unit is the
   * same has this instance's.
   */
  public TimeMeasure convertTo(TimeUnit newUnit) {
    if (unit == newUnit) {
      return this;
    }
    return new TimeMeasure(newUnit, newUnit.convert(value, unit));
  }
  
  /**
   * Assumes this instance to consist of a start time. Returns a duration, taking the current time as an end time.
   * <p>
   * @returns {@link TimeMeasure} corresponding to a duration, in milliseconds.
   */
  public TimeMeasure elapsedMillis() {
    long end  = supplier.currentTimeMillis();
    long diff = unit == TimeUnit.MILLISECONDS ? end - value : end - getMillis(); 
    return TimeMeasure.forValue(TimeUnit.MILLISECONDS, diff);
  }
  
  /**
   * Assumes this instance to consist of a start time. Returns a duration, taking the current time as an end time.
   * <p>
   * @returns {@link TimeMeasure} corresponding to a duration, in nanoseconds.
   */
  public TimeMeasure elapsedNanos() {
    long end  = supplier.currentTimeNanos();
    long diff = unit == TimeUnit.NANOSECONDS ? end - value : end - TimeUnit.NANOSECONDS.convert(value, unit); 
    return TimeMeasure.forValue(unit, unit.convert(diff, unit));
  }
  
  /**
   * This method converts this instance to the "largest" possible time unit. For example,
   * if this instance's value corresponds to 1000 milliseconds, this method will return a
   * new {@link TimeMeasure} instance corresponding to 1 second (that is, with a value of 1, and 
   * a unit corresponding to {@link TimeUnit#SECONDS}.
   * <p>
   * Note that if this instance's value is 1500 millis, the conversion will still amount to
   * 1 second: that is because time value are represented as <code>long</code>s.
   * <p>
   * That is why the {@link TimeMeasure} returned by this method should be deemed an approximation -
   * as the name of this method implies.
   * 
   * @return a new {@link TimeMeasure} instance, resulting from this conversion operation.
   */
  public TimeMeasure approximate() {
    if (getMillis() >= TimeUnit.DAYS.toMillis(1)) {
      return new TimeMeasure(TimeUnit.DAYS, TimeUnit.DAYS.convert(value, unit));
    } else if (getMillis() >= TimeUnit.HOURS.toMillis(1)) {
      return new TimeMeasure(TimeUnit.HOURS, TimeUnit.HOURS.convert(value, unit));
    } else if (getMillis() >= TimeUnit.MINUTES.toMillis(1)) {
      return new TimeMeasure(TimeUnit.MINUTES, TimeUnit.MINUTES.convert(value, unit));
    } else if (getMillis() >= TimeUnit.SECONDS.toMillis(1)) {
      return new TimeMeasure(TimeUnit.SECONDS, TimeUnit.SECONDS.convert(value, unit));
    } else if (getMillis() >= TimeUnit.MILLISECONDS.toMillis(1)) {
      return new TimeMeasure(TimeUnit.MILLISECONDS, TimeUnit.MILLISECONDS.convert(value, unit));
    } else {
      return new TimeMeasure(TimeUnit.NANOSECONDS, TimeUnit.NANOSECONDS.convert(value, unit));
    }
  }
  
  /**
   * @return this instance literal form - can be used for display purposes.
   */
  public String toLiteral() {
    if (value == 1 || value == 0) {
      return new StringBuilder().append(value).append(' ').append(unit.name().toLowerCase().substring(0, unit.name().length() - 1)).toString();
    } else {
      return new StringBuilder().append(value).append(' ').append(unit.name().toLowerCase()).toString();
    }
  }
  
  /**
   * @param supplier the {@link TimeSupplier} to use.
   * @return this instance.
   */
  public TimeMeasure withTimeSupplier(TimeSupplier supplier) {
    this.supplier = supplier;
    return this;
  }

  // --------------------------------------------------------------------------
  // Comparable interface

  @Override
  public int compareTo(TimeMeasure other) {
    if (value == other.value) {
      return 0;
    } else if (value < other.value) {
      return -1;
    } else {
      return 1;
    }
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
 
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TimeMeasure) {
      return ((TimeMeasure) obj).getMillis() == getMillis();
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return (int) value;
  }
  
  @Override
  public String toString() {
    return new StringBuilder()
      .append("[unit=").append(unit.name())
      .append(",value=").append(value).append("]")
      .toString();
  }
  
  // --------------------------------------------------------------------------
  // Factory methods
  
  public static TimeMeasure forMillis(long millis) {
    return new TimeMeasure(TimeUnit.MILLISECONDS, millis);
  }
  
  public static TimeMeasure forSeconds(int seconds) {
    return new TimeMeasure(TimeUnit.SECONDS, seconds);
  }
  
  public static TimeMeasure forMinutes(int minutes) {
    return new TimeMeasure(TimeUnit.MINUTES, minutes);
  }
  
  public static TimeMeasure forValue(TimeUnit unit, long value) {
    return new TimeMeasure(unit, value);
  }
  
  public static TimeMeasure forCurrentTime(TimeSupplier supplier) {
    return new TimeMeasure(TimeUnit.MILLISECONDS, supplier.currentTimeMillis()).withTimeSupplier(supplier);
  }
}
