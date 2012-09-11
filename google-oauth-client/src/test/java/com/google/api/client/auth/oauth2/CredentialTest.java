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

import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpStatusCodes;
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

import java.util.Map;

/**
 * Tests {@link Credential} and {@link BearerToken}.
 *
 * @author Yaniv Inbar
 */
public class CredentialTest extends TestCase {

  static final JsonFactory JSON_FACTORY = new JacksonFactory();
  static final String ACCESS_TOKEN = "abc";
  static final String NEW_ACCESS_TOKEN = "def";
  static final GenericUrl TOKEN_SERVER_URL = new GenericUrl("http://example.com/token");
  static final String CLIENT_ID = "id";
  static final String CLIENT_SECRET = "secret";
  static final String REFRESH_TOKEN = "refreshToken";
  static final String NEW_REFRESH_TOKEN = "newRefreshToken";
  static final long EXPIRES_IN = 3600;

  public void testConstructor_header() throws Exception {
    Credential credential =
        new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(ACCESS_TOKEN);
    HttpRequest request = subtestConstructor(credential);
    assertEquals("Bearer abc", request.getHeaders().getAuthorization());
  }

  public void testConstructor_queryParam() throws Exception {
    Credential credential =
        new Credential(BearerToken.queryParameterAccessMethod()).setAccessToken(ACCESS_TOKEN);
    HttpRequest request = subtestConstructor(credential);
    assertEquals(ACCESS_TOKEN, request.getUrl().get("access_token"));
  }

  public void testConstructor_body() throws Exception {
    Credential credential =
        new Credential(BearerToken.formEncodedBodyAccessMethod()).setAccessToken(ACCESS_TOKEN);
    HttpRequest request = subtestConstructor(credential);
    assertEquals(ACCESS_TOKEN,
        ((Map<?, ?>) ((UrlEncodedContent) request.getContent()).getData()).get("access_token"));
  }

  private HttpRequest subtestConstructor(Credential credential) throws Exception {
    MockHttpTransport transport = new MockHttpTransport();
    HttpRequestFactory requestFactory = transport.createRequestFactory(credential);
    HttpRequest request = requestFactory.buildDeleteRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.execute();
    return request;
  }

  public void testConstructor_expiredHeader() throws Exception {
    HttpRequest request =
        subtestConstructor_expired(BearerToken.authorizationHeaderAccessMethod(), new CheckAuth() {

          public boolean checkAuth(MockLowLevelHttpRequest req) {
            return req.getHeaders().get("Authorization").contains("Bearer def");
          }
        });
    assertEquals("Bearer def", request.getHeaders().getAuthorization());
  }

  public void testConstructor_expiredQueryParam() throws Exception {
    HttpRequest request =
        subtestConstructor_expired(BearerToken.queryParameterAccessMethod(), new CheckAuth() {

          public boolean checkAuth(MockLowLevelHttpRequest req) {
            return req.getUrl().contains("access_token=def");
          }
        });
    assertEquals(NEW_ACCESS_TOKEN, request.getUrl().get("access_token"));
  }

  public void testConstructor_expiredBody() throws Exception {
    HttpRequest request =
        subtestConstructor_expired(BearerToken.formEncodedBodyAccessMethod(), new CheckAuth() {

          public boolean checkAuth(MockLowLevelHttpRequest req) {
            return NEW_ACCESS_TOKEN.equals(
                ((Map<?, ?>) ((UrlEncodedContent) req.getContent()).getData()).get("access_token"));
          }
        });
    assertEquals(NEW_ACCESS_TOKEN,
        ((Map<?, ?>) ((UrlEncodedContent) request.getContent()).getData()).get("access_token"));
  }

  interface CheckAuth {
    boolean checkAuth(MockLowLevelHttpRequest req);
  }

  static class AccessTokenTransport extends MockHttpTransport {

    boolean error400 = false;
    boolean error500 = false;

    @Override
    public LowLevelHttpRequest buildRequest(String method, String url) {
      return new MockLowLevelHttpRequest(url) {
        @Override
        public LowLevelHttpResponse execute() {
          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
          response.setContentType(Json.MEDIA_TYPE);
          GenericData responseData;
          if (error400) {
            TokenErrorResponse json = new TokenErrorResponse();
            json.setError("invalid_client");
            responseData = json;
            response.setStatusCode(400);
          } else if (error500) {
            TokenErrorResponse json = new TokenErrorResponse();
            json.setError("invalid_client");
            responseData = json;
            response.setStatusCode(500);
          } else {
            TokenResponse json = new TokenResponse();
            json.setAccessToken(NEW_ACCESS_TOKEN);
            json.setRefreshToken(NEW_REFRESH_TOKEN);
            json.setExpiresInSeconds(EXPIRES_IN);
            responseData = json;
          }
          response.setContent(JSON_FACTORY.toString(responseData));
          return response;
        }
      };
    }
  }

