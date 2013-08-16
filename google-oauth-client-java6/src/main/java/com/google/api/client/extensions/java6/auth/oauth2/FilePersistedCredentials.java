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
import com.google.api.client.util.Maps;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.IOException;
import java.util.Map;

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
public class FilePersistedCredentials extends GenericJson {

  /** User ID to be used as the primary key. */
  @Key
  private Map<String, FilePersistedCredential> credentials = Maps.newHashMap();

  /**
   * Store information from the credential.
   *
   * @param userId user ID whose credential needs to be stored
   * @param credential credential whose {@link Credential#getAccessToken access token},
   *        {@link Credential#getRefreshToken refresh token}, and
   *        {@link Credential#getExpirationTimeMilliseconds expiration time} need to be stored
   */
  void store(String userId, Credential credential) {
    Preconditions.checkNotNull(userId);
    FilePersistedCredential fileCredential = credentials.get(userId);
    if (fileCredential == null) {
      fileCredential = new FilePersistedCredential();
      credentials.put(userId, fileCredential);
    }
    fileCredential.store(credential);
  }

  /**
   * @param userId user ID whose credential needs to be loaded
   * @param credential credential whose {@link Credential#setAccessToken access token},
   *        {@link Credential#setRefreshToken refresh token}, and
   *        {@link Credential#setExpirationTimeMilliseconds expiration time} need to be set if the
   *        credential already exists in storage
   */
  boolean load(String userId, Credential credential) {
    Preconditions.checkNotNull(userId);
    FilePersistedCredential fileCredential = credentials.get(userId);
    if (fileCredential == null) {
      return false;
    }
    fileCredential.load(credential);
    return true;
  }

  void delete(String userId) {
    Preconditions.checkNotNull(userId);
    credentials.remove(userId);
  }

  @Override
  public FilePersistedCredentials set(String fieldName, Object value) {
    return (FilePersistedCredentials) super.set(fieldName, value);
  }

  @Override
  public FilePersistedCredentials clone() {
    return (FilePersistedCredentials) super.clone();
  }

  void migrateTo(DataStore<StoredCredential> typedDataStore) throws IOException {
    for (Map.Entry<String, FilePersistedCredential> entry : credentials.entrySet()) {
      typedDataStore.set(entry.getKey(), entry.getValue().toStoredCredential());
    }
  }
}
