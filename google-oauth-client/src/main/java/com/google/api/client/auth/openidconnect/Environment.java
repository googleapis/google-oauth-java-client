package com.google.api.client.auth.openidconnect;

class Environment {
  public String getVariable(String name) {
    return System.getenv(name);
  }
}
