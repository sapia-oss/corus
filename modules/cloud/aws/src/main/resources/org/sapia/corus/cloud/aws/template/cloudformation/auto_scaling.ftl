<#include "../common/aws.ftl">
<#macro auto_scaling machine machineSuffix topology environment region zone domainName globalTags>
"AutoScaling${machine.alphaNumericName}${machineSuffix}": {
  "Type":"AWS::AutoScaling::AutoScalingGroup",
  "Properties": {
    "LaunchConfigurationName": { "Ref": "Launch${machine.alphaNumericName}${machineSuffix}" },
    "AvailabilityZones": [ "${zone.name}" ],
    "MinSize": "${machine.minInstances}",
    "MaxSize": "${machine.maxInstances}",
    <#if (zone.subnets?size > 0)>
      <#assign subnetIndex = 0>
      "VPCZoneIdentifier": [
      <#list zone.subnets as subnet>
        <#if (subnetIndex > 0)>,</#if>
        "${subnet.id}"
        <#assign subnetIndex = subnetIndex + 1>
      </#list>
      ],
    </#if>
    <#if (machine.loadBalancerAttachments?size > 0)>
      <#assign lbIndex = 0>
      "LoadBalancerNames": [
      <#list machine.loadBalancerAttachments as lb>
        <#if (lbIndex > 0)>,</#if>
        "${lb.name}"
        <#assign lbIndex = lbIndex + 1>
      </#list>
      ],
    </#if>
    <@assignTags 
      topology=topology 
      environment=environment 
      region=region 
      zone=zone 
      domainName=domainName 
      globalTags=globalTags/>
  }
}
</#macro>