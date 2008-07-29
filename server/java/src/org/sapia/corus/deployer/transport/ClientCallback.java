package org.sapia.corus.deployer.transport;

import java.io.IOException;

import org.sapia.corus.CorusRuntime;
import org.sapia.corus.util.ProgressQueue;
import org.sapia.ubik.rmi.server.transport.MarshalOutputStream;

/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
class ClientCallback {
	
	void handleResult(Deployment deployment, ProgressQueue result) throws IOException{
  	MarshalOutputStream os = new MarshalOutputStream(deployment.getConnection().getOutputStream());
		os.setUp(deployment.getMetadata().getOrigin(), CorusRuntime.getTransport().getServerAddress().getTransportType());
		os.writeObject(result);
		os.flush();
		os.close();
	}

}
