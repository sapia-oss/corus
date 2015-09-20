package org.sapia.corus.cloud.aws.image.creation;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;

/**
 * Creating an image off the instance that was installed (launched and stopped).
 * 
 * @author yduchesne
 *
 */
public class InvokeCreateImage implements WorkflowStep<ImageCreationContext> {
  
  private static final String DESC = "Creating image from instance";
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  public void execute(ImageCreationContext context) throws Exception {
    CreateImageRequest createImgReq = new CreateImageRequest(context.getStartedInstanceId(), 
       context.getConf().getCorusImageNamePrefix() + "-" + new SimpleDateFormat("yyyyMMdd-hhmmss").format(new Date()));
    CreateImageResult createImgRes = context.getEc2Client().createImage(createImgReq);
    context.assignCreatedImageId(createImgRes.getImageId());
  }

}
