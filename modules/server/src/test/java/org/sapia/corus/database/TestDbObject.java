package org.sapia.corus.database;

import org.sapia.corus.client.common.json.JsonInput;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.services.database.persistence.AbstractPersistent;

public class TestDbObject extends AbstractPersistent<String, TestDbObject> implements JsonStreamable {

  private String name;
  private String description;

  @Override
  public String getKey() {
    return name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  @Override
  public void toJson(JsonStream stream) {
    stream.beginObject()
      .field("name").value(name)
      .field("description").value(description)
    .endObject();
  }
  
  public static TestDbObject fromJson(JsonInput input) {
    TestDbObject dbo = new TestDbObject();
    dbo.setName(input.getString("name"));
    dbo.setDescription(input.getString("description"));
    return dbo;
  }
}
