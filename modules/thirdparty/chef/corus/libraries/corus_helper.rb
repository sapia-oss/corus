module CorusHelper
	def self.corus_started(host, port)
        	url = "http://#{host}:#{port.to_s}/ping"
        	response = Chef::REST::RESTRequest.new(:GET, URI.parse(url), nil).call
        	if response.kind_of?(Net::HTTPSuccess) ||
           	   response.kind_of?(Net::HTTPOK) ||
           	   response.kind_of?(Net::HTTPRedirection) ||
           	   response.kind_of?(Net::HTTPForbidden)
                	Chef::Log.debug("GET to #{url} successful")
                	return true
        	else
                	Chef::Log.debug("GET to #{url} returned #{response.code} / #{response.class}")
                	return false
        	end
	rescue EOFError, Errno::ECONNREFUSED
        	Chef::Log.debug("GET to #{url} failed with connection refused")
        	return false
	end
end
