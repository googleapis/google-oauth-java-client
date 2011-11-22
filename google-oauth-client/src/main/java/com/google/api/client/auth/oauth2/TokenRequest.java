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

import com.google.api.client.http.MultiHttpRequestInitializer;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import java.io.IOException;

/**
 * OAuth 2.0 request for an access token as specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-4">Obtaining Authorization</a>.
 * 
 * @since 1.7
 * @author Yaniv Inbar
 */
public class TokenRequest extends GenericData {

  /** HTTP request initializer or {@code null} for none. */
  private HttpRequestInitializer requestInitializer;

  /** Client authentication or {@code null} for none. */
  private HttpRequestInitializer clientAuthentication;

  /** HTTP transport. */
  private final HttpTransport transport;

  /** JSON factory. */
  private final JsonFactory jsonFactory;

  /** Token server URL. */
  private GenericUrl tokenServerUrl;

  /**
   * Space-separated list of scopes (as specified in <a
   * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-3.3">Access Token Scope</a>) or
   * {@code null} for none.
   */
  @Key("scope")
  private String scopes;

  /**
   * Grant type ({@code "authorization_code"}, {@code "password"}, {@code "client_credentials"},
   * {@code "refresh_token"} or absolute URI of the extension grant type).
   */
  @Key("grant_type")
  private String grantType;

  /**
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param tokenServerUrl token server URL
   * @param grantType grant type ({@code "authorization_code"}, {@code "password"},
   *        {@code "client_credentials"}, {@code "refresh_token"} or absolute URI of the extension
   *        grant type)
   */
  public TokenRequest(HttpTransport transport, JsonFactory jsonFactory, GenericUrl tokenServerUrl,
      String grantType) {
    this.transport = Preconditions.checkNotNull(transport);
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    setTokenServerUrl(tokenServerUrl);
    setGrantType(grantType);
  }

  /** Returns the HTTP transport. */
  public final HttpTransport getTransport() {
    return transport;
  }

  /** Returns the JSON factory. */
  public final JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /** Returns the HTTP request initializer or {@code null} for none. */
  public final HttpRequestInitializer getRequestInitializer() {
    return requestInitializer;
  }

  /**
   * Sets the HTTP request initializer or {@code null} for none.
   * 
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public TokenRequest setRequestInitializer(HttpRequestInitializer requestInitializer) {
    this.requestInitializer = requestInitializer;
    return this;
  }

  /** Returns the client authentication or {@code null} for none. */
  public final HttpRequestInitializer getClientAuthentication() {
    return clientAuthentication;
  }

  /**
   * Sets the client authentication or {@code null} for none.
   * 
   * <p>
   * The recommended initializer by the specification is {@link BasicAuthentication}. A common
   * alternative is {@link ClientParametersAuthentication}. An alternative client authentication
   * method may be provided that implements {@link HttpRequestInitializer}. This HTTP request
   * initializer is run after the {@link #getRequestInitializer()}.
   * </p>
   * 
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public TokenRequest setClientAuthentication(HttpRequestInitializer clientAuthentication) {
    this.clientAuthentication = clientAuthentication;
    return this;
  }

  /** Returns the token server URL. */
  public final GenericUrl getTokenServerUrl() {
    return tokenServerUrl;
  }

  /**
   * Sets the token server URL.
   * 
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public TokenRequest setTokenServerUrl(GenericUrl tokenServerUrl) {
    this.tokenServerUrl = tokenServerUrl;
    Preconditions.checkArgument(tokenServerUrl.getFragment() == null);
    return this;
  }

  /**
   * Returns the space-separated list of scopes (as specified in <a
   * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-3.3">Access Token Scope</a>) or
   * {@code null} for none.
   */
  public final String getScopes() {
    return scopes;
  }

  /**
   * Sets the list of scopes (as specified in <a
   * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-3.3">Access Token Scope</a>).
   * 
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   * 
   * @param scopes list of scopes to be joined by a space separator (or a single value containing
   *        multiple space-separated scopes)
   */
  public TokenRequest setScopes(String... scopes) {
    this.scopes = scopes == null ? null : Joiner.on(' ').join(scopes);
    return this;
  }

  /**
   * Returns the grant type ({@code "authorization_code"}, {@code "password"},
   * {@code "client_credentials"}, {@code "refresh_token"} or absolute URI of the extension grant
   * type).
   */
  public final String getGrantType() {
    return grantType;
  }

  /**
   * Sets the grant type ({@code "authorization_code"}, {@code "password"},
   * {@code "client_credentials"}, {@code "refresh_token"} or absolute URI of the extension grant
   * type).
   * 
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public TokenRequest setGrantType(String grantType) {
    this.grantType = Preconditions.checkNotNull(grantType);
    return this;
  }

  /**
   * Executes request for an access token, and returns the HTTP response.
   * 
   * <p>
   * To execute and parse the response to {@link TokenResponse}, use {@link #execute()}
   * </p>
   * 
   * @return HTTP response, which can then be parsed directly using
   *         {@link HttpResponse#parseAs(Class)} or some other parsing method
   * @throws HttpResponseException for an HTTP error response, which can then be parsed using
   *         {@link HttpResponse#parseAs(Class)} on {@link HttpResponseException#getResponse()}
   *         using {@link TokenErrorResponse}
   */
  public final HttpResponse executeUnparsed() throws IOException {
    HttpRequestFactory requestFactory =
        transport.createRequestFactory(new MultiHttpRequestInitializer(requestInitializer,
            clientAuthentication));
    HttpRequest request =
        requestFactory.buildPostRequest(tokenServerUrl, new UrlEncodedContent(this));
    request.addParser(new JsonHttpParser(jsonFactory));
    return request.execute();
  }

  /**
   * Executes request for an access token, and returns the parsed access token response.
   * 
   * <p>
   * To execute without parsing the response, use {@link #executeUnparsed()}.
   * </p>
   * 
   * @return parsed access token response
   * @throws HttpResponseException for an HTTP error response, which can then be parsed using
   *         {@link HttpResponse#parseAs(Class)} on {@link HttpResponseException#getResponse()}
   *         using {@link TokenErrorResponse}
   */
  public final TokenResponse execute() throws IOException {
    return executeUnparsed().parseAs(TokenResponse.class);
  }
}
