package org.sapia.corus.client.facade;

public interface TestInterface {

  public String[] getValues();

  public String[] getValuesThrowException() throws Exception;

  public void throwException() throws Exception;

}
