/*
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

package com.google.api.client.auth.security;

import junit.framework.TestCase;

import java.io.File;
import java.security.PrivateKey;

/**
 * Tests for the {@link PrivateKeys} class.
 *
 * @author ngmiceli@google.com (Nick Miceli)
 */
public class PrivateKeysTest extends TestCase {

  /**
   * Given that secret.p12 and secret.pem are equivalent files in different formats, when
   * {@code PrivateKeys.loadFromP12File} and {@code PrivateKeys.loadFromPemFile} are called on their
   * respective files, they should create equal {@code PrivateKey}s.
   */
  public void testConsistencyForLoadFromFile() throws Exception {
    File p12File = new File(getClass()
        .getClassLoader().getResource("com/google/api/client/auth/security/secret.p12").toURI());
    File pemFile = new File(getClass()
        .getClassLoader().getResource("com/google/api/client/auth/security/secret.pem").toURI());
    PrivateKey p12Key =
        PrivateKeys.loadFromP12File(p12File, "notasecret", "privateKey", "notasecret");
    PrivateKey pemKey = PrivateKeys.loadFromPkcs8PemFile(pemFile);
    assertEquals(p12Key, pemKey);
  }
}
