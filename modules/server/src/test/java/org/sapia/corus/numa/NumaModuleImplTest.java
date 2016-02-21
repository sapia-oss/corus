package org.sapia.corus.numa;



import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.configurator.InternalConfigurator;

@RunWith(MockitoJUnitRunner.class)
public class NumaModuleImplTest {

  private static final AtomicInteger SEQUENCE = new AtomicInteger(1);

  @Mock
  private InternalConfigurator internalConfigurator;
  private List<org.sapia.corus.client.services.processor.Process> processesToReturn;

  @Mock
  private Processor processor;

  @Mock
  private OsModule osModule;
  private StringBuilder scriptOutput = new StringBuilder();
  private StringBuilder errorOutput = new StringBuilder();

  private NumaModuleImpl sut;


  @Before
  public void setUp() throws Exception {
    processesToReturn = new ArrayList<>();
    when(processor.getProcesses(any(ProcessCriteria.class))).
        thenAnswer(new Answer<List<org.sapia.corus.client.services.processor.Process>>() {
          @Override
          public List<org.sapia.corus.client.services.processor.Process> answer(InvocationOnMock invocation) throws Throwable {
            return processesToReturn;
          }
        });

    doAnswer(new Answer<Void>() {
        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
          LogCallback callback = invocation.getArgumentAt(0, LogCallback.class);
          if (scriptOutput.length() > 0) {
            callback.info(scriptOutput.toString());
          }
          if (errorOutput.length() > 0) {
            callback.error(errorOutput.toString());
          }
          return null;
        }
      })
      .when(osModule).executeScript(any(LogCallback.class), any(File.class), any(CmdLine.class));

