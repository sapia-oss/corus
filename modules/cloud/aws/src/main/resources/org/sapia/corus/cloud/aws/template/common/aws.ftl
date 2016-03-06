<#macro assignTags topology environment region zone domainName globalTags specificTags={}>
"Tags": [
  {
    "Key": "corus.environment",
    "Value": "${environment.name}",
    "PropagateAtLaunch" : true
  },
  {
    "Key": "corus.domain",
    "Value": "${domainName}",
    "PropagateAtLaunch" : true
  },
  {
    "Key": "corus.topology.org",
    "Value": "${topology.org}",
    "PropagateAtLaunch" : true
  },
  {
    "Key": "corus.topology.application",
    "Value": "${topology.application}",
    "PropagateAtLaunch" : true
  },
  {
    "Key": "corus.topology.version",
    "Value": "${globalTags["corus.topology.version"]!topology.version}",
    "PropagateAtLaunch" : true
  },
  {
    "Key": "corus.region",
    "Value": "${region.name}",
    "PropagateAtLaunch" : true
  },
  {
    "Key": "corus.zone",
    "Value": "${zone.name}",
    "PropagateAtLaunch" : true
  }  
  <#if (globalTags?size > 0)>
  	<#assign keys = globalTags?keys>
 	<#list keys as k>
 	  <#if (k != "corus.topology.version")>
 	  	,
 		{
		  "Key": "${k}",
		  "Value": "${globalTags[k]}",
		  "PropagateAtLaunch" : true
 		}
 	  </#if>
 	</#list>
  </#if>
  <#if (specificTags?size > 0)>
  	<#assign keys = specificTags?keys>
 	<#list keys as k>
 	  <#if (k != "corus.topology.version")>
 	  	,
 		{
		  "Key": "${k}",
		  "Value": "${specificTags[k]}",
		  "PropagateAtLaunch" : true
 		}
 	  </#if>
 	</#list>
  </#if>
]

</#macro>
