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
 * application to access their protected resources and that returns the access token to a browser
 * client using a scripting language such as JavaScript, as specified in <a
 * href="http://tools.ietf.org/html/rfc6749#section-4.2">Implicit Grant</a>.
 *
 * <p>
 * The default for {@link #getResponseTypes()} is {@code "token"}.
 * </p>
 *
 * <p>
 * Sample usage for a web application:
 * </p>
 *
 * <pre>
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String url = new BrowserClientRequestUrl(
        "https://server.example.com/authorize", "s6BhdRkqt3").setState("xyz")
        .setRedirectUri("https://client.example.com/cb").build();
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
public class BrowserClientRequestUrl extends AuthorizationRequestUrl {

  /**
   * @param encodedAuthorizationServerUrl encoded authorization server URL
   * @param clientId client identifier
   */
  public BrowserClientRequestUrl(String encodedAuthorizationServerUrl, String clientId) {
    super(encodedAuthorizationServerUrl, clientId, Collections.singleton("token"));
  }

  @Override
  public BrowserClientRequestUrl setResponseTypes(Collection<String> responseTypes) {
    return (BrowserClientRequestUrl) super.setResponseTypes(responseTypes);
  }

  @Override
  public BrowserClientRequestUrl setRedirectUri(String redirectUri) {
    return (BrowserClientRequestUrl) super.setRedirectUri(redirectUri);
  }

  @Override
  public BrowserClientRequestUrl setScopes(Collection<String> scopes) {
    return (BrowserClientRequestUrl) super.setScopes(scopes);
  }

  @Override
  public BrowserClientRequestUrl setClientId(String clientId) {
    return (BrowserClientRequestUrl) super.setClientId(clientId);
  }

  @Override
  public BrowserClientRequestUrl setState(String state) {
    return (BrowserClientRequestUrl) super.setState(state);
  }

  @Override
  public BrowserClientRequestUrl set(String fieldName, Object value) {
    return (BrowserClientRequestUrl) super.set(fieldName, value);
  }

  @Override
  public BrowserClientRequestUrl clone() {
    return (BrowserClientRequestUrl) super.clone();
  }
}
