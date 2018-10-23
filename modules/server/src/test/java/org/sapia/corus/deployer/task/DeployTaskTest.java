package org.sapia.corus.deployer.task;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.TestServerContext;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.event.DeploymentCompletedEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentFailedEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentScriptExecutedEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentStartingEvent;
import org.sapia.corus.client.services.deployer.event.DeploymentUnzippedEvent;
import org.sapia.corus.client.services.deployer.event.RollbackCompletedEvent;
import org.sapia.corus.client.services.deployer.event.RollbackFailedEvent;
import org.sapia.corus.client.services.deployer.event.RollbackStartingEvent;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.corus.deployer.processor.DeploymentContext;
import org.sapia.corus.deployer.processor.DeploymentProcessorManager;
import org.sapia.corus.taskmanager.core.TaskParams;

@RunWith(MockitoJUnitRunner.class)
public class DeployTaskTest {
  
  private static final String TEST_RESOURCE = "testCorus.xml";
  
  private TestServerContext ctx;
  
  @Mock
  private FileSystemModule fs;
  
  @Mock
  private RepositoryConfiguration conf;
  
  @Mock
  private EventDispatcher dispatcher;
  
  @Mock
  private File preDeploy, postDeploy, rollback, scriptDir, distZip;
  
  private DeploymentProcessorManager processors;
    
  @Before
  public void setUp() throws Exception {
    System.setProperty("corus.home", System.getProperty("user.dir"));
    ctx = TestServerContext.create();
    ctx.getServices().bind(RepositoryConfiguration.class, conf);
    processors = ctx.getServices().lookup(DeploymentProcessorManager.class);
    
    when(preDeploy.getName()).thenReturn("pre-deploy.corus");
    when(postDeploy.getName()).thenReturn("post-deploy.corus");
    when(rollback.getName()).thenReturn("rollback.corus");
    when(distZip.getName()).thenReturn("testFile.zip");
    when(distZip.getAbsolutePath()).thenReturn("tmp/testFile.zip");
    when(fs.exists(any(File.class))).thenReturn(Boolean.FALSE);
    when(fs.openZipEntryStream(any(File.class), any(String.class)))
      .thenReturn(getCorusXmlStream());

    doAnswer(new Answer<File>() {
      @Override
      public File answer(InvocationOnMock invocation) throws Throwable {
        String fName = (String) invocation.getArguments()[0];
        if (fName.contains("pre-deploy.corus")) return preDeploy;
        else if (fName.contains("post-deploy.corus")) return postDeploy;
        else if (fName.contains("rollback.corus")) return rollback;
        else {
          File mockFile = mock(File.class);
          when(mockFile.getAbsolutePath()).thenReturn("test");
          when(mockFile.exists()).thenReturn(true);
          return mockFile;
        }
      }
    }).when(fs).getFileHandle(anyString());
    
    
    doAnswer(new Answer<Reader>() {
      
      @Override
      public Reader answer(InvocationOnMock invocation) throws Throwable {
        File f = invocation.getArgumentAt(0, File.class);
        if (f.getName().contains("pre-deploy.corus")) {
          return new StringReader("echo \"running pre-deploy\"");
        } else if (f.getName().contains("post-deploy.corus")) {
          return new StringReader("echo \"running post-deploy\"");
        } else {
          return new StringReader("echo \"running rollback\"");
        }
      }
      
    }).when(fs).getFileReader(any(File.class));
    
    ctx.getServices().rebind(FileSystemModule.class, fs);
    ctx.getServices().rebind(EventDispatcher.class, dispatcher);
  }
  
  @After
  public void tearDown() {
    System.clearProperty("corus.home");
  }
  
