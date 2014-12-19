package org.sapia.corus.http;

import org.simpleframework.http.core.Container;

/**
 * Internal interface specifying behavior for exporting a {@link Container} instance over the SSL protocol.
 * 
 * @author yduchesne
 *
 */
public interface SslExporter {

  /**
   * @param container the {@link Container} to expose over SSL.
   */
  public void export(Container container) throws Exception;
}
