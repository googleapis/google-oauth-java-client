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
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import java.util.Map;

/**
 * Tests {@link Credential} and {@link BearerToken}.
 *
 * @author Yaniv Inbar
 */
public class CredentialTest extends AuthenticationTestBase {

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
            return req.getFirstHeaderValue("Authorization").equals("Bearer def");
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
            return NEW_ACCESS_TOKEN.equals(((Map<?, ?>) ((UrlEncodedContent) req
                .getStreamingContent()).getData()).get("access_token"));
          }
        });
    assertEquals(NEW_ACCESS_TOKEN,
        ((Map<?, ?>) ((UrlEncodedContent) request.getContent()).getData()).get("access_token"));
  }

  interface CheckAuth {
    boolean checkAuth(MockLowLevelHttpRequest req);
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

  public void testRefreshToken_request_401() throws Exception {
    AccessTokenTransport transport = new AccessTokenTransport();
    transport.statusCode = 401;
    // 3 requests = 1 invalid token, 1 refresh token, and 1 retry
    subtestRefreshToken_request(transport, 3);
  }

  public void testRefreshToken_request_www_authenticate() throws Exception {
    AccessTokenTransport transport = new AccessTokenTransport();
    transport.statusCode = 444;
    transport.wwwAuthenticate =
        "Bearer realm=\"https://www.google.com/accounts/AuthSubRequest\" error=invalid_token";
    // WWW-Authenticate contains invalid_token error, so we expect 3 requests = 1 invalid token, 1
    // refresh token, and 1 retry
    subtestRefreshToken_request(transport, 3);

    transport = new AccessTokenTransport();
    transport.statusCode = 401;
    transport.wwwAuthenticate = "Bearer error=invalid_token";
    // WWW-Authenticate contains invalid_token error, so we expect 3 requests = 1 invalid token, 1
    // refresh token, and 1 retry
    subtestRefreshToken_request(transport, 3);

    transport = new AccessTokenTransport();
    transport.statusCode = 401;
    transport.wwwAuthenticate = "doesn't contain b-e-a-r-e-r";
    // WWW-Authenticate doesn't contain "Bearer" but the status code is 401, so we expect 3 requests
    // = 1 invalid token, 1 refresh token, and 1 retry
    subtestRefreshToken_request(transport, 3);

    transport = new AccessTokenTransport();
    transport.statusCode = 401;
    transport.wwwAuthenticate = "Bearer blah blah blah";
    // WWW-Authenticate contains "Bearer" but no invalid_token error, and although the error code is
    // 401, we expect only 1 failed request
    subtestRefreshToken_request(transport, 1);

    transport = new AccessTokenTransport();
    transport.statusCode = 444;
    transport.wwwAuthenticate = "Bearer blah blah blah";
    // WWW-Authenticate contains "Bearer" but no invalid_token error, we expect only 1 failed
    // request
    subtestRefreshToken_request(transport, 1);

    transport = new AccessTokenTransport();
    transport.statusCode = 444;
    transport.wwwAuthenticate = "doesn't contain b-e-a-r-e-r";
    // WWW-Authenticate doesn't contain "Bearer" and no 401, we expect only 1 failed request
    subtestRefreshToken_request(transport, 1);
  }

  private void subtestRefreshToken_request(AccessTokenTransport transport, int expectedCalls)
      throws Exception {
    Credential credential =
        new Credential.Builder(BearerToken.queryParameterAccessMethod()).setTransport(transport)
            .setJsonFactory(JSON_FACTORY)
            .setTokenServerUrl(TOKEN_SERVER_URL)
            .setClientAuthentication(new BasicAuthentication(CLIENT_ID, CLIENT_SECRET))
            .build()
            .setRefreshToken(REFRESH_TOKEN)
            .setAccessToken(ACCESS_TOKEN);
    HttpRequestFactory requestFactory = transport.createRequestFactory(credential);
    HttpRequest request = requestFactory.buildDeleteRequest(HttpTesting.SIMPLE_GENERIC_URL);
    request.setThrowExceptionOnExecuteError(false);
    request.execute();

    assertEquals(expectedCalls, transport.calls);
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
    transport.statusCode = 400;
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
    transport.statusCode = 500;
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

  public void testInvalidTokenErrorMatcher() {
    String withQuote = "error = \"invalid_token\"";
    String withoutQuote = "error = invalid_token";
    assertTrue(BearerToken.INVALID_TOKEN_ERROR.matcher(withQuote).find());
    assertTrue(BearerToken.INVALID_TOKEN_ERROR.matcher(withoutQuote).find());
  }
}
