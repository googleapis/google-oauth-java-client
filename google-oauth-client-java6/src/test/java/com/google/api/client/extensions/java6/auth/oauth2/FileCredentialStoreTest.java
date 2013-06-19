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

package com.google.api.client.extensions.java6.auth.oauth2;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Tests {@link FileCredentialStore}.
 *
 * @author Rafael Naufal
 */
@Deprecated
public class FileCredentialStoreTest extends TestCase {

  static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final String ACCESS_TOKEN = "abc";
  static final String NEW_ACCESS_TOKEN = "def";
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

  public void testLoadCredentials_empty() throws Exception {
    File file = createTempFile();
    FileCredentialStore store = new FileCredentialStore(file, JSON_FACTORY);
    Credential actual = createEmptyCredential();
    boolean loaded = store.load(USER_ID, actual);
    assertFalse(loaded);
    assertNull(actual.getAccessToken());
    assertNull(actual.getRefreshToken());
    assertNull(actual.getExpirationTimeMilliseconds());
  }

  public void testStoreCredentials() throws Exception {
    Credential expected = createCredential();
    File file = createTempFile();
    file.delete();
    FileCredentialStore store = new FileCredentialStore(file, JSON_FACTORY);
    store = new FileCredentialStore(file, JSON_FACTORY);
    store.store(USER_ID, expected);

    store = new FileCredentialStore(file, JSON_FACTORY);
    Credential actual = createEmptyCredential();
    boolean loaded = store.load(USER_ID, actual);
    assertTrue(loaded);
    assertEquals(ACCESS_TOKEN, actual.getAccessToken());
    assertEquals(REFRESH_TOKEN, actual.getRefreshToken());
    assertEquals(EXPIRES_IN, actual.getExpirationTimeMilliseconds().longValue());
  }

  public void testNotLoadCredentials() throws Exception {
    Credential expected = createCredential();
    FileCredentialStore store = new FileCredentialStore(createTempFile(), JSON_FACTORY);
    store.store(USER_ID, expected);
    try {
      store.load(USER_ID, null);
      fail("expected " + NullPointerException.class);
    } catch (NullPointerException ex) {
      // expected
    }
  }

  public void testNotCredentialsNoExists() throws Exception {
    FileCredentialStore store = new FileCredentialStore(createTempFile(), JSON_FACTORY);
    boolean loaded = store.load("123", createCredential());
    assertFalse(loaded);
  }

  public void testDeleteCredentials() throws Exception {
    FileCredentialStore store = new FileCredentialStore(createTempFile(), JSON_FACTORY);
    store.delete(USER_ID, createCredential());
  }

  private Credential createCredential() {
    Credential access = new Credential.Builder(
        BearerToken.queryParameterAccessMethod()).setTransport(new AccessTokenTransport())
        .setJsonFactory(JSON_FACTORY)
        .setTokenServerUrl(TOKEN_SERVER_URL)
        .setClientAuthentication(new BasicAuthentication(CLIENT_ID, CLIENT_SECRET))
        .build()
        .setAccessToken(ACCESS_TOKEN)
        .setRefreshToken(REFRESH_TOKEN)
        .setExpirationTimeMilliseconds(EXPIRES_IN);
    return access;
  }

  private Credential createEmptyCredential() {
    Credential access = new Credential.Builder(
        BearerToken.queryParameterAccessMethod()).setTransport(new AccessTokenTransport())
        .setJsonFactory(JSON_FACTORY)
        .setTokenServerUrl(TOKEN_SERVER_URL)
        .setClientAuthentication(new BasicAuthentication(CLIENT_ID, CLIENT_SECRET))
        .build();
    return access;
  }

  static class AccessTokenTransport extends MockHttpTransport {

    boolean error = false;

    @Override
    public LowLevelHttpRequest buildRequest(String method, String url) {
      return new MockLowLevelHttpRequest(url) {
          @Override
        public LowLevelHttpResponse execute() throws IOException {
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

  private File createTempFile() throws Exception {
    File result = File.createTempFile("credentials", null);
    result.deleteOnExit();
    JsonGenerator generator =
        JSON_FACTORY.createJsonGenerator(new FileOutputStream(result), Charsets.UTF_8);
    generator.serialize(new FilePersistedCredentials());
    generator.close();
    return result;
  }

  public void testMigrateTo() throws Exception {
    // create old store
    File file = createTempFile();
    FileCredentialStore store = new FileCredentialStore(file, JSON_FACTORY);
    Credential expected = createCredential();
    store.store(USER_ID, expected);
    // migrate to new store
    File dataDir = Files.createTempDir();
    dataDir.deleteOnExit();
    FileDataStoreFactory newFactory = new FileDataStoreFactory(dataDir);
    store.migrateTo(newFactory);
    // check new store
    DataStore<StoredCredential> newStore =
        newFactory.getDataStore(StoredCredential.DEFAULT_DATA_STORE_ID);
    assertEquals(ImmutableSet.of(USER_ID), newStore.keySet());
    StoredCredential actual = newStore.get(USER_ID);
    assertEquals(expected.getAccessToken(), actual.getAccessToken());
    assertEquals(expected.getRefreshToken(), actual.getRefreshToken());
    assertEquals(expected.getExpirationTimeMilliseconds(), actual.getExpirationTimeMilliseconds());
  }
}
