package org.sapia.corus.os;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.numa.NumaProcessOptions;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@RunWith(MockitoJUnitRunner.class)
public class UnixProcessTest {

  private UnixProcess sut;
  
  private FilePath userBinJava;

  @Before
  public void setUp() throws Exception {
    sut = new UnixProcess();
    userBinJava = FilePath.newInstance().addDir("/usr").addDir("bin").addDir("java");
  }

  @Test
  public void testDoGenerateJavaCommandLine_noProcessOption() throws Exception {
    System.setProperty("corus.home", System.getProperty("user.dir"));
    File tmpFile = File.createTempFile(UnixProcessTest.class.getSimpleName(), "test");
    CmdLine command = CmdLine.parse(userBinJava.createFilePath() + " -server -Xmx4g -Dmyapp.property=snafoo my.app.MainClass debug");
    CmdLine actual = sut.doGenerateJavaCommandLine(tmpFile.getParentFile(), command, new HashMap<String, String>());

    assertThat(actual).isNotNull();
    assertThat(actual.toString()).isEqualTo("sh "
          + System.getProperty("corus.home") + FilePath.newInstance().addDir("/bin").setRelativeFile("javastart.sh").createFilePath()
          + " -o " + tmpFile.getParentFile().getAbsolutePath() + FilePath.newInstance().addDir("/").setRelativeFile("process.out").createFilePath()
          + " " +  userBinJava.createFilePath() + " -server -Xmx4g -Dmyapp.property=snafoo my.app.MainClass debug");
  }

  @Test
  public void testDoGenerateJavaCommandLine_withNumaProcessOptions() throws Exception {
    System.setProperty("corus.home", System.getProperty("user.dir"));
    File tmpFile = File.createTempFile(UnixProcessTest.class.getSimpleName(), "test");
    CmdLine command = CmdLine.parse(userBinJava.createFilePath() + " -server -Xmx4g -Dmyapp.property=snafoo my.app.MainClass debug");
    CmdLine actual = sut.doGenerateJavaCommandLine(tmpFile.getParentFile(), command, ImmutableMap.of(
            NumaProcessOptions.NUMA_CORE_ID, "2",
            NumaProcessOptions.NUMA_BIND_CPU, "true",
            NumaProcessOptions.NUMA_BIND_MEMORY, "true"));

    assertThat(actual).isNotNull();
    assertThat(actual.toString()).isEqualTo("sh "
          + System.getProperty("corus.home") + FilePath.newInstance().addDir("/bin").setRelativeFile("javastart.sh").createFilePath()
          + " -o " + tmpFile.getParentFile().getAbsolutePath() + FilePath.newInstance().addDir("/").setRelativeFile("process.out").createFilePath()
          + " numactl --cpunodebind=2 --membind=2 -- " + userBinJava.createFilePath() + " -server -Xmx4g -Dmyapp.property=snafoo my.app.MainClass debug");
  }

  @Test
  public void testDoProcessNumaOptions_emptyMap() throws Exception {
    CmdLine actual = sut.doProcessNumaOptions(Maps.<String, String>newHashMap());

    assertThat(actual).isNotNull();
    assertThat(actual.toString()).isEqualTo("");
  }

  @Test
  public void testDoProcessNumaOptions_numaCtlOnly() throws Exception {
    CmdLine actual = sut.doProcessNumaOptions(ImmutableMap.of(NumaProcessOptions.NUMA_CORE_ID, "0"));

    assertThat(actual).isNotNull();
    assertThat(actual.toString()).isEqualTo("numactl --");
  }

  @Test(expected = NumberFormatException.class)
  public void testDoProcessNumaOptions_numaCtl_invalidValue() throws Exception {
    sut.doProcessNumaOptions(ImmutableMap.of(NumaProcessOptions.NUMA_CORE_ID, "A"));
  }

  @Test
  public void testDoProcessNumaOptions_numaCtl_cpuBind() throws Exception {
    CmdLine actual = sut.doProcessNumaOptions(ImmutableMap.of(
        NumaProcessOptions.NUMA_CORE_ID, "0",
        NumaProcessOptions.NUMA_BIND_CPU, "true",
        NumaProcessOptions.NUMA_BIND_MEMORY, "false"));

    assertThat(actual).isNotNull();
    assertThat(actual.toString()).isEqualTo("numactl --cpunodebind=0 --");
  }

  @Test
  public void testDoProcessNumaOptions_numaCtl_memoryBind() throws Exception {
    CmdLine actual = sut.doProcessNumaOptions(ImmutableMap.of(
        NumaProcessOptions.NUMA_CORE_ID, "1",
        NumaProcessOptions.NUMA_BIND_CPU, "false",
        NumaProcessOptions.NUMA_BIND_MEMORY, "true"));

    assertThat(actual).isNotNull();
    assertThat(actual.toString()).isEqualTo("numactl --membind=1 --");
  }

  @Test
  public void testDoProcessNumaOptions_numaCtl_cpuAndMemoryBind() throws Exception {
    CmdLine actual = sut.doProcessNumaOptions(ImmutableMap.of(
        NumaProcessOptions.NUMA_CORE_ID, "2",
        NumaProcessOptions.NUMA_BIND_CPU, "true",
        NumaProcessOptions.NUMA_BIND_MEMORY, "true"));

    assertThat(actual).isNotNull();
    assertThat(actual.toString()).isEqualTo("numactl --cpunodebind=2 --membind=2 --");
  }

}
