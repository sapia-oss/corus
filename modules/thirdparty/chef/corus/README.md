Corus Cookbook
==============
This cookbook provides the following recipes:

- default: performs the Corus installation.
- avis_rpm: installs Avis.

Notes
-----

- The cookbook is meant to be run on Linux. It installs the Linux-64 package by default.
- Java is presumed to have previously been installed. Do not forget to state your Java home (see the `java_home` attribute further below)
- Typically, you would install Avis or RabbitMQ on separate hosts, and modify the Avis/RabbitMQ connection URLs accordingly at the Corus level.
- For Corus: live upgrade is disabled by default (it consists in upgrading Corus transparently if it is currently running, without stopping the
  processes controlled by it). See the `live_upgrade` attribute further below for more info.

Requirements
------------

- `java` - depends on the java cookbook for starting Corus and generating the keystore used for SSL.
- `tar`  - depends on the tar cookbook for downloading and installing the Corus package.

Attributes
----------

#### corus::default
<table>
  <tr>
    <th>Key</th>
    <th>Type</th>
    <th>Description</th>
    <th>Default</th>
  </tr>
  <tr>
    <td><tt>['corus']['live_upgrade']</tt></td>
    <td>Boolean</td>
    <td>Indicates if live upgrade should be performed: if yes (true), the recipe will check for a Corus instance currently running; if it's the case, that instance's state
    will be saved, in order to be restored into the new version. <b>NOTE</b>: this should be set to true only if the currently running version corresponds to 4.7 or
    above.</td>
    <td><tt>false</tt></td>
  </tr> 
  <tr>
    <td><tt>['corus']['extract_dir']</tt></td>
    <td>String</td>
    <td>The path to the directory where the Corus .tar package will be un-packaged. A subdirectory will be
    created under this path, corresponding to the root of the extracted .tar.</td>
    <td><tt>/opt/corus</tt></td>
  </tr> 
  <tr>
    <td><tt>['corus']['current_sym_link']</tt></td>
    <td>String</td>
    <td>The path of the symbolic link that will be created, and which will point to the directory where the latest
    current package was extracted (see previous attribute). This symbolic link in fact corresponds to <tt>CORUS_HOME</tt>.</td>
    <td><tt>/opt/corus/current</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['user']</tt></td>
    <td>String</td>
    <td>The user under which Corus will run.</td>
    <td><tt>corus</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['group']</tt></td>
    <td>String</td>
    <td>The group of the Corus user.</td>
    <td><tt>corus</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['gid']</tt></td>
    <td>Integer</td>
    <td>The group ID (<tt>gid</tt>) of the Corus group.</td>
    <td><tt>33000</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['version']</tt></td>
    <td>String</td>
    <td>The Corus version.</td>
    <td>Set to correspond to the latest Corus version (in the context of which the Cookbook was last built).</td>
  </tr>  
  <tr>
    <td><tt>['corus']['archive_download_url']</tt></td>
    <td>String</td>
    <td>The URL of the Corus package to download</td>
    <td>Defaults to the URL of the latest Corus version (preferrably: use an internal corporate URL to which you've uploaded the Corus package)</td>
  </tr> 
  <tr>
    <td><tt>['corus']['java_home']</tt></td>
    <td>String</td>
    <td>The path to the JDK' home directory.</td>
    <td><tt>/usr/lib/jvm/java</tt></td>
  </tr>    
  <tr>
    <td><tt>['corus']['port']</tt></td>
    <td>Integer</td>
    <td>The port on which the Corus server should run.</td>
    <td><tt>33000</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['log_dir']</tt></td>
    <td>String</td>
    <td>The directory where Corus should create its log files.</td>
    <td>Defaults to <tt>$CORUS_HOME/logs</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['log_level']</tt></td>
    <td>String</td>
    <td>The log level to use can be one of: <tt>DEBUG</tt>, <tt>INFO</tt>, <tt>WARNING</tt>, <tt>ERROR</tt>.</td>
    <td><tt>INFO</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['xms']</tt></td>
    <td>String</td>
    <td>Value of the <tt>-Xms</tt> switch used when starting Corus.</td>
    <td><tt>32M</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['xmx']</tt></td>
    <td>String</td>
    <td>Value of the <tt>-Xmx</tt> switch used when starting Corus.</td>
    <td><tt>96M</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['gc']</tt></td>
    <td>String</td>
    <td>Name of the GC algo to use by the JVM in which Corus runs.</td>
    <td><tt>UseConcMarkSweepGC</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['domain']</tt></td>
    <td>String</td>
    <td>Domain name under which Corus should start.</td>
    <td><tt>default</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['repo_type']</tt></td>
    <td>String</td>
    <td>Repository role of the Corus instance.</td>
    <td><tt>none</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['server_address_pattern']</tt></td>
    <td>String</td>
    <td>If specified, indicates to Corus to which IP address it should bind 
    its server (e.g.: 10\\.10\\.\\d+\\.\\d+).</td>
    <td><tt>UNDEFINED</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['tags']</tt></td>
    <td>String</td>
    <td>If specified, a comma-delimited list of tags to add to the Corus instance
    (e.g.: <tt>foo,bar</tt>.</td>
    <td><tt>UNDEFINED</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['process_properties']</tt></td>
    <td>String</td>
    <td>If specified, a comma-delimited list of process properties to add to the Corus instance.
     (e.g.: <tt>foo=bar,sna=fu</tt></td>
    <td><tt>UNDEFINED</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['log_sym_link_name']</tt></td>
    <td>String</td>
    <td>If specified: indicates to the logical name under which the directory for
    application logs should be displayed in the Corus web UI.</td>
    <td><tt>UNDEFINED</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['log_sym_link_path']</tt></td>
    <td>String</td>
    <td>Indicates the path of the application log directory.</td>
    <td><tt>/var/log/apps</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['rest_admin_client_id']</tt></td>
    <td>String</td>
    <td>The client ID of the REST admin account.</td>
    <td><tt>admin</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['rest_admin_appkey']</tt></td>
    <td>String</td>
    <td>The application key of the REST admin account.</td>
    <td><tt>383ab9ffbb604ebcbf7053a04938ca50</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['rest_admin_role']</tt></td>
    <td>String</td>
    <td>The name of the REST admin role.</td>
    <td><tt>admin</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['rest_automation_client_id']</tt></td>
    <td>String</td>
    <td>The client ID of the REST account used to control Corus from external apps.</td>
    <td><tt>automation</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['rest_automation_appkey']</tt></td>
    <td>String</td>
    <td>The application key of the REST account used to control Corus from external apps.</td>
    <td><tt>08d73288239547c6a70400f59746e387</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['rest_automation_role']</tt></td>
    <td>String</td>
    <td>The name of the REST account used to control Corus from external apps.</td>
    <td><tt>automation</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['rest_automation_guest_id']</tt></td>
    <td>String</td>
    <td>The client ID of the REST guest account.</td>
    <td><tt>guest</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['rest_guest_appkey']</tt></td>
    <td>String</td>
    <td>The application key of the REST guest account.</td>
    <td><tt>08d73288239547c6a70400f59746e387</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['rest_guest_role']</tt></td>
    <td>String</td>
    <td>The name of the REST guest account.</td>
    <td><tt>guest</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['api_auth_always']</tt></td>
    <td>String</td>
    <td>Corresponds to the <tt>corus.server.api.auth.required</tt> property in <tt>corus.properties</tt>.</td>
    <td><tt>true</tt></td>
  </tr>    
  <tr>
    <td><tt>['corus']['ssl_enabled']</tt></td>
    <td>String</td>
    <td>Corresponds to the <tt>corus.server.api.ssl.enforced</tt> property in <tt>corus.properties</tt>.</td>
    <td><tt>true</tt></td>
  </tr>      
  <tr>
    <td><tt>['corus']['ssl_port']</tt></td>
    <td>Integer</td>
    <td>Determines the SSL port: if set to 0, the HTTPS port will take the value resulting from corus_port + 1 (that is, the Corus' non-HTTPS port is 33000, the SSL port will be 33443).</td>
    <td><tt>0</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['keystore_path']</tt></td>
    <td>String</td>
    <td>The path of the keystore file that will be created to hold the SSL encryption keys.</td>
    <td><tt>$HOME/$CORUS_USER/.keystore.jks</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['keystore_alias']</tt></td>
    <td>String</td>
    <td>The alias associated to the encryption keys in the keystore.</td>
    <td><tt>corus_server</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['keystore_keypass']</tt></td>
    <td>String</td>
    <td>The password to use both as a keypass word, and as a keystore password (having different passwords for each results in an error at runtime).</td>
    <td><tt>1q2w3e4r</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['keystore_CN']</tt></td>
    <td>String</td>
    <td>Value of the CN (common name) keystore attribute.</td>
    <td><tt>corus</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['keystore_OU']</tt></td>
    <td>String</td>
    <td>Value of the OU (organizational unit) keystore attribute.</td>
    <td><tt>Deployment</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['keystore_O']</tt></td>
    <td>String</td>
    <td>Value of the O (organization) keystore attribute.</td>
    <td><tt>www.sapia-oss.org</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['keystore_L']</tt></td>
    <td>String</td>
    <td>Value of the L (locality) keystore attribute.</td>
    <td><tt>Seattle</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['keystore_ST']</tt></td>
    <td>String</td>
    <td>Value of the ST (state) keystore attribute.</td>
    <td><tt>WA</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['keystore_C']</tt></td>
    <td>String</td>
    <td>Value of the C (country) keystore attribute.</td>
    <td><tt>US</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['broadcast_provider']</tt></td>
    <td>String</td>
    <td>Indicates the so-called "broadcast provider" to use for Corus peer discover. Can be either <tt>avis</tt> or <tt>rabbitmq</tt>.</td>
    <td><tt>avis</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['avis_url']</tt></td>
    <td>String</td>
    <td>The URL of the Avis instance to connect to.</td>
    <td><tt>elvin://localhost</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['rabbitmq_url']</tt></td>
    <td>String</td>
    <td>The URL of the RabbitMQ instance to connect to - including the topic name as the last element of the path. <b>IMPORTANT</b>: use a topic specific to Corus.</td>
    <td><tt>rabbitmq://localhost/corus_cluster_channel</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['rabbitmq_vhost']</tt></td>
    <td>String</td>
    <td>The RabbitMQ vhost path to use.</td>
    <td><tt>/</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['rabbitmq_addresses']</tt></td>
    <td>String</td>
    <td>A comma-delimited list of <tt>host:port</tt> pairs corresponding to additional RabbitMQ brokers to connect to (makes sense only if using 
    multiple RabbitMQ brokers organized in a cluster).</td>
    <td><tt>UNDEFINED</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['rabbitmq_username']</tt></td>
    <td>String</td>
    <td>The username associated to the RabbitMQ account to use.</td>
    <td><tt>UNDEFINED</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['rabbitmq_password']</tt></td>
    <td>String</td>
    <td>The password associated to the RabbitMQ account to use.</td>
    <td><tt>UNDEFINED</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['rabbitmq_password']</tt></td>
    <td>String</td>
    <td>The password associated to the RabbitMQ account to use.</td>
    <td><tt>UNDEFINED</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['consul_url']</tt></td>
    <td>String</td>
    <td>The URL of the Consul agent to connect to (e.g.: http://localhost:8500).</td>
    <td><tt>UNDEFINED</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['consul_interval']</tt></td>
    <td>String</td>
    <td>The interval (in seconds) at which Corus should publish its service definition to the Consul agent.</td>
    <td><tt>30</tt></td>
  </tr> 
  <tr>
    <td><tt>['corus']['consul_ttl']</tt></td>
    <td>String</td>
    <td>The time-to-live (in seconds) of the service definition published to the Consul agent by Corus - should be greater than <tt>consul_interval</tt>.</td>
    <td><tt>45</tt></td>
  </tr> 
</table>

#### avis::rpm

<table>
  <tr>
    <th>Key</th>
    <th>Type</th>
    <th>Description</th>
    <th>Default</th>
  </tr>
  <tr>
    <td><tt>['corus']['avis_xms']</tt></td>
    <td>String</td>
    <td>The value to pass to Avis' -Xms JVM startup argument.</td>
    <td><tt>16M</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['avis_xmx']</tt></td>
    <td>String</td>
    <td>The value to pass to Avis' -Xmx JVM startup argument.</td>
    <td><tt>96M</tt></td>
  </tr>  
  <tr>
    <td><tt>['corus']['avis_gc']</tt></td>
    <td>String</td>
    <td>The name of the GC algo to use for Avis.</td>
    <td><tt>UseConcMarkSweepGC</tt></td>
  </tr>
  <tr>
    <td><tt>['corus']['avis_download_url']</tt></td>
    <td>String</td>
    <td>The URL of the Avis RPM package to install.</td>
    <td><tt>http://www.mediafire.com/download/jech2hca5yc49q7/avis-1.2.2.rpm</tt></td>
  </tr>
</table>

Usage
-----
Installs Avis, then Corus:

```json
{
  "run_list": [
    "recipe[corus::avis_rpm]",
    "recipe[corus]"
  ]
}
```

Contributing
------------
1. Fork the repository on Github
2. Create a named feature branch (like `add_component_x`)
3. Write your change
4. Write tests for your change (if applicable)
5. Run the tests, ensuring they all pass
6. Submit a Pull Request using Github

License
-------
This Cookbook is licensed under Apache 2.0.
