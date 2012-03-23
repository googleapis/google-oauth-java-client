/*
 * Copyright (c) 2012 Google Inc.
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

package com.google.api.client.auth.jsontoken;

import com.google.api.client.auth.jsontoken.JsonWebToken.Payload;

import junit.framework.TestCase;

/**
 * Tests {@link JsonWebToken}.
 *
 * @author Yaniv Inbar
 */
public class JsonWebTokenTest extends TestCase {

  public void testPayloadIsValidTime() {
    Payload payload = new Payload();
    payload.setExpirationTimeSeconds(100 + System.currentTimeMillis() / 1000);
    payload.setIssuedAtTimeSeconds(System.currentTimeMillis() / 1000);
    Payload payload2 = new Payload();
    payload2.setExpirationTimeSeconds(System.currentTimeMillis() / 1000);
    payload2.setIssuedAtTimeSeconds(-1 + System.currentTimeMillis() / 1000);
    assertTrue(payload.isValidTime(5));
    assertFalse(payload2.isValidTime(0));
    assertTrue(payload2.isValidTime(10));
  }
}