  @Test
  public void testExecute() throws Exception{
    
    DeployTask task = new DeployTask();
    ctx.getTm().executeAndWait(task, TaskParams.createFor(distZip, DeployPreferences.newInstance())).get();
    DistributionCriteria criteria = DistributionCriteria.builder().name("test").version("1.0").build();

    assertTrue("Distribution was not deployed", ctx.getDepl().getDistributionDatabase().containsDistribution(criteria));
    verify(fs).openZipEntryStream(any(File.class), eq("META-INF/corus.xml"));
    verify(fs, times(2)).createDirectory(any(File.class));
    verify(fs).unzip(any(File.class), any(File.class));
    verify(fs).deleteFile(any(File.class));
    verify(dispatcher).dispatch(isA(DeploymentStartingEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentUnzippedEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentCompletedEvent.class));
    verify(processors).onPostDeploy(any(DeploymentContext.class), any(LogCallback.class));
  }
  
  @Test
  public void testExecute_pre_deploy_script() throws Exception{
    when(preDeploy.exists()).thenReturn(true);
        
    DeployTask task = new DeployTask();
    ctx.getTm().executeAndWait(task, TaskParams.createFor(distZip, DeployPreferences.newInstance().executeDeployScripts())).get();
    DistributionCriteria criteria = DistributionCriteria.builder().name("test").version("1.0").build();

    assertTrue("Distribution was not deployed", ctx.getDepl().getDistributionDatabase().containsDistribution(criteria));
    verify(dispatcher).dispatch(isA(DeploymentStartingEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentUnzippedEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentCompletedEvent.class));
    verify(processors).onPostDeploy(any(DeploymentContext.class), any(LogCallback.class));
  }
  
  @Test
  public void testExecute_pre_deploy_script_for_repo_client() throws Exception{
    ctx.getCorusHost().setRepoRole(RepoRole.CLIENT);
    when(conf.isRepoClientDeployScriptEnabled()).thenReturn(true);
    when(preDeploy.exists()).thenReturn(true);
        
    DeployTask task = new DeployTask();
    ctx.getTm().executeAndWait(task, TaskParams.createFor(distZip, DeployPreferences.newInstance())).get();
    DistributionCriteria criteria = DistributionCriteria.builder().name("test").version("1.0").build();

    assertTrue("Distribution was not deployed", ctx.getDepl().getDistributionDatabase().containsDistribution(criteria));
    verify(dispatcher).dispatch(isA(DeploymentScriptExecutedEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentStartingEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentUnzippedEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentCompletedEvent.class));
    verify(processors).onPostDeploy(any(DeploymentContext.class), any(LogCallback.class));
  }
  
  @Test
  public void testExecute_pre_deploy_script_disabled_for_repo_client() throws Exception{
    ctx.getCorusHost().setRepoRole(RepoRole.CLIENT);
    when(preDeploy.exists()).thenReturn(true);
        
    DeployTask task = new DeployTask();
    ctx.getTm().executeAndWait(task, TaskParams.createFor(distZip, DeployPreferences.newInstance())).get();
    DistributionCriteria criteria = DistributionCriteria.builder().name("test").version("1.0").build();

    assertTrue("Distribution was not deployed", ctx.getDepl().getDistributionDatabase().containsDistribution(criteria));
    verify(dispatcher, never()).dispatch(isA(DeploymentScriptExecutedEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentStartingEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentUnzippedEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentCompletedEvent.class));
    verify(processors).onPostDeploy(any(DeploymentContext.class), any(LogCallback.class));
  }
  
  @Test
  public void testExecute_pre_deploy_script_not_found() throws Exception{
    when(preDeploy.exists()).thenReturn(false);
        
    DeployTask task = new DeployTask();
    ctx.getTm().executeAndWait(task, TaskParams.createFor(distZip, DeployPreferences.newInstance().executeDeployScripts())).get();
    DistributionCriteria criteria = DistributionCriteria.builder().name("test").version("1.0").build();

    assertFalse("Distribution was deployed but should not have been", 
        ctx.getDepl().getDistributionDatabase().containsDistribution(criteria));
    verify(fs, times(2)).deleteDirectory(any(File.class));
    verify(dispatcher).dispatch(isA(DeploymentStartingEvent.class));
    verify(dispatcher, never()).dispatch(isA(DeploymentUnzippedEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentFailedEvent.class));
    verify(processors).onPostUndeploy(any(DeploymentContext.class), any(LogCallback.class));
  }
  
