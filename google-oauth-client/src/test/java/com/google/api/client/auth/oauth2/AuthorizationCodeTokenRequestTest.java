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

/**
 * Tests {@link AuthorizationCodeTokenRequest}.
 * 
 * @author Yaniv Inbar
 */
public class AuthorizationCodeTokenRequestTest extends TestCase {

  private static final String CODE = "i1WsRn1uB1";
  private static final String REDIRECT_URI = "https://client.example.com/rd";

  public void testConstructor() {
    check(new AuthorizationCodeTokenRequest(TokenRequestTest.TRANSPORT,
        TokenRequestTest.JSON_FACTORY, TokenRequestTest.AUTHORIZATION_SERVER_URL, CODE)
        .setRedirectUri(REDIRECT_URI));
  }

  private void check(AuthorizationCodeTokenRequest request) {
    TokenRequestTest.check(request, "authorization_code");
    assertEquals(CODE, request.getCode());
    assertEquals(REDIRECT_URI, request.getRedirectUri());
  }
}
