require "./lib/corus_lib/topology.rb"
require "test/unit"

class TestRegionTemplate < Test::Unit::TestCase


  def setup
    @template = TopologyModel::RegionTemplate.new
    @template.name = "template"
  end

  def test_eql?
    r1 = TopologyModel::RegionTemplate.new
    r1.name = "r1"
 
    r2 = TopologyModel::RegionTemplate.new
    r2.name = "r2"

    r3 = TopologyModel::RegionTemplate.new
    r3.name = "r1"
 
    assert_equal(r1, r1)
    assert_not_equal(r1, r2)
    assert_equal(r1, r3)
  end

  def test_addZone
    @template.addZone("z")

    assert_equal(1, @template.zones.size)
  end

  def test_addZone_duplicate
    @template.addZone("z")

    assert_raise ArgumentError do
      @template.addZone("z")
    end
  end

  def test_copyFrom
    other = TopologyModel::RegionTemplate.new
    other.name = "other"
    other.addZone("z")

    @template.copyFrom(other)

    assert_equal(1, @template.zones.size)
  end

  def test_validate_name_set
    @template.validate
  end


  def test_validate_name_not_set
    other = TopologyModel::RegionTemplate.new

    assert_raise ArgumentError do
      other.validate
    end
  end
end 
