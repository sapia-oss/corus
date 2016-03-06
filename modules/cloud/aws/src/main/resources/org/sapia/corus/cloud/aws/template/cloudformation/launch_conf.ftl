<#include "../common/aws.ftl">
<#macro launch_conf topology environment region zone cluster domainName globalTags machine suffix>
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
      <#list machine.getParam("corus.cloud.instance.security.groups").value?split(",") as sg>
        <#if (sgGroupCount > 0)>,</#if>
        "${sg?trim}"
        <#assign sgGroupCount = sgGroupCount + 1>
      </#list>
    ],
    "IamInstanceProfile": "${topology.getParam("corus.cloud.instance.role").value}",
    "KeyName" : "${machine.getParam("corus.cloud.instance.key.name").value}",
    "UserData" : { 
      "Fn::Base64" :  {
          "Fn::Join" : ["", [
          <@generate_user_data_file topology domainName machine suffix></@generate_user_data_file>
          ]]              
      }
    },
    <#if (machine.seedNode)>
   		<@assignTags 
	      topology=topology 
	      environment=environment 
	      region=region 
	      zone=zone 
	      domainName=domainName 
	      globalTags=globalTags
	      specificTags={"seedNode" : "true"} />
    <#else>
	    <@assignTags 
	      topology=topology 
	      environment=environment 
	      region=region 
	      zone=zone 
	      domainName=domainName 
	      globalTags=globalTags />
	</#if>
  }
}
</#macro>