  @Test
  public void testExecute_pre_deploy_script_not_found_for_repo_client() throws Exception{
    ctx.getCorusHost().setRepoRole(RepoRole.CLIENT);
    when(preDeploy.exists()).thenReturn(false);
        
    DeployTask task = new DeployTask();
    ctx.getTm().executeAndWait(task, TaskParams.createFor(distZip, DeployPreferences.newInstance().executeDeployScripts())).get();
    DistributionCriteria criteria = DistributionCriteria.builder().name("test").version("1.0").build();

    assertTrue("Distribution should have been deployed", 
        ctx.getDepl().getDistributionDatabase().containsDistribution(criteria));
    verify(fs).deleteDirectory(any(File.class));
    verify(dispatcher).dispatch(isA(DeploymentStartingEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentUnzippedEvent.class));
    verify(dispatcher, never()).dispatch(isA(DeploymentFailedEvent.class));
    verify(processors).onPostDeploy(any(DeploymentContext.class), any(LogCallback.class));
  }
  
  @Test
  public void testExecute_post_deploy_script() throws Exception {
    when(preDeploy.exists()).thenReturn(true);
    when(postDeploy.exists()).thenReturn(true);
    
    DeployTask task = new DeployTask();
    ctx.getTm().executeAndWait(task, TaskParams.createFor(distZip, DeployPreferences.newInstance().executeDeployScripts())).get();
    DistributionCriteria criteria = DistributionCriteria.builder().name("test").version("1.0").build();
    
    assertTrue("Distribution was not deployed", 
        ctx.getDepl().getDistributionDatabase().containsDistribution(criteria));

    verify(preDeploy).exists();
    verify(postDeploy).exists();
    verify(dispatcher).dispatch(isA(DeploymentStartingEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentUnzippedEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentCompletedEvent.class));
    verify(processors).onPostDeploy(any(DeploymentContext.class), any(LogCallback.class));
  }
  
  @Test
  public void testExecute_post_deploy_script_for_repo_client() throws Exception {
    ctx.getCorusHost().setRepoRole(RepoRole.CLIENT);
    when(conf.isRepoClientDeployScriptEnabled()).thenReturn(true);
    when(preDeploy.exists()).thenReturn(true);
    when(postDeploy.exists()).thenReturn(true);
    
    DeployTask task = new DeployTask();
    ctx.getTm().executeAndWait(task, TaskParams.createFor(distZip, DeployPreferences.newInstance().executeDeployScripts())).get();
    DistributionCriteria criteria = DistributionCriteria.builder().name("test").version("1.0").build();
    
    assertTrue("Distribution was not deployed", 
        ctx.getDepl().getDistributionDatabase().containsDistribution(criteria));

    verify(preDeploy).exists();
    verify(postDeploy).exists();
    verify(dispatcher, times(2)).dispatch(isA(DeploymentScriptExecutedEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentStartingEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentUnzippedEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentCompletedEvent.class));
    verify(processors).onPostDeploy(any(DeploymentContext.class), any(LogCallback.class));
  }
  
