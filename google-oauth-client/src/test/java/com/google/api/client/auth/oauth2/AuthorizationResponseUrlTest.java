/*
 * Copyright (c) 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.auth.oauth2;

import junit.framework.TestCase;

import java.util.Collections;

/**
 * Tests {@link AuthorizationResponseUrl}.
 * 
 * @author Yaniv Inbar
 */
public class AuthorizationResponseUrlTest extends TestCase {

  public void testConstructor() {
    AuthorizationResponseUrl response = new AuthorizationResponseUrl("http://example.com");
    assertTrue(response.isEmpty());
    response =
        new AuthorizationResponseUrl(
            "https://client.example.com/cb?code=SplxlOBeZQQYbYS6WxSbIA&state=xyz");
    assertEquals("xyz", response.getState());
    assertEquals(Collections.singletonList("SplxlOBeZQQYbYS6WxSbIA"), response.get("code"));
    response =
        new AuthorizationResponseUrl("https://client.example.com/cb?error=access_denied&state=xyz");
    assertEquals("access_denied", response.getError());
    assertEquals("xyz", response.getState());
  }
}
