require "./lib/corus_lib/topology.rb"
require "test/unit"

class TestRegion < Test::Unit::TestCase

  def setup
    @region = TopologyModel::Region.new
    @region.name = "region"
    @region.templateRef = "template"

    @template = TopologyModel::RegionTemplate.new
    @template.name = "template"
    @template.addZone("z")
  end

  def test_validate_name_set
    @region.validate
  end

  def test_validate_name_not_set
    r = TopologyModel::Region.new

    assert_raise ArgumentError do
      r.validate
    end
  end

  def test_render
    ctx = TopologyModel::TopologyContext.new
    ctx.addRegionTemplate(@template)

    @region.render(ctx)

    assert_equal(1, @region.zones.size)
  end

end