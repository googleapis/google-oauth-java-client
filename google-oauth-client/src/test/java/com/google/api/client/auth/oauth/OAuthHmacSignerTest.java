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

import junit.framework.TestCase;

import java.security.GeneralSecurityException;

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
}
