package org.sapia.corus.cloud.aws.image.creation;

import org.mockito.Mockito;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;

public class ImageCreationTestHelper {
  
  static ImageCreationContext createContext() {
    return new ImageCreationContext(
        new ImageCreationConf()
          .withAwsCredentials(Mockito.mock(AWSCredentials.class))
          .withKeypair("testKeyPair")
          .withRecipeAttributes("")
          .withImageId("testImage")
          .withSubnetId("testSubnet")
          .withIamRole("testRole"), 
        Mockito.mock(AmazonEC2.class)
    );
  }

}
