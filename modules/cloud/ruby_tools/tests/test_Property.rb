require "./lib/corus_lib/topology.rb"
require "test/unit"

class TestProperty < Test::Unit::TestCase


  def test_eql?
  	p1 = TopologyModel::Property.new
  	p1.name = "p1"
  	p1.value = "v1"

  	p2 = TopologyModel::Property.new
  	p2.name = "p2"
  	p2.value = "v2"

  	p3 = TopologyModel::Property.new
  	p3.name = "p1"
  	p3.value = "v3"

    assert_equal(p1, p1)
  	assert_not_equal(p1, p2)
  	assert_equal(p1, p3)
  end

  def test_members
  	p1 = TopologyModel::Property.new
  	p1.name = "p1"
  	p1.value = "v1"

  	assert_equal("p1", p1.name)
  	assert_equal("v1", p1.value)
  end

end
