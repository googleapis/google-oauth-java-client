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

import java.util.Collection;
import java.util.Collections;

/**
 * OAuth 2.0 URL builder for an authorization web page to allow the end user to authorize the
 * application to access their protected resources and that returns an authorization code, as
 * specified in <a href="http://tools.ietf.org/html/rfc6749#section-4.1">Authorization Code
 * Grant</a>.
 *
 * <p>
 * The default for {@link #getResponseTypes()} is {@code "code"}. Use
 * {@link AuthorizationCodeResponseUrl} to parse the redirect response after the end user
 * grants/denies the request. Using the authorization code in this response, use
 * {@link AuthorizationCodeTokenRequest} to request the access token.
 * </p>
 *
 * <p>
 * Sample usage for a web application:
 * </p>
 *
 * <pre>
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String url =
        new AuthorizationCodeRequestUrl("https://server.example.com/authorize", "s6BhdRkqt3")
            .setState("xyz").setRedirectUri("https://client.example.com/rd").build();
    response.sendRedirect(url);
  }
 * </pre>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class AuthorizationCodeRequestUrl extends AuthorizationRequestUrl {

  /**
   * @param authorizationServerEncodedUrl authorization server encoded URL
   * @param clientId client identifier
   */
  public AuthorizationCodeRequestUrl(String authorizationServerEncodedUrl, String clientId) {
    super(authorizationServerEncodedUrl, clientId, Collections.singleton("code"));
  }

  @Override
  public AuthorizationCodeRequestUrl setResponseTypes(Collection<String> responseTypes) {
    return (AuthorizationCodeRequestUrl) super.setResponseTypes(responseTypes);
  }

  @Override
  public AuthorizationCodeRequestUrl setRedirectUri(String redirectUri) {
    return (AuthorizationCodeRequestUrl) super.setRedirectUri(redirectUri);
  }

  @Override
  public AuthorizationCodeRequestUrl setScopes(Collection<String> scopes) {
    return (AuthorizationCodeRequestUrl) super.setScopes(scopes);
  }

  @Override
  public AuthorizationCodeRequestUrl setClientId(String clientId) {
    return (AuthorizationCodeRequestUrl) super.setClientId(clientId);
  }

  @Override
  public AuthorizationCodeRequestUrl setState(String state) {
    return (AuthorizationCodeRequestUrl) super.setState(state);
  }

  @Override
  public AuthorizationCodeRequestUrl set(String fieldName, Object value) {
    return (AuthorizationCodeRequestUrl) super.set(fieldName, value);
  }

  @Override
  public AuthorizationCodeRequestUrl clone() {
    return (AuthorizationCodeRequestUrl) super.clone();
  }
}
