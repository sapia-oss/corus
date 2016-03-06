<#macro json line>
  "echo '${line?json_string}' >> etc/corus/user_data.json\n"
</#macro>

<#macro generate_user_data_file topology domainName machine suffix>
  "#!/bin/bash\n",
  "mkdir -p /etc/corus\n",
  <@json "{ \"corus\": {"></@json>,
  <@json "    \"domain\": \"${domainName}\","></@json>,
  <@json "    \"repo-role\": \"${machine.repoRole}\","></@json>,
  <@json "    \"artifacts\": ["></@json>,
  <#assign count = 0>
  <#list machine.artifacts as artifact>
    <@json "  {"></@json>,
    <@json "    \"url\": \"${artifact.url}\""></@json>,
    <@json "  }"></@json>,
    <#if (count < machine.artifacts?size)>
      <@json ","></@json>,
    </#if>
    <#assign count = count + 1>
  </#list>  
  <@json "    ]"></@json>,
  <@json "    \"server\": "></@json>,
  <@json "    { \"properties\": ["></@json>,
    <#assign count = 0>
    <#list machine.serverProperties.properties as prop>
      <#if (count < machine.serverProperties.properties?size - 1)>
      <@json "       { \"name\": \"${prop.name}\", \"value\": \"${prop.value}\" },"></@json>,      
      <#else>
	  <@json "       { \"name\": \"${prop.name}\", \"value\": \"${prop.value}\" }"></@json>,      
      </#if>
  	  <#assign count = count + 1>
    </#list>
  <@json "    ]},"></@json>,
  
  <@json "    { \"tags\": ["></@json>,
    <#assign count = 0>
    <#list machine.serverTags as tag>
      <#if (count < machine.serverTags?size - 1)>
      <@json "        \"${tag.value}\","></@json>,  
      <#else>
      <@json "        \"${tag.value}\""></@json>,  
      </#if>
      <#assign count = count + 1>
    </#list>
  <@json "    ]},"></@json>,
  <@json "    \"processes\": "></@json>,
  <@json "    { \"properties\": ["></@json>,
    <#assign count = 0>
    <#list machine.processProperties.properties as prop>
      <#if (count < machine.processProperties.properties?size - 1)>
      	<@json "       { \"name\": \"${prop.name}\", \"value\": \"${prop.value}\" },"></@json>,
      <#else>
      	<@json "       { \"name\": \"${prop.name}\", \"value\": \"${prop.value}\" }"></@json>, 
      </#if>
      <#assign count = count + 1>
    </#list>
  <@json "    ]}"></@json>,
  <@json "}"></@json>,
  "chown -R ${topology.getParam("corus.user", "corus").value}:${topology.getParam("corus.group", "corus").value} /etc/corus\n"
  <#if (machine.userData.lines?size > 0)>
  ,
    <#assign count = 0>
    <#list machine.userData.lines as line>
      <#if (count > 0)>,</#if>
      "${line}\n"
      <#assign count = count + 1>
    </#list>
  </#if>
</#macro>