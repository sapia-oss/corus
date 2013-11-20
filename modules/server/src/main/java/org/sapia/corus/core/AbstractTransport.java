package org.sapia.corus.core;

import java.rmi.RemoteException;

import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.StubProcessor;
import org.sapia.ubik.rmi.server.transport.TransportProvider;

/**
 * This class implements the {@link CorusTransport} partly: inheriting class
 * must implement a template method and are in charge of initializing the Ubik
 * {@link TransportProvider} that they handle.
 * 
 * @author Yanick Duchesne
 */
public abstract class AbstractTransport implements CorusTransport {

  private static final int SHUTDOWN_TIMEOUT = 30000;

  private boolean isInit;

  public AbstractTransport() {
    StubProcessor processor = Hub.getModules().getStubProcessor();
    processor.insertOIDCreationStrategy(new CorusOIDCreationStrategy());
    processor.insertOIDCreationStrategy(new CorusModuleOIDCreationStrategy());
  }

  /**
   * This method will, the first time it is invoked, delegate the export
   * operation to the {@link #initExport(Object)} method of this class.
   * 
   * @see #initExport(Object)
   * @see org.sapia.corus.core.CorusTransport#exportObject(java.lang.Object)
   */
  public Object exportObject(Object anObject) throws RemoteException {
    Object stub;
    if (!isInit)
      stub = initExport(anObject);
    else
      stub = Hub.exportObject(anObject, getTransportProvider().getTransportType());

    isInit = true;
    return stub;
  }

  /**
   * @see CorusTransport#shutdown()
   */
  public void shutdown() {
    try {
      Hub.shutdown(SHUTDOWN_TIMEOUT);
    } catch (InterruptedException ie) {
      ie.printStackTrace();
    }
  }

  /**
   * Inheriting classes must implement this method in the following way:
   * <ol>
   * <li>Create the appropriate {@link TransportProvider}.
   * <li>Export the given object through Ubik's {@link Hub}, in a way that is
   * compliant with the provider that has been previously created.
   * <li>Return the stub resulting from the export operation.
   * </ol>
   * 
   * @param anObject
   *          an {@link Object} to export.
   * @return the stub that was generated for the given object.
   * @throws RemoteException
   */
  protected abstract Object initExport(Object anObject) throws RemoteException;

}
