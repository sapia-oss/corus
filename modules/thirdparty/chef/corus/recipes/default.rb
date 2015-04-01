#
# Cookbook Name:: corus
# Recipe:: default
#
# Copyright 2015, www.sapia-oss.org
#
# Redistribute at will
#
include_recipe "java"

service "corus" do
	action :stop
end

group node['corus']['group'] do
	gid node['corus']['gid']
end

user node['corus']['user'] do
	supports :manage_home => true
	gid node['corus']['gid']
	home "/home/" + node['corus']['user']
	:create
end

# workaround: home directory was not being created by user resource above
directory  "/home/" + node['corus']['user'] do
	owner node['corus']['user']
end

directory node['corus']['extract_dir'] + "/" + node['corus']['version'] do
	owner node['corus']['user']
	group node['corus']['group']
	recursive true
end

tar_extract node['corus']['archive_download_url']  do
	target_dir node['corus']['extract_dir'] + "/" + node['corus']['version']
	user node['corus']['user']
	group node['corus']['group']
        creates node['corus']['extract_dir'] + "/" + node['corus']['version'] + "/.chef"
	tar_flags ['-P', '--strip-components 1']
end

link node['corus']['current_sym_link'] do
	action :delete
	only_if "test -L " + node['corus']['current_sym_link']
end

link node['corus']['current_sym_link'] do
	to node['corus']['extract_dir'] + "/" + node['corus']['version']
        user node['corus']['user']
        group node['corus']['group']
end

template "/etc/init.d/corus" do
        source 'corus.init-d.erb'
        mode 0755
        owner node['corus']['user']
        group node['corus']['group']
        variables(
		:java_home => node['corus']['java_home'],
		:user => node['corus']['user'],
		:group => node['corus']['group'],
		:home => node['corus']['current_sym_link'],
		:port => node['corus']['port'],
		:log_dir => node['corus']['log_dir'],
		:log_level => node['corus']['log_level'],
                :xms => node['corus']['xms'],
                :xmx => node['corus']['xmx'],
                :gc  => node['corus']['gc'])
end

template "/etc/profile.d/corus.sh" do
	source 'corus.profile-d.erb'
	mode 0755
	variables(
		:corus_home => node['corus']['current_sym_link'])
end

template node['corus']['current_sym_link'] + "/config/corus_" + (node['corus']['port'].to_s) + ".properties" do
	source 'corus.properties.erb'
        owner node['corus']['user']
        group node['corus']['group']
	variables(
		:domain => node['corus']['domain'],
		:repo_type => node['corus']['repo_type'],
		:log_sym_link_name => node['corus']['log_sym_link_name'],
		:log_sym_link_path => node['corus']['log_sym_link_path'],
		:api_auth_always => node['corus']['api_auth_always'],
		:ssl_enabled => node['corus']['ssl_enabled'],
		:ssl_port => node['corus']['ssl_port'],
		:keystore_path => node['corus']['keystore_path'],
		:keystore_storepass => node['corus']['keystore_storepass'],
		:keystore_keypass => node['corus']['keystore_keypass'],
		:broadcast_provider => node['corus']['broadcast_provider'],
		:rabbitmq_url => node['corus']['rabbitmq_url'],
		:rabbitmq_vhost => node['corus']['rabbitmq_vhost'],
		:rabbitmq_addresses => node['corus']['rabbitmq_addresses'],
		:rabbitmq_username => node['corus']['rabbitmq_username'],
		:rabbitmq_password => node['corus']['rabbitmq_password'],
		:avis_url => node['corus']['avis_url'],
		:server_address_pattern => node['corus']['server_address_pattern'],
		:consul_url => node['corus']['consul_url'],
		:consul_interval => node['corus']['consul_interval'],
		:consul_ttl => node['corus']['consul_ttl'])
end

##### CREATING KEYSTORE (FOR REST AUTHENTICATION)

file node["corus"]["keystore_path"] do
        action :delete
        only_if do
                FileTest.exists? node["corus"]["keystore_path"]
        end
end

execute "keytool-generate" do
        command <<-eos
                #{node["corus"]["java_home"]}/bin/keytool -genkey \
