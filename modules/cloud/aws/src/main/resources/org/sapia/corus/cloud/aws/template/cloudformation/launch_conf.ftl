<#include "../common/aws.ftl">
<#macro launch_conf topology environment region zone domainName globalTags machine suffix>
"Launch${suffix}": {
  "Type":"AWS::AutoScaling::LaunchConfiguration",
  "Properties": {
  <#if machine.publicIpEnabled>
    "AssociatePublicIpAddress": true,
  </#if>
    "ImageId": "${machine.imageId}",
    "InstanceType": "${machine.instanceType}",
    "SecurityGroups": [
      <#assign sgGroupCount = 0>
      <#list machine.getParam("corus.aws.ec2.security.groups").value?split(",") as sg>
        <#if (sgGroupCount > 0)>,</#if>
        "${sg?trim}"
        <#assign sgGroupCount = sgGroupCount + 1>
      </#list>
    ],
    "IamInstanceProfile": "${topology.getParam("corus.aws.ec2.iam.role").value}",
    "KeyName" : "${machine.getParam("corus.aws.ec2.key.name").value}",
    "UserData" : { 
      "Fn::Base64" :  {
          "Fn::Join" : ["", [
          <@generate_user_data_file topology machine domainName></@generate_user_data_file>
          ]]              
      }
    },
    <@assignTags 
      topology=topology 
      environment=environment 
      region=region zone=zone 
      domainName=domainName 
      globalTags=globalTags/>
  }
}
</#macro>