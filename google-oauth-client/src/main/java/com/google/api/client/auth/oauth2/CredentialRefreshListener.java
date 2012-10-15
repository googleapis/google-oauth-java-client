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

package com.google.api.client.auth.oauth2;

import java.io.IOException;


/**
 * Listener for refresh token results.
 *
 * <p>
 * These methods are called from {@link Credential#refreshToken()} after a response has been
 * received from refreshing the token. {@link #onTokenResponse} is called on a successful HTTP
 * response, and {@link #onTokenErrorResponse} is called on an error HTTP response.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public interface CredentialRefreshListener {

  /**
   * Notifies of a successful token response from {@link Credential#refreshToken()}.
   *
   * <p>
   * Typical use is to provide functionality like persisting the access token in a data store.
   * Implementations can assume proper thread synchronization is already taken care of inside
   * {@link Credential#refreshToken()}. Implementations can also assume that
   * {@link Credential#setAccessToken}, {@link Credential#setRefreshToken}, and
   * {@link Credential#setExpiresInSeconds} have already been called previously with the information
   * from the {@link TokenResponse}.
   * </p>
   *
   * @param credential credential on which the token refresh applied
   * @param tokenResponse token response
   */
  void onTokenResponse(Credential credential, TokenResponse tokenResponse) throws IOException;

  /**
   * Notifies of an error token response from {@link Credential#refreshToken()}.
   *
   * <p>
   * Typical use is to provide functionality like removing persistence of the access token from the
   * data store. Implementations can assume proper thread synchronization is already taken care of
   * inside {@link Credential#refreshToken()}. Implementations can also assume that
   * {@link Credential#setAccessToken}, and {@link Credential#setExpiresInSeconds} have already been
   * called previously with {@code null} to clear their values.
   * </p>
   *
   * @param credential credential on which the token refresh applied
   * @param tokenErrorResponse token error response or {@code null} for none supplied
   */
  void onTokenErrorResponse(Credential credential, TokenErrorResponse tokenErrorResponse)
      throws IOException;
}
