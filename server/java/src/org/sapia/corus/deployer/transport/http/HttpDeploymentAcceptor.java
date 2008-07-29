package org.sapia.corus.deployer.transport.http;

import java.io.File;

import org.sapia.corus.CorusRuntime;
import org.sapia.corus.deployer.transport.Deployment;
import org.sapia.corus.deployer.transport.DeploymentAcceptor;
import org.sapia.corus.deployer.transport.DeploymentConnector;
import org.sapia.ubik.rmi.server.transport.http.HttpTransportProvider;

import simple.http.Request;
import simple.http.Response;
import simple.http.load.BasicService;
import simple.http.serve.FileContext;

/**
 * Implements the <code>DeploymentAcceptor</code> interface over a <code>HttpTransportProvider</code>.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HttpDeploymentAcceptor extends BasicService implements DeploymentAcceptor{
	
	public static final String DEPLOYER_CONTEXT = "/corus/deployer";
	
	private HttpTransportProvider _provider;
	private DeploymentConnector   _connector;
	
	public HttpDeploymentAcceptor(HttpTransportProvider provider){
		super(new FileContext(new File(CorusRuntime.getCorusHome())));
		_provider  = provider;
	}
	
	/**
   * @see org.sapia.corus.deployer.transport.DeploymentAcceptor#init()
   */
  public void init() throws Exception {
  }
  
  /**
   * @see org.sapia.corus.deployer.transport.DeploymentAcceptor#start()
   */
  public void start() throws Exception {
		_provider.getServiceMapper().addService(DEPLOYER_CONTEXT, this);  	
  }
  
  /**
   * @see org.sapia.corus.deployer.transport.DeploymentAcceptor#stop()
   */
  public void stop() throws Exception {
  }
  
  /**
   * @see org.sapia.corus.deployer.transport.DeploymentAcceptor#registerConnector(DeploymentConnector)
   */
  public void registerConnector(DeploymentConnector connector) {
    _connector = connector;	
  }
  
  /**
   * @see simple.http.serve.BasicResource#process(simple.http.Request, simple.http.Response)
   */
  protected void process(Request req, Response res) throws Exception {
  	_connector.connect(new Deployment(new HttpConnection(req, res)));
  }

}
