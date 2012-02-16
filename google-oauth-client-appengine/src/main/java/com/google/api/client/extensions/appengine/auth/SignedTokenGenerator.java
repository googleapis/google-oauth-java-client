/*
 * Copyright (c) 2011 Google Inc.
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

package com.google.api.client.extensions.appengine.auth;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityService.SigningResult;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;

import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.crypto.AbstractSigner;
import net.oauth.jsontoken.crypto.SignatureAlgorithm;
import net.oauth.signatures.SignedJsonAssertionToken;
import org.joda.time.Duration;
import org.joda.time.Instant;

import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Random;
import java.util.logging.Logger;

/**
 * This class will allow you to create a signed JSON web token (JWT) that can be used for assertion
 * flows with an appropriate auth endpoint.
 *
 * @author moshenko@google.com (Jacob Moshenko)
 *
 * @since 1.5
 */
public class SignedTokenGenerator {
  private static final Logger LOGGER = Logger.getLogger(SignedTokenGenerator.class.toString());

  private static final Random GENERATOR = new SecureRandom();

  /**
   * This class allows you to sign the JSON web token using the App Engine
   * {@link AppIdentityService}.
   */
  private static class AppEngineSigner extends AbstractSigner {

    private final AppIdentityService identityService;

    protected AppEngineSigner(String keyId, AppIdentityService identityService) {
      super(identityService.getServiceAccountName(), keyId);
      this.identityService = identityService;
    }

    public SignatureAlgorithm getSignatureAlgorithm() {
      return SignatureAlgorithm.RS256;
    }

    public byte[] sign(byte[] source) throws SignatureException {
      SigningResult key = identityService.signForApp(source);
      this.setSigningKeyId(key.getKeyName());
      return key.getSignature();
    }
  }

  /**
   * Static function to create a signable JSON token initialized with the proper parameters for
   * performing an assertion token request.
   *
   * @param scope Scope for which we are requesting access.
   * @param audience Audience field in the json web token.
   * @return Signable JSON web token (JWT).
   */
  public static JsonToken createJsonTokenForScopes(String scope, String audience) {
    AppIdentityService identityService = AppIdentityServiceFactory.getAppIdentityService();
    AppEngineSigner signer = new AppEngineSigner("", identityService);
    SignedJsonAssertionToken jwt = new SignedJsonAssertionToken(signer);
    jwt.setAudience(audience);
    jwt.setScope(scope);
    jwt.setNonce(Long.toString(GENERATOR.nextLong()));

    Instant now = new Instant();
    jwt.setIssuedAt(now);
    jwt.setExpiration(now.plus(Duration.standardHours(1)));

    LOGGER.fine("JWT: " + jwt.toString());

    return jwt;
  }
}
