require "./lib/corus_lib/topology.rb"
require "test/unit"

class TestClusterTemplate < Test::Unit::TestCase

  def test_eql?
    c1 = TopologyModel::ClusterTemplate.new
    c1.name = "c1"

    c2 = TopologyModel::ClusterTemplate.new
    c2.name = "c2"

    c3 = TopologyModel::ClusterTemplate.new
    c3.name = "c1"


    assert_equal(c1, c1)
    assert_equal(c1, c3)
    assert_not_equal(c1, c2)
  end

  def test_addMachine
    m = TopologyModel::Machine.new
    m.name = "machine"
    c = TopologyModel::ClusterTemplate.new
    c.name = "cluster"

    c.addMachine(m)

    assert_equal(1, c.machines.size)
  end

  def test_addMachine_duplicate
    m = TopologyModel::Machine.new
    m.name = "machine"
    c = TopologyModel::ClusterTemplate.new
    c.name = "cluster"

    c.addMachine(m)

    assert_raise ArgumentError do
      c.addMachine(m)
    end
  end

  def test_copyFrom
    m = TopologyModel::Machine.new
    m.name = "machine"
    c1 = TopologyModel::ClusterTemplate.new
    c1.name = "c1"
    c1.addMachine(m)

    c2 = TopologyModel::ClusterTemplate.new
    c2.name = "c2"
    c2.copyFrom(c1)

    assert_equal(1, c2.machines.size)
  end

  def test_copyFrom_instances_set
    c1 = TopologyModel::ClusterTemplate.new
    c1.name = "c1"
    c1.instances = 5
    c2 = TopologyModel::ClusterTemplate.new
    c2.name = "c2"
    c2.instances = 10

    c1.copyFrom(c2)
  
    assert_equal(5, c1.instances)
  end

  def test_copyFrom_instances_not_set
    c1 = TopologyModel::ClusterTemplate.new
    c1.name = "c1"
    c2 = TopologyModel::ClusterTemplate.new
    c2.name = "c2"
    c2.instances = 10

    c1.copyFrom(c2)
  
    assert_equal(10, c1.instances)
  end

  def test_validate_name_set
    c = TopologyModel::ClusterTemplate.new
    c.name = "c"
 
    c.validate
  end

  def test_validate_name_not_set
    c = TopologyModel::ClusterTemplate.new

    assert_raise ArgumentError do
      c.validate
    end
  end

end