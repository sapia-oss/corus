require "./lib/corus_lib/topology.rb"
require "test/unit"

class TestEnvTemplate < Test::Unit::TestCase

  def setup
    @template = TopologyModel::EnvTemplate.new
    @template.name = "template"
  end

  def test_eql?
    t1 = TopologyModel::EnvTemplate.new
    t1.name = "t1"

    t2 = TopologyModel::EnvTemplate.new
    t2.name = "t2"

    t3 = TopologyModel::EnvTemplate.new
    t3.name = "t1"

    assert_equal(t1, t1)
    assert_equal(t1, t3)
    assert_not_equal(t1, t2)
  end

  def test_addCluster
    c1 = TopologyModel::Cluster.new
    c1.name = "c1"

    c2 = TopologyModel::Cluster.new
    c2.name = "c2"
    
    @template.addCluster(c1)
    @template.addCluster(c2)

    assert_equal(2, @template.clusters.size)
  end

  def test_addCluster_duplicate
    c1 = TopologyModel::Cluster.new
    c1.name = "c1"

    c2 = TopologyModel::Cluster.new
    c2.name = "c1"

    @template.addCluster(c1)

    assert_raise ArgumentError do
      @template.addCluster(c2)
    end
  end

  def test_addRegion
    r1 = TopologyModel::Region.new
    r1.name = "r1"

    r2 = TopologyModel::Region.new
    r2.name = "r2"

    @template.addRegion(r1)
    @template.addRegion(r2)

    assert_equal(2, @template.regions.size)
  end

  def test_addRegion_duplicate
    r1 = TopologyModel::Region.new
    r1.name = "r1"

    r2 = TopologyModel::Region.new
    r2.name = "r1"

    @template.addRegion(r1)

    assert_raise ArgumentError do
      @template.addRegion(r2)
    end
  end

end