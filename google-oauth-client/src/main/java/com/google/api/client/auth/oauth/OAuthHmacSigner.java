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

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * {@link Beta} <br/>
 * OAuth {@code "HMAC-SHA1"} signature method.
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
@Beta
public final class OAuthHmacSigner implements OAuthSigner {

  /** Client-shared secret or {@code null} for none. */
  public String clientSharedSecret;

  /** Token-shared secret or {@code null} for none. */
  public String tokenSharedSecret;

  public String getSignatureMethod() {
    return "HMAC-SHA1";
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
    SecretKey secretKey = new SecretKeySpec(StringUtils.getBytesUtf8(key), "HmacSHA1");
    Mac mac = Mac.getInstance("HmacSHA1");
    mac.init(secretKey);
    return Base64.encodeBase64String(mac.doFinal(StringUtils.getBytesUtf8(signatureBaseString)));
  }
}
