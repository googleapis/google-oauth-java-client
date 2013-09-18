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

package com.google.api.client.extensions.servlet.auth.oauth2;

import com.google.api.client.auth.oauth2.OAuthContext;
import com.google.api.client.util.Beta;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

/**
 * {@link Beta} <br/>
 * An abstract servlet OAuth context which implements {@link OAuthContext} and add support in
 * {@link #getRedirectUri redirect URI} for retrieving the authorization code.
 *
 * @author Nick Miceli
 * @author Eyal Peled
 * @since 1.18
 */
@Beta
public abstract class ServletOAuthContext implements OAuthContext {

  /** Scopes which are used in the OAuth2 flow. */
  private final Collection<String> scopes;

  /** Redirect URI for retrieving the authorization code. */
  private final String redirectUri;

  /** Application name which is used as part of the user-agent header. */
  private final String applicationName;

  /**
   * Constructs a new servlet OAuth context.
   *
   * @param redirectUri the redirect URI in the authorization code flow
   * @param scopes the scopes
   * @param applicationName the application name
   */
  public ServletOAuthContext(
      String redirectUri, Collection<String> scopes, String applicationName) {
    this.redirectUri = redirectUri;
    this.scopes = scopes;
    this.applicationName = applicationName;
  }

  public Collection<String> getScopes() {
    return scopes;
  }

  public String getUserAgent() {
    return applicationName;
  }

  /** Returns the redirect URI for retrieving the authorization code. */
  public String getRedirectUri() {
    return redirectUri;
  }

  /**
   * Returns the user ID for the given HTTP servlet request.
   *
   * @param request the HTTP servlet request
   */
  public abstract String getUserId(HttpServletRequest request);
}
