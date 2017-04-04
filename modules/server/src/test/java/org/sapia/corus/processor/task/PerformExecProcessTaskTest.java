package org.sapia.corus.processor.task;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.PublicKey;
import java.util.Properties;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.PropertyMasker;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.dist.Property;
import org.sapia.corus.client.services.processor.ActivePort;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.core.InternalServiceContext;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.ubik.rmi.server.transport.socket.TcpSocketAddress;

public class PerformExecProcessTaskTest extends TestBaseTask {

    private Distribution  dist;
    private ProcessConfig conf;
    private Process process;
    private TaskExecutionContext   taskContext;
    private PerformExecProcessTask task;

    @Override
    @Before
    public void setUp() throws Exception {
      super.setUp();
      dist = super.createDistribution("testDist", "1.0");
      conf = super.createProcessConfig(dist, "testProc", "testProfile");
      process = super.createProcess(dist, conf, "junit");
      taskContext = mock(TaskExecutionContext.class);
      ServerContext serverContext = mock(ServerContext.class);
      InternalServiceContext serviceContext = mock(InternalServiceContext.class);
      Configurator configurator = mock(Configurator.class);

      CorusHost node = CorusHost.newInstance("test-node", new Endpoint(new TcpSocketAddress("test", 1001), new TcpSocketAddress("test", 1001)), "test", "test", mock(PublicKey.class));
      node.setRepoRole(RepoRole.CLIENT);

      when(taskContext.getServerContext()).thenReturn(serverContext);
      when(serverContext.getServices()).thenReturn(serviceContext);
      when(serverContext.getCorusHost()).thenReturn(node);
      when(serverContext.getHomeDir()).thenReturn("~/.");
      when(serviceContext.getConfigurator()).thenReturn(configurator);
      when(configurator.getPropertyMasker()).thenReturn(PropertyMasker.newDefaultInstance());
      task = new PerformExecProcessTask();
    }

    private Properties createProperties(String... parameters) {
        Properties created = new Properties();
        for (int i = 0; i < parameters.length; i++) {
            created.setProperty(parameters[i], parameters[i+1]);
            i++;
        }

        return created;
    }

    @Test
    public void testGetProcessProps_noDoubleQuotes() throws Exception {
        Properties props = createProperties("sna", "foo");
        Assertions.assertThat(task.getProcessProps(conf, process, dist, taskContext, props))
                .contains(new Property("sna","foo"));
    }

    @Test
    public void testGetProcessProps_singleDoubleQuotes_firstChar() throws Exception {
        Properties props = createProperties("sna", "\"foo");
        Assertions.assertThat(task.getProcessProps(conf, process, dist, taskContext, props))
                .contains(new Property("sna","\\\"foo"));
    }

    @Test
    public void testGetProcessProps_singleDoubleQuotes_lastChar() throws Exception {
        Properties props = createProperties("sna", "foo\"");
        Assertions.assertThat(task.getProcessProps(conf, process, dist, taskContext, props))
                .contains(new Property("sna","foo\\\""));
    }

    @Test
    public void testGetProcessProps_singleDoubleQuotes_middleChar() throws Exception {
        Properties props = createProperties("sna", "foo\"bar");
        Assertions.assertThat(task.getProcessProps(conf, process, dist, taskContext, props))
                .contains(new Property("sna","foo\\\"bar"));
    }

    @Test
    public void testGetProcessProps_withSpace_atBeginning() throws Exception {
        Properties props = createProperties("sna", " foo");
        Assertions.assertThat(task.getProcessProps(conf, process, dist, taskContext, props))
                .contains(new Property("sna","\" foo\""));
    }

    @Test
    public void testGetProcessProps_withSpace_atEnd() throws Exception {
        Properties props = createProperties("sna", "foo ");
        Assertions.assertThat(task.getProcessProps(conf, process, dist, taskContext, props))
                .contains(new Property("sna","\"foo \""));
    }

    @Test
    public void testGetProcessProps_withSpace_inMiddle() throws Exception {
        Properties props = createProperties("sna", "foo bar");
        Assertions.assertThat(task.getProcessProps(conf, process, dist, taskContext, props))
                .contains(new Property("sna","\"foo bar\""));
    }

    @Test
    public void testGetProcessProps_withDoubleQuotes() throws Exception {
        Properties props = createProperties("sna", "\"foo\"");
        Assertions.assertThat(task.getProcessProps(conf, process, dist, taskContext, props))
                .contains(new Property("sna","\"foo\""));
    }

    @Test
    public void testGetProcessProps_withDoubleQuotes_withSpace() throws Exception {
        Properties props = createProperties("sna", "\"foo bar\"");
        Assertions.assertThat(task.getProcessProps(conf, process, dist, taskContext, props))
                .contains(new Property("sna","\"foo bar\""));
    }

    @Test
    public void testGetProcessProps_withDoubleQuotes_withAddtionalQuote() throws Exception {
        Properties props = createProperties("sna", "\"foo \"bar\"");
        Assertions.assertThat(task.getProcessProps(conf, process, dist, taskContext, props))
                .contains(new Property("sna","\"foo \\\"bar\""));
    }

    @Test
    public void testGetProcessProps_emptyValue() throws Exception {
        Properties props = createProperties("emptyVal", "");
        Assertions.assertThat(task.getProcessProps(conf, process, dist, taskContext, props))
                .contains(new Property("emptyVal",""));
    }

    @Test
    public void testGetProcessProps_interpolationWithPortVars() throws Exception {
      process.addActivePort(new ActivePort("myapp.http", 8800));
      Properties props = createProperties("myDir", "/var/log/myapp_${corus.process.port.myapp.http}");

      Assertions.assertThat(task.getProcessProps(conf, process, dist, taskContext, props))
                .contains(new Property("corus.process.port.myapp.http","8800"))
                .contains(new Property("myDir","/var/log/myapp_8800"));
    }

}
