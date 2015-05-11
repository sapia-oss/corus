package org.sapia.corus.processor.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.taskmanager.core.TaskParams;

@RunWith(MockitoJUnitRunner.class)
public class PublishConfigurationChangeTaskTest extends TestBaseTask {

  @Mock
  private ProcessRepository processRepo;
  @Mock
  private Deployer deployer;
  
  private PublishConfigurationChangeTask task;
  
  @Before
  public void setUp() throws Exception {
    super.setUp();

    task = new PublishConfigurationChangeTask();
  }
  
  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void assertProcessConfigurationUpdated(org.sapia.corus.client.services.processor.Process actualProcess, Property... eProperties) {
    if (eProperties.length == 0) {
      verify(actualProcess, never()).configurationUpdated(anyCollectionOf(Property.class));
    } else {
      ArgumentCaptor<Collection> propertiesCaptor = ArgumentCaptor.forClass(Collection.class);
      verify(actualProcess).configurationUpdated(propertiesCaptor.capture());
      
      Collection<Property> actual = (Collection<Property>) propertiesCaptor.getValue();
      assertThat(actual).contains(eProperties);
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected void assertProcessConfigurationDeleted(org.sapia.corus.client.services.processor.Process actualProcess, Property... eProperties) {
    if (eProperties.length == 0) {
      verify(actualProcess, never()).configurationDeleted(anyCollectionOf(Property.class));
    } else {
      ArgumentCaptor<Collection> propertiesCaptor = ArgumentCaptor.forClass(Collection.class);
      verify(actualProcess).configurationDeleted(propertiesCaptor.capture());
      
      Collection<Property> actual = (Collection<Property>) propertiesCaptor.getValue();
      assertThat(actual).contains(eProperties);
    }
  }

  
  
  
  @Test
  public void testExecute_noActiveProcess() throws Throwable {
    List<Property> updatedProperties = new ArrayList<>();
    List<Property> deletedProperties = new ArrayList<>();
    updatedProperties.add(new Property("nothing", "runs", null));
    
    tm.executeAndWait(task, TaskParams.createFor(updatedProperties, deletedProperties))
        .get(10000L);
  }
  
  @Test
  public void testExecute_singleProcess_noPropertyChange() throws Throwable {
    List<Property> updatedProperties = new ArrayList<>();
    List<Property> deletedProperties = new ArrayList<>();
    
    Distribution distA = super.createDistribution("distA", "1.0");
    ProcessConfig serverProcess = super.createProcessConfig(distA, "server", "base");
    org.sapia.corus.client.services.processor.Process process = super.createProcess(distA, serverProcess, "base");
    
    tm.executeAndWait(task, TaskParams.createFor(updatedProperties, deletedProperties))
        .get(10000L);
    
    assertProcessConfigurationUpdated(process);
    assertProcessConfigurationDeleted(process);
  }

  @Test
  public void testExecute_singleProcess_addedProperty_noCategory() throws Throwable {
    List<Property> updatedProperties = new ArrayList<>();
    List<Property> deletedProperties = new ArrayList<>();
    updatedProperties.add(new Property("sna", "foo", null));
    
    Distribution distA = super.createDistribution("distA", "1.0");
    ProcessConfig serverProcess = super.createProcessConfig(distA, "server", "base");
    org.sapia.corus.client.services.processor.Process process = super.createProcess(distA, serverProcess, "base");
    
    tm.executeAndWait(task, TaskParams.createFor(updatedProperties, deletedProperties))
        .get(10000L);
    
    assertProcessConfigurationUpdated(process, new Property("sna", "foo", null));
    assertProcessConfigurationDeleted(process);
  }

  @Test
  public void testExecute_singleProcess_addedProperty_categoryNotfound() throws Throwable {
    List<Property> updatedProperties = new ArrayList<>();
    List<Property> deletedProperties = new ArrayList<>();
    updatedProperties.add(new Property("sna", "foo", "AAA"));
    
    Distribution distA = super.createDistribution("distA", "1.0");
    distA.setPropertyCategories("other");
    ProcessConfig serverProcess = super.createProcessConfig(distA, "server", "base");
    serverProcess.setPropertyCategories("other");
    org.sapia.corus.client.services.processor.Process process = super.createProcess(distA, serverProcess, "base");
    
    tm.executeAndWait(task, TaskParams.createFor(updatedProperties, deletedProperties))
        .get(10000L);
    
    assertProcessConfigurationUpdated(process);
    assertProcessConfigurationDeleted(process);
  }

  @Test
  public void testExecute_singleProcess_addedProperty_categoryOnDistribution() throws Throwable {
    List<Property> updatedProperties = new ArrayList<>();
    List<Property> deletedProperties = new ArrayList<>();
    updatedProperties.add(new Property("sna", "foo", "AAA"));
    
    Distribution distA = super.createDistribution("distA", "1.0");
    distA.setPropertyCategories("AAA");
    ProcessConfig serverProcess = super.createProcessConfig(distA, "server", "base");
    serverProcess.setPropertyCategories("other");
    org.sapia.corus.client.services.processor.Process process = super.createProcess(distA, serverProcess, "base");
    
    tm.executeAndWait(task, TaskParams.createFor(updatedProperties, deletedProperties))
        .get(10000L);
    
    assertProcessConfigurationUpdated(process, new Property("sna", "foo", "AAA"));
    assertProcessConfigurationDeleted(process);
  }

  @Test
  public void testExecute_singleProcess_addedProperty_categoryOnProcess() throws Throwable {
    List<Property> updatedProperties = new ArrayList<>();
    List<Property> deletedProperties = new ArrayList<>();
    updatedProperties.add(new Property("sna", "foo", "AAA"));
    
    Distribution distA = super.createDistribution("distA", "1.0");
    distA.setPropertyCategories("other");
    ProcessConfig serverProcess = super.createProcessConfig(distA, "server", "base");
    serverProcess.setPropertyCategories("AAA");
    org.sapia.corus.client.services.processor.Process process = super.createProcess(distA, serverProcess, "base");
    
    tm.executeAndWait(task, TaskParams.createFor(updatedProperties, deletedProperties))
        .get(10000L);
    
    assertProcessConfigurationUpdated(process, new Property("sna", "foo", "AAA"));
    assertProcessConfigurationDeleted(process);
  }

  @Test
  public void testExecute_singleProcess_addedProperties_noCategory() throws Throwable {
    List<Property> updatedProperties = new ArrayList<>();
    List<Property> deletedProperties = new ArrayList<>();
    updatedProperties.add(new Property("sna1", "foo1", null));
    updatedProperties.add(new Property("sna2", "foo2", null));
    updatedProperties.add(new Property("sna3", "foo3", null));
    
    Distribution distA = super.createDistribution("distA", "1.0");
    ProcessConfig serverProcess = super.createProcessConfig(distA, "server", "base");
    org.sapia.corus.client.services.processor.Process process = super.createProcess(distA, serverProcess, "base");
    
    tm.executeAndWait(task, TaskParams.createFor(updatedProperties, deletedProperties))
        .get(10000L);
    
    assertProcessConfigurationUpdated(process, new Property("sna1", "foo1", null), new Property("sna2", "foo2", null), new Property("sna3", "foo3", null));
    assertProcessConfigurationDeleted(process);
  }

  @Test
  public void testExecute_singleProcess_addedProperties_sameCategory() throws Throwable {
    List<Property> updatedProperties = new ArrayList<>();
    List<Property> deletedProperties = new ArrayList<>();
    updatedProperties.add(new Property("sna1", "foo1", "AAA"));
    updatedProperties.add(new Property("sna2", "foo2", "AAA"));
    updatedProperties.add(new Property("sna3", "foo3", "AAA"));
    
    Distribution distA = super.createDistribution("distA", "1.0");
    distA.setPropertyCategories("AAA");
    ProcessConfig serverProcess = super.createProcessConfig(distA, "server", "base");
    org.sapia.corus.client.services.processor.Process process = super.createProcess(distA, serverProcess, "base");
    
    tm.executeAndWait(task, TaskParams.createFor(updatedProperties, deletedProperties))
        .get(10000L);
    
    assertProcessConfigurationUpdated(process, new Property("sna1", "foo1", "AAA"), new Property("sna2", "foo2", "AAA"), new Property("sna3", "foo3", "AAA"));
    assertProcessConfigurationDeleted(process);
  }

  @Test
  public void testExecute_singleProcess_addedProperties_differentCategory() throws Throwable {
    List<Property> updatedProperties = new ArrayList<>();
    List<Property> deletedProperties = new ArrayList<>();
    updatedProperties.add(new Property("sna1", "foo1", "AAA"));
    updatedProperties.add(new Property("sna1", "foo1", "BBB"));
    updatedProperties.add(new Property("sna1", "foo1", "CCC"));
    
    Distribution distA = super.createDistribution("distA", "1.0");
    ProcessConfig serverProcess = super.createProcessConfig(distA, "server", "base");
    distA.setPropertyCategories("AAA,BBB");
    org.sapia.corus.client.services.processor.Process process = super.createProcess(distA, serverProcess, "base");
    
    tm.executeAndWait(task, TaskParams.createFor(updatedProperties, deletedProperties))
        .get(10000L);
    
    assertProcessConfigurationUpdated(process, new Property("sna1", "foo1", "AAA"));
    assertProcessConfigurationDeleted(process);
  }
  
  @Test
  public void testExecute_singleProcess_deletedProperty() throws Throwable {
    List<Property> updatedProperties = new ArrayList<>();
    List<Property> deletedProperties = new ArrayList<>();
    deletedProperties.add(new Property("foo", "old", null));
    
    Distribution distA = super.createDistribution("distA", "1.0");
    ProcessConfig serverProcess = super.createProcessConfig(distA, "server", "base");
    org.sapia.corus.client.services.processor.Process process = super.createProcess(distA, serverProcess, "base");
    
    tm.executeAndWait(task, TaskParams.createFor(updatedProperties, deletedProperties))
        .get(10000L);
    
    assertProcessConfigurationUpdated(process);
    assertProcessConfigurationDeleted(process, new Property("foo", "old", null));
  }
  
}
