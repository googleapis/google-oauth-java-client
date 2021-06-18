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

import java.security.GeneralSecurityException;
import junit.framework.TestCase;
import org.junit.function.ThrowingRunnable;

import static org.junit.Assert.assertThrows;

/**
 * Tests {@link OAuthHmacSigner}.
 *
 * @author Yaniv Inbar
 */
public class OAuthHmacSignerTest extends TestCase {

  private static final String EXPECTED_SIGNATURE = "0anl6O7gtZfslLZ5j3QoTwd0uPY=";

  public void testComputeSignature() throws GeneralSecurityException {
    OAuthHmacSigner signer = new OAuthHmacSigner();
    signer.clientSharedSecret = "abc";
    signer.tokenSharedSecret = "def";
    assertEquals(EXPECTED_SIGNATURE, signer.computeSignature("foo"));
  }

  public void testComputeSignatureHmacSha256() throws GeneralSecurityException {
    final OAuthHmacSigner signer = new OAuthHmacSigner();
    signer.setSignatureMethod("HMAC-SHA256");
    signer.clientSharedSecret = "apiSecret";
    signer.tokenSharedSecret = "tokenSecret";
    final String expected = "xDJIQbKJTwGumZFvSG1V3ctym2tz6kD8fKGWPr5ImPU=";
    assertEquals(expected, signer.computeSignature("baseString"));
  }

  public void testGetSignatureMethod() {
    final OAuthHmacSigner signer = new OAuthHmacSigner();
    final String expected = "HMAC-SHA1";
    assertEquals(expected, signer.getSignatureMethod());
  }

  public void testGetSignatureMethodHmacSha256() {
    final OAuthHmacSigner signer = new OAuthHmacSigner();
    final String signatureMethod = "HMAC-SHA256";
    signer.setSignatureMethod(signatureMethod);
    assertEquals(signatureMethod, signer.getSignatureMethod());
  }

  public void testSetSignatureMethodHmacMD5() {
    assertThrows(
            "Signature method HMAC-MD5 not available",
            IllegalArgumentException.class,
            new ThrowingRunnable() {
              @Override
              public void run() {
                new OAuthHmacSigner().setSignatureMethod("HMAC-MD5");
              }
            }
    );
  }
}
