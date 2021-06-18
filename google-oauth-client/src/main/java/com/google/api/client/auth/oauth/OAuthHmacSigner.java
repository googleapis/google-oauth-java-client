/*
 * Copyright (c) 2010 Google Inc.
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
import com.google.api.client.util.Beta;
import com.google.api.client.util.StringUtils;

import java.security.GeneralSecurityException;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * {@link Beta} <br>
 * OAuth {@code "HMAC-SHA1"} signature method.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
@Beta
public final class OAuthHmacSigner implements OAuthSigner {

  private final Map<String, String> signatureMethodMap = ImmutableMap.of(
          "HMAC-SHA1", "HmacSHA1",
          "HMAC-SHA256", "HmacSHA256"
  );

  private String signatureMethod = "HMAC-SHA1";

  /** Client-shared secret or {@code null} for none. */
  public String clientSharedSecret;

  /** Token-shared secret or {@code null} for none. */
  public String tokenSharedSecret;

  private String getAlgorithm() {
    return signatureMethodMap.get(signatureMethod);
  }

  public String getSignatureMethod() {
    return signatureMethod;
  }

  /** Set the signature method.  Valid signature methods are "HMAC-SHA1" and "HMAC-SHA256" */
  public void setSignatureMethod(String signatureMethod) throws IllegalArgumentException {
    if (signatureMethodMap.containsKey(signatureMethod)) {
      this.signatureMethod = signatureMethod;
    } else {
      throw new IllegalArgumentException("Signature method " + signatureMethod + " not available");
    }
  }

  public String computeSignature(String signatureBaseString) throws GeneralSecurityException {
    // compute key
    StringBuilder keyBuf = new StringBuilder();
    String clientSharedSecret = this.clientSharedSecret;
    if (clientSharedSecret != null) {
      keyBuf.append(OAuthParameters.escape(clientSharedSecret));
    }
    keyBuf.append('&');
    String tokenSharedSecret = this.tokenSharedSecret;
    if (tokenSharedSecret != null) {
      keyBuf.append(OAuthParameters.escape(tokenSharedSecret));
    }
    String key = keyBuf.toString();
    // sign
    SecretKey secretKey = new SecretKeySpec(StringUtils.getBytesUtf8(key), getAlgorithm());
    Mac mac = Mac.getInstance(getAlgorithm());
    mac.init(secretKey);
    return Base64.encodeBase64String(mac.doFinal(StringUtils.getBytesUtf8(signatureBaseString)));
  }
}
