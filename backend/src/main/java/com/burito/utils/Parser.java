package com.burito.utils;

public class Parser {
  public static String parseJwtToken(String authHeader) {
    return authHeader.substring(7);
  }
}
