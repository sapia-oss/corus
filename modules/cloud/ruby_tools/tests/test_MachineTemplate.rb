require "./lib/corus_lib/topology.rb"
require "test/unit"

class TestMachineTemplate < Test::Unit::TestCase

  def test_eql?
  	t1 = TopologyModel::MachineTemplate.new
  	t1.name = "t1"
 
  	t2 = TopologyModel::MachineTemplate.new
  	t2.name = "p2"

  	t3 = TopologyModel::MachineTemplate.new
  	t3.name = "t1"
 
	  assert_equal(t1, t1)
  	assert_not_equal(t1, t2)
  	assert_equal(t1, t3)
  end

  def test_serverTagCsv
  	 t = TopologyModel::MachineTemplate.new
  	 t.serverTagCsv("t1, t2")

  	 assert t.serverTags().include?("t1")
  	 assert t.serverTags().include?("t2")
  end

  def test_addServerTag
  	t = TopologyModel::MachineTemplate.new
  	t.addServerTag("t")

  	assert t.serverTags().include?("t")
  end

  def test_addServerTag_duplicate
  	t = TopologyModel::MachineTemplate.new
  	t.addServerTag("t")

  	assert_raise ArgumentError do
	    t.addServerTag("t")
  	end
  end

  def test_addServerProperty
  	t = TopologyModel::MachineTemplate.new
  	t.addServerProperty(TopologyModel::Property.of("n", "v"))

  	assert t.serverProperties().include?(TopologyModel::Property.of("n", "v"))
  	assert t.serverProperties().include?(TopologyModel::Property.of("n", "v2"))
  end

  def test_addServerProperty_duplicate
  	t = TopologyModel::MachineTemplate.new
  	t.addServerProperty(TopologyModel::Property.of("n", "v"))

  	assert_raise ArgumentError do
	    t.addServerProperty(TopologyModel::Property.of("n", "v"))
  	end
  end


  def test_addProcessProperty
  	t = TopologyModel::MachineTemplate.new
  	t.addProcessProperty(TopologyModel::Property.of("n", "v"))

  	assert t.processProperties().include?(TopologyModel::Property.of("n", "v"))
  	assert t.processProperties().include?(TopologyModel::Property.of("n", "v2"))
  end

  def test_addProcessProperty_duplicate
  	t = TopologyModel::MachineTemplate.new
  	t.addProcessProperty(TopologyModel::Property.of("n", "v"))

  	assert_raise ArgumentError do
	    t.addProcessProperty(TopologyModel::Property.of("n", "v"))
  	end
  end

  def test_copyFrom_tags
  	t1 = TopologyModel::MachineTemplate.new
  	t2 = TopologyModel::MachineTemplate.new
 	
 	t2.addServerTag("t")

 	t1.copyFrom(t2)

 	assert t1.serverTags().include?("t")

  end

  def test_copyFrom_properties
  	t1 = TopologyModel::MachineTemplate.new
  	t2 = TopologyModel::MachineTemplate.new
 	
 	  t1.addServerProperty(TopologyModel::Property.of("n1", "v1"))
 	  t2.addProcessProperty(TopologyModel::Property.of("n2", "v2"))

  	t2.addServerProperty(TopologyModel::Property.of("n3", "v3"))
 	  t2.addProcessProperty(TopologyModel::Property.of("n4", "v4"))

 	  t1.copyFrom(t2)

  	assert t1.serverProperties().include?(TopologyModel::Property.of("n1", "v1"))
  	assert t1.processProperties().include?(TopologyModel::Property.of("n2", "v2"))
  	assert t1.serverProperties().include?(TopologyModel::Property.of("n3", "v3"))
  	assert t1.processProperties().include?(TopologyModel::Property.of("n4", "v4"))
  end


  def test_copyFrom_minInstances_set
  	t1 = TopologyModel::MachineTemplate.new
  	t1.minInstances = 5

  	t2 = TopologyModel::MachineTemplate.new
 	  t2.minInstances = 10

 	  t1.copyFrom(t2)

 	  assert_equal(5, t1.minInstances())
  end

  def test_copyFrom_maxInstances_set
  	t1 = TopologyModel::MachineTemplate.new
  	t1.maxInstances = 5
  	t2 = TopologyModel::MachineTemplate.new
 	  t2.maxInstances = 10

 	  t1.copyFrom(t2)

  	assert_equal(5, t1.maxInstances())
  end

  def test_copyFrom_minInstances_not_set
  	t1 = TopologyModel::MachineTemplate.new
  	t2 = TopologyModel::MachineTemplate.new
 	  t2.minInstances = 10

 	  t1.copyFrom(t2)

 	  assert_equal(10, t1.minInstances())
  end

  def test_copyFrom_maxInstances_not_set
  	t1 = TopologyModel::MachineTemplate.new
  	t2 = TopologyModel::MachineTemplate.new
 	  t2.maxInstances = 10

 	  t1.copyFrom(t2)

 	  assert_equal(10, t1.maxInstances())
  end

  def test_copyFrom_imageId_set
  	t1 = TopologyModel::MachineTemplate.new
  	t1.imageId = "i1"
  	t2 = TopologyModel::MachineTemplate.new
 	  t2.imageId = "i2"

 	  t1.copyFrom(t2)

 	  assert_equal("i1", t1.imageId)
  end

  def test_copyFrom_imageId_not_set
  	t1 = TopologyModel::MachineTemplate.new
  	t2 = TopologyModel::MachineTemplate.new
 	  t2.imageId = "i2"

 	  t1.copyFrom(t2)

 	  assert_equal("i2", t2.imageId)
  end  

  def test_validate_name_set
  	t = TopologyModel::MachineTemplate.new
  	t.name = "t"
  	t.validate()
  end

  def test_validate_name_not_set
  	t = TopologyModel::MachineTemplate.new

  	assert_raise ArgumentError do
  	  t.validate()
  	end
  end

end

