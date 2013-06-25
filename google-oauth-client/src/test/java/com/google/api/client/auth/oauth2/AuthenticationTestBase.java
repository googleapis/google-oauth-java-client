/*
 * Copyright (c) 2013 Google Inc.
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
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.GenericData;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Base class for tests of authentication code.
 *
 * @author Yaniv Inbar
 */
public abstract class AuthenticationTestBase extends TestCase {

  protected static final JsonFactory JSON_FACTORY = new JacksonFactory();
  protected static final String ACCESS_TOKEN = "abc";
  protected static final String NEW_ACCESS_TOKEN = "def";
  protected static final GenericUrl TOKEN_SERVER_URL = new GenericUrl("http://example.com/token");
  protected static final String CLIENT_ID = "id";
  protected static final String CLIENT_SECRET = "secret";
  protected static final String REFRESH_TOKEN = "refreshToken";
  protected static final String NEW_REFRESH_TOKEN = "newRefreshToken";
  protected static final long EXPIRES_IN = 3600;

  /**
   * Mock transport class which has been extended to enable more precise testing.
   */
  protected static class AccessTokenTransport extends MockHttpTransport {

    int statusCode = 200;
    String wwwAuthenticate = null;

    int calls = 0;

    @Override
    public LowLevelHttpRequest buildRequest(String method, String url) {
      return new MockLowLevelHttpRequest(url) {
        @Override
        public LowLevelHttpResponse execute() throws IOException {
          calls++;
          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
          response.setContentType(Json.MEDIA_TYPE);
          GenericData responseData;
          if (statusCode == 401 || wwwAuthenticate != null) {
            // return 401 or invalid_token error (with the given status code), and then reset
            // wwwAuthenticate and statusCode - so next request to refresh the token will succeed
            if (wwwAuthenticate != null) {
              response.addHeader("WWW-Authenticate", wwwAuthenticate);
              wwwAuthenticate = null;
            }

            response.setStatusCode(statusCode);
            statusCode = 200;
            return response;
          }
          if (statusCode != 200) {
            TokenErrorResponse json = new TokenErrorResponse();
            json.setError("invalid_client");
            responseData = json;
            response.setStatusCode(statusCode);
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

  public AuthenticationTestBase() {
    super();
  }

  public AuthenticationTestBase(String name) {
    super(name);
  }
}
