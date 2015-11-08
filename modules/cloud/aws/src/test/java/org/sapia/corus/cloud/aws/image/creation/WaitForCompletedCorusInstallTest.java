package org.sapia.corus.cloud.aws.image.creation;

import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

public class WaitForCompletedCorusInstallTest {
  
  private ImageCreationContext         context;
  private WaitForCompletedCorusInstall step;
  private int retryCount;
  
  @Before
  public void setUp() {
    context = ImageCreationTestHelper.createContext();
    context.assignAllocatedPublicIp("publicIp", "allocId");
    step = new WaitForCompletedCorusInstall() {
      
      @Override
      protected String getCorusResponse(ImageCreationContext context,
          URL corusUrl) throws IOException, IllegalStateException {
        if (retryCount > 0) {
          return "CLOUD_READY";
        } else {
          retryCount++;
          throw new IOException("Corus not yet up");
        }
      }
    };
    retryCount = 0;
  }

  @Test
  public void testExecute() throws Exception {
    step.execute(context);
  }

}
