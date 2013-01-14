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
 * Tests {@link PasswordTokenRequest}.
 *
 * @author Yaniv Inbar
 */
public class PasswordTokenRequestTest extends TestCase {

  private static final String USERNAME = "johndoe";
  private static final String PASSWORD = "A3ddj3w";

  public void testConstructor() {
    check(new PasswordTokenRequest(TokenRequestTest.TRANSPORT, TokenRequestTest.JSON_FACTORY,
        TokenRequestTest.AUTHORIZATION_SERVER_URL, USERNAME, PASSWORD));
  }

  private void check(PasswordTokenRequest request) {
    TokenRequestTest.check(request, "password");
    assertEquals(USERNAME, request.getUsername());
    assertEquals(PASSWORD, request.getPassword());
  }
}
