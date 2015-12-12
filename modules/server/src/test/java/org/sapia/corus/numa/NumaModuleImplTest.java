package org.sapia.corus.numa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

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

    sut = new NumaModuleImpl(internalConfigurator, processor);
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
    sut.getNextNumaNode();
  }

  @Test
  public void testGetNextNumaNode_singleProcess_fromNode0() throws Exception {
    sut.setEnabled(true);
    sut.setFirstNumaNodeId(0);
    sut.setNumaNodeCount(4);

    int actual = sut.getNextNumaNode();

    assertThat(actual).isEqualTo(0);
  }

  @Test
  public void testGetNextNumaNode_singleProcess_fromNode2() throws Exception {
    sut.setEnabled(true);
    sut.setFirstNumaNodeId(2);
    sut.setNumaNodeCount(4);

    int actual = sut.getNextNumaNode();

    assertThat(actual).isEqualTo(2);
  }

  @Test
  public void testGetNextNumaNode_multiProcessesOneByNode_fromNode0() throws Exception {
    sut.setEnabled(true);
    sut.setFirstNumaNodeId(0);
    sut.setNumaNodeCount(4);

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

    String[] processIds = new String[3];
    int[] assignedNodeIds = new int[3];
    for (int i = 0; i < processIds.length; i++) {
      assignedNodeIds[i] = sut.getNextNumaNode();
      processesToReturn.add(doCreateProcessFor(assignedNodeIds[i]));
    }

    processesToReturn.remove(1);

    assertThat(sut.getNextNumaNode()).isEqualTo(1);
  }

}
