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

/**
 * Tests {@link OAuthHmacSha256Signer}.
 */
public class OAuthHmacSha256SignerTest extends TestCase {

  private static final String EXPECTED_SIGNATURE = "xDJIQbKJTwGumZFvSG1V3ctym2tz6kD8fKGWPr5ImPU=";

  public void testComputeSignature() throws GeneralSecurityException {
    OAuthHmacSha256Signer signer = new OAuthHmacSha256Signer();
    signer.clientSharedSecret = "apiSecret";
    signer.tokenSharedSecret = "tokenSecret";
    assertEquals(EXPECTED_SIGNATURE, signer.computeSignature("baseString"));
  }
}