  private HttpRequest subtestConstructor_expired(
      Credential.AccessMethod method, final CheckAuth checkAuth) throws Exception {
    final Credential credential =
        new Credential.Builder(method).setTransport(new AccessTokenTransport())
            .setJsonFactory(JSON_FACTORY)
            .setTokenServerUrl(TOKEN_SERVER_URL)
            .setClientAuthentication(new BasicAuthentication(CLIENT_ID, CLIENT_SECRET))
            .build()
            .setAccessToken(ACCESS_TOKEN)
            .setRefreshToken(REFRESH_TOKEN);
    class MyTransport extends MockHttpTransport {
      boolean resetAccessToken;

      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) {
        return new MockLowLevelHttpRequest(url) {
          @Override
          public LowLevelHttpResponse execute() {
            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
            if (!checkAuth.checkAuth(this)) {
              response.setStatusCode(HttpStatusCodes.STATUS_CODE_UNAUTHORIZED);
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

  public void testRefreshToken_noRefreshToken() throws Exception {
    Credential access =
        new Credential(BearerToken.queryParameterAccessMethod()).setAccessToken(ACCESS_TOKEN);
    assertFalse(access.refreshToken());
  }

  public void testRefreshToken_noRefreshToken2() throws Exception {
    AccessTokenTransport transport = new AccessTokenTransport();
    Credential access =
        new Credential.Builder(BearerToken.queryParameterAccessMethod()).setTransport(transport)
            .setJsonFactory(JSON_FACTORY)
            .setTokenServerUrl(TOKEN_SERVER_URL)
            .setClientAuthentication(new BasicAuthentication(CLIENT_ID, CLIENT_SECRET))
            .build()
            .setAccessToken(ACCESS_TOKEN);
    assertFalse(access.refreshToken());
    assertEquals(ACCESS_TOKEN, access.getAccessToken());
    assertNull(access.getRefreshToken());
    assertNull(access.getExpirationTimeMilliseconds());
  }

  public void testRefreshToken_refreshToken() throws Exception {
    AccessTokenTransport transport = new AccessTokenTransport();
    Credential access =
        new Credential.Builder(BearerToken.queryParameterAccessMethod()).setTransport(transport)
            .setJsonFactory(JSON_FACTORY)
            .setTokenServerUrl(TOKEN_SERVER_URL)
            .setClientAuthentication(new BasicAuthentication(CLIENT_ID, CLIENT_SECRET))
            .build()
            .setRefreshToken(REFRESH_TOKEN)
            .setAccessToken(ACCESS_TOKEN);
    assertTrue(access.refreshToken());
    assertEquals(NEW_ACCESS_TOKEN, access.getAccessToken());
    assertEquals(NEW_REFRESH_TOKEN, access.getRefreshToken());
    assertNotNull(access.getExpirationTimeMilliseconds());
  }

  public void testRefreshToken_withoutRequiredParameters() {
    Credential access = new Credential(BearerToken.queryParameterAccessMethod());
    try {
      access.setRefreshToken(REFRESH_TOKEN);
      fail("Expected an " + IllegalArgumentException.class);
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  public void testRefreshToken_refreshTokenErrorWith400() throws Exception {
    AccessTokenTransport transport = new AccessTokenTransport();
    transport.error400 = true;
    Credential access =
        new Credential.Builder(BearerToken.queryParameterAccessMethod()).setTransport(transport)
            .setJsonFactory(JSON_FACTORY)
            .setTokenServerUrl(TOKEN_SERVER_URL)
            .setClientAuthentication(new BasicAuthentication(CLIENT_ID, CLIENT_SECRET))
            .build()
            .setExpiresInSeconds(3600L)
            .setAccessToken(ACCESS_TOKEN)
            .setRefreshToken(REFRESH_TOKEN);
    try {
      access.refreshToken();
      fail("Expected " + TokenResponseException.class);
    } catch (TokenResponseException e) {
      // Expected
    }
    assertNull(access.getAccessToken());
    assertEquals("refreshToken", access.getRefreshToken());
    assertNull(access.getExpirationTimeMilliseconds());
  }

  public void testRefreshToken_refreshTokenErrorWith500() throws Exception {
    AccessTokenTransport transport = new AccessTokenTransport();
    transport.error500 = true;
    Credential access =
        new Credential.Builder(BearerToken.queryParameterAccessMethod()).setTransport(transport)
            .setJsonFactory(JSON_FACTORY)
            .setTokenServerUrl(TOKEN_SERVER_URL)
            .setClientAuthentication(new BasicAuthentication(CLIENT_ID, CLIENT_SECRET))
            .build()
            .setExpiresInSeconds(3600L)
            .setAccessToken(ACCESS_TOKEN)
            .setRefreshToken(REFRESH_TOKEN);

    assertFalse(access.refreshToken());
    assertNotNull(access.getAccessToken());
    assertEquals("refreshToken", access.getRefreshToken());
    assertNotNull(access.getExpirationTimeMilliseconds());
  }
}
