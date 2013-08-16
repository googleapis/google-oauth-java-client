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

package com.google.api.client.extensions.appengine.auth.oauth2;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.util.Beta;
import com.google.api.client.util.store.DataStore;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import java.io.IOException;

/**
 * {@link Beta} <br/>
 * Thread-safe Google App Engine implementation of a credential store that directly uses the App
 * Engine Data Store API.
 *
 * @since 1.7
 * @author Yaniv Inbar
 * @deprecated (to be removed in the future) Use {@link AppEngineDataStoreFactory} with
 *             {@link StoredCredential} instead,
 *             optionally using {@link #migrateTo(AppEngineDataStoreFactory)} or
 *             {@link #migrateTo(DataStore)} to migrating an existing
 *             {@link AppEngineCredentialStore}.
 */
@Deprecated
@Beta
public class AppEngineCredentialStore implements CredentialStore {

  private static final String KIND = AppEngineCredentialStore.class.getName();

  @Override
  public void store(String userId, Credential credential) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity entity = new Entity(KIND, userId);
    entity.setProperty("accessToken", credential.getAccessToken());
    entity.setProperty("refreshToken", credential.getRefreshToken());
    entity.setProperty("expirationTimeMillis", credential.getExpirationTimeMilliseconds());
    datastore.put(entity);
  }

  @Override
  public void delete(String userId, Credential credential) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(KIND, userId);
    datastore.delete(key);
  }

  @Override
  public boolean load(String userId, Credential credential) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key key = KeyFactory.createKey(KIND, userId);
    try {
      Entity entity = datastore.get(key);
      credential.setAccessToken((String) entity.getProperty("accessToken"));
      credential.setRefreshToken((String) entity.getProperty("refreshToken"));
      credential.setExpirationTimeMilliseconds((Long) entity.getProperty("expirationTimeMillis"));
      return true;
    } catch (EntityNotFoundException exception) {
      return false;
    }
  }

  /**
   * Migrates to the new {@link AppEngineDataStoreFactory} format.
   *
   * <p>
   * Sample usage:
   * </p>
   *
   * <pre>
  public static AppEngineDataStore migrate(AppEngineCredentialStore credentialStore)
      throws IOException {
    AppEngineDataStore dataStore = new AppEngineDataStore();
    credentialStore.migrateTo(dataStore);
    return dataStore;
  }
   * </pre>
   * @param dataStoreFactory App Engine data store factory
   * @since 1.16
   */
  public final void migrateTo(AppEngineDataStoreFactory dataStoreFactory) throws IOException {
    migrateTo(StoredCredential.getDefaultDataStore(dataStoreFactory));
  }

  /**
   * Migrates to the new format using {@link DataStore} of {@link StoredCredential}.
   *
   * @param credentialDataStore credential data store
   * @since 1.16
   */
  public final void migrateTo(DataStore<StoredCredential> credentialDataStore) throws IOException {
    DatastoreService service = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery queryResult = service.prepare(new Query(KIND));
    for (Entity entity : queryResult.asIterable()) {
      StoredCredential storedCredential = new StoredCredential().setAccessToken(
          (String) entity.getProperty("accessToken"))
          .setRefreshToken((String) entity.getProperty("refreshToken"))
          .setExpirationTimeMilliseconds((Long) entity.getProperty("expirationTimeMillis"));
      credentialDataStore.set(entity.getKey().getName(), storedCredential);
    }
  }
}