    sut = new NumaModuleImpl(internalConfigurator, processor, osModule);
    sut.setEnabled(false);
    sut.setAutoDetectionEnabled(false);
  }

  protected org.sapia.corus.client.services.processor.Process doCreateProcessFor(Integer aNumaNodeId) {
    DistributionInfo distribution = new DistributionInfo(RandomStringUtils.randomAlphabetic(10),
            RandomStringUtils.randomNumeric(2),
            RandomStringUtils.randomAlphabetic(5),
            RandomStringUtils.randomAlphabetic(8));
    String processId = "process-" + SEQUENCE.getAndIncrement();

    org.sapia.corus.client.services.processor.Process created = new org.sapia.corus.client.services.processor.Process(distribution, processId);
    if (aNumaNodeId != null) {
      created.setNumaNode(aNumaNodeId);
    }

    return created;
  }



  @Test
  public void testGetProcessBindings_disabled() throws Exception {
    sut.setEnabled(false);
    Map<String, Integer> actual = sut.getProcessBindings();

    assertThat(actual).isEmpty();
  }

  @Test
  public void testGetProcessBindings_empty() throws Exception {
    sut.setEnabled(true);
    Map<String, Integer> actual = sut.getProcessBindings();
    assertThat(actual).isEmpty();
  }

  @Test
  public void testGetProcessBindings_singleProcess_noNumaNode() throws Exception {
    sut.setEnabled(true);
    processesToReturn.add(doCreateProcessFor(null));

    Map<String, Integer> actual = sut.getProcessBindings();

    assertThat(actual).isEmpty();
  }

  @Test
  public void testGetProcessBindings_singleProcess_assignedNumaNode() throws Exception {
    sut.setEnabled(true);
    processesToReturn.add(doCreateProcessFor(3));

    Map<String, Integer> actual = sut.getProcessBindings();

    assertThat(actual).containsOnly(
        MapEntry.entry(processesToReturn.get(0).getProcessID(), 3));
  }

  @Test
  public void testGetProcessBindings_multiProcesses_notAllAssignedNumaNode() throws Exception {
    sut.setEnabled(true);
    processesToReturn.add(doCreateProcessFor(4));
    processesToReturn.add(doCreateProcessFor(null));
    processesToReturn.add(doCreateProcessFor(6));

    Map<String, Integer> actual = sut.getProcessBindings();

    assertThat(actual).containsOnly(
        MapEntry.entry(processesToReturn.get(0).getProcessID(), 4),
        MapEntry.entry(processesToReturn.get(2).getProcessID(), 6));
  }

  @Test
  public void testGetProcessBindings_multiProcesses_allAssignedNumaNode() throws Exception {
    sut.setEnabled(true);
    processesToReturn.add(doCreateProcessFor(3));
    processesToReturn.add(doCreateProcessFor(5));
    processesToReturn.add(doCreateProcessFor(7));

    Map<String, Integer> actual = sut.getProcessBindings();

    assertThat(actual).containsOnly(
        MapEntry.entry(processesToReturn.get(0).getProcessID(), 3),
        MapEntry.entry(processesToReturn.get(1).getProcessID(), 5),
        MapEntry.entry(processesToReturn.get(2).getProcessID(), 7));
  }



  @Test(expected = IllegalStateException.class)
  public void testGetNextNumaNode_disbaled() throws Exception {
    sut.setEnabled(false);
    sut.init();

    sut.getNextNumaNode();
  }

  @Test
  public void testGetNextNumaNode_autoDetect_singleProcess_fromNode0() throws Exception {
    scriptOutput.append("available: 4 nodes (0-3)");

    sut.setFirstNumaNodeId(0);
    sut.setNumaNodeCount(-1);
    sut.setAutoDetectionEnabled(true);
    sut.setEnabled(true);
    sut.init();

    int actual = sut.getNextNumaNode();

    assertThat(actual).isEqualTo(0);
  }

  @Test
  public void testGetNextNumaNode_manualOverride_singleProcess_fromNode0() throws Exception {
    sut.setEnabled(true);
    sut.setFirstNumaNodeId(0);
    sut.setNumaNodeCount(4);
    sut.init();

    int actual = sut.getNextNumaNode();

    assertThat(actual).isEqualTo(0);
  }

  @Test
  public void testGetNextNumaNode_manualOverride_singleProcess_fromNode2() throws Exception {
    sut.setEnabled(true);
    sut.setFirstNumaNodeId(2);
    sut.setNumaNodeCount(4);
    sut.init();

    int actual = sut.getNextNumaNode();

    assertThat(actual).isEqualTo(2);
  }

  @Test
  public void testGetNextNumaNode_autoDetect_singleProcess_fromNode2() throws Exception {
    scriptOutput.append("available: 4 nodes (0-3)");

    sut.setFirstNumaNodeId(2);
    sut.setNumaNodeCount(-1);
    sut.setAutoDetectionEnabled(true);
    sut.setEnabled(true);
    sut.init();

    int actual = sut.getNextNumaNode();

    assertThat(actual).isEqualTo(2);
  }

  @Test
  public void testGetNextNumaNode_multiProcessesOneByNode_fromNode0() throws Exception {
    sut.setEnabled(true);
    sut.setFirstNumaNodeId(0);
    sut.setNumaNodeCount(4);
    sut.init();

    int[] actuals = new int[4];
    for (int i = 0; i < actuals.length; i++) {
      actuals[i] = sut.getNextNumaNode();
      processesToReturn.add(doCreateProcessFor(actuals[i]));
    }

    assertThat(actuals).containsSequence(0, 1, 2, 3);
  }

  @Test
  public void testGetNextNumaNode_multiProcessesOneByNode_fromNode2() throws Exception {
    sut.setEnabled(true);
    sut.setFirstNumaNodeId(2);
    sut.setNumaNodeCount(4);
    sut.init();

    int[] actuals = new int[2];
    for (int i = 0; i < actuals.length; i++) {
      actuals[i] = sut.getNextNumaNode();
      processesToReturn.add(doCreateProcessFor(actuals[i]));
    }

    assertThat(actuals).containsSequence(2, 3);
  }

  @Test
  public void testBindProcess_multiProcessesMultiByNode_fromNode0() throws Exception {
    sut.setEnabled(true);
    sut.setFirstNumaNodeId(0);
    sut.setNumaNodeCount(4);
    sut.init();

    int[] actuals = new int[8];
    for (int i = 0; i < actuals.length; i++) {
      actuals[i] = sut.getNextNumaNode();
      processesToReturn.add(doCreateProcessFor(actuals[i]));
    }

    assertThat(actuals).containsSequence(0, 1, 2, 3, 0, 1, 2, 3);
  }

  @Test
  public void testBindProcess_multiProcessesMultiByNode_fromNode2() throws Exception {
    sut.setEnabled(true);
    sut.setFirstNumaNodeId(2);
    sut.setNumaNodeCount(4);
    sut.init();

    int[] actuals = new int[4];
    for (int i = 0; i < actuals.length; i++) {
      actuals[i] = sut.getNextNumaNode();
      processesToReturn.add(doCreateProcessFor(actuals[i]));
    }

    assertThat(actuals).containsSequence(2, 3, 2, 3);
  }

  @Test
  public void testBindProcess_multiProcesses_reuseReleasedNode() throws Exception {
    sut.setEnabled(true);
    sut.setFirstNumaNodeId(0);
    sut.setNumaNodeCount(4);
    sut.init();

    String[] processIds = new String[3];
    int[] assignedNodeIds = new int[3];
    for (int i = 0; i < processIds.length; i++) {
      assignedNodeIds[i] = sut.getNextNumaNode();
      processesToReturn.add(doCreateProcessFor(assignedNodeIds[i]));
    }

    processesToReturn.remove(1);

    assertThat(sut.getNextNumaNode()).isEqualTo(1);
  }



  @Test
  public void testDoDetectNuma_noOutput() throws Exception {
    sut.doDetectNuma();

    assertThat(sut.getDetectedNumaNodeCount().isNull()).isTrue();
  }

  @Test
  public void testDoDetectNuma_exception() throws Exception {
    doThrow(new IOException("Simulated error failure"))
    .when(osModule).executeScript(any(LogCallback.class), any(File.class), any(CmdLine.class));

    sut.doDetectNuma();

    assertThat(sut.getDetectedNumaNodeCount().isNull()).isTrue();
  }

  @Test
  public void testDoDetectNuma_withErrorOutput() throws Exception {
    errorOutput.append("numactl: command not found");

    sut.doDetectNuma();

    assertThat(sut.getDetectedNumaNodeCount().isNull()).isTrue();
  }

  @Test
  public void testDoDetectNuma_withNumaOutput_noPatternFound() throws Exception {
    scriptOutput.append("totally invalid output with no expected pattern");

    sut.doDetectNuma();

    assertThat(sut.getDetectedNumaNodeCount().isNull()).isTrue();
  }

  @Test
  public void testDoDetectNuma_withNumaOutput_patternFoundWithNoNumber() throws Exception {
    scriptOutput.append("available: xcx nodes (0)\nnode 0 cpus: 0 1 2 3 4\nnode 0 size: 786421 MB\nnode 0 free: 634409 MB\nnode distances:\nnode 0\n0: 10");

    sut.doDetectNuma();

    assertThat(sut.getDetectedNumaNodeCount().isNull()).isTrue();
  }

  @Test
  public void testDoDetectNuma_withNumaOutput_valid() throws Exception {
    scriptOutput.append("available: 4 nodes (0-3)\nnode 0 cpus: 0 1 2 3 4 5 6 7 8 9 40 41 42 43 44 45 46 47 48 49\nnode 0 size: 196597 MB\nnode 0 free: 2941 MB\nnode 1 cpus: 10 11 12 13 14 15 16 17 18 19 50 51 52 53 54 55 56 57 58 59\nnode 1 size: 196608 MB\nnode 1 free: 50873 MB\nnode 2 cpus: 20 21 22 23 24 25 26 27 28 29 60 61 62 63 64 65 66 67 68 69\nnode 2 size: 196608 MB\nnode 2 free: 2651 MB\nnode 3 cpus: 30 31 32 33 34 35 36 37 38 39 70 71 72 73 74 75 76 77 78 79\nnode 3 size: 196607 MB\nnode 3 free: 1365 MB\nnode distances:\nnode 0 1 2 3\n0: 10 20 20 20\n1: 20 10 20 20\n2: 20 20 10 20\n3: 20 20 20 10");

    sut.doDetectNuma();

    assertThat(sut.getDetectedNumaNodeCount().isSet()).isTrue();
    assertThat(sut.getDetectedNumaNodeCount().get()).isEqualTo(4);
  }

}
