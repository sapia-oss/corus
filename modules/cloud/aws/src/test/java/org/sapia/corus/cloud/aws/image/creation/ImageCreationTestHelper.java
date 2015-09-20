package org.sapia.corus.cloud.aws.image.creation;

import org.mockito.Mockito;
import org.sapia.corus.cloud.aws.image.EC2ImageConf;

import com.amazonaws.services.ec2.AmazonEC2;

public class ImageCreationTestHelper {
  
  static ImageCreationContext createContext() {
    return new ImageCreationContext(new EC2ImageConf(), Mockito.mock(AmazonEC2.class));
  }

}
