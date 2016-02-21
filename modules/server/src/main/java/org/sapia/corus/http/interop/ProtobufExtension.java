package org.sapia.corus.http.interop;

import org.sapia.corus.core.ServerContext;
import org.sapia.corus.interop.InteropCodec.InteropWireFormat;

/**
 * This extension implement the server side of the Corus interoperability spec.
 * It can be accessed with an URL similar as the following one:
 * <p>
 * 
 * <pre>
 * http://localhost:33000/interoap/protobuf
 * </pre>
 * 
 * @author Yanick Duchesne
 */
public class ProtobufExtension extends BaseInteropExtension {

  public ProtobufExtension(ServerContext serverContext) {
    super(InteropWireFormat.PROTOBUF, serverContext);
  }
}
