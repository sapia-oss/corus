<#macro json line>
  "echo etc/corus/user_data.json << ${line?json_string}\n"
</#macro>

<#macro generate_user_data_file topology machine domain_name>
  "#!/bin/bash\n",
  "mkdir -p /etc/corus\n",
  <@json "{ \"corus\": "></@json>,
  <@json "    \"domain\": \"${domain_name}\","></@json>,
  <@json "    \"repo-role\": \"${machine.repoRole}\","></@json>,
  <@json "    \"artifacts\": ["></@json>,
  <#assign artifactIndex = 0>
  <#list machine.artifacts as artifact>
    <#if (artifactIndex > 0)><@json ","></@json></#if>
    <@json "  {"></@json>
    <@json "    \"url\": \"${artifact.url}\""></@json>
    <@json "  }"></@json>
    <#assign artifactIndex = artifactIndex + 1>
  </#list>  
  <@json "  ]"></@json>,
  <@json "    \"server\": "></@json>,
  <@json "    { \"properties\": ["></@json>
    <#list machine.serverProperties.properties as prop>
  ,
  <@json "       { \"name\": \"${prop.name}\", \"value\": \"${prop.value}\" }"></@json>,
    </#list>
  <@json "    ]}"></@json>,
  
  <@json "    { \"tags\": ["></@json>,
    <#assign count = 0>
    <#list machine.serverTags as tag>
      <#if (count > 0)>
      ,
      </#if>
        <@json "        \"${tag.value}\""></@json>  
      <#assign count = count + 1>
    </#list>
  ,
  <@json "    ]}"></@json>,
  <@json "    \"processes\": "></@json>,
  <@json "    { \"properties\": ["></@json>,
    <#assign count = 0>
    <#list machine.processProperties.properties as prop>
      <#if (count > 0)>,</#if>
      <@json "       { \"name\": \"${prop.name}\", \"value\": \"${prop.value}\" }"></@json>
      <#assign count = count + 1>
    </#list>
  ,
  <@json "    ]}"></@json>,
  <@json "}"></@json>  
  "chown -R ${topology.getParam("corus.user").value}:${topology.getParam("corus.group").value} /etc/corus\n",
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