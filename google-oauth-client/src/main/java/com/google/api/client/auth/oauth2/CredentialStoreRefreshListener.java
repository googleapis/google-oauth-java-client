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

import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;

import java.io.IOException;

/**
 * {@link Beta} <br/>
 * Thread-safe OAuth 2.0 credential refresh listener that stores the refresh token response in the
 * credential store.
 *
 * <p>
 * It needs to be added as a refresh listener using {@link Credential.Builder#addRefreshListener}.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 * @deprecated (to be removed in the future) Use {@link DataStoreCredentialRefreshListener}
 *             instead.
 */
@Deprecated
@Beta
public final class CredentialStoreRefreshListener implements CredentialRefreshListener {

  /** Credential store. */
  private final CredentialStore credentialStore;

  /** User ID whose credential is to be updated. */
  private final String userId;

  /**
   * @param userId user ID whose credential is to be updated
   * @param credentialStore credential store
   */
  public CredentialStoreRefreshListener(String userId, CredentialStore credentialStore) {
    this.userId = Preconditions.checkNotNull(userId);
    this.credentialStore = Preconditions.checkNotNull(credentialStore);
  }

  public void onTokenResponse(Credential credential, TokenResponse tokenResponse)
      throws IOException {
    makePersistent(credential);
  }

  public void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse)
      throws IOException {
    makePersistent(credential);
  }

  /** Returns the credential store. */
  public CredentialStore getCredentialStore() {
    return credentialStore;
  }

  /** Stores the updated credential in the credential store. */
  public void makePersistent(Credential credential) throws IOException {
    credentialStore.store(userId, credential);
  }
}
