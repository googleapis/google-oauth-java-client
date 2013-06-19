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
import com.google.api.client.util.store.MemoryDataStoreFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link Beta} <br/>
 * Thread-safe in-memory implementation of a credential store.
 *
 * @since 1.7
 * @author Yaniv Inbar
 * @deprecated (scheduled to be removed in 1.17) Use {@link MemoryDataStoreFactory} with
 *             {@link StoredCredential} instead.
 */
@Deprecated
@Beta
public class MemoryCredentialStore implements CredentialStore {

  /** Lock on access to the store. */
  private final Lock lock = new ReentrantLock();

  /** Store of memory persisted credentials, indexed by userId. */
  private final Map<String, MemoryPersistedCredential> store =
      new HashMap<String, MemoryPersistedCredential>();

  public void store(String userId, Credential credential) {
    lock.lock();
    try {
      MemoryPersistedCredential item = store.get(userId);
      if (item == null) {
        item = new MemoryPersistedCredential();
        store.put(userId, item);
      }
      item.store(credential);
    } finally {
      lock.unlock();
    }
  }

  public void delete(String userId, Credential credential) {
    lock.lock();
    try {
      store.remove(userId);
    } finally {
      lock.unlock();
    }
  }

  public boolean load(String userId, Credential credential) {
    lock.lock();
    try {
      MemoryPersistedCredential item = store.get(userId);
      if (item != null) {
        item.load(credential);
      }
      return item != null;
    } finally {
      lock.unlock();
    }
  }
}
