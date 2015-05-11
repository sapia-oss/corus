package org.sapia.corus.client.common.json;


public interface JsonInput {

  public boolean getBoolean(String field);
  
  public int getInt(String field);
  
  public int[] getIntArray(String field);
  
  public Integer[] getIntObjectArray(String field);
  
  public long getLong(String field);
  
  public String getString(String field);
  
  public String[] getStringArray(String field);
  
  public Iterable<JsonInput> iterate(String field);
  
  public JsonInput getObject(String field);
  
}
