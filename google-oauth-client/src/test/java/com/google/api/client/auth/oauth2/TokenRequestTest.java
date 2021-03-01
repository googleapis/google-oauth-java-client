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

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.common.collect.ImmutableList;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Tests {@link TokenRequest}.
 *
 * @author Yaniv Inbar
 */
public class TokenRequestTest extends TestCase {

  static final MockHttpTransport TRANSPORT = new MockHttpTransport();
  static final GsonFactory JSON_FACTORY = new GsonFactory();
  static final GenericUrl AUTHORIZATION_SERVER_URL =
      new GenericUrl("https://server.example.com/authorize");

  public void testConstructor() {
    TokenRequest request =
        new TokenRequest(TRANSPORT, JSON_FACTORY, AUTHORIZATION_SERVER_URL, "foo");
    check(request, "foo");
    assertEquals(TokenResponse.class, request.getResponseClass());
  }

  static void check(TokenRequest request, String grantType) {
    assertEquals(grantType, request.getGrantType());
    assertNull(request.getScopes());
    assertEquals(TRANSPORT, request.getTransport());
    assertEquals(JSON_FACTORY, request.getJsonFactory());
    assertEquals(AUTHORIZATION_SERVER_URL, request.getTokenServerUrl());
  }

  public void testScopes() throws IOException {
    TokenRequest request = new TokenRequest(TRANSPORT, JSON_FACTORY, AUTHORIZATION_SERVER_URL, "foo")
        .setScopes(ImmutableList.of("scope1"));
    HttpContent content = new UrlEncodedContent(request);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    content.writeTo(outputStream);
    String encoded = new String(outputStream.toByteArray());
    assertEquals("grant_type=foo&scope=scope1", encoded);
  }

  public void testEmptyScopes() throws IOException {
    TokenRequest request = new TokenRequest(TRANSPORT, JSON_FACTORY, AUTHORIZATION_SERVER_URL, "foo")
        .setScopes(ImmutableList.<String>of());
    HttpContent content = new UrlEncodedContent(request);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    content.writeTo(outputStream);
    String encoded = new String(outputStream.toByteArray());
    System.out.println(encoded);
    assertEquals("grant_type=foo", encoded);
  }

  public void testNullScopes() throws IOException {
    TokenRequest request = new TokenRequest(TRANSPORT, JSON_FACTORY, AUTHORIZATION_SERVER_URL, "foo")
        .setScopes(null);
    HttpContent content = new UrlEncodedContent(request);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    content.writeTo(outputStream);
    String encoded = new String(outputStream.toByteArray());
    assertEquals("grant_type=foo", encoded);
  }
}
