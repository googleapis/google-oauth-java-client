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

import com.google.api.client.util.Beta;
import com.google.api.client.util.Clock;
import com.google.api.client.util.Preconditions;

import java.util.Collection;
import java.util.Collections;

/**
 * {@link Beta} <br/>
 * Thread-safe ID token verifier based on <a
 * href="http://openid.net/specs/openid-connect-basic-1_0-27.html#id.token.validation">ID Token
 * Validation</a>.
 *
 * <p>
 * Call {@link #verify(IdToken)} to verify a ID token. This is a light-weight object, so you may use
 * a new instance for each configuration of expected issuer and trusted client IDs. Sample usage:
 * </p>
 *
 * <pre>
    IdTokenVerifier verifier = new IdTokenVerifier.Builder()
        .setIssuer("issuer.example.com")
        .setAudience(Arrays.asList("myClientId"))
        .build();
    ...
    if (!verifier.verify(idToken)) {...}
 * </pre>
 *
 * <p>
 * Note that {@link #verify(IdToken)} only implements a subset of the verification steps, mostly
 * just the MUST steps. Please read <a
 * href="http://openid.net/specs/openid-connect-basic-1_0-27.html#id.token.validation>ID Token
 * Validation</a> for the full list of verification steps.
 * </p>
 *
 * @since 1.16
 */
@Beta
public class IdTokenVerifier {

  /** Default value for seconds of time skew to accept when verifying time (5 minutes). */
  public static final long DEFAULT_TIME_SKEW_SECONDS = 300;

  /** Clock to use for expiration checks. */
  private final Clock clock;

  /** Seconds of time skew to accept when verifying time. */
  private final long acceptableTimeSkewSeconds;

  /** Expected issuer or {@code null} to suppress the issuer check. */
  private final String issuer;

  /**
   * Unmodifiable list of trusted audience client IDs or {@code null} to suppress the audience
   * check.
   */
  private final Collection<String> audience;

  public IdTokenVerifier() {
    this(new Builder());
  }

  /**
   * @param builder builder
   */
  protected IdTokenVerifier(Builder builder) {
    clock = builder.clock;
    acceptableTimeSkewSeconds = builder.acceptableTimeSkewSeconds;
    issuer = builder.issuer;
    audience =
        builder.audience == null ? null : Collections.unmodifiableCollection(builder.audience);
  }

  /** Returns the clock. */
  public final Clock getClock() {
    return clock;
  }

  /** Returns the seconds of time skew to accept when verifying time. */
  public final long getAcceptableTimeSkewSeconds() {
    return acceptableTimeSkewSeconds;
  }

  /** Returns the expected issuer or {@code null} to suppress the issuer check. */
  public final String getIssuer() {
    return issuer;
  }

  /**
   * Returns the unmodifiable list of trusted audience client IDs or {@code null} to suppress the
   * audience check.
   */
  public final Collection<String> getAudience() {
    return audience;
  }

  /**
   * Verifies that the given ID token is valid using the cached public keys.
   *
   * It verifies:
   *
   * <ul>
   * <li>The issuer is {@link #getIssuer()} by calling {@link IdToken#verifyIssuer(String)}.</li>
   * <li>The audience is one of {@link #getAudience()} by calling
   * {@link IdToken#verifyAudience(Collection)}.</li>
   * <li>The current time against the issued at and expiration time, using the {@link #getClock()}
   * and allowing for a time skew specified in {#link {@link #getAcceptableTimeSkewSeconds()} , by
   * calling {@link IdToken#verifyTime(long, long)}.</li>
   * </ul>
   *
   * <p>
   * Overriding is allowed, but it must call the super implementation.
   * </p>
   *
   * @param idToken ID token
   * @return {@code true} if verified successfully or {@code false} if failed
   */
  public boolean verify(IdToken idToken) {
    return (issuer == null || idToken.verifyIssuer(issuer))
        && (audience == null || idToken.verifyAudience(audience))
        && idToken.verifyTime(clock.currentTimeMillis(), acceptableTimeSkewSeconds);
  }

  /**
   * {@link Beta} <br/>
   * Builder for {@link IdTokenVerifier}.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   *
   * @since 1.16
   */
  @Beta
  public static class Builder {

    /** Clock. */
    Clock clock = Clock.SYSTEM;

    /** Seconds of time skew to accept when verifying time. */
    long acceptableTimeSkewSeconds = DEFAULT_TIME_SKEW_SECONDS;

    /** Expected issuer or {@code null} to suppress the issuer check. */
    String issuer;

    /** List of trusted audience client IDs or {@code null} to suppress the audience check. */
    Collection<String> audience;

    /** Builds a new instance of {@link IdTokenVerifier}. */
    public IdTokenVerifier build() {
      return new IdTokenVerifier(this);
    }

    /** Returns the clock. */
    public final Clock getClock() {
      return clock;
    }

    /**
     * Sets the clock.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Builder setClock(Clock clock) {
      this.clock = Preconditions.checkNotNull(clock);
      return this;
    }

    /** Returns the expected issuer or {@code null} to suppress the issuer check. */
    public final String getIssuer() {
      return issuer;
    }

    /**
     * Sets the expected issuer or {@code null} to suppress the issuer check.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Builder setIssuer(String issuer) {
      this.issuer = issuer;
      return this;
    }

    /**
     * Returns the list of trusted audience client IDs or {@code null} to suppress the audience
     * check.
     */
    public final Collection<String> getAudience() {
      return audience;
    }

    /**
     * Sets the list of trusted audience client IDs or {@code null} to suppress the audience check.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Builder setAudience(Collection<String> audience) {
      this.audience = audience;
      return this;
    }

    /** Returns the seconds of time skew to accept when verifying time. */
    public final long getAcceptableTimeSkewSeconds() {
      return acceptableTimeSkewSeconds;
    }

    /**
     * Sets the seconds of time skew to accept when verifying time (default is
     * {@link #DEFAULT_TIME_SKEW_SECONDS}).
     *
     * <p>
     * It must be greater or equal to zero.
     * </p>
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Builder setAcceptableTimeSkewSeconds(long acceptableTimeSkewSeconds) {
      Preconditions.checkArgument(acceptableTimeSkewSeconds >= 0);
      this.acceptableTimeSkewSeconds = acceptableTimeSkewSeconds;
      return this;
    }

  }
}
