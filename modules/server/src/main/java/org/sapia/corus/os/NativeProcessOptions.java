package org.sapia.corus.os;

import java.util.Map;

import com.google.common.base.Optional;

public class NativeProcessOptions {

  public static Optional<Integer> extractOptionValueAsInteger(String optionName, Map<String, String> processOptions) {
    String value = processOptions.get(optionName);
    if (value == null) {
      return Optional.absent();
    } else {
      return Optional.of(Integer.parseInt(value));
    }
  }

  public static Optional<Boolean> extractOptionValueAsBoolean(String optionName, Map<String, String> processOptions) {
    String value = processOptions.get(optionName);
    if (value == null) {
      return Optional.absent();
    } else {
      return Optional.of(Boolean.parseBoolean(value));
    }
  }

}
