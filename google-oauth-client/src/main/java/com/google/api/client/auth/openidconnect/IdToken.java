/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.auth.openidconnect;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.json.webtoken.JsonWebToken;
import com.google.api.client.util.Experimental;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.util.Collection;

/**
 * {@link Experimental} <br/>
 * ID token as described in <a
 * href="http://openid.net/specs/openid-connect-basic-1_0-23.html#id_token">ID Token</a>.
 *
 * <p>
 * Use {@link #parse(JsonFactory, String)} to parse an ID token from a string. Then, use the
 * {@code verify} methods to verify the ID token as required by the specification.
 * </p>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.14
 * @author Yaniv Inbar
 */
@Experimental
public class IdToken extends JsonWebSignature {

  /**
   * @param header header
   * @param payload payload
   * @param signatureBytes bytes of the signature
   * @param signedContentBytes bytes of the signature content
   */
  public IdToken(Header header, Payload payload, byte[] signatureBytes, byte[] signedContentBytes) {
    super(header, payload, signatureBytes, signedContentBytes);
  }

  @Override
  public Payload getPayload() {
    return (Payload) super.getPayload();
  }

  /**
   * Returns whether the issuer in the payload matches the given expected issuer as specified in
   * step 1 of <a
   * href="http://openid.net/specs/openid-connect-basic-1_0-23.html#id.token.validation">ID Token
   * Validation</a>.
   *
   * @param expectedIssuer expected issuer
   */
  public final boolean verifyIssuer(String expectedIssuer) {
    return expectedIssuer.equals(getPayload().getIssuer());
  }

  /**
   * Returns whether the audience in the payload contains only client IDs that are trusted as
   * specified in step 2 of <a
   * href="http://openid.net/specs/openid-connect-basic-1_0-23.html#id.token.validation">ID Token
   * Validation</a>.
   *
   * @param trustedClientIds list of trusted client IDs
   */
  public final boolean verifyAudience(Collection<String> trustedClientIds) {
    return trustedClientIds.containsAll(getPayload().getAudienceAsList());
  }

  /**
   * Returns whether the {@link Payload#getExpirationTimeSeconds} and
   * {@link Payload#getIssuedAtTimeSeconds} are valid relative to the current time, allowing for a
   * clock skew as specified in steps 3 and 4 of <a
   * href="http://openid.net/specs/openid-connect-basic-1_0-23.html#id.token.validation">ID Token
   * Validation</a>.
   *
   * @param currentTimeMillis current time in milliseconds (typically
   *        {@link System#currentTimeMillis()})
   * @param acceptableTimeSkewSeconds seconds of acceptable clock skew
   */
  public final boolean verifyTime(long currentTimeMillis, long acceptableTimeSkewSeconds) {
    return verifyExpirationTime(currentTimeMillis, acceptableTimeSkewSeconds)
        && verifyIssuedAtTime(currentTimeMillis, acceptableTimeSkewSeconds);
  }

  /**
   * Returns whether the {@link Payload#getExpirationTimeSeconds} is valid relative to the current
   * time, allowing for a clock skew as specified in step 3 of <a
   * href="http://openid.net/specs/openid-connect-basic-1_0-23.html#id.token.validation">ID Token
   * Validation</a>.
   *
   * @param currentTimeMillis current time in milliseconds (typically
   *        {@link System#currentTimeMillis()})
   * @param acceptableTimeSkewSeconds seconds of acceptable clock skew
   */
  public final boolean verifyExpirationTime(
      long currentTimeMillis, long acceptableTimeSkewSeconds) {
    return currentTimeMillis
        <= (getPayload().getExpirationTimeSeconds() + acceptableTimeSkewSeconds) * 1000;
  }

  /**
   * Returns whether the {@link Payload#getIssuedAtTimeSeconds} is valid relative to the current
   * time, allowing for a clock skew as specified in steps 4 of <a
   * href="http://openid.net/specs/openid-connect-basic-1_0-23.html#id.token.validation">ID Token
   * Validation</a>.
   *
   * @param currentTimeMillis current time in milliseconds (typically
   *        {@link System#currentTimeMillis()})
   * @param acceptableTimeSkewSeconds seconds of acceptable clock skew
   */
  public final boolean verifyIssuedAtTime(long currentTimeMillis, long acceptableTimeSkewSeconds) {
    return currentTimeMillis
        >= (getPayload().getIssuedAtTimeSeconds() - acceptableTimeSkewSeconds) * 1000;
  }

  /**
   * Parses the given ID token string and returns the parsed ID token.
   *
   * @param jsonFactory JSON factory
   * @param idTokenString ID token string
   * @return parsed ID token
   */
  public static IdToken parse(JsonFactory jsonFactory, String idTokenString) throws IOException {
    JsonWebSignature jws =
        JsonWebSignature.parser(jsonFactory).setPayloadClass(Payload.class).parse(idTokenString);
    return new IdToken(jws.getHeader(), (Payload) jws.getPayload(), jws.getSignatureBytes(),
        jws.getSignedContentBytes());
  }

  /** ID token payload. */
  public static class Payload extends JsonWebToken.Payload {

    /** Time (in seconds) of end-user authorization or {@code null} for none. */
    @Key("auth_time")
    private Long authorizationTimeSeconds;

    /** Authorized party or {@code null} for none. */
    @Key("azp")
    private Object authorizedParty;

    /** Returns the time (in seconds) of end-user authorization or {@code null} for none. */
    public final Long getAuthorizationTimeSeconds() {
      return authorizationTimeSeconds;
    }

    /**
     * Sets the time (in seconds) of end-user authorization or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Payload setAuthorizationTimeSeconds(Long authorizationTimeSeconds) {
      this.authorizationTimeSeconds = authorizationTimeSeconds;
      return this;
    }

    /** Returns the authorized party or {@code null} for none. */
    public final Object getAuthorizedParty() {
      return authorizedParty;
    }

    /**
     * Sets the authorized party or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Payload setAuthorizedParty(Object authorizedParty) {
      this.authorizedParty = authorizedParty;
      return this;
    }
  }
}
