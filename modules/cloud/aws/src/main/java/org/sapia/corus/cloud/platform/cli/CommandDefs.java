package org.sapia.corus.cloud.platform.cli;

/**
 * Holds constants corresponding to the various command definitions.
 * 
 * @author yduchesne
 *
 */
public enum CommandDefs {

  CREATE_IMAGE("create-image", "Creates an image on the targeted provider");
  
  private String name, desc;
  
  private CommandDefs(String name, String desc) {
    this.name = name;
    this.desc = desc;
  }
  
  public String getDescription() {
    return desc;
  }
  
  public String getName() {
    return name;
  }
}
