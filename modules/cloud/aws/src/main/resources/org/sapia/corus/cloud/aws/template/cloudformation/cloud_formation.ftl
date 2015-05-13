<#include "../common/json.ftl">
<#include "../common/aws.ftl">
<#include "scale_up.ftl">
<#include "scale_down.ftl">
<#include "launch_conf.ftl">

{
  <#assign defaultBucketName = topology.org + "-" + topology.application>
  "AWSTemplateFormatVersion":"2010-09-09",
  "Description" : "Cloud Formation for topology: ${topology.application}",
  "Resources": {
    <#assign regionIndex = 0>
    <#list environment.regions as region>
      <#if (regionIndex > 0)>,</#if>
      <#assign zoneIndex = 0>
      <#list region.zones as zone>
        <#if (zoneIndex > 0)>,</#if>
        <#assign clusterIndex = 0>
        <#list environment.clusters as cluster>
          <#if (clusterIndex > 0)>,</#if>
          <#assign domainName = environment.name + "-" + zone.name + "-" + clusterIndex>
          <#assign machineIndex = 0>
          <#list cluster.machines as machine>
            <#assign machineSuffix = regionIndex + "" + zoneIndex + "" + clusterIndex + "" + machineIndex>
            <#if (machineIndex > 0)>,</#if>
            <@scale_up suffix="${machine.alphaNumericName}${machineSuffix}"/>,
            <@scale_down suffix="${machine.alphaNumericName}${machineSuffix}"/>,
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
                  region=region zone=zone 
                  domainName=domainName 
                  globalTags=globalTags/>
              }
            },
            <@launch_conf 
              topology=topology 
              environment=environment 
              region=region zone=zone 
              domainName=domainName 
              globalTags=globalTags 
              machine=machine 
              suffix="${machine.alphaNumericName}${machineSuffix}"/> 
            <#assign machineIndex = machineIndex + 1>
          </#list>
          <#assign clusterIndex = clusterIndex + 1>
        </#list>             
        <#assign zoneIndex = zoneIndex + 1>
      </#list>  
      <#assign regionIndex = regionIndex + 1>
    </#list> 
    }
  }
