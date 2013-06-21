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

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Joiner;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;

import java.util.Collection;

/**
 * OAuth 2.0 URL builder for an authorization web page to allow the end user to authorize the
 * application to access their protected resources, as specified in <a
 * href="http://tools.ietf.org/html/rfc6749#section-3.1">Authorization Endpoint</a>.
 *
 * <p>
 * Sample usage for a web application:
 * </p>
 *
 * <pre>
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String url = new AuthorizationRequestUrl(
        "https://server.example.com/authorize", "s6BhdRkqt3", Arrays.asList("code")).setState("xyz")
        .setRedirectUri("https://client.example.com/rd").build();
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
public class AuthorizationRequestUrl extends GenericUrl {

  /**
   * <a href="http://tools.ietf.org/html/rfc6749#section-3.1.1">Response type</a>, which must be
   * {@code "code"} for requesting an authorization code, {@code "token"} for requesting an access
   * token (implicit grant), or space-separated registered extension values.
   */
  @Key("response_type")
  private String responseTypes;

  /**
   * URI that the authorization server directs the resource owner's user-agent back to the client
   * after a successful authorization grant (as specified in <a
   * href="http://tools.ietf.org/html/rfc6749#section-3.1.2">Redirection Endpoint</a>) or
   * {@code null} for none.
   */
  @Key("redirect_uri")
  private String redirectUri;

  /**
   * Space-separated list of scopes (as specified in <a
   * href="http://tools.ietf.org/html/rfc6749#section-3.3">Access Token Scope</a>) or {@code null}
   * for none.
   */
  @Key("scope")
  private String scopes;

  /** Client identifier. */
  @Key("client_id")
  private String clientId;

  /**
   * State (an opaque value used by the client to maintain state between the request and callback,
   * as mentioned in <a href="http://tools.ietf.org/html/rfc6749#section-3.1.2.2">Registration
   * Requirements</a>) or {@code null} for none.
   */
  @Key
  private String state;

  /**
   * @param authorizationServerEncodedUrl authorization server encoded URL
   * @param clientId client identifier
   * @param responseTypes <a href="http://tools.ietf.org/html/rfc6749#section-3.1.1">response
   *        type</a>, which must be {@code "code"} for requesting an authorization code,
   *        {@code "token"} for requesting an access token (implicit grant), or a list of registered
   *        extension values to join with a space
   * @since 1.15
   */
  public AuthorizationRequestUrl(
      String authorizationServerEncodedUrl, String clientId, Collection<String> responseTypes) {
    super(authorizationServerEncodedUrl);
    Preconditions.checkArgument(getFragment() == null);
    setClientId(clientId);
    setResponseTypes(responseTypes);
  }

  /**
   * Returns the <a href="http://tools.ietf.org/html/rfc6749#section-3.1.1">Response type</a>, which
   * must be {@code "code"} for requesting an authorization code, {@code "token"} for requesting an
   * access token (implicit grant), or space-separated registered extension values.
   */
  public final String getResponseTypes() {
    return responseTypes;
  }

  /**
   * Sets the <a href="http://tools.ietf.org/html/rfc6749#section-3.1.1">response type</a>, which
   * must be {@code "code"} for requesting an authorization code, {@code "token"} for requesting an
   * access token (implicit grant), or a list of registered extension values to join with a space.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   *
   * @since 1.15
   */
  public AuthorizationRequestUrl setResponseTypes(Collection<String> responseTypes) {
    this.responseTypes = Joiner.on(' ').join(responseTypes);
    return this;
  }

  /**
   * Returns the URI that the authorization server directs the resource owner's user-agent back to
   * the client after a successful authorization grant (as specified in <a
   * href="http://tools.ietf.org/html/rfc6749#section-3.1.2">Redirection Endpoint</a>) or
   * {@code null} for none.
   */
  public final String getRedirectUri() {
    return redirectUri;
  }

  /**
   * Sets the URI that the authorization server directs the resource owner's user-agent back to the
   * client after a successful authorization grant (as specified in <a
   * href="http://tools.ietf.org/html/rfc6749#section-3.1.2">Redirection Endpoint</a>) or
   * {@code null} for none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AuthorizationRequestUrl setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
    return this;
  }

  /**
   * Returns the space-separated list of scopes (as specified in <a
   * href="http://tools.ietf.org/html/rfc6749#section-3.3">Access Token Scope</a>) or {@code null}
   * for none.
   */
  public final String getScopes() {
    return scopes;
  }

  /**
   * Sets the list of scopes (as specified in <a
   * href="http://tools.ietf.org/html/rfc6749#section-3.3">Access Token Scope</a>) or {@code null}
   * for none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   *
   * @param scopes collection of scopes to be joined by a space separator (or a single value
   *        containing multiple space-separated scopes) or {@code null} for none
   * @since 1.15
   */
  public AuthorizationRequestUrl setScopes(Collection<String> scopes) {
    this.scopes =
        scopes == null || !scopes.iterator().hasNext() ? null : Joiner.on(' ').join(scopes);
    return this;
  }

  /** Returns the client identifier. */
  public final String getClientId() {
    return clientId;
  }

  /**
   * Sets the client identifier.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AuthorizationRequestUrl setClientId(String clientId) {
    this.clientId = Preconditions.checkNotNull(clientId);
    return this;
  }

  /**
   * Returns the state (an opaque value used by the client to maintain state between the request and
   * callback, as mentioned in <a
   * href="http://tools.ietf.org/html/rfc6749#section-3.1.2.2">Registration Requirements</a>) or
   * {@code null} for none.
   */
  public final String getState() {
    return state;
  }

  /**
   * Sets the state (an opaque value used by the client to maintain state between the request and
   * callback, as mentioned in <a
   * href="http://tools.ietf.org/html/rfc6749#section-3.1.2.2">Registration Requirements</a>) or
   * {@code null} for none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AuthorizationRequestUrl setState(String state) {
    this.state = state;
    return this;
  }

  @Override
  public AuthorizationRequestUrl set(String fieldName, Object value) {
    return (AuthorizationRequestUrl) super.set(fieldName, value);
  }

  @Override
  public AuthorizationRequestUrl clone() {
    return (AuthorizationRequestUrl) super.clone();
  }
}
