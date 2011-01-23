package org.sapia.corus.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class IteratorFilter<T>{
  
  Matcher<T> matcher;
  
  public IteratorFilter(Matcher<T> matcher) {
    this.matcher = matcher;
  }
  
  public FilterResult<T> filter(Iterator<T> iterator){
    List<T> toReturn = new ArrayList<T>();
    while(iterator.hasNext()){
      T toMatch = iterator.next();
      if(matcher.matches(toMatch)){
        toReturn.add(toMatch);
      }
    }
    return new FilterResult<T>(toReturn);
  }
  
  public static class FilterResult<T>{
    
    private List<T> result;
    
    public FilterResult(List<T> result) {
      this.result = result;
    }
    
    public List<T> get() {
      return result;
    }
    
    public FilterResult<T> sort(Comparator<T> c){
      Collections.sort(result, c);
      return this;
    }
  }

}
