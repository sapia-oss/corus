package org.sapia.corus.client.services.configurator;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.IOUtil;
import org.sapia.corus.client.common.OptionalValue;

@RunWith(MockitoJUnitRunner.class)
public class JsonPropertyParserTest {

  @Mock
  private Consumer<Property> callback;
  
  @Test
  public void testParse() throws IOException {
    String content = IOUtil.textStreamToString(new FileInputStream(new File("etc/test/properties.json")));
    
    JsonPropertyParser.parse(OptionalValue.none(), content, callback);
    
    verify(callback).accept(new Property("prop1", "value1", "cat1"));
    verify(callback).accept(new Property("prop2", "value2", "cat1"));

    verify(callback).accept(new Property("prop3", "value3", "cat1"));
    verify(callback).accept(new Property("prop3", "value3", "cat2"));

    verify(callback).accept(new Property("prop4", "value4"));

  }
  
  @Test
  public void testParse_with_global_category() throws IOException {
    String content = IOUtil.textStreamToString(new FileInputStream(new File("etc/test/properties.json")));
    
    JsonPropertyParser.parse(OptionalValue.of("global"), content, callback);
    
    verify(callback).accept(new Property("prop1", "value1", "cat1"));
    verify(callback).accept(new Property("prop1", "value1", "global"));
    verify(callback).accept(new Property("prop2", "value2", "cat1"));
    verify(callback).accept(new Property("prop2", "value2", "global"));

    verify(callback).accept(new Property("prop3", "value3", "cat1"));
    verify(callback).accept(new Property("prop3", "value3", "cat2"));
    verify(callback, times(2)).accept(new Property("prop3", "value3", "global"));

    verify(callback).accept(new Property("prop4", "value4"));
    verify(callback).accept(new Property("prop4", "value4", "global"));
  }

}
