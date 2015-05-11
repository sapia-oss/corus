require "./lib/corus_lib/topology.rb"
require "test/unit"

class TestMachine < Test::Unit::TestCase
  
  def test_render
  	ctx = TopologyModel::TopologyContext.new
  	t = TopologyModel::MachineTemplate.new
  	t.name = "template"
  	t.minInstances = 1
  	t.maxInstances = 5
  	t.imageId = "image"

  	ctx.addMachineTemplate(t)

  	m = TopologyModel::Machine.new
  	m.name = "machine"
  	m.templateRef = "template"
  	m.render(ctx)

  	assert_equal(1, m.minInstances)
  	assert_equal(5, m.maxInstances)
  	assert_equal("image", m.imageId)
  end

  def test_validate_name_not_set
  	m = TopologyModel::Machine.new
  	m.minInstances = 1
  	m.maxInstances = 5
  	m.imageId = "image"

  	assert_raise ArgumentError do
	    m.validate
  	end
  end

  def test_validate_imageId_not_set
  	m = TopologyModel::Machine.new
  	m.name = "machine"
  	m.minInstances = 1
  	m.maxInstances = 5

  	assert_raise ArgumentError do
	    m.validate
  	end
  end

  def test_validate_minInstances_not_set
  	m = TopologyModel::Machine.new
  	m.name = "machine"
  	m.maxInstances = 5
  	m.imageId = "image"

  	assert_raise ArgumentError do
      m.validate
  	end
  end

  def test_validate_maxInstances_not_set
  	m = TopologyModel::Machine.new
  	m.name = "machine"
  	m.minInstances = 1
  	m.imageId = "image"

  	assert_raise ArgumentError do
	   m.validate
  	end
  end

  def test_validate_maxInstances_invalid
  	m = TopologyModel::Machine.new
  	m.name = "machine"
  	m.minInstances = 5
  	m.maxInstances = 1
  	m.imageId = "image"

  	assert_raise ArgumentError do
	  m.validate
  	end
  end
end