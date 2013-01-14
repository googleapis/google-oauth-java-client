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

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;

import junit.framework.TestCase;

import java.util.Map;

/**
 * Tests {@link ClientParametersAuthentication}.
 *
 * @author Yaniv Inbar
 */
public class ClientParametersAuthenticationTest extends TestCase {

  private static final String CLIENT_ID = "s6BhdRkqt3";
  private static final String CLIENT_SECRET = "7Fjfp0ZBr1KtDRbnfVdmIw";

  public void test() throws Exception {
    HttpRequest request = new MockHttpTransport().createRequestFactory()
        .buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    ClientParametersAuthentication auth =
        new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET);
    assertEquals(CLIENT_ID, auth.getClientId());
    assertEquals(CLIENT_SECRET, auth.getClientSecret());
    auth.intercept(request);
    UrlEncodedContent content = (UrlEncodedContent) request.getContent();
    @SuppressWarnings("unchecked")
    Map<String, ?> data = (Map<String, ?>) content.getData();
    assertEquals(CLIENT_ID, data.get("client_id"));
    assertEquals(CLIENT_SECRET, data.get("client_secret"));
  }

  public void test_noSecret() throws Exception {
    HttpRequest request = new MockHttpTransport().createRequestFactory()
        .buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    ClientParametersAuthentication auth =
        new ClientParametersAuthentication(CLIENT_ID, null);
    assertEquals(CLIENT_ID, auth.getClientId());
    assertNull(auth.getClientSecret());
    auth.intercept(request);
    UrlEncodedContent content = (UrlEncodedContent) request.getContent();
    @SuppressWarnings("unchecked")
    Map<String, ?> data = (Map<String, ?>) content.getData();
    assertEquals(CLIENT_ID, data.get("client_id"));
    assertNull(data.get("client_secret"));
  }
}
