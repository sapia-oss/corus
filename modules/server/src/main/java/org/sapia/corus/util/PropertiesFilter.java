package org.sapia.corus.util;

/**
 * An instance of this interface determines if some properties should be filtered out.
 * @author yduchesne
 *
 */
public interface PropertiesFilter {
	
	/**
	 * A {@link PropertiesFilter} that will accept the properties whose name match a given prefix.
	 */
	public static class NamePrefixPropertiesFilter implements PropertiesFilter {
		
		private String prefix;
		
		private NamePrefixPropertiesFilter(String prefix) {
			this.prefix = prefix;
    }
		
		@Override
		public boolean accepts(String name, String value) {
		  return name.startsWith(prefix);
		}
		
		/**
		 * @param prefix a prefix.
		 * @return a new instance of this class.
		 */
		public static NamePrefixPropertiesFilter createInstance(String prefix) {
			return new NamePrefixPropertiesFilter(prefix);
		}
		
	}
	
	// ==========================================================================

	/**
	 * A {@link PropertiesFilter} that will accept the properties whose name contain a given substring.
	 */
	public static class NameContainsPropertiesFilter implements PropertiesFilter {
		
		private String substring;
		
		private NameContainsPropertiesFilter(String substring) {
			this.substring = substring;
		}
		
		@Override
		public boolean accepts(String name, String value) {
		  return name.contains(substring);
		}
		
		/**
		 * @param substring a substring whose containment should be checked.
		 * @return a new instance of this class.
		 */
		public static NameContainsPropertiesFilter createInstance(String substring) {
			return new NameContainsPropertiesFilter(substring);
		}
		
	}
	
	// ==========================================================================

	/**
	 * A {@link PropertiesFilter} that negates the outcome of a delegate {@link PropertiesFilter}.
	 *
	 */
	public static class NotPropertiesFilter implements PropertiesFilter {
	  
	  private PropertiesFilter delegate;
	   
	  public NotPropertiesFilter(PropertiesFilter delegate) {
      this.delegate = delegate;
    }
	  
	  @Override
	  public boolean accepts(String name, String value) {
	    return !delegate.accepts(name, value);
	  }
	  
	  public static NotPropertiesFilter createInstance(PropertiesFilter delegate) {
	    return new NotPropertiesFilter(delegate);
	  }
	}
	
	// ==========================================================================
	
	/**
	 * This method is used to filter out property.
	 * 
	 * @param name the name of the property to check.
	 * @param value the property value.
	 * @return <code>true</code> if the property should be retained.
	 */
	public boolean accepts(String name, String value);

}
