package org.sapia.corus.cloud.platform.util;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.cloud.platform.util.TimeMeasure;
import org.sapia.corus.cloud.platform.util.TimeSupplier.MutableTime;

public class TimeMeasureTest {
  
  private TimeMeasure t1, t2;
  private MutableTime clock;
  
  @Before
  public void setUp() {
    clock = MutableTime.getInstance();
    t1 = TimeMeasure.forCurrentTime(clock);
    clock.addTime(60000);
    t2 = TimeMeasure.forCurrentTime(clock);
  }

  @Test
  public void testDiff_same() {
    assertEquals(0, t1.diff(t1).getMillis());
  }

  @Test
  public void testDiff_negative() {
    assertEquals(-60000, t1.diff(t2).getMillis());
  }

  @Test
  public void testDiff_positive() {
    assertEquals(60000, t2.diff(t1).getMillis());
  }
  
  @Test
  public void testElapsedMillis() {
    assertEquals(60000, t1.elapsedMillis().getValue());
  }

  @Test
  public void testElapsed() {
    assertEquals(60, t1.elapsed(TimeUnit.SECONDS).getValue());
  }

  @Test
  public void testConvertTo() {
    assertEquals(60, t2.convertTo(TimeUnit.SECONDS).getValue());
  }

  @Test
  public void testApproximate() {
    assertEquals(1, t2.approximate().getValue());
  }

  @Test
  public void testToLiteral() {
    assertEquals("1 minute", t2.approximate().toLiteral());
  }
  
  @Test
  public void testToLiteral_plural() {
    assertEquals("2 minutes", TimeMeasure.forMillis(120000).approximate().toLiteral());
  }

  @Test
  public void testCompareTo_same() {
    assertEquals(0, t1.compareTo(t1));
  }
  
  @Test
  public void testCompareTo_lowerThan() {
    assertEquals(-1, t1.compareTo(t2));
  }
  
  @Test
  public void testCompareTo_greaterThan() {
    assertEquals(1, t2.compareTo(t1));
  }

  @Test
  public void testEqualsObject_same() {
    assertEquals(t1, t1);
  }
  
  @Test
  public void testEqualsObject_not_same() {
    assertNotEquals(t1, t2);
  }

  @Test
  public void testForMillis() {
    assertEquals(TimeUnit.MILLISECONDS, TimeMeasure.forMillis(1000).getUnit());
    assertEquals(1000, TimeMeasure.forMillis(1000).getValue());
  }

  @Test
  public void testForSeconds() {
    assertEquals(TimeUnit.SECONDS, TimeMeasure.forSeconds(1000).getUnit());
    assertEquals(1000, TimeMeasure.forMillis(1000).getValue());
  }

  @Test
  public void testForMinutes() {
    assertEquals(TimeUnit.MINUTES, TimeMeasure.forMinutes(1000).getUnit());
    assertEquals(1000, TimeMeasure.forMillis(1000).getValue());
  }

  @Test
  public void testForValue() {
    assertEquals(TimeUnit.MINUTES, TimeMeasure.forValue(TimeUnit.MINUTES, 1000).getUnit());
    assertEquals(1000, TimeMeasure.forMillis(1000).getValue());
    
  }

  @Test
  public void testForCurrentTime() {
    assertEquals(TimeUnit.MILLISECONDS, TimeMeasure.forCurrentTime(clock).getUnit());
    assertEquals(60000,  TimeMeasure.forCurrentTime(clock).getValue());
  }

}
