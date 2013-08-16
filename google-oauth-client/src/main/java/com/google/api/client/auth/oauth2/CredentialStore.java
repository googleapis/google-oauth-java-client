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

package com.google.api.client.auth.oauth2;

import com.google.api.client.util.Beta;
import com.google.api.client.util.store.DataStoreFactory;

import java.io.IOException;

/**
 * {@link Beta} <br/>
 * OAuth 2.0 credential persistence store interface to provide a fully pluggable storage mechanism.
 *
 * <p>
 * The user ID should be used as the primary key for storage, and the rest of the data consists of
 * the {@link Credential#getAccessToken access token}, {@link Credential#getRefreshToken refresh
 * token}, and {@link Credential#getExpirationTimeMilliseconds expiration time}.
 * </p>
 *
 * <p>
 * Implementations should be thread safe.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 * @deprecated (to be removed in the future) Use {@link DataStoreFactory} with
 *             {@link StoredCredential} instead.
 */
@Deprecated
@Beta
public interface CredentialStore {

  /**
   * Loads the credential for the given user ID.
   *
   * @param userId user ID whose credential needs to be loaded
   * @param credential credential whose {@link Credential#setAccessToken access token},
   *        {@link Credential#setRefreshToken refresh token}, and
   *        {@link Credential#setExpirationTimeMilliseconds expiration time} need to be set if the
   *        credential already exists in storage
   * @return {@code true} if the credential has been successfully found and loaded or {@code false}
   *         otherwise
   */
  boolean load(String userId, Credential credential) throws IOException;

  /**
   * Stores the credential of the given user ID.
   *
   * @param userId user ID whose credential needs to be stored
   * @param credential credential whose {@link Credential#getAccessToken access token},
   *        {@link Credential#getRefreshToken refresh token}, and
   *        {@link Credential#getExpirationTimeMilliseconds expiration time} need to be stored
   */
  void store(String userId, Credential credential) throws IOException;

  /**
   * Deletes the credential of the given user ID.
   *
   * @param userId user ID whose credential needs to be deleted
   * @param credential credential to be deleted
   */
  void delete(String userId, Credential credential) throws IOException;
}
