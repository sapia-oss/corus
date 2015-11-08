package org.sapia.corus.cloud.aws.image.creation;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.Region;

public class ValidateRegionTest {
  
  private ImageCreationContext context;
  private ValidateRegion       step;
  
  @Before
  public void setUp() {
    context = ImageCreationTestHelper.createContext();
    step = new ValidateRegion();
  }

  @Test
  public void testExecute_matching_region() {
    Region toReturn = new Region().withRegionName(context.getSettings().getNotNull("region").get(String.class));
    when(context.getEc2Client().describeRegions()).thenReturn(new DescribeRegionsResult().withRegions(toReturn));
    
    step.execute(context); 
  }

  
  @Test(expected = IllegalStateException.class)
  public void testExecute_no_matching_region() {
    Region toReturn = new Region().withRegionName("no-match");
    when(context.getEc2Client().describeRegions()).thenReturn(new DescribeRegionsResult().withRegions(toReturn));
    
    step.execute(context); 
  }
}
