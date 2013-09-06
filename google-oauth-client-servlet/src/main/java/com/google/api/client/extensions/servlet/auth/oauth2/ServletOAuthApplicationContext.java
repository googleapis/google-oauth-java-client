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

import com.google.api.client.auth.oauth2.OAuthApplicationContext;

import java.util.Collection;

import javax.servlet.ServletRequest;

/**
 * @author Nick Miceli
 * @author Eyal Peled
 *
 * @since 1.18
 *
 */
public abstract class ServletOAuthApplicationContext implements OAuthApplicationContext {

  private final Collection<String> scopes;
  private final String redirectUri;
  private final String applicationName;

  /**
   *
   * @param redirectUri the redirect URI in the authorization code flow
   * @param scopes the scopes
   * @param applicationName the application name
   */
  public ServletOAuthApplicationContext(
      String redirectUri, Collection<String> scopes, String applicationName) {
    this.redirectUri = redirectUri;
    this.scopes = scopes;
    this.applicationName = applicationName;
  }

  public Collection<String> getScopes() {
    return scopes;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public abstract String getUserId(ServletRequest request);
}
