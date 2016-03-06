package org.sapia.corus.cloud.platform.rest;

public interface FeedbackHandler {
  
  public void onInfoFeedback(String[] messages);

  public void onErrorFeedback(String[] messages);
  
  class NullFeedbackHandler implements FeedbackHandler {
    
    @Override
    public void onErrorFeedback(String[] messages) {
    }
    
    @Override
    public void onInfoFeedback(String[] messages) {
    }
  }

}
