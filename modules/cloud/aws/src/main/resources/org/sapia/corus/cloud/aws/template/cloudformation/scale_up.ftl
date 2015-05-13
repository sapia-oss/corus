<#macro scale_up suffix>
"ScaleUp${suffix}": {
  "Type":"AWS::AutoScaling::ScalingPolicy",
  "Properties": {
    "AdjustmentType": "ChangeInCapacity",
    "AutoScalingGroupName": { "Ref": "AutoScaling${suffix}" },
    "Cooldown": "60",
    "ScalingAdjustment": "1"
  }
}
</#macro>