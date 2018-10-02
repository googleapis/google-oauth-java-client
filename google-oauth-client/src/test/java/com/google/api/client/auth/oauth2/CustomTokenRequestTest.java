/*
 * Copyright 2018 Google LLC
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

import com.google.api.client.auth.openidconnect.IdToken;
import com.google.api.client.auth.openidconnect.IdTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;

import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import java.io.IOException;
import junit.framework.TestCase;

/**
 * Tests {@link TokenRequest}.
 *
 * @author Jeff Ching
 */
public class CustomTokenRequestTest extends TestCase {

  private static final MockHttpTransport TRANSPORT = new MockHttpTransport();
  private static final JacksonFactory JSON_FACTORY = new JacksonFactory();
  private static final GenericUrl AUTHORIZATION_SERVER_URL = new GenericUrl(
      "https://server.example.com/authorize");
  private static final String JWT_ENCODED_CONTENT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

  public void testConstructorResponseClass() {
    TokenRequest request = new TokenRequest(TRANSPORT, JSON_FACTORY, AUTHORIZATION_SERVER_URL, "foo",
        IdTokenResponse.class);
    assertEquals(IdTokenResponse.class, request.getResponseClass());
  }

  static class AccessTokenTransport extends MockHttpTransport {

    @Override
    public LowLevelHttpRequest buildRequest(String method, String url) {
      return new MockLowLevelHttpRequest(url) {
        @Override
        public LowLevelHttpResponse execute() throws IOException {
          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
          response.setContentType(Json.MEDIA_TYPE);
          IdTokenResponse json = new IdTokenResponse();
          json.setAccessToken("abc");
          json.setRefreshToken("def");
          json.setExpiresInSeconds(3600L);
          json.setIdToken(JWT_ENCODED_CONTENT);
          response.setContent(JSON_FACTORY.toString(json));
          return response;
        }
      };
    }
  }

  public void testSetResponseClass() throws IOException {
    TokenRequest request = new TokenRequest(new AccessTokenTransport(), JSON_FACTORY, AUTHORIZATION_SERVER_URL, "foo")
        .setResponseClass(IdTokenResponse.class);
    assertEquals(IdTokenResponse.class, request.getResponseClass());
    TokenResponse response = request.execute();
    assertTrue(response instanceof IdTokenResponse);
    IdTokenResponse tokenResponse = (IdTokenResponse)response;
    IdToken idToken = tokenResponse.parseIdToken();
    assertEquals("John Doe", idToken.getPayload().get("name"));
  }
}
