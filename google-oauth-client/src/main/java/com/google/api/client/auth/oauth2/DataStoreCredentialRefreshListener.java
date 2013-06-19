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

import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;

import java.io.IOException;

/**
 * {@link Beta} <br/>
 * Thread-safe OAuth 2.0 credential refresh listener that stores the refresh token response in the
 * credential data store.
 *
 * <p>
 * It needs to be added as a refresh listener using {@link Credential.Builder#addRefreshListener}.
 * Sample usage:
 * </p>
 *
 * <pre>
  static void addDataStoreCredentialRefreshListener(
      Credential.Builder credentialBuilder, String userId, DataStoreFactory dataStoreFactory)
      throws IOException {
    credentialBuilder.addRefreshListener(
        new DataStoreCredentialRefreshListener(userId, dataStoreFactory));
  }
 * </pre>
 *
 * @since 1.6
 * @author Yaniv Inbar
 */
@Beta
public final class DataStoreCredentialRefreshListener implements CredentialRefreshListener {

  /** Stored credential data store. */
  private final DataStore<StoredCredential> credentialDataStore;

  /** User ID whose credential is to be updated. */
  private final String userId;

  /**
   * Constructor using {@link StoredCredential#getDefaultDataStore(DataStoreFactory)} for the stored
   * credential data store.
   *
   * @param userId user ID whose credential is to be updated
   * @param dataStoreFactory data store factory
   */
  public DataStoreCredentialRefreshListener(String userId, DataStoreFactory dataStoreFactory)
      throws IOException {
    this(userId, StoredCredential.getDefaultDataStore(dataStoreFactory));
  }

  /**
   * @param userId user ID whose credential is to be updated
   * @param credentialDataStore stored credential data store
   */
  public DataStoreCredentialRefreshListener(
      String userId, DataStore<StoredCredential> credentialDataStore) {
    this.userId = Preconditions.checkNotNull(userId);
    this.credentialDataStore = Preconditions.checkNotNull(credentialDataStore);
  }

  public void onTokenResponse(Credential credential, TokenResponse tokenResponse)
      throws IOException {
    makePersistent(credential);
  }

  public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse)
      throws IOException {
    makePersistent(credential);
  }

  /** Returns the stored credential data store. */
  public DataStore<StoredCredential> getCredentialDataStore() {
    return credentialDataStore;
  }

  /** Stores the updated credential in the credential store. */
  public void makePersistent(Credential credential) throws IOException {
    credentialDataStore.set(userId, new StoredCredential(credential));
  }
}
