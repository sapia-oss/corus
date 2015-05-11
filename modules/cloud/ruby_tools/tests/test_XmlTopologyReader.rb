require "./lib/corus_lib/xml.rb"
require "test/unit"

class TestTopology < Test::Unit::TestCase

  def test_read_topology_file
    file = File.new( "data/testTopology.xml" )
    reader = Xml::XmlTopologyReader.new
    topology = reader.readFromFile(file)
    topology.render

    puts "#{topology.environments.size}"

    topology.environments.each { | e |

      puts "#{e}"



    }
  end

end