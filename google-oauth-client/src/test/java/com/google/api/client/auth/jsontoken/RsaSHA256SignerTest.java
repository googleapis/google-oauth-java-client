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

package com.google.api.client.auth.jsontoken;

import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.FixedClock;

import junit.framework.TestCase;

import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.SecureRandom;

/**
 * Tests {@link RsaSHA256Signer}.
 *
 * @author Yaniv Inbar
 */
public class RsaSHA256SignerTest extends TestCase {

  public void testSign() throws GeneralSecurityException {
    FixedClock clock = new FixedClock(0L);
    JsonWebSignature.Header header = new JsonWebSignature.Header();
    header.setAlgorithm("RS256");
    header.setType("JWT");
    JsonWebToken.Payload payload = new JsonWebToken.Payload(clock);
    payload.setIssuer("issuer").setAudience("audience").setIssuedAtTimeSeconds(0L)
        .setExpirationTimeSeconds(3600L);
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(512, new SecureRandom(new byte[0]));
    PrivateKey privateKey = keyGen.generateKeyPair().getPrivate();
    RsaSHA256Signer.sign(privateKey, new JacksonFactory(), header, payload);
  }
}
