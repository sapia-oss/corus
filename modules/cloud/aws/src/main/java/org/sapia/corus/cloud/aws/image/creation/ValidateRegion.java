package org.sapia.corus.cloud.aws.image.creation;

import java.util.Collection;

import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.Region;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * Validates the region passed in, assigns it to the {@link AmazonEC2Client}.
 * 
 * @author yduchesne
 *
 */
public class ValidateRegion implements WorkflowStep<ImageCreationContext> {
  
  private static final String DESC = "Validating specified region";
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  @Override
  public void execute(final ImageCreationContext context) {
    DescribeRegionsResult  regionRes = context.getEc2Client().describeRegions();
    Collection<Region> regionCandidates = Collections2.filter(regionRes.getRegions(), new Predicate<Region>() {
      @Override
      public boolean apply(Region input) {
        return input.getRegionName().equals(context.getConf().getRegion());
      }
    });
    
    Preconditions.checkState(!regionCandidates.isEmpty(), "No region matched: " + context.getConf().getRegion());
    context.getEc2Client().setRegion(com.amazonaws.regions.Region.getRegion(Regions.fromName(context.getConf().getRegion())));    
  }

}
