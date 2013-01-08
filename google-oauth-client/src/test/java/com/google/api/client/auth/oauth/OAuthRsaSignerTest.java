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

package com.google.api.client.auth.oauth;

import com.google.api.client.util.Base64;
import com.google.api.client.util.SecurityUtils;
import com.google.api.client.util.StringUtils;

import junit.framework.TestCase;

import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;

/**
 * Tests {@link OAuthRsaSigner}.
 *
 * @author Yaniv Inbar
 */
public class OAuthRsaSignerTest extends TestCase {

  public void testComputeSignature() throws GeneralSecurityException {
    OAuthRsaSigner signer = new OAuthRsaSigner();
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(1024);
    signer.privateKey = keyPairGenerator.genKeyPair().getPrivate();
    byte[] expected = SecurityUtils.sign(
        SecurityUtils.getSha1WithRsaSignatureAlgorithm(), signer.privateKey,
        StringUtils.getBytesUtf8("foo"));
    assertEquals(Base64.encodeBase64String(expected), signer.computeSignature("foo"));
  }
}
