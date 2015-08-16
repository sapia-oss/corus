package org.sapia.corus.client.rest;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.http.HttpResponseFacade;

@RunWith(MockitoJUnitRunner.class)
public class ProgressResultTest {

  private ProgressResult success, error1, error2;
  
  @Mock
  private JsonStreamable json;
  
  @Before
  public void setUp() throws Exception {
    success = new ProgressResult(Arrays.asList("success"))
      .setStatus(HttpResponseFacade.STATUS_OK);
   
    error1 = new ProgressResult(Arrays.asList("error"), new Exception("exc"));
    error2 = new ProgressResult(Arrays.asList("error"))
      .setStatus(HttpResponseFacade.STATUS_SERVER_ERROR);
  }

  @Test
  public void testWithNestedJson() {
    success.withNestedJson(json);
    JsonStream stream = Mockito.mock(JsonStream.class);
    when(stream.beginObject()).thenReturn(stream);
    when(stream.field(anyString())).thenReturn(stream);
    when(stream.endObject()).thenReturn(stream);
    
    success.toJson(stream, ContentLevel.DETAIL);
    
    verify(json).toJson(stream, ContentLevel.DETAIL);
  }

  @Test
  public void testAddMessage() {
    success.addMessage("test");
    
    assertTrue(success.getMessages().contains("test"));
  }

  @Test
  public void testIsError() {
    assertTrue(error1.isError());
  }
  
  @Test
  public void testIsError_with_status() {
    assertTrue(error2.isError());
  }

  @Test
  public void testIsError_false() {
    assertFalse(success.isError());
  }

  @Test
  public void testMerge_success_with_exception() {
    success.merge(error1);
    
    assertTrue(success.isError());
    assertNotNull(success.getThrowable());
    assertEquals(2, success.getMessages().size());
  }

  @Test
  public void testMerge_success_with_error_status() {
    success.merge(error2);
    
    assertTrue(success.isError());
    assertNull(success.getThrowable());
    assertEquals(2, success.getMessages().size());
  }
  
  @Test
  public void testMerge_error_with_success() {
    error2.merge(success);
    
    assertTrue(error2.isError());
    assertNull(error2.getThrowable());
    assertEquals(1, error2.getMessages().size());    
  }

}
