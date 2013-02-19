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

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow.CredentialCreatedListener;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.json.MockJsonFactory;

import junit.framework.TestCase;

import java.io.IOException;

/**
 * Tests {@link AuthorizationCodeFlow}.
 *
 * @author Yaniv Inbar
 */
public class AuthorizationCodeFlowTest extends TestCase {

  static class MyCredentialCreatedListener implements CredentialCreatedListener {

    boolean called = false;

    public void onCredentialCreated(Credential credential, TokenResponse tokenResponse)
        throws IOException {
      called = true;
    }
  }

  public void testCredentialCreatedListener() throws IOException {
    MyCredentialCreatedListener listener = new MyCredentialCreatedListener();
    AuthorizationCodeFlow flow =
        new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
            new MockHttpTransport(),
            new MockJsonFactory(),
            HttpTesting.SIMPLE_GENERIC_URL,
            null,
            "clientId",
            "authorizationServerEncodedUrl").setCredentialCreatedListener(listener).build();
    assertFalse(listener.called);
    flow.createAndStoreCredential(new TokenResponse(), "userId");
    assertTrue(listener.called);
  }
}
