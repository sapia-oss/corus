package org.sapia.corus.numa;

import java.util.Map;

import org.sapia.corus.os.NativeProcessOptions;


public class NumaProcessOptions extends NativeProcessOptions {

  /** Process option that defines the numa core id to which this process is assotiated (integer value). */
  public static final String NUMA_CORE_ID = "option.numa.core.id";

  /** Process option that defines if the cpu should be binded to a numa core (boolean value). */
  public static final String NUMA_BIND_CPU = "option.numa.bind.cpu";

  /** Process option that defines if the memory should be binded to a numa core (boolean value). */
  public static final String NUMA_BIND_MEMORY = "option.numa.bind.memory";

  /**
   * Generates native process options for NUMA integration and appends them to the map provided.
   *
   * @param numaNodeId The numa node id binded.
   * @param isBindingCpu Flag indicating if cpu binding is required.
   * @param isBindingMemory Flag indicating if memory binding is required.
   * @param nativeProcessOptions The map in which to add the generated native process options.
   */
  public static void appendProcessOptions(int numaNodeId, boolean isBindingCpu, boolean isBindingMemory, Map<String, String> nativeProcessOptions) {
    if (numaNodeId >= 0) {
      nativeProcessOptions.put(NUMA_CORE_ID, String.valueOf(numaNodeId));
      nativeProcessOptions.put(NUMA_BIND_CPU, String.valueOf(isBindingCpu));
      nativeProcessOptions.put(NUMA_BIND_MEMORY, String.valueOf(isBindingMemory));
    }
  }

}
