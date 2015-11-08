<#macro assignTags topology environment region zone domainName globalTags>
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
]

</#macro>
