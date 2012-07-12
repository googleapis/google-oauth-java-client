package com.google.api.client.extensions.java6.auth.oauth2;

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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.GenericData;

/**
 * Tests {@link FileCredentialStore}.
 * 
 * @author Rafael Naufal
 */
public class FileCredentialStoreTest extends TestCase {

  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final String ACCESS_TOKEN = "abc";
  static final String NEW_ACCESS_TOKEN = "def";
  private static final String FILE_NAME = System.getProperty("user.dir")
      + "/src/test/resources/credentials.json".replace('/', File.separatorChar);
  private static final GenericUrl TOKEN_SERVER_URL = new GenericUrl("http://example.com/token");
  private static final String CLIENT_ID = "id";
  private static final String CLIENT_SECRET = "secret";
  private static final String REFRESH_TOKEN = "refreshToken";
  static final String NEW_REFRESH_TOKEN = "newRefreshToken";
  private static final String USER_ID = "123abc";
  private static final long EXPIRES_IN = 3600;

  public void testParametersMustNotBeNull() {
    boolean exceptionThrow = false;
    String message = "";
    try {
      new FileCredentialStore(null, null);
    } catch (Exception ex) {
      exceptionThrow = true;
      message = ex.getMessage();
    }
    assertTrue(message, exceptionThrow);
  }

  public void testStoreCredentials() throws IOException {
    Credential expected = createCredential();
    FileCredentialStore store = new FileCredentialStore(new File(FILE_NAME), JSON_FACTORY);
    store.store(USER_ID, expected);

    Credential actual = createEmptyCredential();
    boolean loaded = store.load(USER_ID, actual);
    assertTrue(loaded);
    assertEquals(ACCESS_TOKEN, actual.getAccessToken());
    assertEquals(REFRESH_TOKEN, actual.getRefreshToken());
    assertEquals(EXPIRES_IN, actual.getExpirationTimeMilliseconds().longValue());
  }

  public void testNotLoadCredentials() {
    boolean exceptionThrow = false;
    String message = "";
    boolean loaded = false;
    try {
      FileCredentialStore store = new FileCredentialStore(new File(FILE_NAME), JSON_FACTORY);
      loaded = store.load(USER_ID, null);
    } catch (Exception ex) {
      exceptionThrow = true;
      message = ex.getMessage();
    }
    assertTrue(message, exceptionThrow);
    assertFalse(loaded);
  }

  public void testNotCredentialsNoExists() throws IOException {
    FileCredentialStore store = new FileCredentialStore(new File(FILE_NAME), JSON_FACTORY);
    boolean loaded = store.load("123", createCredential());
    assertFalse(loaded);
  }

  public void testDeleteCredentials() throws IOException {
    FileCredentialStore store = new FileCredentialStore(new File(FILE_NAME), JSON_FACTORY);
    store.delete(USER_ID, createCredential());
  }

  private Credential createCredential() {
    Credential access = new Credential.Builder(BearerToken.queryParameterAccessMethod())
        .setTransport(new AccessTokenTransport()).setJsonFactory(JSON_FACTORY)
        .setTokenServerUrl(TOKEN_SERVER_URL)
        .setClientAuthentication(new BasicAuthentication(CLIENT_ID, CLIENT_SECRET)).build()
        .setAccessToken(ACCESS_TOKEN).setRefreshToken(REFRESH_TOKEN)
        .setExpirationTimeMilliseconds(EXPIRES_IN);
    return access;
  }

  private Credential createEmptyCredential() {
    Credential access = new Credential.Builder(BearerToken.queryParameterAccessMethod())
        .setTransport(new AccessTokenTransport()).setJsonFactory(JSON_FACTORY)
        .setTokenServerUrl(TOKEN_SERVER_URL)
        .setClientAuthentication(new BasicAuthentication(CLIENT_ID, CLIENT_SECRET)).build();
    return access;
  }

  private static class AccessTokenTransport extends MockHttpTransport {

    boolean error = false;

    @Override
    public LowLevelHttpRequest buildPostRequest(String url) {
      return new MockLowLevelHttpRequest(url) {
        @Override
        public LowLevelHttpResponse execute() {
          MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
          response.setContentType("UTF-8");
          GenericData responseData;
          if (error) {
            TokenErrorResponse json = new TokenErrorResponse();
            json.setError("invalid_client");
            responseData = json;
            response.setStatusCode(400);
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
}
