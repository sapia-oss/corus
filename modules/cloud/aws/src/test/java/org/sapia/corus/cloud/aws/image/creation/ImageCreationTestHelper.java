package org.sapia.corus.cloud.aws.image.creation;

import org.mockito.Mockito;

import com.amazonaws.services.ec2.AmazonEC2;

public class ImageCreationTestHelper {
  
  static ImageCreationContext createContext() {
    return new ImageCreationContext(new ImageCreationConf(), Mockito.mock(AmazonEC2.class));
  }

}
