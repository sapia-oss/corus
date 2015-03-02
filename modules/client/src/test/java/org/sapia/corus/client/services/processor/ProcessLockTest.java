package org.sapia.corus.client.services.processor;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.exceptions.processor.ProcessLockException;

public class ProcessLockTest {
  
  ProcessLock lock;
  LockOwner   excOwner, nonExcOwner, otherOwner;

  @Before
  public void setUp() {
    lock = new ProcessLock();
    excOwner    = LockOwner.createInstance();
    nonExcOwner = LockOwner.createInstance().nonExclusive();
    otherOwner  = LockOwner.createInstance();
  }
  
  @Test
  public void testAcquire_exclusive_free() throws Exception {
    lock.acquire(excOwner);
  }

  @Test(expected = ProcessLockException.class)
  public void testAcquire_exclusive_acquire_other_owner() throws Exception {
    lock.acquire(excOwner);
    lock.acquire(otherOwner);
  }
  
  @Test
  public void testAcquire_exclusive_acquire_same_owner() throws Exception {
    lock.acquire(excOwner);
    lock.acquire(excOwner);
  }
  
  @Test
  public void testAcquire_non_exclusive_acquire_other_owner() throws Exception {
    lock.acquire(nonExcOwner);
    lock.acquire(otherOwner);
  }
  
  @Test
  public void testAwaitRelease() {
  }

  @Test
  public void testGetOwner() throws Exception {
    lock.acquire(excOwner);
    assertTrue(lock.isLocked());
    assertEquals(excOwner, lock.getOwner());
  }

  @Test
  public void testRelease() throws Exception {
    lock.acquire(excOwner);
    lock.release();
    assertFalse(lock.isLocked());
  }

  @Test
  public void testReleaseLockOwner_not_set() {
    assertFalse(lock.isLocked());
    lock.release();
    assertFalse(lock.isLocked());
  }
  
  @Test
  public void testReleaseLockOwner_exclusive_same_owner() throws Exception {
    lock.acquire(excOwner);
    assertTrue(lock.isLocked());
    lock.release(excOwner);
    assertFalse(lock.isLocked());
  }
  
  @Test
  public void testReleaseLockOwner_exclusive_other_owner() throws Exception {
    lock.acquire(excOwner);
    assertTrue(lock.isLocked());
    lock.release(otherOwner);
    assertTrue(lock.isLocked());
  }
  
  @Test
  public void testReleaseLockOwner_non_exclusive_other_owner() throws Exception {
    lock.acquire(nonExcOwner);
    assertFalse(lock.isLocked());
    assertTrue(lock.isShared());

    lock.release(otherOwner);
    assertFalse(lock.isLocked());
  }

}
