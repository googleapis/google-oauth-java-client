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

package com.google.api.client.auth.security;

import com.google.api.client.util.Base64;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Utility methods for private keys.
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class PrivateKeys {

  private static final String BEGIN = "-----BEGIN PRIVATE KEY-----";
  private static final String END = "-----END PRIVATE KEY-----";

  /**
   * Retrieves the private key from the specified key store stream using default key store.
   *
   * @param keyStream input stream to the key store file (closed at the end of this method in a
   *        finally block)
   * @param storePass password protecting the key store file
   * @param alias alias under which the private key is stored
   * @param keyPass password protecting the private key
   * @return the private key from the specified key store
   */
  public static PrivateKey loadFromKeyStore(
      InputStream keyStream, String storePass, String alias, String keyPass)
      throws IOException, GeneralSecurityException {
    return loadFromKeyStore(
        KeyStore.getInstance(KeyStore.getDefaultType()), keyStream, storePass, alias, keyPass);
  }

  /**
   * Retrieves the private key from the specified key store stream and specified key store.
   *
   * @param keyStore key store
   * @param keyStream input stream to the key store file (closed at the end of this method in a
   *        finally block)
   * @param storePass password protecting the key store file
   * @param alias alias under which the private key is stored
   * @param keyPass password protecting the private key
   * @return the private key from the specified key store
   */
  public static PrivateKey loadFromKeyStore(
      KeyStore keyStore, InputStream keyStream, String storePass, String alias, String keyPass)
      throws IOException, GeneralSecurityException {
    try {
      keyStore.load(keyStream, storePass.toCharArray());
      return (PrivateKey) keyStore.getKey(alias, keyPass.toCharArray());
    } finally {
      keyStream.close();
    }
  }

  /** Reads a {@code PKCS#8} format private key from a given file. */
  public static PrivateKey loadFromPk8File(File file) throws IOException, GeneralSecurityException {
    byte[] privKeyBytes = new byte[(int) file.length()];
    DataInputStream inputStream = new DataInputStream(new FileInputStream(file));
    try {
      inputStream.readFully(privKeyBytes);
    } finally {
      inputStream.close();
    }
    String str = new String(privKeyBytes);
    if (str.startsWith(BEGIN) && str.endsWith(END)) {
      str = str.substring(BEGIN.length(), str.lastIndexOf(END));
    }
    KeyFactory fac = KeyFactory.getInstance("RSA");
    EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(str));
    return fac.generatePrivate(privKeySpec);
  }

  /**
   * Reads a {@code PKCS#12} format private key from a given file.
   *
   * @param p12File p12 file
   * @param storePass password protecting the key store file
   * @param alias alias under which the private key is stored
   * @param keyPass password protecting the private key
   * @return loaded private key
   */
  public static PrivateKey loadFromP12File(
      File p12File, String storePass, String alias, String keyPass)
      throws GeneralSecurityException, IOException {
    return loadFromKeyStore(
        KeyStore.getInstance("PKCS12"), new FileInputStream(p12File), storePass, alias, keyPass);
  }

  private PrivateKeys() {
  }
}
