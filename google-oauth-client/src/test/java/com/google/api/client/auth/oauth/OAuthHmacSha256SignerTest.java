/*
 * Copyright 2021 Google LLC
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

import static org.junit.Assert.assertEquals;

import java.security.GeneralSecurityException;
import org.junit.Test;

/** Tests {@link OAuthHmacSha256Signer}. */
public class OAuthHmacSha256SignerTest {

  @Test
  public void testComputeSignatureWithNullSecrets() throws GeneralSecurityException {
    OAuthHmacSha256Signer signer = new OAuthHmacSha256Signer(null);
    String expectedSignature = "l/Es58FI4BtBciSH9XtY/5jXFee70v7/rPiQgEpvv00=";
    assertEquals(expectedSignature, signer.computeSignature("baseString"));
  }

  @Test
  public void testComputeSignatureWithNullClientSecret() throws GeneralSecurityException {
    OAuthHmacSha256Signer signer = new OAuthHmacSha256Signer(null);
    signer.setTokenSecret("tokenSecret");
    String expectedSignature = "PgNWY2qQ53qvk3WySct/f037/usxMGpNDjmJeISmgCM=";
    assertEquals(expectedSignature, signer.computeSignature("baseString"));
  }

  @Test
  public void testComputeSignatureWithNullTokenSecret() throws GeneralSecurityException {
    OAuthHmacSha256Signer signer = new OAuthHmacSha256Signer("clientSecret");
    String expectedSignature = "cNrT2sqgyQ+dd7rbAhYBFBk8o82/yZyZkavqsfMDqpo=";
    assertEquals(expectedSignature, signer.computeSignature("baseString"));
  }

  @Test
  public void testComputeSignature() throws GeneralSecurityException {
    OAuthHmacSha256Signer signer = new OAuthHmacSha256Signer("clientSecret");
    signer.setTokenSecret("tokenSecret");
    String expectedSignature = "sfnrBcfwccOs2mpc60VQ5zXx5ReP/46lgUcBhU2a4PM=";
    assertEquals(expectedSignature, signer.computeSignature("baseString"));
  }
}
