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

package com.google.api.client.extensions.auth.helpers.appengine;

import com.google.api.client.auth.oauth2.draft10.AccessProtectedResource;
import com.google.api.client.auth.oauth2.draft10.AccessTokenRequest.AssertionGrant;
import com.google.api.client.extensions.appengine.auth.SignedTokenGenerator;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import net.oauth.jsontoken.JsonToken;

import java.io.IOException;
import java.security.SignatureException;

/**
 * Subclass of {@link AccessProtectedResource} that handles the different logic required to obtain a
 * new access token using an assertion grant when the existing one expires.
 *
 * @author moshenko@google.com (Jake Moshenko)
 *
 * @since 1.5
 */
public class AccessAppResource extends AccessProtectedResource {
  private static final String ASSERTION_TYPE = "http://oauth.net/grant_type/jwt/1.0/bearer";

  /** HTTP transport for executing refresh token request or {@code null} for none. */
  private final HttpTransport transport;

  /**
   * JSON factory to use for parsing response for refresh token request or {@code null} for none.
   */
  private final JsonFactory jsonFactory;

  /** Encoded authorization server URL or {@code null} for none. */
  private final String authorizationServerUrl;

  /** Scope for which we are requesting access. */
  private final String scope;

  /** Audience encoded in the JSON web token, used in the assertion grant. */
  private final String audience;

  /**
   * Create an instance of the access initializer.
   *
   * @param accessToken Initial access token, or {@code null} if one is not yet available.
   * @param method Method with which to authorize the request.
   * @param transport Instance used to communicate with the auth server.
   * @param jsonFactory Instance used to deserialize communications from the auth server.
   * @param authorizationServerUrl Url at which the auth server is located.
   * @param scope Scope for which we are requesting access.
   * @param audience Audience used in the JSON web token used when obtaining access tokens.
   */
  public AccessAppResource(String accessToken,
      Method method,
      HttpTransport transport,
      JsonFactory jsonFactory,
      String authorizationServerUrl,
      String scope,
      String audience) throws IOException {
    super("", method);

    this.transport = transport;
    this.jsonFactory = jsonFactory;
    this.scope = scope;
    this.audience = audience;
    this.authorizationServerUrl = authorizationServerUrl;

    // This credential is the result of a two legged flow, so the method to refresh the token is the
    // same as the method to generate a new one. We will use an existing access token until it fails
    // if we are provided with one, or we will immediately request a new access token if none was
    // provided.
    if (accessToken == null) {
      refreshToken();
    } else {
      setAccessToken(accessToken);
    }
  }

  @Override
  protected boolean executeRefreshToken() throws IOException {
    JsonToken jwt = SignedTokenGenerator.createJsonTokenForScopes(scope, audience);

    String assertion;
    try {
      assertion = jwt.serializeAndSign();
    } catch (SignatureException exception) {
      IOException rewrite = new IOException("Unable to sign JSON Web Token");
      rewrite.initCause(exception);
      throw rewrite;
    }
    AssertionGrant tokenRequest = new AssertionGrant(
        transport, jsonFactory, authorizationServerUrl, ASSERTION_TYPE, assertion);
    return executeAccessTokenRequest(tokenRequest);
  }
}
