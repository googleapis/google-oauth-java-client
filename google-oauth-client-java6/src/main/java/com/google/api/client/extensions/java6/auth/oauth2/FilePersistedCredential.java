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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Key;
import com.google.api.client.util.store.FileDataStoreFactory;

/**
 * {@link Beta} <br/>
 * Persisted credential implementation to be used exclusively with {@link FileCredentialStore}.
 *
 * @author Rafael Naufal
 * @since 1.11
 * @deprecated (to be removed in the future) Use {@link FileDataStoreFactory} instead.
 */
@Deprecated
@Beta
public class FilePersistedCredential extends GenericJson {

  /** Access token or {@code null} for none. */
  @Key("access_token")
  private String accessToken;

  /** Refresh token {@code null} for none. */
  @Key("refresh_token")
  private String refreshToken;

  /** Expiration time in milliseconds {@code null} for none. */
  @Key("expiration_time_millis")
  private Long expirationTimeMillis;

  /**
   * Store information from the credential.
   *
   * @param credential credential whose {@link Credential#getAccessToken access token},
   *        {@link Credential#getRefreshToken refresh token}, and
   *        {@link Credential#getExpirationTimeMilliseconds expiration time} need to be stored
   */
  void store(Credential credential) {
    accessToken = credential.getAccessToken();
    refreshToken = credential.getRefreshToken();
    expirationTimeMillis = credential.getExpirationTimeMilliseconds();
  }

  /**
   * @param credential credential whose {@link Credential#setAccessToken access token},
   *        {@link Credential#setRefreshToken refresh token}, and
   *        {@link Credential#setExpirationTimeMilliseconds expiration time} need to be set if the
   *        credential already exists in storage
   */
  void load(Credential credential) {
    credential.setAccessToken(accessToken);
    credential.setRefreshToken(refreshToken);
    credential.setExpirationTimeMilliseconds(expirationTimeMillis);
  }

  @Override
  public FilePersistedCredential set(String fieldName, Object value) {
    return (FilePersistedCredential) super.set(fieldName, value);
  }

  @Override
  public FilePersistedCredential clone() {
    return (FilePersistedCredential) super.clone();
  }

  StoredCredential toStoredCredential() {
    return new StoredCredential().setAccessToken(accessToken)
        .setRefreshToken(refreshToken).setExpirationTimeMilliseconds(expirationTimeMillis);
  }
}
