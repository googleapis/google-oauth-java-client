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
import com.google.api.client.util.Beta;
import com.google.api.client.util.Key;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * {@link Beta} <br/>
 * ID token as described in <a
 * href="http://openid.net/specs/openid-connect-basic-1_0-27.html#id_token">ID Token</a>.
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
@Beta
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
   * href="http://openid.net/specs/openid-connect-basic-1_0-27.html#id.token.validation">ID Token
   * Validation</a>.
   *
   * @param expectedIssuer expected issuer
   */
  public final boolean verifyIssuer(String expectedIssuer) {
    return verifyIssuer(Collections.singleton(expectedIssuer));
  }

  /**
   * Returns whether the issuer in the payload matches the given expected issuer as specified in
   * step 1 of <a
   * href="http://openid.net/specs/openid-connect-basic-1_0-27.html#id.token.validation">ID Token
   * Validation</a>. When an issuer is migrating to a new issuer string the expected issuer has
   * multiple aliases, so multiple are permitted here.
   *
   * @since 1.21.0
   */
  public final boolean verifyIssuer(Collection<String> expectedIssuer) {
    return expectedIssuer.contains(getPayload().getIssuer());
  }

  /**
   * Returns whether the audience in the payload contains only client IDs that are trusted as
   * specified in step 2 of <a
   * href="http://openid.net/specs/openid-connect-basic-1_0-27.html#id.token.validation">ID Token
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
   * clock skew as specified in steps 5 and 6 of <a
   * href="http://openid.net/specs/openid-connect-basic-1_0-27.html#id.token.validation">ID Token
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
   * time, allowing for a clock skew as specified in step 5 of <a
   * href="http://openid.net/specs/openid-connect-basic-1_0-27.html#id.token.validation">ID Token
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
   * time, allowing for a clock skew as specified in step 6 of <a
   * href="http://openid.net/specs/openid-connect-basic-1_0-27.html#id.token.validation">ID Token
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

  /**
   * {@link Beta} <br/>
   * ID token payload.
   */
  @Beta
  public static class Payload extends JsonWebToken.Payload {

    /** Time (in seconds) of end-user authorization or {@code null} for none. */
    @Key("auth_time")
    private Long authorizationTimeSeconds;

    /** Authorized party or {@code null} for none. */
    @Key("azp")
    private String authorizedParty;

    /** Value used to associate a client session with an ID token or {@code null} for none. */
    @Key
    private String nonce;

    /** Access token hash value or {@code null} for none. */
    @Key("at_hash")
    private String accessTokenHash;

    /** Authentication context class reference or {@code null} for none. */
    @Key("acr")
    private String classReference;

    /** Authentication methods references or {@code null} for none. */
    @Key("amr")
    private List<String> methodsReferences;

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

    /**
     * Returns the authorized party or {@code null} for none.
     *
     * <p>
     * Upgrade warning: in prior version 1.15 this method returned an {@link Object}, but starting
     * with version 1.16 it returns a {@link String}.
     * </p>
     */
    public final String getAuthorizedParty() {
      return authorizedParty;
    }

    /**
     * Sets the authorized party or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * <p>
     * Upgrade warning: in prior version 1.15 the parameter was an {@link Object}, but starting with
     * version 1.16 the parameter is a {@link String}.
     * </p>
     */
    public Payload setAuthorizedParty(String authorizedParty) {
      this.authorizedParty = authorizedParty;
      return this;
    }

    /**
     * Returns the value used to associate a client session with an ID token or {@code null} for
     * none.
     *
     * @since 1.16
     */
    public final String getNonce() {
      return nonce;
    }

    /**
     * Sets the value used to associate a client session with an ID token or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @since 1.16
     */
    public Payload setNonce(String nonce) {
      this.nonce = nonce;
      return this;
    }

    /**
     * Returns the access token hash value or {@code null} for none.
     *
     * @since 1.16
     */
    public final String getAccessTokenHash() {
      return accessTokenHash;
    }

    /**
     * Sets the access token hash value or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @since 1.16
     */
    public Payload setAccessTokenHash(String accessTokenHash) {
      this.accessTokenHash = accessTokenHash;
      return this;
    }

    /**
     * Returns the authentication context class reference or {@code null} for none.
     *
     * @since 1.16
     */
    public final String getClassReference() {
      return classReference;
    }

    /**
     * Sets the authentication context class reference or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @since 1.16
     */
    public Payload setClassReference(String classReference) {
      this.classReference = classReference;
      return this;
    }

    /**
     * Returns the authentication methods references or {@code null} for none.
     *
     * @since 1.16
     */
    public final List<String> getMethodsReferences() {
      return methodsReferences;
    }

    /**
     * Sets the authentication methods references or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @since 1.16
     */
    public Payload setMethodsReferences(List<String> methodsReferences) {
      this.methodsReferences = methodsReferences;
      return this;
    }

    @Override
    public Payload setExpirationTimeSeconds(Long expirationTimeSeconds) {
      return (Payload) super.setExpirationTimeSeconds(expirationTimeSeconds);
    }

    @Override
    public Payload setNotBeforeTimeSeconds(Long notBeforeTimeSeconds) {
      return (Payload) super.setNotBeforeTimeSeconds(notBeforeTimeSeconds);
    }

    @Override
    public Payload setIssuedAtTimeSeconds(Long issuedAtTimeSeconds) {
      return (Payload) super.setIssuedAtTimeSeconds(issuedAtTimeSeconds);
    }

    @Override
    public Payload setIssuer(String issuer) {
      return (Payload) super.setIssuer(issuer);
    }

    @Override
    public Payload setAudience(Object audience) {
      return (Payload) super.setAudience(audience);
    }

    @Override
    public Payload setJwtId(String jwtId) {
      return (Payload) super.setJwtId(jwtId);
    }

    @Override
    public Payload setType(String type) {
      return (Payload) super.setType(type);
    }

    @Override
    public Payload setSubject(String subject) {
      return (Payload) super.setSubject(subject);
    }

    @Override
    public Payload set(String fieldName, Object value) {
      return (Payload) super.set(fieldName, value);
    }

    @Override
    public Payload clone() {
      return (Payload) super.clone();
    }
  }
}
