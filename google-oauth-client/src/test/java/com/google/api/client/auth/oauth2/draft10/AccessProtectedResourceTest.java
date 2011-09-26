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

package com.google.api.client.auth.oauth2.draft10;


import com.google.api.client.auth.oauth2.draft10.AccessProtectedResource.Method;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.GenericData;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Tests {@link AccessProtectedResource}.
 *
 * @author Yaniv Inbar
 */
public class AccessProtectedResourceTest extends TestCase {

  static final JsonFactory JSON_FACTORY = new JacksonFactory();
  static final String ACCESS_TOKEN = "abc";
  static final String NEW_ACCESS_TOKEN = "def";
  static final String AUTHORIZATION_SERVER_URL = "http://foo.com";
  static final String CLIENT_ID = "id";
  static final String CLIENT_SECRET = "secret";
  static final String REFRESH_TOKEN = "refreshToken";

  public void testAccessProtectedResource_header() throws IOException {
    AccessProtectedResource credential =
        new AccessProtectedResource(ACCESS_TOKEN, Method.AUTHORIZATION_HEADER);
    HttpRequest request = subtestAccessProtectedResource(credential);
    assertEquals("OAuth abc", request.getHeaders().getAuthorization());
  }

  public void testAccessProtectedResource_queryParam() throws IOException {
    AccessProtectedResource credential =
        new AccessProtectedResource(ACCESS_TOKEN, Method.QUERY_PARAMETER);
    HttpRequest request = subtestAccessProtectedResource(credential);
    assertEquals(ACCESS_TOKEN, request.getUrl().get("oauth_token"));
  }

  public void testAccessProtectedResource_body() throws IOException {
    AccessProtectedResource credential =
        new AccessProtectedResource(ACCESS_TOKEN, Method.FORM_ENCODED_BODY);
    HttpRequest request = subtestAccessProtectedResource(credential);
    assertEquals(ACCESS_TOKEN,
        ((GenericData) ((UrlEncodedContent) request.getContent()).getData()).get("oauth_token"));
  }

  private HttpRequest subtestAccessProtectedResource(AccessProtectedResource credential)
      throws IOException {
    MockHttpTransport transport = new MockHttpTransport();
    HttpRequestFactory requestFactory = transport.createRequestFactory(credential);
    HttpRequest request = requestFactory.buildDeleteRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.execute();
    return request;
  }

  public void testAccessProtectedResource_expiredHeader() throws IOException {
    HttpRequest request =
        subtestAccessProtectedResource_expired(Method.AUTHORIZATION_HEADER, new CheckAuth() {

          public boolean checkAuth(MockLowLevelHttpRequest req) {
            return req.getHeaders().get("Authorization").contains("OAuth def");
          }
        });
    assertEquals("OAuth def", request.getHeaders().getAuthorization());
  }

  public void testAccessProtectedResource_expiredQueryParam() throws IOException {
    HttpRequest request =
        subtestAccessProtectedResource_expired(Method.QUERY_PARAMETER, new CheckAuth() {

          public boolean checkAuth(MockLowLevelHttpRequest req) {
            return req.getUrl().contains("oauth_token=def");
          }
        });
    assertEquals(NEW_ACCESS_TOKEN, request.getUrl().get("oauth_token"));
  }

  public void testAccessProtectedResource_expiredBody() throws IOException {
    HttpRequest request =
        subtestAccessProtectedResource_expired(Method.FORM_ENCODED_BODY, new CheckAuth() {

          public boolean checkAuth(MockLowLevelHttpRequest req) {
            return NEW_ACCESS_TOKEN.equals(
                ((GenericData) ((UrlEncodedContent) req.getContent()).getData()).get(
                    "oauth_token"));
          }
        });
    assertEquals(NEW_ACCESS_TOKEN,
        ((GenericData) ((UrlEncodedContent) request.getContent()).getData()).get("oauth_token"));
  }

  interface CheckAuth {
    boolean checkAuth(MockLowLevelHttpRequest req);
  }

  static class AccessTokenTransport extends MockHttpTransport {

    boolean error = false;

    @Override
    public LowLevelHttpRequest buildPostRequest(String url) {
      return new MockLowLevelHttpRequest(url) {
        @Override
        public LowLevelHttpResponse execute() {
          final MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
          response.setContentType(Json.CONTENT_TYPE);
          GenericData responseData;
          if (error) {
            AccessTokenErrorResponse json = new AccessTokenErrorResponse();
            json.error =
                AccessTokenErrorResponse.KnownError.INVALID_CLIENT.toString().toLowerCase();
            responseData = json;
          } else {
            AccessTokenResponse json = new AccessTokenResponse();
            json.accessToken = NEW_ACCESS_TOKEN;
            responseData = json;
          }
          response.setContent(JSON_FACTORY.toString(responseData));
          return response;
        }
      };
    }
  }

  private HttpRequest subtestAccessProtectedResource_expired(
      Method method, final CheckAuth checkAuth) throws IOException {
    final AccessProtectedResource credential = new AccessProtectedResource(ACCESS_TOKEN,
        method,
        new AccessTokenTransport(),
        JSON_FACTORY,
        AUTHORIZATION_SERVER_URL,
        CLIENT_ID,
        CLIENT_SECRET,
        REFRESH_TOKEN);
    class MyTransport extends MockHttpTransport {
      boolean resetAccessToken;

      @Override
      public LowLevelHttpRequest buildDeleteRequest(String url) {
        return new MockLowLevelHttpRequest(url) {
          @Override
          public LowLevelHttpResponse execute() {
            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
            if (!checkAuth.checkAuth(this)) {
              response.setStatusCode(401);
              if (resetAccessToken) {
                credential.setAccessToken(NEW_ACCESS_TOKEN);
              }
            }
            return response;
          }
        };
      }
    }
    MyTransport transport = new MyTransport();
    HttpRequestFactory requestFactory = transport.createRequestFactory(credential);
    HttpRequest request = requestFactory.buildDeleteRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.execute();
    credential.setAccessToken(ACCESS_TOKEN);
    transport.resetAccessToken = true;
    request.execute();
    return request;
  }

  public void testRefreshToken_noRefreshToken() throws IOException {
    AccessProtectedResource accesss =
        new AccessProtectedResource(ACCESS_TOKEN, Method.QUERY_PARAMETER);
    assertFalse(accesss.refreshToken());
  }

  public void testRefreshToken_refreshToken() throws IOException {
    AccessTokenTransport transport = new AccessTokenTransport();
    AccessProtectedResource access = new AccessProtectedResource(ACCESS_TOKEN,
        Method.QUERY_PARAMETER,
        transport,
        JSON_FACTORY,
        AUTHORIZATION_SERVER_URL,
        CLIENT_ID,
        CLIENT_SECRET,
        REFRESH_TOKEN);
    assertTrue(access.refreshToken());
    assertEquals(NEW_ACCESS_TOKEN, access.getAccessToken());
  }

  public void testRefreshToken_refreshTokenError() throws IOException {
    AccessTokenTransport transport = new AccessTokenTransport();
    transport.error = true;
    AccessProtectedResource access = new AccessProtectedResource(ACCESS_TOKEN,
        Method.QUERY_PARAMETER,
        transport,
        JSON_FACTORY,
        AUTHORIZATION_SERVER_URL,
        CLIENT_ID,
        CLIENT_SECRET,
        REFRESH_TOKEN);
    assertFalse(access.refreshToken());
    assertNull(access.getAccessToken());
  }
}
