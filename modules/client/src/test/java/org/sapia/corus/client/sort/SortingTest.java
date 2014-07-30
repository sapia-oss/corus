package org.sapia.corus.client.sort;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.services.cluster.ClusterStatus;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.sort.Sorting.SortSwitch;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.EventChannel.Role;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

@RunWith(MockitoJUnitRunner.class)
public class SortingTest {
  
  @Mock
  private ServerAddress channelAddress;

  @Test
  public void testGetProcessComparatorFor_DistName_Ascending() {
    Process p1 = process("d1", "v1", "p1", "prof", "1", "1");
    Process p2 = process("d2", "v1", "p2", "prof", "1", "1");
    
    Comparator<Process> c = Sorting.getProcessComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.DIST_NAME).build());
 
    assertTrue(c.compare(p1, p2) < 0);
    assertTrue(c.compare(p2, p1) > 0);
    assertTrue(c.compare(p1, p1) == 0);
  }
  
  @Test
  public void testGetProcessComparatorFor_DistName_Descending() {
    Process p1 = process("d1", "v1", "p1", "prof", "1", "1");
    Process p2 = process("d2", "v1", "p2", "prof", "1", "1");
    
    Comparator<Process> c = Sorting.getProcessComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.DIST_NAME).build());
 
    assertTrue(c.compare(p2, p1) < 0);
    assertTrue(c.compare(p1, p2) > 0);
    assertTrue(c.compare(p1, p1) == 0);
  }
  
  @Test
  public void testGetProcessComparatorFor_DistVersion_Ascending() {
    Process p1 = process("d1", "v1", "p1", "prof", "1", "1");
    Process p2 = process("d1", "v2", "p2", "prof", "1", "1");
    
    Comparator<Process> c = Sorting.getProcessComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.DIST_VERSION).build());
 
    assertTrue(c.compare(p1, p2) < 0);
    assertTrue(c.compare(p2, p1) > 0);
    assertTrue(c.compare(p1, p1) == 0);
  }

  @Test
  public void testGetProcessComparatorFor_DistVersion_Descending() {
    Process p1 = process("d1", "v1", "p1", "prof", "1", "1");
    Process p2 = process("d1", "v2", "p2", "prof", "1", "1");
    
    Comparator<Process> c = Sorting.getProcessComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.DIST_VERSION).build());
 
    assertTrue(c.compare(p2, p1) < 0);
    assertTrue(c.compare(p1, p2) > 0);
    assertTrue(c.compare(p1, p1) == 0);
  }
  
  @Test
  public void testGetProcessComparatorFor_ProcName_Ascending() {
    Process p1 = process("d1", "v1", "p1", "prof", "1", "1");
    Process p2 = process("d1", "v1", "p2", "prof", "1", "1");
    
    Comparator<Process> c = Sorting.getProcessComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.PROC_NAME).build());
 
    assertTrue(c.compare(p1, p2) < 0);
    assertTrue(c.compare(p2, p1) > 0);
    assertTrue(c.compare(p1, p1) == 0);
  }

  @Test
  public void testGetProcessComparatorFor_ProcName_Descending() {
    Process p1 = process("d1", "v1", "p1", "prof", "1", "1");
    Process p2 = process("d1", "v1", "p2", "prof", "1", "1");
    
    Comparator<Process> c = Sorting.getProcessComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.PROC_NAME).build());
 
    assertTrue(c.compare(p2, p1) < 0);
    assertTrue(c.compare(p1, p2) > 0);
    assertTrue(c.compare(p1, p1) == 0);
  }
  
  @Test
  public void testGetProcessComparatorFor_ProcID_Ascending() {
    Process p1 = process("d1", "v1", "p1", "prof", "1", "1");
    Process p2 = process("d1", "v1", "p1", "prof", "2", "1");
    
    Comparator<Process> c = Sorting.getProcessComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.PROC_ID).build());
 
    assertTrue(c.compare(p1, p2) < 0);
    assertTrue(c.compare(p2, p1) > 0);
    assertTrue(c.compare(p1, p1) == 0);
  }

  @Test
  public void testGetProcessComparatorFor_ProcID_Descending() {
    Process p1 = process("d1", "v1", "p1", "prof", "1", "1");
    Process p2 = process("d1", "v1", "p1", "prof", "2", "1");
    
    Comparator<Process> c = Sorting.getProcessComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.PROC_ID).build());
 
    assertTrue(c.compare(p2, p1) < 0);
    assertTrue(c.compare(p1, p2) > 0);
    assertTrue(c.compare(p1, p1) == 0);
  }

  @Test
  public void testGetProcessComparatorFor_ProcPID_Ascending() {
    Process p1 = process("d1", "v1", "p1", "prof", "1", "1");
    Process p2 = process("d1", "v1", "p1", "prof", "1", "2");
    
    Comparator<Process> c = Sorting.getProcessComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.PROC_OS_PID).build());
 
    assertTrue(c.compare(p1, p2) < 0);
    assertTrue(c.compare(p2, p1) > 0);
    assertTrue(c.compare(p1, p1) == 0);
  }

  @Test
  public void testGetProcessComparatorFor_ProcPID_Descending() {
    Process p1 = process("d1", "v1", "p1", "prof", "1", "1");
    Process p2 = process("d1", "v1", "p1", "prof", "1", "2");
    
    Comparator<Process> c = Sorting.getProcessComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.PROC_OS_PID).build());
 
    assertTrue(c.compare(p2, p1) < 0);
    assertTrue(c.compare(p1, p2) > 0);
    assertTrue(c.compare(p1, p1) == 0);
  }  

  @Test
  public void testGetProcessComparatorFor_Profile_Ascending() {
    Process p1 = process("d1", "v1", "p1", "prof1", "1", "1");
    Process p2 = process("d1", "v1", "p1", "prof2", "1", "1");
    
    Comparator<Process> c = Sorting.getProcessComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.PROC_PROFILE).build());
 
    assertTrue(c.compare(p1, p2) < 0);
    assertTrue(c.compare(p2, p1) > 0);
    assertTrue(c.compare(p1, p1) == 0);
  }

  @Test
  public void testGetProcessComparatorFor_Profile_Descending() {
    Process p1 = process("d1", "v1", "p1", "prof1", "1", "1");
    Process p2 = process("d1", "v1", "p1", "prof2", "1", "1");
    
    Comparator<Process> c = Sorting.getProcessComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.PROC_PROFILE).build());
 
    assertTrue(c.compare(p2, p1) < 0);
    assertTrue(c.compare(p1, p2) > 0);
    assertTrue(c.compare(p1, p1) == 0);
  }  
  
  @Test
  public void testGetDistributionComparatorFor_Version_Ascending() {
    Distribution d1 = distribution("n1", "v1");
    Distribution d2 = distribution("n1", "v2");
    
    Comparator<Distribution> c = Sorting.getDistributionComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.DIST_VERSION).build());
    
    assertTrue(c.compare(d1, d2) < 0);
    assertTrue(c.compare(d2, d1) > 0);
    assertTrue(c.compare(d1, d1) == 0);
  }
  
  @Test
  public void testGetDistributionComparatorFor_Version_Descending() {
    Distribution d1 = distribution("n1", "v1");
    Distribution d2 = distribution("n1", "v2");
    
    Comparator<Distribution> c = Sorting.getDistributionComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.DIST_VERSION).build());
    
    assertTrue(c.compare(d2, d1) < 0);
    assertTrue(c.compare(d1, d2) > 0);
    assertTrue(c.compare(d1, d1) == 0);
  }
  
  @Test
  public void testGetDistributionComparatorFor_Name_Ascending() {
    Distribution d1 = distribution("n1", "v1");
    Distribution d2 = distribution("n2", "v1");
    
    Comparator<Distribution> c = Sorting.getDistributionComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.DIST_NAME).build());
    
    assertTrue(c.compare(d1, d2) < 0);
    assertTrue(c.compare(d2, d1) > 0);
    assertTrue(c.compare(d1, d1) == 0);
  }
  
  @Test
  public void testGetDistributionComparatorFor_Name_Descending() {
    Distribution d1 = distribution("n1", "v1");
    Distribution d2 = distribution("n2", "v1");
    
    Comparator<Distribution> c = Sorting.getDistributionComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.DIST_NAME).build());
    
    assertTrue(c.compare(d2, d1) < 0);
    assertTrue(c.compare(d1, d2) > 0);
    assertTrue(c.compare(d1, d1) == 0);
  }

  @Test
  public void testGetHostComparatorFor_IP_Ascending() {
    CorusHost h1 = host(RepoRole.SERVER, "h1", 1000);
    CorusHost h2 = host(RepoRole.SERVER, "h2", 1000);
    
    Comparator<CorusHost> c = Sorting.getHostComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.HOST_IP).build());
    
    assertTrue(c.compare(h1, h2) < 0);
    assertTrue(c.compare(h2, h1) > 0);
    assertTrue(c.compare(h1, h1) == 0);  
  }
  
  @Test
  public void testGetHostComparatorFor_IP_Descending() {
    CorusHost h1 = host(RepoRole.SERVER, "h1", 1000);
    CorusHost h2 = host(RepoRole.SERVER, "h2", 1000);
    
    Comparator<CorusHost> c = Sorting.getHostComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.HOST_IP).build());
    
    assertTrue(c.compare(h2, h1) < 0);
    assertTrue(c.compare(h1, h2) > 0);
    assertTrue(c.compare(h1, h1) == 0);  
  }
  
  @Test
  public void testGetHostComparatorFor_Name_Ascending() {
    CorusHost h1 = host(RepoRole.SERVER, "h1", 1000);
    CorusHost h2 = host(RepoRole.SERVER, "h2", 1000);
    
    Comparator<CorusHost> c = Sorting.getHostComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.HOST_NAME).build());
    
    assertTrue(c.compare(h1, h2) < 0);
    assertTrue(c.compare(h2, h1) > 0);
    assertTrue(c.compare(h1, h1) == 0);  
  }
  
  @Test
  public void testGetHostComparatorFor_Name_Descending() {
    CorusHost h1 = host(RepoRole.SERVER, "h1", 1000);
    CorusHost h2 = host(RepoRole.SERVER, "h2", 1000);
    
    Comparator<CorusHost> c = Sorting.getHostComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.HOST_NAME).build());
    
    assertTrue(c.compare(h2, h1) < 0);
    assertTrue(c.compare(h1, h2) > 0);
    assertTrue(c.compare(h1, h1) == 0);  
  }
  
  @Test
  public void testGetHostComparatorFor_Role_Ascending() {
    CorusHost h1 = host(RepoRole.CLIENT, "h1", 1000);
    CorusHost h2 = host(RepoRole.SERVER, "h1", 1000);
    
    Comparator<CorusHost> c = Sorting.getHostComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.HOST_ROLE).build());
    
    assertTrue(c.compare(h1, h2) < 0);
    assertTrue(c.compare(h2, h1) > 0);
    assertTrue(c.compare(h1, h1) == 0);  
  }
  
  @Test
  public void testGetHostComparatorFor_Role_Descending() {
    CorusHost h1 = host(RepoRole.CLIENT, "h1", 1000);
    CorusHost h2 = host(RepoRole.SERVER, "h1", 1000);
    
    Comparator<CorusHost> c = Sorting.getHostComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.HOST_ROLE).build());
    
    assertTrue(c.compare(h2, h1) < 0);
    assertTrue(c.compare(h1, h2) > 0);
    assertTrue(c.compare(h1, h1) == 0);  
  }

  @Test
  public void testGetClusterStatusComparatorFor_Name_Ascending() {
    ClusterStatus s1 = status(EventChannel.Role.MASTER, "h1", 1000);
    ClusterStatus s2 = status(EventChannel.Role.MASTER, "h2", 1000);
    
    Comparator<ClusterStatus> c = Sorting.getClusterStatusComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.HOST_NAME).build());
    
    assertTrue(c.compare(s1, s2) < 0);
    assertTrue(c.compare(s2, s1) > 0);
    assertTrue(c.compare(s1, s1) == 0);  
  }
  
  @Test
  public void testGetClusterStatusComparatorFor_Name_Descending() {
    ClusterStatus s1 = status(EventChannel.Role.MASTER, "h1", 1000);
    ClusterStatus s2 = status(EventChannel.Role.MASTER, "h2", 1000);
    
    Comparator<ClusterStatus> c = Sorting.getClusterStatusComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.HOST_NAME).build());
    
    assertTrue(c.compare(s2, s1) < 0);
    assertTrue(c.compare(s1, s2) > 0);
    assertTrue(c.compare(s1, s1) == 0);  
  }
  
  @Test
  public void testGetClusterStatusComparatorFor_IP_Ascending() {
    ClusterStatus s1 = status(EventChannel.Role.MASTER, "h1", 1000);
    ClusterStatus s2 = status(EventChannel.Role.MASTER, "h2", 1000);
    
    Comparator<ClusterStatus> c = Sorting.getClusterStatusComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.HOST_IP).build());
    
    assertTrue(c.compare(s1, s2) < 0);
    assertTrue(c.compare(s2, s1) > 0);
    assertTrue(c.compare(s1, s1) == 0);  
  }
  
  @Test
  public void testGetClusterStatusComparatorFor_IP_Descending() {
    ClusterStatus s1 = status(EventChannel.Role.MASTER, "h1", 1000);
    ClusterStatus s2 = status(EventChannel.Role.MASTER, "h2", 1000);
    
    Comparator<ClusterStatus> c = Sorting.getClusterStatusComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.HOST_IP).build());
    
    assertTrue(c.compare(s2, s1) < 0);
    assertTrue(c.compare(s1, s2) > 0);
    assertTrue(c.compare(s1, s1) == 0);  
  }
  
  @Test
  public void testGetClusterStatusComparatorFor_Role_Ascending() {
    ClusterStatus s1 = status(EventChannel.Role.MASTER, "h1", 1000);
    ClusterStatus s2 = status(EventChannel.Role.SLAVE, "h1", 1000);
    
    Comparator<ClusterStatus> c = Sorting.getClusterStatusComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.HOST_ROLE).build());
    
    assertTrue(c.compare(s1, s2) < 0);
    assertTrue(c.compare(s2, s1) > 0);
    assertTrue(c.compare(s1, s1) == 0);  
  }
  
  @Test
  public void testGetClusterStatusComparatorFor_Role_Descending() {
    ClusterStatus s1 = status(EventChannel.Role.MASTER, "h1", 1000);
    ClusterStatus s2 = status(EventChannel.Role.SLAVE, "h1", 1000);
    
    Comparator<ClusterStatus> c = Sorting.getClusterStatusComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.HOST_ROLE).build());
    
    assertTrue(c.compare(s2, s1) < 0);
    assertTrue(c.compare(s1, s2) > 0);
    assertTrue(c.compare(s1, s1) == 0);  
  }

  @Test
  public void testGetExecConfigComparatorFor_Name_Ascending() {
    ExecConfig c1 = config("c1", "p1");
    ExecConfig c2 = config("c2", "p1");
    
    Comparator<ExecConfig> c = Sorting.getExecConfigComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.EXEC_CONFIG_NAME).build());

    assertTrue(c.compare(c1, c2) < 0);
    assertTrue(c.compare(c2, c1) > 0);
    assertTrue(c.compare(c1, c1) == 0);
  }
  
  @Test
  public void testGetExecConfigComparatorFor_Name_Descending() {
    ExecConfig c1 = config("c1", "p1");
    ExecConfig c2 = config("c2", "p1");
    
    Comparator<ExecConfig> c = Sorting.getExecConfigComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.EXEC_CONFIG_NAME).build());

    assertTrue(c.compare(c2, c1) < 0);
    assertTrue(c.compare(c1, c2) > 0);
    assertTrue(c.compare(c1, c1) == 0);
  }
  
  @Test
  public void testGetExecConfigComparatorFor_Profile_Ascending() {
    ExecConfig c1 = config("c1", "p1");
    ExecConfig c2 = config("c1", "p2");
    
    Comparator<ExecConfig> c = Sorting.getExecConfigComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.EXEC_CONFIG_PROFILE).build());

    assertTrue(c.compare(c1, c2) < 0);
    assertTrue(c.compare(c2, c1) > 0);
    assertTrue(c.compare(c1, c1) == 0);
  }
  
  @Test
  public void testGetExecConfigComparatorFor_Profile_Descending() {
    ExecConfig c1 = config("c1", "p1");
    ExecConfig c2 = config("c1", "p2");
    
    Comparator<ExecConfig> c = Sorting.getExecConfigComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.EXEC_CONFIG_PROFILE).build());

    assertTrue(c.compare(c2, c1) < 0);
    assertTrue(c.compare(c1, c2) > 0);
    assertTrue(c.compare(c1, c1) == 0);
  }

  @Test
  public void testSortSingle_ClusterStatus() {
    Results<ClusterStatus> results = new Results<ClusterStatus>();
    for (int i = 4; i >= 0; i--) {
      Result<ClusterStatus> r = new Result<ClusterStatus>(host(RepoRole.CLIENT, "h" + i, i), status(Role.SLAVE, "h" + i, i), Result.Type.ELEMENT);
      results.addResult(r);
    }
    
    Results<ClusterStatus> sorted = Sorting.sortSingle(results, ClusterStatus.class, SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.HOST_NAME).build());
    for (int i = 0; i < 5; i++) {
      assertEquals("h" + i, sorted.next().getOrigin().getEndpoint().getServerTcpAddress().getHost());
    }
  }

  @Test
  public void testSortMulti_Process() {
    Results<List<Process>> results = new Results<List<Process>>();
    for (int i = 4; i >= 0; i--) {
      List<Process> ps = new ArrayList<Process>();
      for (int j = 4; j >= 0; j--) {
        ps.add(process("d1", "v1", "p" + j, "prof", "1", "1"));
      }
      Result<List<Process>> r = new Result<List<Process>>(host(RepoRole.CLIENT, "h" + i, i), ps, Result.Type.COLLECTION);
      results.addResult(r);
    }
    
    Results<List<Process>> sorted = Sorting.sortList(results, Process.class, SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.HOST_NAME).ascending(SortSwitch.PROC_NAME).build());
    for (int i = 0; i < 5; i++) {
      Result<List<Process>> r = sorted.next();
      assertEquals("h" + i, r.getOrigin().getEndpoint().getServerTcpAddress().getHost());
      for (int j = 0; j < 5; j++) {
        Process p = r.getData().get(j);
        assertEquals("p" + j, p.getDistributionInfo().getProcessName());
      }
    }
  }
  
  @Test
  public void testSortMulti_Distribution() {
    Results<List<Distribution>> results = new Results<List<Distribution>>();
    for (int i = 4; i >= 0; i--) {
      List<Distribution> ds = new ArrayList<Distribution>();
      for (int j = 4; j >= 0; j--) {
        ds.add(distribution("d1", "v" + j));
      }
      Result<List<Distribution>> r = new Result<List<Distribution>>(host(RepoRole.CLIENT, "h" + i, i), ds, Result.Type.COLLECTION);
      results.addResult(r);
    }
    
    Results<List<Distribution>> sorted = Sorting.sortList(results, Distribution.class, SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.HOST_NAME).ascending(SortSwitch.DIST_VERSION).build());
    for (int i = 0; i < 5; i++) {
      Result<List<Distribution>> r = sorted.next();
      assertEquals("h" + i, r.getOrigin().getEndpoint().getServerTcpAddress().getHost());
      for (int j = 0; j < 5; j++) {
        Distribution d = r.getData().get(j);
        assertEquals("v" + j, d.getVersion());
      }
    }
  }
  
  @Test
  public void testSortMulti_ExecConfig() {
    Results<List<ExecConfig>> results = new Results<List<ExecConfig>>();
    for (int i = 4; i >= 0; i--) {
      List<ExecConfig> ds = new ArrayList<ExecConfig>();
      for (int j = 4; j >= 0; j--) {
        ds.add(config("n1", "p" + j));
      }
      Result<List<ExecConfig>> r = new Result<List<ExecConfig>>(host(RepoRole.CLIENT, "h" + i, i), ds, Result.Type.COLLECTION);
      results.addResult(r);
    }
    
    Results<List<ExecConfig>> sorted = Sorting.sortList(results, ExecConfig.class, SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.HOST_NAME).ascending(SortSwitch.EXEC_CONFIG_PROFILE).build());
    for (int i = 0; i < 5; i++) {
      Result<List<ExecConfig>> r = sorted.next();
      assertEquals("h" + i, r.getOrigin().getEndpoint().getServerTcpAddress().getHost());
      for (int j = 0; j < 5; j++) {
        ExecConfig d = r.getData().get(j);
        assertEquals("p" + j, d.getProfile());
      }
    }
  }
  
  @Test
  public void testGetPortRangeComparatorFor_Name_Ascending() throws Exception {
    PortRange r1 = range("r1");
    PortRange r2 = range("r2");

    Comparator<PortRange> c = Sorting.getPortRangeComparatorFor(SortSwitchInfo.Builder.newInstance().ascending(SortSwitch.PORT_RANGE_NAME).build());

    assertTrue(c.compare(r1, r2) < 0);
    assertTrue(c.compare(r2, r1) > 0);
    assertTrue(c.compare(r1, r1) == 0);
  }
  
  @Test
  public void testGetPortRangeComparatorFor_Name_Descending() throws Exception {
    PortRange r1 = range("r1");
    PortRange r2 = range("r2");

    Comparator<PortRange> c = Sorting.getPortRangeComparatorFor(SortSwitchInfo.Builder.newInstance().descending(SortSwitch.PORT_RANGE_NAME).build());

    assertTrue(c.compare(r2, r1) < 0);
    assertTrue(c.compare(r1, r2) > 0);
    assertTrue(c.compare(r1, r1) == 0);
  }

  private Process process(String dist, String version, String name, String profile, String id, String osPid) {
    Process p = new Process(new DistributionInfo(dist, version, profile, name), id);
    p.setOsPid(osPid);
    return p;
  }
  
  private Distribution distribution(String name, String version) {
    return new Distribution(name, version);
  }

  private CorusHost host(RepoRole role, String host, int port) {
    CorusHost h = CorusHost.newInstance(new Endpoint(new TCPAddress("test", host, port), channelAddress), "", "");
    h.setRepoRole(role);
    h.setHostName(host);
    return h;
  }
  
  private ClusterStatus status(EventChannel.Role role, String host, int port) {
    CorusHost h = CorusHost.newInstance(new Endpoint(new TCPAddress("test", host, port), channelAddress), "", "");
    h.setHostName(host);
    return new ClusterStatus(role, h);
  }
  
  private ExecConfig config(String name, String profile) {
    ExecConfig c = new ExecConfig();
    c.setName(name);
    c.setProfile(profile);
    return c;
  }
  
  private PortRange range(String name) throws Exception{
    PortRange r = new PortRange(name, 1, 2);
    return r;
  }
}
