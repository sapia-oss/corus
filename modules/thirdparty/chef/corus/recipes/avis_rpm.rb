#
# Cookbook Name:: corus
# Recipe:: avis_rpm
#
# Copyright 2015, www.sapia-oss.org
#
# Redistribute at will
#

remote_file "/tmp/avis.rpm" do
	source node['corus']['avis_download_url']
end

service "avisd" do
	:stop
end

rpm_package "/tmp/avis.rpm" do
	action :install
end

template '/sbin/avisd' do
	source 'avisd.sbin.erb'
	mode 0755
	owner 'root'
	group 'root'
	variables(
		:xms => node['corus']['avis_xms'],
		:xmx => node['corus']['avis_xmx'],
		:gc  => node['corus']['avis_gc'])
end

service "avisd" do
	action [:enable, :start]
end
