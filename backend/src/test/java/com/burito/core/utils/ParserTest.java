package com.burito.core.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserTest {

  @Test
  void shouldStripBearerPrefixFromAuthHeader() {
    assertEquals("some.jwt.token", Parser.parseJwtToken("Bearer some.jwt.token"));
  }
}
