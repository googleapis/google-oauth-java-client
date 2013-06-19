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

package com.google.api.client.extensions.jdo.auth.oauth2;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.jdo.JdoDataStoreFactory;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.json.MockJsonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.common.collect.ImmutableSet;

import junit.framework.TestCase;

import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

/**
 * Tests {@link JdoCredentialStore}.
 *
 * @author Yaniv Inbar
 */
@Deprecated
public class JdoCredentialStoreTest extends TestCase {

  /**
   * This test is *disabled* by default just because you need to run special set up steps first.
   * Specifically on Linux:
   *
   * <pre>
sudo apt-get install mysql-server
mysql -u root -p
create database mytest;
   * </pre>
   *
   * Then update ConnectionUserName and ConnectionPassword below.
   */
  static boolean ENABLE_TEST = false;

  static class PersistenceManagerFactoryHolder {
    static PersistenceManagerFactory pmf;

    public static PersistenceManagerFactory get() {
      return pmf;
    }

    static {
      Properties properties = new Properties();
      properties.setProperty("javax.jdo.PersistenceManagerFactoryClass",
          "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
      properties.setProperty("javax.jdo.option.ConnectionDriverName", "com.mysql.jdbc.Driver");
      properties.setProperty(
          "javax.jdo.option.ConnectionURL", "jdbc:mysql://localhost:3306/mytest");
      properties.setProperty("javax.jdo.option.ConnectionUserName", "root");
      properties.setProperty("javax.jdo.option.ConnectionPassword", "");
      properties.setProperty("datanucleus.autoCreateSchema", "true");
      pmf = JDOHelper.getPersistenceManagerFactory(properties);
    }
  }

  private static final String ACCESS_TOKEN = "abc";
  private static final String REFRESH_TOKEN = "refreshToken";
  private static final long EXPIRES_IN = 3600;
  private static final String USER_ID = "123abc";
  private static final String CLIENT_ID = "id";
  private static final String CLIENT_SECRET = "secret";
  private static final GenericUrl TOKEN_SERVER_URL = new GenericUrl("http://example.com/token");

  private Credential createCredential() {
    return new Credential.Builder(BearerToken.queryParameterAccessMethod()).setTransport(
        new MockHttpTransport())
        .setJsonFactory(new MockJsonFactory())
        .setTokenServerUrl(TOKEN_SERVER_URL)
        .setClientAuthentication(new BasicAuthentication(CLIENT_ID, CLIENT_SECRET))
        .build()
        .setAccessToken(ACCESS_TOKEN)
        .setRefreshToken(REFRESH_TOKEN)
        .setExpirationTimeMilliseconds(EXPIRES_IN);
  }

  public void testMigrateTo() throws Exception {
    if (!ENABLE_TEST) {
      return;
    }
    // create old store
    JdoCredentialStore store = new JdoCredentialStore(PersistenceManagerFactoryHolder.get());
    Credential expected = createCredential();
    store.store(USER_ID, expected);
    // migrate to new store
    JdoDataStoreFactory newFactory = new JdoDataStoreFactory(PersistenceManagerFactoryHolder.get());
    DataStore<StoredCredential> newStore =
        newFactory.getDataStore(StoredCredential.DEFAULT_DATA_STORE_ID);
    newStore.clear();
    store.migrateTo(newFactory);
    // check new store
    assertEquals(ImmutableSet.of(USER_ID), newStore.keySet());
    StoredCredential actual = newStore.get(USER_ID);
    assertEquals(expected.getAccessToken(), actual.getAccessToken());
    assertEquals(expected.getRefreshToken(), actual.getRefreshToken());
    assertEquals(expected.getExpirationTimeMilliseconds(), actual.getExpirationTimeMilliseconds());
  }

}
