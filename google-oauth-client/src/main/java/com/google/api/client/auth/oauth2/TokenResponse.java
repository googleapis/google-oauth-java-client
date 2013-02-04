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

package com.google.api.client.auth.oauth2;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;

/**
 * OAuth 2.0 JSON model for a successful access token response as specified in <a
 * href="http://tools.ietf.org/html/rfc6749#section-5.1">Successful Response</a>.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class TokenResponse extends GenericJson {

  /** Access token issued by the authorization server. */
  @Key("access_token")
  private String accessToken;

  /**
   * Token type (as specified in <a href="http://tools.ietf.org/html/rfc6749#section-7.1">Access
   * Token Types</a>).
   */
  @Key("token_type")
  private String tokenType;

  /**
   * Lifetime in seconds of the access token (for example 3600 for an hour) or {@code null} for
   * none.
   */
  @Key("expires_in")
  private Long expiresInSeconds;

  /**
   * Refresh token which can be used to obtain new access tokens using {@link RefreshTokenRequest}
   * or {@code null} for none.
   */
  @Key("refresh_token")
  private String refreshToken;

  /**
   * Scope of the access token as specified in <a
   * href="http://tools.ietf.org/html/rfc6749#section-3.3">Access Token Scope</a> or {@code null}
   * for none.
   */
  @Key
  private String scope;

  /** Returns the access token issued by the authorization server. */
  public final String getAccessToken() {
    return accessToken;
  }

  /**
   * Sets the access token issued by the authorization server.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public TokenResponse setAccessToken(String accessToken) {
    this.accessToken = Preconditions.checkNotNull(accessToken);
    return this;
  }

  /**
   * Returns the token type (as specified in <a
   * href="http://tools.ietf.org/html/rfc6749#section-7.1">Access Token Types</a>).
   */
  public final String getTokenType() {
    return tokenType;
  }

  /**
   * Sets the token type (as specified in <a
   * href="http://tools.ietf.org/html/rfc6749#section-7.1">Access Token Types</a>).
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public TokenResponse setTokenType(String tokenType) {
    this.tokenType = Preconditions.checkNotNull(tokenType);
    return this;
  }

  /**
   * Returns the lifetime in seconds of the access token (for example 3600 for an hour) or
   * {@code null} for none.
   */
  public final Long getExpiresInSeconds() {
    return expiresInSeconds;
  }

  /**
   * Sets the lifetime in seconds of the access token (for example 3600 for an hour) or {@code null}
   * for none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public TokenResponse setExpiresInSeconds(Long expiresInSeconds) {
    this.expiresInSeconds = expiresInSeconds;
    return this;
  }

  /**
   * Returns the refresh token which can be used to obtain new access tokens using the same
   * authorization grant or {@code null} for none.
   */
  public final String getRefreshToken() {
    return refreshToken;
  }

  /**
   * Sets the refresh token which can be used to obtain new access tokens using the same
   * authorization grant or {@code null} for none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public TokenResponse setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
    return this;
  }

  /**
   * Returns the scope of the access token or {@code null} for none.
   */
  public final String getScope() {
    return scope;
  }

  /**
   * Sets the scope of the access token or {@code null} for none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public TokenResponse setScope(String scope) {
    this.scope = scope;
    return this;
  }

  @Override
  public TokenResponse set(String fieldName, Object value) {
    return (TokenResponse) super.set(fieldName, value);
  }

  @Override
  public TokenResponse clone() {
    return (TokenResponse) super.clone();
  }
}
