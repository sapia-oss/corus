<#macro scale_down suffix>
"ScaleDown${suffix}": {
  "Type":"AWS::AutoScaling::ScalingPolicy",
  "Properties": {
    "AdjustmentType": "ChangeInCapacity",
    "AutoScalingGroupName": { "Ref": "AutoScaling${suffix}" },
    "Cooldown": "60",
    "ScalingAdjustment": "-1"
  }
}
</#macro>