-alias #{node["corus"]["keystore_alias"]} \
-dname 'CN=#{node["corus"]["keystore_CN"]}, OU=#{node["corus"]["keystore_OU"]}, O=#{node["corus"]["keystore_O"]}, L=#{node["corus"]["keystore_L"]}, ST=#{node["corus"]["keystore_ST"]}, C=#{node["corus"]["keystore_C"]}' \
-keypass #{node["corus"]["keystore_keypass"]} \
-keyalg RSA \
-storepass #{node["corus"]["keystore_storepass"]} \
-keystore #{node["corus"]["keystore_path"]}
        eos
        creates node["corus"]["keystore_path"]
end

execute "user owns keystore" do
  command "chown -R #{node['corus']['user']} #{node['corus']['keystore_path']}"
end

service "corus" do
        action [:enable, :start]
end

# waiting for Corus startup

maxAttempts  = 10
attemptCount = 0
ruby_block 'wait for Corus start' do
	block do
		until (CorusHelper.corus_started(node["hostname"], node['corus']['port']))
			sleep(1)
			attemptCount = attemptCount + 1
			if attemptCount > maxAttempts then
				raise "Corus not started, aborting installation"
			end
		end
	end
end

##### ADDING TAGS (if defined)

if node['corus']['tags'] != "UNDEFINED" then
        execute "coruscli-add-tags" do
                command node['corus']['current_sym_link'] + "/bin/coruscli -c conf add -t " + node['corus']['tags']
                environment ({'CORUS_HOME' => node['corus']['current_sym_link'], 'JAVA_HOME' => node['corus']['java_home']})
        end
end

##### ADDING PROCESS PROPERTIES (if defined)

if node['corus']['process_properties'] != "UNDEFINED" then
	nameValues = node['corus']['process_properties'].split(',')
	for nv in nameValues do
	        execute "coruscli-add-process-properties" do
        	        command node['corus']['current_sym_link'] + "/bin/coruscli -c conf add -p " + nv
          	     	environment ({'CORUS_HOME' => node['corus']['current_sym_link'], 'JAVA_HOME' => node['corus']['java_home']})
        	end
	end
end

##### INITIALIZING REST AUTHENTICATION

# creating roles (admin, automation, guest)

execute "coruscli-add-admin-role" do
	command node['corus']['current_sym_link'] + "/bin/coruscli -c role add -n #{node['corus']['rest_admin_role']} -p arwxd"
	environment ({'CORUS_HOME' => node['corus']['current_sym_link'], 'JAVA_HOME' => node['corus']['java_home']})
end

execute "coruscli-add-automation-role" do
        command node['corus']['current_sym_link'] + "/bin/coruscli -c role add -n #{node['corus']['rest_automation_role']} -p rwxd"
        environment ({'CORUS_HOME' => node['corus']['current_sym_link'], 'JAVA_HOME' => node['corus']['java_home']})
end

execute "coruscli-add-guest-role" do
        command node['corus']['current_sym_link'] + "/bin/coruscli -c role add -n #{node['corus']['rest_guest_role']} -p r"
        environment ({'CORUS_HOME' => node['corus']['current_sym_link'], 'JAVA_HOME' => node['corus']['java_home']})
end

# creating appkeys for above roles

execute "coruscli-add-admin-appkey" do
        command node['corus']['current_sym_link'] + "/bin/coruscli -c appkey add -a #{node['corus']['rest_admin_client_id']} -r #{node['corus']['rest_admin_role']} -k #{node['corus']['rest_admin_appkey']}"
        environment ({'CORUS_HOME' => node['corus']['current_sym_link'], 'JAVA_HOME' => node['corus']['java_home']})
end

execute "coruscli-add-automation-appkey" do
        command node['corus']['current_sym_link'] + "/bin/coruscli -c appkey add -a #{node['corus']['rest_automation_client_id']} -r #{node['corus']['rest_automation_role']} -k #{node['corus']['rest_automation_appkey']}"
        environment ({'CORUS_HOME' => node['corus']['current_sym_link'], 'JAVA_HOME' => node['corus']['java_home']})
end

execute "coruscli-add-guest-appkey" do
        command node['corus']['current_sym_link'] + "/bin/coruscli -c appkey add -a #{node['corus']['rest_guest_client_id']} -r #{node['corus']['rest_guest_role']} -k #{node['corus']['rest_guest_appkey']}"
        environment ({'CORUS_HOME' => node['corus']['current_sym_link'], 'JAVA_HOME' => node['corus']['java_home']})
end
