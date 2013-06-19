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

package com.google.api.client.extensions.jdo.auth.oauth2;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Persisted credential implementation to be used exclusively with {@link JdoCredentialStore}.
 *
 * @author Yaniv Inbar
 */
@Deprecated
@Beta
@PersistenceCapable
class JdoPersistedCredential {

  /** User ID to be used as the primary key. */
  @PrimaryKey
  private String userId;

  /** Access token or {@code null} for none. */
  @Persistent
  private String accessToken;

  /** Refresh token {@code null} for none. */
  @Persistent
  private String refreshToken;

  /** Expiration time in milliseconds {@code null} for none. */
  @Persistent
  private Long expirationTimeMillis;

  /**
   * @param userId user ID whose credential needs to be stored
   * @param credential credential whose {@link Credential#getAccessToken access token},
   *        {@link Credential#getRefreshToken refresh token}, and
   *        {@link Credential#getExpirationTimeMilliseconds expiration time} need to be stored
   */
  JdoPersistedCredential(String userId, Credential credential) {
    this.userId = Preconditions.checkNotNull(userId);
    update(credential);

  }

  /**
   * Loads details into the given credential from this instance.
   *
   * @param credential credential whose {@link Credential#setAccessToken access token},
   *        {@link Credential#setRefreshToken refresh token}, and
   *        {@link Credential#setExpirationTimeMilliseconds expiration time} will be set
   */
  void load(Credential credential) {
    credential.setAccessToken(accessToken);
    credential.setRefreshToken(refreshToken);
    credential.setExpirationTimeMilliseconds(expirationTimeMillis);
  }

  /**
   * Updates current instance values with the given credential.
   *
   * @param credential credential whose {@link Credential#getAccessToken access token},
   *        {@link Credential#getRefreshToken refresh token}, and
   *        {@link Credential#getExpirationTimeMilliseconds expiration time} are used to set this
   *        instance corresponding values
   */
  void update(Credential credential) {
    accessToken = credential.getAccessToken();
    refreshToken = credential.getRefreshToken();
    expirationTimeMillis = credential.getExpirationTimeMilliseconds();
  }

  StoredCredential toStoredCredential() {
    return new StoredCredential().setAccessToken(accessToken)
        .setRefreshToken(refreshToken).setExpirationTimeMilliseconds(expirationTimeMillis);
  }

  String getUserId() {
    return userId;
  }
}
