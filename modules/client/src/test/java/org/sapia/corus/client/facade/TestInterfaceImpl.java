package org.sapia.corus.client.facade;

public class TestInterfaceImpl implements TestInterface{
  
  @Override
  public String[] getValues() {
    return new String[]{"1", "2"};
  }
  
  @Override
  public void throwException() throws Exception{
    throw new Exception("Method has thrown an exception");
  }
  
  @Override
  public String[] getValuesThrowException() throws Exception {
    throw new Exception("Method has thrown an exception");
  }
  

}
