/*
 * Copyright (c) 2017 Google Inc.
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

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Unit tests for class {@link StoredCredential}.
 *
 * @author Michael Hausegger, hausegger.michael@googlemail.com
 */
public class StoredCredentialTest {

  @Test
  public void testGetRefreshTokenReturningNonEmptyString() throws Exception {
    StoredCredential storedCredential = new StoredCredential();
    StoredCredential storedCredentialTwo = storedCredential.setRefreshToken("HEAD");
    assertEquals("HEAD", storedCredentialTwo.getRefreshToken());
  }

  @Test
  public void testSetGetExpirationTimeMillisecondsAndSetExpirationTimeMilliseconds()
          throws Exception {
    StoredCredential storedCredential = new StoredCredential();
    Long longValue = new Long(1000000L);
    StoredCredential storedCredentialTwo =
            storedCredential.setExpirationTimeMilliseconds(longValue);
    assertEquals(1000000L, (long) storedCredential.getExpirationTimeMilliseconds());
  }

  @Test
  public void testGetAccessTokenReturningNonEmptyString() throws Exception {
    StoredCredential storedCredential = new StoredCredential();
    StoredCredential storedCredentialTwo = storedCredential.setAccessToken("a");
    assertEquals("a", storedCredentialTwo.getAccessToken());
  }

  @Test(expected = NullPointerException.class)
  public void testGetDefaultDataStoreThrowsNullPointerException() throws Exception {
    StoredCredential.getDefaultDataStore(null);
  }

  @Test(expected = NullPointerException.class)
  public void testFailsToCreateStoredCredentialTakingCredentialThrowsNullPointerException()
          throws Exception {
    new StoredCredential(null);
  }

  @Test
  public void testEqualsReturningTrue() throws Exception {
    StoredCredential storedCredential = new StoredCredential();
    StoredCredential storedCredentialTwo = new StoredCredential();
    assertTrue(storedCredential.equals(storedCredentialTwo));
    assertTrue(storedCredentialTwo.equals(storedCredential));
    assertTrue(storedCredential.equals(storedCredential));
    assertTrue(storedCredentialTwo.equals(storedCredentialTwo));
  }

  @Test
  public void testEqualsReturningFalse() throws Exception {
    StoredCredential storedCredential = new StoredCredential();
    StoredCredential storedCredentialTwo = new StoredCredential();
    storedCredentialTwo.setAccessToken("a");
    assertFalse(storedCredential.equals(storedCredentialTwo));
    assertFalse(storedCredentialTwo.equals(storedCredential));
    assertFalse(storedCredential.equals(null));
    assertFalse(storedCredentialTwo.equals(null));
  }

  @Test
  public void testToString() throws Exception {
    StoredCredential storedCredential = new StoredCredential();
    assertEquals(
            "Class{accessToken=null, refreshToken=null, expirationTimeMilliseconds=null}",
            storedCredential.toString());
  }
}
