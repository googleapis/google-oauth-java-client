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
import com.google.api.client.util.Objects;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link Beta} <br/>
 * Credential information to be stored in a {@link DataStoreFactory}.
 *
 * <p>
 * Implementation is thread safe.
 * </p>
 *
 * @author Yaniv Inbar
 * @since 1.16
 */
@Beta
public final class StoredCredential implements Serializable {

  /** Default data store ID. */
  public static final String DEFAULT_DATA_STORE_ID = StoredCredential.class.getSimpleName();

  private static final long serialVersionUID = 1L;

  /** Lock on access to the store. */
  private final Lock lock = new ReentrantLock();

  /** Access token or {@code null} for none. */
  private String accessToken;

  /** Expected expiration time in milliseconds or {@code null} for none. */
  private Long expirationTimeMilliseconds;

  /** Refresh token or {@code null} for none. */
  private String refreshToken;

  public StoredCredential() {
  }

  /**
   * @param credential existing credential to copy from
   */
  public StoredCredential(Credential credential) {
    setAccessToken(credential.getAccessToken());
    setRefreshToken(credential.getRefreshToken());
    setExpirationTimeMilliseconds(credential.getExpirationTimeMilliseconds());
  }

  /** Returns the access token or {@code null} for none. */
  public String getAccessToken() {
    lock.lock();
    try {
      return accessToken;
    } finally {
      lock.unlock();
    }
  }

  /** Sets the access token or {@code null} for none. */
  public StoredCredential setAccessToken(String accessToken) {
    lock.lock();
    try {
      this.accessToken = accessToken;
    } finally {
      lock.unlock();
    }
    return this;
  }

  /** Returns the expected expiration time in milliseconds or {@code null} for none. */
  public Long getExpirationTimeMilliseconds() {
    lock.lock();
    try {
      return expirationTimeMilliseconds;
    } finally {
      lock.unlock();
    }
  }

  /** Sets the expected expiration time in milliseconds or {@code null} for none. */
  public StoredCredential setExpirationTimeMilliseconds(Long expirationTimeMilliseconds) {
    lock.lock();
    try {
      this.expirationTimeMilliseconds = expirationTimeMilliseconds;
    } finally {
      lock.unlock();
    }
    return this;
  }

  /** Returns the refresh token or {@code null} for none. */
  public String getRefreshToken() {
    lock.lock();
    try {
      return refreshToken;
    } finally {
      lock.unlock();
    }
  }

  /** Sets the refresh token or {@code null} for none. */
  public StoredCredential setRefreshToken(String refreshToken) {
    lock.lock();
    try {
      this.refreshToken = refreshToken;
    } finally {
      lock.unlock();
    }
    return this;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(StoredCredential.class)
        .add("accessToken", getAccessToken())
        .add("refreshToken", getRefreshToken())
        .add("expirationTimeMilliseconds", getExpirationTimeMilliseconds())
        .toString();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof StoredCredential)) {
      return false;
    }
    StoredCredential o = (StoredCredential) other;
    return Objects.equal(getAccessToken(), o.getAccessToken())
        && Objects.equal(getRefreshToken(), o.getRefreshToken())
        && Objects.equal(getExpirationTimeMilliseconds(), o.getExpirationTimeMilliseconds());
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(
        new Object[] {getAccessToken(), getRefreshToken(), getExpirationTimeMilliseconds()});
  }

  /**
   * Returns the stored credential data store using the ID {@link #DEFAULT_DATA_STORE_ID}.
   *
   * @param dataStoreFactory data store factory
   * @return stored credential data store
   */
  public static DataStore<StoredCredential> getDefaultDataStore(DataStoreFactory dataStoreFactory)
      throws IOException {
    return dataStoreFactory.getDataStore(DEFAULT_DATA_STORE_ID);
  }
}
