package org.sapia.corus.numa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.assertj.core.data.MapEntry;
import org.junit.Test;

public class NumaProcessOptionsTest {


  @Test
  public void testAppendProcessOptions_invalidNodeId() {
    Map<String, String> options = new LinkedHashMap<>();
    NumaProcessOptions.appendProcessOptions(-1, false, false, options);

    assertThat(options).isEmpty();
  }

  @Test
  public void testAppendProcessOptions_noCpuBind_noMemoryBind() {
    Map<String, String> options = new LinkedHashMap<>();
    NumaProcessOptions.appendProcessOptions(0, false, false, options);

    assertThat(options).containsOnly(
        MapEntry.entry(NumaProcessOptions.NUMA_CORE_ID, "0"),
        MapEntry.entry(NumaProcessOptions.NUMA_BIND_CPU, "false"),
        MapEntry.entry(NumaProcessOptions.NUMA_BIND_MEMORY, "false"));
  }

  @Test
  public void testAppendProcessOptions_withCpuBind_noMemoryBind() {
    Map<String, String> options = new LinkedHashMap<>();
    NumaProcessOptions.appendProcessOptions(2, true, false, options);

    assertThat(options).containsOnly(
        MapEntry.entry(NumaProcessOptions.NUMA_CORE_ID, "2"),
        MapEntry.entry(NumaProcessOptions.NUMA_BIND_CPU, "true"),
        MapEntry.entry(NumaProcessOptions.NUMA_BIND_MEMORY, "false"));
  }

  @Test
  public void testAppendProcessOptions_noCpuBind_withMemoryBind() {
    Map<String, String> options = new LinkedHashMap<>();
    NumaProcessOptions.appendProcessOptions(3, false, true, options);

    assertThat(options).containsOnly(
        MapEntry.entry(NumaProcessOptions.NUMA_CORE_ID, "3"),
        MapEntry.entry(NumaProcessOptions.NUMA_BIND_CPU, "false"),
        MapEntry.entry(NumaProcessOptions.NUMA_BIND_MEMORY, "true"));
  }

  @Test
  public void testAppendProcessOptions_withCpuBind_withMemoryBind() {
    Map<String, String> options = new LinkedHashMap<>();
    NumaProcessOptions.appendProcessOptions(1, true, true, options);

    assertThat(options).containsOnly(
        MapEntry.entry(NumaProcessOptions.NUMA_CORE_ID, "1"),
        MapEntry.entry(NumaProcessOptions.NUMA_BIND_CPU, "true"),
        MapEntry.entry(NumaProcessOptions.NUMA_BIND_MEMORY, "true"));
  }

}
