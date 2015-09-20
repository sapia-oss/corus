<#macro launch_conf topology machine domainName suffix>
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
      <#list machine.getParam("securityGroups").value?split(",") as sg>
        <#if (sgGroupCount > 0)>,</#if>
        "${sg?trim}"
        <#assign sgGroupCount = sgGroupCount + 1>
      </#list>
    ],
    "IamInstanceProfile": "${topology.getParam("corus.ec2.instance.role").value}",
    "KeyName" : "${machine.getParam("keyName").value}",
    "UserData" : { 
      "Fn::Base64" :  {
          "Fn::Join" : ["", [
          <@generate_user_data_file topology machine domainName></@generate_user_data_file>
          ]]              
      }
    }
  }
}
</#macro>