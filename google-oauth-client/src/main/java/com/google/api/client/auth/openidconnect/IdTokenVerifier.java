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

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature.Header;
import com.google.api.client.util.Base64;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Clock;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * {@link Beta} <br>
 * Thread-safe ID token verifier based on <a
 * href="http://openid.net/specs/openid-connect-basic-1_0-27.html#id.token.validation">ID Token
 * Validation</a>.
 *
 * <p>Call {@link #verify(IdToken)} to verify a ID token. This is a light-weight object, so you may
 * use a new instance for each configuration of expected issuer and trusted client IDs. Sample
 * usage:
 *
 * <pre>
 * IdTokenVerifier verifier = new IdTokenVerifier.Builder()
 * .setIssuer("issuer.example.com")
 * .setAudience(Arrays.asList("myClientId"))
 * .build();
 * ...
 * if (!verifier.verify(idToken)) {...}
 * </pre>
 *
 * <p>Note that {@link #verify(IdToken)} only implements a subset of the verification steps, mostly
 * just the MUST steps. Please read <a
 * href="http://openid.net/specs/openid-connect-basic-1_0-27.html#id.token.validation>ID Token
 * Validation</a> for the full list of verification steps.
 *
 * @since 1.16
 */
@Beta
public class IdTokenVerifier {
  private static final String IAP_CERT_URL = "https://www.gstatic.com/iap/verify/public_key-jwk";
  private static final String FEDERATED_SIGNON_CERT_URL =
      "https://www.googleapis.com/oauth2/v3/certs";
  private static final Set<String> SUPPORTED_ALGORITHMS = ImmutableSet.of("RS256", "ES256");
  private static final String NOT_SUPPORTED_ALGORITHM =
      "Unexpected signing algorithm %s: expected either RS256 or ES256";

  static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  static final String SKIP_SIGNATURE_ENV_VAR = "OAUTH_CLIENT_SKIP_SIGNATURE";
  /** Default value for seconds of time skew to accept when verifying time (5 minutes). */
  public static final long DEFAULT_TIME_SKEW_SECONDS = 300;

  /** Clock to use for expiration checks. */
  private final Clock clock;

  private final String certificatesLocation;
  private final Environment environment;
  private final LoadingCache<String, Map<String, PublicKey>> publicKeyCache;

  /** Seconds of time skew to accept when verifying time. */
  private final long acceptableTimeSkewSeconds;

  /**
   * Unmodifiable collection of equivalent expected issuers or {@code null} to suppress the issuer
   * check.
   */
  private final Collection<String> issuers;

  /**
   * Unmodifiable list of trusted audience client IDs or {@code null} to suppress the audience
   * check.
   */
  private final Collection<String> audience;

  public IdTokenVerifier() {
    this(new Builder());
  }

  /** @param builder builder */
  protected IdTokenVerifier(Builder builder) {
    this.certificatesLocation = builder.certificatesLocation;
    clock = builder.clock;
    acceptableTimeSkewSeconds = builder.acceptableTimeSkewSeconds;
    issuers = builder.issuers == null ? null : Collections.unmodifiableCollection(builder.issuers);
    audience =
        builder.audience == null ? null : Collections.unmodifiableCollection(builder.audience);
    HttpTransportFactory transport =
        builder.httpTransportFactory == null
            ? new DefaultHttpTransportFactory()
            : builder.httpTransportFactory;
    this.publicKeyCache =
        CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new PublicKeyLoader(transport));
    this.environment = builder.environment == null ? new Environment() : builder.environment;
  }

  /** Returns the clock. */
  public final Clock getClock() {
    return clock;
  }

  /** Returns the seconds of time skew to accept when verifying time. */
  public final long getAcceptableTimeSkewSeconds() {
    return acceptableTimeSkewSeconds;
  }

  /**
   * Returns the first of equivalent expected issuers or {@code null} if issuer check suppressed.
   */
  public final String getIssuer() {
    if (issuers == null) {
      return null;
    } else {
      return issuers.iterator().next();
    }
  }

  /**
   * Returns the equivalent expected issuers or {@code null} if issuer check suppressed.
   *
   * @since 1.21.0
   */
  public final Collection<String> getIssuers() {
    return issuers;
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
   * <p>It verifies:
   *
   * <ul>
   *   <li>The issuer is one of {@link #getIssuers()} by calling {@link
   *       IdToken#verifyIssuer(String)}.
   *   <li>The audience is one of {@link #getAudience()} by calling {@link
   *       IdToken#verifyAudience(Collection)}.
   *   <li>The current time against the issued at and expiration time, using the {@link #getClock()}
   *       and allowing for a time skew specified in {@link #getAcceptableTimeSkewSeconds()} , by
   *       calling {@link IdToken#verifyTime(long, long)}.
   * </ul>
   *
   * <p>Overriding is allowed, but it must call the super implementation.
   *
   * @param idToken ID token
   * @return {@code true} if verified successfully or {@code false} if failed
   */
  public boolean verify(IdToken idToken) {
    boolean simpleChecks =
        (issuers == null || idToken.verifyIssuer(issuers))
            && (audience == null || idToken.verifyAudience(audience))
            && idToken.verifyTime(clock.currentTimeMillis(), acceptableTimeSkewSeconds);

    if (!simpleChecks) {
      return false;
    }

    // This method validates token signature per current OpenID Connect Spec:
    // https://openid.net/specs/openid-connect-core-1_0.html#IDTokenValidation
    // By default, method gets a certificate from well-known location
    // A request to certificate location is performed using
    // {@link com.google.api.client.http.javanet.NetHttpTransport}
    // Both certificate location and transport implementation can be overridden via {@link Builder}
    // not recommended: this check can be disabled with OAUTH_CLIENT_SKIP_SIGNATURE
    // environment variable set to true.
    try {
      return verifySignature(idToken);
    } catch (VerificationException ex) {
      return false;
    }
  }

  /**
   * Verifies the signature part of the id token By default, method gets a certificate from
   * well-known location A request to certificate location is performed using {@link
   * com.google.api.client.http.javanet.NetHttpTransport} Both default can be overridden via {@link
   * Builder}
   *
   * @param idToken an id token
   * @return true if signature validated successfully, false otherwise
   */
  @VisibleForTesting
  boolean verifySignature(IdToken idToken) throws VerificationException {

    if (Boolean.parseBoolean(environment.getVariable(SKIP_SIGNATURE_ENV_VAR))) {
      return true;
    }

    // Short-circuit signature types
    if (!SUPPORTED_ALGORITHMS.contains(idToken.getHeader().getAlgorithm())) {
      throw new VerificationException(
          String.format(NOT_SUPPORTED_ALGORITHM, idToken.getHeader().getAlgorithm()));
    }

    PublicKey publicKeyToUse = null;
    try {
      String certificateLocation = getCertificateLocation(idToken.getHeader());
      publicKeyToUse = publicKeyCache.get(certificateLocation).get(idToken.getHeader().getKeyId());
    } catch (ExecutionException | UncheckedExecutionException e) {
      throw new VerificationException(
          "Error fetching PublicKey from certificate location " + certificatesLocation, e);
    }

    if (publicKeyToUse == null) {
      throw new VerificationException(
          "Could not find PublicKey for provided keyId: " + idToken.getHeader().getKeyId());
    }

    try {
      if (idToken.verifySignature(publicKeyToUse)) {
        return true;
      }
      throw new VerificationException("Invalid signature");
    } catch (GeneralSecurityException e) {
      throw new VerificationException("Error validating token", e);
    }
  }

  private String getCertificateLocation(Header header) throws VerificationException {
    if (certificatesLocation != null) return certificatesLocation;

    switch (header.getAlgorithm()) {
      case "RS256":
        return FEDERATED_SIGNON_CERT_URL;
      case "ES256":
        return IAP_CERT_URL;
    }

    throw new VerificationException(String.format(NOT_SUPPORTED_ALGORITHM, header.getAlgorithm()));
  }

  /**
   * {@link Beta} <br>
   * Builder for {@link IdTokenVerifier}.
   *
   * <p>Implementation is not thread-safe.
   *
   * @since 1.16
   */
  @Beta
  public static class Builder {

    /** Clock. */
    Clock clock = Clock.SYSTEM;

    String certificatesLocation;

    /** wrapper for environment variables */
    Environment environment;

    /** Seconds of time skew to accept when verifying time. */
    long acceptableTimeSkewSeconds = DEFAULT_TIME_SKEW_SECONDS;

    /** Collection of equivalent expected issuers or {@code null} to suppress the issuer check. */
    Collection<String> issuers;

    /** List of trusted audience client IDs or {@code null} to suppress the audience check. */
    Collection<String> audience;

    HttpTransportFactory httpTransportFactory;

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
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setClock(Clock clock) {
      this.clock = Preconditions.checkNotNull(clock);
      return this;
    }

    /**
     * Returns the first of equivalent expected issuers or {@code null} if issuer check suppressed.
     */
    public final String getIssuer() {
      if (issuers == null) {
        return null;
      } else {
        return issuers.iterator().next();
      }
    }

    /**
     * Sets the expected issuer or {@code null} to suppress the issuer check.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setIssuer(String issuer) {
      if (issuer == null) {
        return setIssuers(null);
      } else {
        return setIssuers(Collections.singleton(issuer));
      }
    }

    /**
     * Override the location URL that contains published public keys. Defaults to well-known Google
     * locations.
     *
     * @param certificatesLocation URL to published public keys
     * @return the builder
     */
    public Builder setCertificatesLocation(String certificatesLocation) {
      this.certificatesLocation = certificatesLocation;
      return this;
    }

    /**
     * Returns the equivalent expected issuers or {@code null} if issuer check suppressed.
     *
     * @since 1.21.0
     */
    public final Collection<String> getIssuers() {
      return issuers;
    }

    /**
     * Sets the list of equivalent expected issuers or {@code null} to suppress the issuer check.
     * Typically only a single issuer should be used, but multiple may be specified to support an
     * issuer transitioning to a new string. The collection must not be empty.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     *
     * @since 1.21.0
     */
    public Builder setIssuers(Collection<String> issuers) {
      Preconditions.checkArgument(
          issuers == null || !issuers.isEmpty(), "Issuers must not be empty");
      this.issuers = issuers;
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
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
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
     * Sets the seconds of time skew to accept when verifying time (default is {@link
     * #DEFAULT_TIME_SKEW_SECONDS}).
     *
     * <p>It must be greater or equal to zero.
     *
     * <p>Overriding is only supported for the purpose of calling the super implementation and
     * changing the return type, but nothing else.
     */
    public Builder setAcceptableTimeSkewSeconds(long acceptableTimeSkewSeconds) {
      Preconditions.checkArgument(acceptableTimeSkewSeconds >= 0);
      this.acceptableTimeSkewSeconds = acceptableTimeSkewSeconds;
      return this;
    }

    /** Returns an instance of the {@link Environment} */
    final Environment getEnvironment() {
      return environment;
    }

    /** Sets the environment. Used mostly for testing */
    Builder setEnvironment(Environment environment) {
      this.environment = environment;
      return this;
    }

    /**
     * Set the HttpTransportFactory used for requesting public keys from the certificate URL. Used
     * mostly for testing.
     *
     * @param httpTransportFactory the HttpTransportFactory used to build certificate URL requests
     * @return the builder
     */
    public Builder setHttpTransportFactory(HttpTransportFactory httpTransportFactory) {
      this.httpTransportFactory = httpTransportFactory;
      return this;
    }
  }

  /** Custom CacheLoader for mapping certificate urls to the contained public keys. */
  static class PublicKeyLoader extends CacheLoader<String, Map<String, PublicKey>> {
    private final HttpTransportFactory httpTransportFactory;

    /**
     * Data class used for deserializing a JSON Web Key Set (JWKS) from an external HTTP request.
     */
    public static class JsonWebKeySet extends GenericJson {
      @Key public List<JsonWebKey> keys;
    }

    /** Data class used for deserializing a single JSON Web Key. */
    public static class JsonWebKey {
      @Key public String alg;

      @Key public String crv;

      @Key public String kid;

      @Key public String kty;

      @Key public String use;

      @Key public String x;

      @Key public String y;

      @Key public String e;

      @Key public String n;
    }

    PublicKeyLoader(HttpTransportFactory httpTransportFactory) {
      super();
      this.httpTransportFactory = httpTransportFactory;
    }

    @Override
    public Map<String, PublicKey> load(String certificateUrl) throws Exception {
      HttpTransport httpTransport = httpTransportFactory.create();
      JsonWebKeySet jwks;
      try {
        HttpRequest request =
            httpTransport
                .createRequestFactory()
                .buildGetRequest(new GenericUrl(certificateUrl))
                .setParser(GsonFactory.getDefaultInstance().createJsonObjectParser());
        HttpResponse response = request.execute();
        jwks = response.parseAs(JsonWebKeySet.class);
      } catch (IOException io) {
        return ImmutableMap.of();
      }

      ImmutableMap.Builder<String, PublicKey> keyCacheBuilder = new ImmutableMap.Builder<>();
      if (jwks.keys == null) {
        // Fall back to x509 formatted specification
        for (String keyId : jwks.keySet()) {
          String publicKeyPem = (String) jwks.get(keyId);
          keyCacheBuilder.put(keyId, buildPublicKey(publicKeyPem));
        }
      } else {
        for (JsonWebKey key : jwks.keys) {
          try {
            keyCacheBuilder.put(key.kid, buildPublicKey(key));
          } catch (NoSuchAlgorithmException
              | InvalidKeySpecException
              | InvalidParameterSpecException ignored) {
            ignored.printStackTrace();
          }
        }
      }

      return keyCacheBuilder.build();
    }

    private PublicKey buildPublicKey(JsonWebKey key)
        throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeySpecException {
      if ("ES256".equals(key.alg)) {
        return buildEs256PublicKey(key);
      } else if ("RS256".equals((key.alg))) {
        return buildRs256PublicKey(key);
      } else {
        return null;
      }
    }

    private PublicKey buildPublicKey(String publicPem)
        throws CertificateException, UnsupportedEncodingException {
      return CertificateFactory.getInstance("X.509")
          .generateCertificate(new ByteArrayInputStream(publicPem.getBytes("UTF-8")))
          .getPublicKey();
    }

    private PublicKey buildRs256PublicKey(JsonWebKey key)
        throws NoSuchAlgorithmException, InvalidKeySpecException {
      com.google.common.base.Preconditions.checkArgument("RSA".equals(key.kty));
      com.google.common.base.Preconditions.checkNotNull(key.e);
      com.google.common.base.Preconditions.checkNotNull(key.n);

      BigInteger modulus = new BigInteger(1, Base64.decodeBase64(key.n));
      BigInteger exponent = new BigInteger(1, Base64.decodeBase64(key.e));

      RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
      KeyFactory factory = KeyFactory.getInstance("RSA");
      return factory.generatePublic(spec);
    }

    private PublicKey buildEs256PublicKey(JsonWebKey key)
        throws NoSuchAlgorithmException, InvalidParameterSpecException, InvalidKeySpecException {
      com.google.common.base.Preconditions.checkArgument("EC".equals(key.kty));
      com.google.common.base.Preconditions.checkArgument("P-256".equals(key.crv));

      BigInteger x = new BigInteger(1, Base64.decodeBase64(key.x));
      BigInteger y = new BigInteger(1, Base64.decodeBase64(key.y));
      ECPoint pubPoint = new ECPoint(x, y);
      AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
      parameters.init(new ECGenParameterSpec("secp256r1"));
      ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
      ECPublicKeySpec pubSpec = new ECPublicKeySpec(pubPoint, ecParameters);
      KeyFactory kf = KeyFactory.getInstance("EC");
      return kf.generatePublic(pubSpec);
    }
  }

  /** Custom exception for wrapping all verification errors. */
  public static class VerificationException extends Exception {
    public VerificationException(String message) {
      super(message);
    }

    public VerificationException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  static class DefaultHttpTransportFactory implements HttpTransportFactory {

    public HttpTransport create() {
      return HTTP_TRANSPORT;
    }
  }
}
