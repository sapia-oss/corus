package org.sapia.corus.client.cli;

/**
 * Holds info meant to define column display in the CLI.
 * 
 * @author yduchesne
 * 
 */
public class ColumnDef {

  private String name;
  private int index;
  private int width;

  /**
   * @param name
   *          the column's name.
   * @param index
   *          the column's index.
   * @param width
   *          the column's with.
   */
  public ColumnDef(String name, int position, int width) {
    this.name = name;
    this.index = position;
    this.width = width;
  }

  /**
   * @return the column's name.
   */
  public String name() {
    return name;
  }

  /**
   * @return this instance's width.
   */
  public int width() {
    return width;
  }

  /**
   * @return this instance's position.
   */
  public int index() {
    return index;
  }
}
