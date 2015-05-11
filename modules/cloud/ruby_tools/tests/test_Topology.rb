require "./lib/corus_lib/topology.rb"
require "test/unit"

class TestTopology < Test::Unit::TestCase

  def setup
    @topology = TopologyModel::Topology.new
    @topology.application = "test"
  end

  def test_render_application_set
    @topology.render
  end

  def test_render_application_not_set
    t = TopologyModel::Topology.new 
    assert_raise ArgumentError do
      t.render
    end
  end

  def test_with_env
    et = TopologyModel::EnvTemplate.new
    et.name = "envTemplate"

    ct = TopologyModel::ClusterTemplate.new
    ct.name = "clusterTemplate"

    rt = TopologyModel::RegionTemplate.new
    rt.name = "regionTemplate"
    rt.addZone("z1")

    mt = TopologyModel::MachineTemplate.new
    mt.name         = "machineTemplate"
    mt.minInstances = 1
    mt.maxInstances = 10
    mt.imageId      = "image" 

    e = TopologyModel::Env.new
    e.name = "env"
    e.templateRef = "envTemplate"
  
    c = TopologyModel::Cluster.new
    c.name = "cluster"
    c.templateRef = "clusterTemplate"
    
    r = TopologyModel::Region.new
    r.name = "region"
    r.templateRef = "regionTemplate"
    r.addZone("z2")

    m = TopologyModel::Machine.new
    m.name = "machine"
    m.templateRef  = "machineTemplate"

    c.addMachine(m)
    e.addCluster(c)
    e.addRegion(r)

    @topology.addEnvTemplate(et)
    @topology.addRegionTemplate(rt)
    @topology.addClusterTemplate(ct)
    @topology.addMachineTemplate(mt)
    @topology.addEnvironment(e)

    @topology.render

  end

end