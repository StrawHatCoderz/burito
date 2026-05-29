package com.burito.utils;

public class Parser {
  private Parser() {}

  public static String parseJwtToken(String authHeader) {
    return authHeader.substring(7);
  }
}
