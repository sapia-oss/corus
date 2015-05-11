require "./lib/corus_lib/topology.rb"
require "test/unit"

class TestCluster < Test::Unit::TestCase

  def test_render
    ctx = TopologyModel::TopologyContext.new
    t = TopologyModel::ClusterTemplate.new
    t.name = "template"
    t.instances = 5
   
    ctx.addClusterTemplate(t)

    c = TopologyModel::Cluster.new
    c.name = "c"
    c.templateRef = "template"
    c.render(ctx)

    assert_equal(5, c.instances)
   end

  def test_validate_name_set
    c = TopologyModel::Machine.new
    c.name = "c"

    assert_raise ArgumentError do
      c.validate
    end
  end

  def test_validate_name_not_set
    c = TopologyModel::Cluster.new

    assert_raise ArgumentError do
      c.validate
    end
  end

end