  @Test
  public void testExecute_post_deploy_script_disabled_for_repo_client() throws Exception {
    ctx.getCorusHost().setRepoRole(RepoRole.CLIENT);
    when(preDeploy.exists()).thenReturn(true);
    when(postDeploy.exists()).thenReturn(true);
    
    DeployTask task = new DeployTask();
    ctx.getTm().executeAndWait(task, TaskParams.createFor(distZip, DeployPreferences.newInstance().executeDeployScripts())).get();
    DistributionCriteria criteria = DistributionCriteria.builder().name("test").version("1.0").build();
    
    assertTrue("Distribution was not deployed", 
        ctx.getDepl().getDistributionDatabase().containsDistribution(criteria));

    verify(preDeploy, never()).exists();
    verify(postDeploy, never()).exists();
    verify(dispatcher, never()).dispatch(isA(DeploymentScriptExecutedEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentStartingEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentUnzippedEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentCompletedEvent.class));
    verify(processors).onPostDeploy(any(DeploymentContext.class), any(LogCallback.class));
  }
  
  
  @Test
  public void testExecute_rollback_script() throws Exception{
    when(preDeploy.exists()).thenReturn(true);
    when(rollback.exists()).thenReturn(true);
    
    doThrow(new IOException("I/O error")).when(fs).unzip(any(File.class), any(File.class));
    
    DeployTask task = new DeployTask();
    ctx.getTm().executeAndWait(task, TaskParams.createFor(distZip, DeployPreferences.newInstance().executeDeployScripts())).get();
    DistributionCriteria criteria = DistributionCriteria.builder().name("test").version("1.0").build();
    assertFalse("Distribution should not have been deployed", 
        ctx.getDepl().getDistributionDatabase().containsDistribution(criteria));
    
    verify(rollback).exists();
    verify(fs, times(2)).deleteDirectory(any(File.class));
    verify(dispatcher).dispatch(isA(DeploymentStartingEvent.class));
    verify(dispatcher, never()).dispatch(isA(DeploymentUnzippedEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentFailedEvent.class));
    verify(dispatcher).dispatch(isA(RollbackStartingEvent.class));
    verify(dispatcher).dispatch(isA(RollbackCompletedEvent.class));
    verify(processors).onPostUndeploy(any(DeploymentContext.class), any(LogCallback.class));
  }
  
  @Test
  public void testExecute_rollback_script_failure() throws Exception{
    when(preDeploy.exists()).thenReturn(true);
    doAnswer(new Answer<File>() {
      
      @Override
      public File answer(InvocationOnMock invocation) throws Throwable {
        String fName = (String) invocation.getArguments()[0];
        if (fName.contains("pre-deploy.corus")) return preDeploy;
        else if (fName.contains("post-deploy.corus")) return postDeploy;
        else if (fName.contains("rollback.corus")) {
          throw new IOException("I/O error");
        } else {
          File mockFile = mock(File.class);
          when(mockFile.getAbsolutePath()).thenReturn("test");
          when(mockFile.exists()).thenReturn(true);
          return mockFile;
        }
      }
      
    }).when(fs).getFileHandle(anyString());    
    
    
    doThrow(new IOException("I/O error")).when(fs).unzip(any(File.class), any(File.class));
    
    DeployTask task = new DeployTask();
    try {
      ctx.getTm().executeAndWait(task, TaskParams.createFor(distZip, DeployPreferences.newInstance().executeDeployScripts())).get();
    } catch (Throwable t) {
      // noop
    }
    DistributionCriteria criteria = DistributionCriteria.builder().name("test").version("1.0").build();
    assertFalse("Distribution should not have been deployed", 
        ctx.getDepl().getDistributionDatabase().containsDistribution(criteria));

    verify(dispatcher).dispatch(isA(DeploymentStartingEvent.class));
    verify(dispatcher, never()).dispatch(isA(DeploymentUnzippedEvent.class));
    verify(dispatcher).dispatch(isA(DeploymentFailedEvent.class));
    verify(dispatcher).dispatch(isA(RollbackStartingEvent.class));
    verify(dispatcher).dispatch(isA(RollbackFailedEvent.class));
    verify(processors).onPostUndeploy(any(DeploymentContext.class), any(LogCallback.class));
  }
  
  private InputStream getCorusXmlStream() throws IOException{
    InputStream is = getClass().getClassLoader().getResourceAsStream(TEST_RESOURCE);
    if(is == null){
      throw new FileNotFoundException(TEST_RESOURCE);
    }
    return is;
  }

}
