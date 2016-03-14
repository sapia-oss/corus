package org.sapia.corus.cloud.topology;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class MachineTest {

  private Cluster cluster;
  private MachineTemplate template;
  private Machine machine;

  @Before
  public void setUp() throws Exception {
    cluster  = new Cluster();
    cluster.setName("test-cluster");
    cluster.addParam("n3", "v3");
    cluster.addParam("n4", "v4");

    template = new MachineTemplate();
    template.setName("template");
    template.setImageId("test-image");
    template.setMaxInstances(5);
    template.setMinInstances(2);
    template.setPublicIpEnabled(true);
    template.setRepoRole("client");
    template.setSeedNode(true);
    template.setInstanceType("test-instance");
    template.setProcessProperties(new PropertyCollection().addProperty("n1", "v1"));
    template.setServerProperties(new PropertyCollection().addProperty("n1", "v1"));
    template.setServerTags("tag1");
    template.addLoadBalancerAttachment(new LoadBalancerAttachment("test-lb-1"));
    template.addArtifact(new Artifact("test-artifact-1"));
    template.addParam("n1", "v1");
    template.addParam("n2", "v2");
    template.addMachineTag(new MachineTag("mt1", "mtv1"));

    machine = new Machine();
    machine.setTemplateRef("template");
    machine.setProcessProperties(new PropertyCollection().addProperty("n2", "v2"));
    machine.setServerProperties(new PropertyCollection().addProperty("n2", "v2"));
    machine.setServerTags("tag2");
    machine.addLoadBalancerAttachment(new LoadBalancerAttachment("test-lb-2"));
    machine.addArtifact(new Artifact("test-artifact-2"));
    machine.addParam("n2", "v2-override");
    machine.addParam("n4", "v4-override");
    machine.addMachineTag(new MachineTag("mt2", "mtv2"));
    
    cluster.addMachine(machine);
  }
  
  @Test
  public void testHasMachineTag_from_template() {
    machine.getMachineTags().contains(new MachineTag("mt1", "mtv1"));
  }
  
  @Test
  public void testHasMachineTag() {
    machine.getMachineTags().contains(new MachineTag("mt2", "mtv2"));
  }
  
  @Test
  public void testExistsParam() {
    TopologyContext context = new TopologyContext();
    context.addMachineTemplate(template);
    machine.render(context);
    assertTrue(machine.existsParam("n2"));
  }
  
  @Test
  public void testExistsParam_from_template() {
    TopologyContext context = new TopologyContext();
    context.addMachineTemplate(template);
    machine.render(context);
    assertTrue(machine.existsParam("n1"));
  }

  @Test
  public void testExistsParam_from_cluster() {
    TopologyContext context = new TopologyContext();
    context.addMachineTemplate(template);
    machine.render(context);
    assertTrue(machine.existsParam("n3"));
  }
  
  @Test
  public void testGetParam_override_template() {
    TopologyContext context = new TopologyContext();
    context.addMachineTemplate(template);
    machine.render(context);
    assertEquals("v2-override", machine.getParam("n2").getValue());
  }
  
  @Test
  public void testGetParam_override_cluster() {
    TopologyContext context = new TopologyContext();
    context.addMachineTemplate(template);
    machine.render(context);
    assertEquals("v4-override", machine.getParam("n4").getValue());
  }

  @Test
  public void testRender() {
    TopologyContext context = new TopologyContext();
    context.addMachineTemplate(template);
    machine.render(context);
    
    assertEquals("test-image", machine.getImageId());
    assertEquals(2, machine.getMinInstances());
    assertEquals(5, machine.getMaxInstances());
    assertTrue(machine.isPublicIpEnabled());
    assertTrue(machine.isSeedNode());
    assertEquals("client", machine.getRepoRole());
    assertEquals(2, machine.getProcessProperties().size());
    assertEquals(2, machine.getServerProperties().size());
    assertEquals(2, machine.getProcessProperties().size());
    assertEquals(2, machine.getServerTags().size());
    assertEquals(2, machine.getLoadBalancerAttachments().size());
    assertEquals(2, machine.getArtifacts().size());
  }

  @Test
  public void testValidate() {
    TopologyContext context = new TopologyContext();
    context.addMachineTemplate(template);
    machine.render(context);
    machine.validate();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidate_image_id() {
    TopologyContext context = new TopologyContext();
    template.setImageId(null);
    context.addMachineTemplate(template);
    machine.render(context);
    machine.validate();
  }
  
  @Test
  public void testSetImageId() {
    TopologyContext context = new TopologyContext();
    context.addMachineTemplate(template);
    
    machine.setImageId("image-override");
    machine.render(context);
    
    assertEquals("image-override", machine.getImageId());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testValidate_instance_type() {
    TopologyContext context = new TopologyContext();
    template.setInstanceType(null);
    context.addMachineTemplate(template);
    machine.render(context);
    machine.validate();
  }
  
  @Test
  public void testSetInstanceType() {
    TopologyContext context = new TopologyContext();
    context.addMachineTemplate(template);
    
    machine.setInstanceType("instanceType-override");
    machine.render(context);
    
    assertEquals("instanceType-override", machine.getInstanceType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValidate_max_instances_negative() {
    TopologyContext context = new TopologyContext();
    template.setMaxInstances(0);
    context.addMachineTemplate(template);
    machine.render(context);
    machine.validate();
  }
  
  @Test
  public void testValidate_max_instances_override() {
    TopologyContext context = new TopologyContext();
    context.addMachineTemplate(template);
    
    machine.setMaxInstances(10);
    machine.render(context);
    
    assertEquals(10, machine.getMaxInstances());
  }
  
  @Test
  public void testValidate_min_instances_override() {
    TopologyContext context = new TopologyContext();
    context.addMachineTemplate(template);
    
    machine.setMinInstances(1);
    machine.render(context);
    
    assertEquals(1, machine.getMinInstances());
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testValidate_repo_role() {
    TopologyContext context = new TopologyContext();
    template.setRepoRole("test");
    context.addMachineTemplate(template);
    machine.render(context);
    machine.validate();
  }
}
