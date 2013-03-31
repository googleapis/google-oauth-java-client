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

package com.google.api.client.auth.openidconnect;

import com.google.api.client.auth.oauth2.TokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;

import java.io.IOException;

/**
 * {@link Beta} <br/>
 * OAuth ID Connect JSON model for a successful access token response as specified in <a
 * href="http://openid.net/specs/openid-connect-basic-1_0-23.html">OpenID Connect Basic Client
 * Profile 1.0 (draft 23)</a>.
 *
 * <p>
 * Implementation is not thread-safe. Sample usage:
 * </p>
 *
 * <pre>
  static JsonWebSignature executeIdToken(TokenRequest tokenRequest) throws IOException {
    IdTokenResponse idTokenResponse = IdTokenResponse.execute(tokenRequest);
    return idTokenResponse.parseIdToken();
  }
 * </pre>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
@Beta
public class IdTokenResponse extends TokenResponse {

  /** ID token. */
  @Key("id_token")
  private String idToken;

  /** Returns the ID token. */
  public final String getIdToken() {
    return idToken;
  }

  /**
   * Sets the ID token.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public IdTokenResponse setIdToken(String idToken) {
    this.idToken = Preconditions.checkNotNull(idToken);
    return this;
  }

  @Override
  public IdTokenResponse setAccessToken(String accessToken) {
    super.setAccessToken(accessToken);
    return this;
  }

  @Override
  public IdTokenResponse setTokenType(String tokenType) {
    super.setTokenType(tokenType);
    return this;
  }

  @Override
  public IdTokenResponse setExpiresInSeconds(Long expiresIn) {
    super.setExpiresInSeconds(expiresIn);
    return this;
  }

  @Override
  public IdTokenResponse setRefreshToken(String refreshToken) {
    super.setRefreshToken(refreshToken);
    return this;
  }

  @Override
  public IdTokenResponse setScope(String scope) {
    super.setScope(scope);
    return this;
  }

  /**
   * Parses using {@link JsonWebSignature#parse(JsonFactory, String)} based on the
   * {@link #getFactory() JSON factory} and {@link #getIdToken() ID token}.
   */
  @SuppressWarnings("javadoc")
  public IdToken parseIdToken() throws IOException {
    return IdToken.parse(getFactory(), idToken);
  }

  /**
   * Executes the given ID token request, and returns the parsed ID token response.
   *
   * @param tokenRequest token request
   * @return parsed successful ID token response
   * @throws TokenResponseException for an error response
   */
  public static IdTokenResponse execute(TokenRequest tokenRequest) throws IOException {
    return tokenRequest.executeUnparsed().parseAs(IdTokenResponse.class);
  }

  @Override
  public IdTokenResponse set(String fieldName, Object value) {
    return (IdTokenResponse) super.set(fieldName, value);
  }

  @Override
  public IdTokenResponse clone() {
    return (IdTokenResponse) super.clone();
  }
}
