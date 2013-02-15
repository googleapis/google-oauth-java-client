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
import com.google.api.client.util.SecurityUtils;

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
 * @deprecated (scheduled to be removed in the future) Use {@link SecurityUtils} instead.
 */
@Deprecated
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
   * @deprecated (scheduled to be removed in the future) Use
   *             {@link SecurityUtils#loadPrivateKeyFromKeyStore} with
   *             {@link SecurityUtils#getDefaultKeyStore()} instead
   */
  @Deprecated
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
   * @deprecated (scheduled to be removed in the future) Use
   *             {@link SecurityUtils#loadPrivateKeyFromKeyStore} instead.
   */
  @Deprecated
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

  /**
   * Reads a {@code PKCS#8} format private key from a given file.
   *
   * @deprecated (scheduled to be removed in the future) Use {@link #loadFromPkcs8PemFile} instead.
   *             Note that the file formatting requirements for {@link #loadFromPkcs8PemFile} are
   *             stricter than this method's requirements.
   */
  @Deprecated
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
   * Reads a {@code PEM} formatted private key from a given file.
   *
   * <p>
   * This supports any file if and only if it contains a DER and Base64 encoded key, and the
   * contents are enclosed by the following:
   * </p>
   *
   * <pre>
   *-----BEGIN PRIVATE KEY-----
   *-----END PRIVATE KEY-----
   *</pre>
   *
   * <p>
   * The file may contain additional content outside of the BEGIN and END tags, but it will be
   * ignored. This method does not support additional content such as headers inside the BEGIN and
   * END tags. If the file contains multiple BEGIN and END tags, only the content inside the first
   * pair will be read.
   * </p>
   *
   * @since 1.13
   * @deprecated (scheduled to be removed in the future) Use
   *             {@link SecurityUtils#readPrivateKeyFromPem(InputStream, String)} instead.
   */
  @Deprecated
  public static byte[] readFromPemFormattedFile(File file)
      throws IOException, GeneralSecurityException {
    byte[] privKeyBytes = new byte[(int) file.length()];
    DataInputStream inputStream = new DataInputStream(new FileInputStream(file));
    try {
      inputStream.readFully(privKeyBytes);
    } finally {
      inputStream.close();
    }
    String str = new String(privKeyBytes);
    if (!str.contains(BEGIN) || !str.contains(END)) {
      throw new GeneralSecurityException(
          "File missing required BEGIN PRIVATE KEY or END PRIVATE KEY tags.");
    }
    if (str.indexOf(BEGIN) + BEGIN.length() > str.indexOf(END)) {
      throw new GeneralSecurityException("END PRIVATE KEY tag found before BEGIN PRIVATE KEY tag.");
    }

    String privKey = str.substring(str.indexOf(BEGIN) + BEGIN.length(), str.indexOf(END));
    return Base64.decodeBase64(privKey);
  }

  /**
   * Reads a {@code PEM} formatted PKCS8 encoded private key from a given file.
   *
   * <p>
   * This method uses {@link #readFromPemFormattedFile}, and the file formatting requirements from
   * that method apply.
   * <p>
   *
   * @since 1.13
   * @deprecated (scheduled to be removed in the future) Use
   *             {@link SecurityUtils#loadPkcs8PrivateKeyFromPem(KeyFactory, InputStream, String)}
   *             instead.
   */
  @Deprecated
  public static PrivateKey loadFromPkcs8PemFile(File pemFile)
      throws IOException, GeneralSecurityException {
    KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePrivate(new PKCS8EncodedKeySpec(readFromPemFormattedFile(pemFile)));
  }

  /**
   * Reads a {@code PKCS#12} format private key from a given file.
   *
   * @param p12File p12 file
   * @param storePass password protecting the key store file
   * @param alias alias under which the private key is stored
   * @param keyPass password protecting the private key
   * @return loaded private key
   * @deprecated (scheduled to be removed in the future) Use
   *             {@link SecurityUtils#loadPrivateKeyFromKeyStore} with
   *             {@link SecurityUtils#getPkcs12KeyStore()} instead
   */
  @Deprecated
  public static PrivateKey loadFromP12File(
      File p12File, String storePass, String alias, String keyPass)
      throws GeneralSecurityException, IOException {
    return loadFromKeyStore(
        KeyStore.getInstance("PKCS12"), new FileInputStream(p12File), storePass, alias, keyPass);
  }

  private PrivateKeys() {
  }
}
