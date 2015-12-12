package org.sapia.corus.client.common.json;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.util.Lists;
import org.junit.Test;

public class JsonObjectInputTest {

  private JsonObjectInput sut;

  @Test
  public void testFields_empty() throws Exception {
    sut = new JsonObjectInput("{}");
    Iterable<String> actual = sut.fields();

    assertThat(Lists.newArrayList(actual)).hasSize(0);
  }

  @Test
  public void testFields_single() throws Exception {
    sut = new JsonObjectInput("{\"name\": \"snafoo\"}");
    Iterable<String> actual = sut.fields();

    assertThat(Lists.newArrayList(actual)).containsSequence("name");
  }

  @Test
  public void testFields_multi() throws Exception {
    sut = new JsonObjectInput("{\"foo\": \"bar\", \"sna\": \"foo\"}");
    Iterable<String> actual = sut.fields();

    assertThat(Lists.newArrayList(actual)).containsSequence("foo", "sna");
  }

}
