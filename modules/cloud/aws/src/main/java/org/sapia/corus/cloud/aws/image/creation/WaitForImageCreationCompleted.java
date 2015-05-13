package org.sapia.corus.cloud.aws.image.creation;

import java.util.Set;

import org.sapia.corus.cloud.platform.util.RetryLatch;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.google.common.collect.Sets;

public class WaitForImageCreationCompleted implements WorkflowStep<ImageCreationContext> {
  
  private static final String DESC = "waiting for completion of image creation";
  
  private static final Set<String> FAILURE_STATES = Sets.newHashSet("invalid", "failed", "error");
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  public void execute(ImageCreationContext context) throws Exception {
    DescribeImagesRequest describeImgReq = new DescribeImagesRequest();
    describeImgReq.withImageIds(context.getCreatedImageId());
    
    RetryLatch latch = new RetryLatch(context.getConf().getImageCreationCheckRetry());
    
    do {
      DescribeImagesResult result = context.getEc2Client().describeImages(describeImgReq);
      if (!result.getImages().isEmpty()) {
        Image img = result.getImages().get(0);
        if (img.getState().equals("available")) {
          break;
        } else if (FAILURE_STATES.contains(img.getState().toLowerCase())) {
          throw new IllegalStateException(String.format("Image creation failed for image %s (%s): %s", 
              context.getCreatedImageId(), img.getState(), img.getStateReason()));
        } 
      }
    } while (latch.incrementAndPause().shouldContinue());
  }
}
