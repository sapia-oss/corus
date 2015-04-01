# coding: utf-8
lib = File.expand_path('../lib', __FILE__)
$LOAD_PATH.unshift(lib) unless $LOAD_PATH.include?(lib)

Gem::Specification.new do |spec|
  spec.name          = "corus_lib"
  spec.version       = '1.0'
  spec.authors       = ["YD"]
  spec.email         = ["yd@sapia-oss.org"]
  spec.summary       = %q{Ruby API/Tools for Corus in the Cloud}
  spec.description   = %q{Processes Corus Cloud Topology, allows for hooking up with custom logic}
  spec.homepage      = "http://www.sapia-oss.org/"
  spec.license       = "Apache 2.0"
  spec.files         = ['lib/corus_lib/topology.rb', 'lib/corus_lib/xml.rb']
  spec.executables   = ['bin/topology']
  spec.test_files    = ['tests/test_XmlTopologyReader.rb']
  spec.require_paths = ["lib"]
end
