default["java"]["install_flavor"] = "oracle"
default["java"]["jdk_version"] = "7"
default["java"]["oracle"]["accept_oracle_download_terms"] = true

default['corus']['version'] = '4.6.4'
default['corus']['archive_download_url'] = 'http://www.mediafire.com/download/g4i33lmdnrhe4lv/sapia_corus_server_package-4.6.4-linux64.tar.gz'
default['corus']['extract_dir'] = '/opt/corus'
default['corus']['current_sym_link'] = '/opt/corus/current' 
default['corus']['user'] = 'corus'
default['corus']['group'] = 'corus'
default['corus']['gid'] = 33000
default['corus']['java_home'] = '/usr/lib/jvm/java'
default['corus']['port'] = 33000
default['corus']['log_dir'] = default['corus']['current_sym_link'] + "/logs"
default['corus']['log_level'] = "INFO"
default["corus"]["xms"] = "32M"
default["corus"]["xmx"] = "96M"
default['corus']['gc'] = "UseConcMarkSweepGC"

default["corus"]["domain"]="default"
default["corus"]["repo_type"]="none"
# if defined, takes Java regex (e.g.: 10\\.10\\.\\d+\\.\\d+)
default["corus"]["server_address_pattern"]="UNDEFINED"

# if defined, takes form: tag-0,tag-1[,...,tag-N]
default["corus"]["tags"] = "UNDEFINED"
# if defined, takes form: name-0=value-0,name-1=value-1[,...,name-N=value-N]
default["corus"]["process_properties"] = "UNDEFINED"
# if defined, takes a logical name (e.g.: var_log)
default["corus"]["log_sym_link_name"]="UNDEFINED"
default["corus"]["log_sym_link_path"]="/var/log/apps"

default['corus']['rest_admin_client_id']="admin"
default['corus']['rest_admin_role']="admin"
default['corus']['rest_admin_appkey']="383ab9ffbb604ebcbf7053a04938ca50"
default['corus']['rest_automation_client_id']="automation"
default['corus']['rest_automation_role']="automation"
default['corus']['rest_automation_appkey']="08d73288239547c6a70400f59746e387"
default['corus']['rest_guest_client_id']="guest"
default['corus']['rest_guest_role']="guest"
default['corus']['rest_guest_appkey']="9d6e0367e27b4ce696529b040815f396"

default["corus"]["api_auth_always"]="true"
default["corus"]["ssl_enabled"]="true"
default["corus"]["ssl_port"]=0
default["corus"]["keystore_path"] = File.join("/home/" + default["corus"]["user"], "keystore.jks")
default["corus"]["keystore_alias"] = "corus_server"
default["corus"]["keystore_keypass"] = "1q2w3e4r"
default["corus"]["keystore_storepass"] = default['corus']['keystore_keypass']
default["corus"]["keystore_CN"] = "corus"
default["corus"]["keystore_OU"] = "Deployment"
default["corus"]["keystore_O"] = "www.sapia-oss.org"
default["corus"]["keystore_L"] = "Seattle"
default["corus"]["keystore_ST"] = "WA"
default["corus"]["keystore_C"] = "US"

# following's value can be 'avis' or 'rabbitmq'
default["corus"]["broadcast_provider"]="avis"

default["corus"]["avis_url"]="elvin://localhost"
default["corus"]["avis_xms"] = "16M"
default["corus"]["avis_xmx"] = "96M"
default["corus"]["avis_gc"] = "UseConcMarkSweepGC"
default["corus"]["avis_download_url"] = "http://www.mediafire.com/download/jech2hca5yc49q7/avis-1.2.2.rpm"

default["corus"]["rabbitmq_url"]="rabbitmq://localhost/corus_cluster_channel"
default["corus"]["rabbitmq_vhost"]="/"
default["corus"]["rabbitmq_addresses"]="UNDEFINED"
default["corus"]["rabbitmq_username"]="UNDEFINED"
default["corus"]["rabbitmq_password"]="UNDEFINED"

# if defined, takes consul URL (e.g: http://localhost:8500) 
default["corus"]["consul_url"]="UNDEFINED"
default["corus"]["consul_interval"]=30
default["corus"]["consul_ttl"]=45

