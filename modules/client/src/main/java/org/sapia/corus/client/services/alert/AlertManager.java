package org.sapia.corus.client.services.alert;

/**
 * Specifies the behavior of the module that sends alerts.
 * 
 * @author yduchesne
 * 
 */
public interface AlertManager {

  public static String ROLE = AlertManager.class.getName();

  /**
   * Identifies the various alert levels.
   * 
   * @author yduchesne
   * 
   */
  public enum AlertLevel {

    INFO(0), WARNING(1), ERROR(2), FATAL(3);

    private int value;
    
    private AlertLevel(int value) {
      this.value = value;
    }
    
    /**
     * @param toCheck another {@link AlertLevel} to check.
     * @return <code>true</code> if the given {@link AlertLevel} has a value
     * greater than/equal to this instance's value.
     */
    public boolean isEnabled(AlertLevel toCheck) {
      return toCheck.value >= value;
    }
    
    public static AlertLevel forName(String name) {
      for (AlertLevel al : AlertLevel.values()) {
        if (al.name().equalsIgnoreCase(name)) {
          return al;
        }
      }
      throw new IllegalArgumentException("Invalid alert level: " + name);
    }
  }

}
