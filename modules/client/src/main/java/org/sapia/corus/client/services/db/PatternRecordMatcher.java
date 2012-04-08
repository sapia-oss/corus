package org.sapia.corus.client.services.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.services.db.persistence.ClassDescriptor;
import org.sapia.corus.client.services.db.persistence.FieldDescriptor;
import org.sapia.corus.client.services.db.persistence.Record;

/**
 * An instance of this class matches field values of a {@link Record} based on a predefined
 * patterns. It also takes custom {@link FieldMatcher}s (see {@link #add(String, FieldMatcher)}).
 * 
 * @see #addPattern(String, String)
 * 
 * @author yduchesne
 *
 * @param <V>
 */
public class PatternRecordMatcher<V> implements RecordMatcher<V>{
  
  private ClassDescriptor<V> 							desc;
  private Map<String, List<FieldMatcher>> fieldMatchers = new HashMap<String, List<FieldMatcher>>();
  
  public PatternRecordMatcher(ClassDescriptor<V> desc) {
    this.desc = desc;
  }
  
  /**
   * @return this instance's field names.
   */
  public Collection<String> fieldNames(){
    return Collections.unmodifiableSet(fieldMatchers.keySet());
  }
  
  
  /**
   * Creates an instance of this class and returns it.
   * 
   * @param dbMap a {@link DbMap} for which to create a new instance of this class.
   * @return the newly created {@link PatternRecordMatcher}.
   */
  public static <K, V> PatternRecordMatcher<V> createFor(DbMap<K, V> dbMap){
    return new PatternRecordMatcher<V>(dbMap.getClassDescriptor());
  }

  /**
   * @param fieldName a field name.
   * @param matcher the matcher to use against the corresponding field's value.
   * @return this instance.
   */
  public PatternRecordMatcher<V> add(String fieldName, FieldMatcher matcher){
    getMatchers(fieldName, true).add(matcher);
    return this;
  }

  /**
   * @param fieldName a field name.
   * @param pattern the pattern to use against the corresponding field's value.
   * @return this instance.
   */
  public PatternRecordMatcher<V> addPattern(String fieldName, String pattern){
    if(pattern == null){
      getMatchers(fieldName, true).add(new ArgFieldMatcher(null));
    }
    else{
      getMatchers(fieldName, true).add(new ArgFieldMatcher(ArgFactory.parse(pattern)));
    }
    return this;
  }

  
  @Override
  public boolean matches(Record<V> rec) {
    for(String fieldName:fieldMatchers.keySet()){
      List<FieldMatcher> fms = fieldMatchers.get(fieldName);
      if(fms != null){
        FieldDescriptor fd = desc.getFieldForName(fieldName);
        for(FieldMatcher fm:fms){
          Object value = rec.getValueAt(fd.getIndex());
          if(!fm.matches(value)){
            return false;
          }
        }
      }
    }
    return true;
  }
  
  public boolean matches(Object o){

    for(String fieldName:this.fieldMatchers.keySet()){
      FieldDescriptor fd = desc.getFieldForName(fieldName);
      List<FieldMatcher> fms = fieldMatchers.get(fieldName);
      Object otherValue = fd.invokeAccessor(o);
      if(fms != null){
        for(FieldMatcher fm:fms){
          if(!fm.matches(otherValue)){
            return false;
          }
        }
      }
    }
    return true;
  }

  private List<FieldMatcher> getMatchers(String name, boolean create){
    List<FieldMatcher> toReturn = fieldMatchers.get(name);
    if(toReturn == null){
      if(create){
        toReturn = new ArrayList<FieldMatcher>();
        fieldMatchers.put(name, toReturn);
      }
    }
    return toReturn;
  }
  
 
  
  public interface FieldMatcher{
    
    public boolean matches(Object value);
   
  }
  
  public class ArgFieldMatcher implements FieldMatcher{
    
    Arg arg;
    
    ArgFieldMatcher(Arg arg) {
      this.arg = arg;
    }
    
    @Override
    public boolean matches(Object value) {
      if(value == null){
        return arg == null;
      }
      else if(arg == null){
        return true;
      }
      if(value instanceof String){
        return arg.matches((String)value);
      }
      else{
        return arg.matches(value.toString());
      } 
    }
    
  }
}
