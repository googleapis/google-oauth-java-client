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

import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.util.GenericData;
import com.google.api.client.util.Joiner;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;

import java.io.IOException;
import java.util.Collection;

/**
 * OAuth 2.0 request for an access token as specified in <a
 * href="http://tools.ietf.org/html/rfc6749#section-4">Obtaining Authorization</a>.
 *
 * <p>
 * Call {@link #execute()} to execute the request and use the returned {@link TokenResponse}. On
 * error, it will instead throw {@link TokenResponseException}.
 * </p>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class TokenRequest extends GenericData {

  /** HTTP request initializer or {@code null} for none. */
  HttpRequestInitializer requestInitializer;

  /** Client authentication or {@code null} for none. */
  HttpExecuteInterceptor clientAuthentication;

  /** HTTP transport. */
  private final HttpTransport transport;

  /** JSON factory. */
  private final JsonFactory jsonFactory;

  /** Token server URL. */
  private GenericUrl tokenServerUrl;

  /**
   * Space-separated list of scopes (as specified in <a
   * href="http://tools.ietf.org/html/rfc6749#section-3.3">Access Token Scope</a>) or {@code null}
   * for none.
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
  public final HttpExecuteInterceptor getClientAuthentication() {
    return clientAuthentication;
  }

  /**
   * Sets the client authentication or {@code null} for none.
   *
   * <p>
   * The recommended initializer by the specification is {@link BasicAuthentication}. All
   * authorization servers must support that. A common alternative is
   * {@link ClientParametersAuthentication}. An alternative client authentication method may be
   * provided that implements {@link HttpRequestInitializer}.
   * </p>
   *
   * <p>
   * This HTTP request execute interceptor is guaranteed to be the last execute interceptor before
   * the request is executed, and after any execute interceptor set by the
   * {@link #getRequestInitializer()}.
   * </p>
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public TokenRequest setClientAuthentication(HttpExecuteInterceptor clientAuthentication) {
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
   *        containing multiple space-separated scopes)
   * @since 1.15
   */
  public TokenRequest setScopes(Collection<String> scopes) {
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
   * To execute and parse the response to {@link TokenResponse}, instead use {@link #execute()}.
   * </p>
   *
   * <p>
   * Callers should call {@link HttpResponse#disconnect} when the returned HTTP response object is
   * no longer needed. However, {@link HttpResponse#disconnect} does not have to be called if the
   * response stream is properly closed. Example usage:
   * </p>
   *
   * <pre>
     HttpResponse response = tokenRequest.executeUnparsed();
     try {
       // process the HTTP response object
     } finally {
       response.disconnect();
     }
   * </pre>
   *
   * @return successful access token response, which can then be parsed directly using
   *         {@link HttpResponse#parseAs(Class)} or some other parsing method
   * @throws TokenResponseException for an error response
   */
  public final HttpResponse executeUnparsed() throws IOException {
    // must set clientAuthentication as last execute interceptor in case it needs to sign request
    HttpRequestFactory requestFactory =
        transport.createRequestFactory(new HttpRequestInitializer() {

          public void initialize(HttpRequest request) throws IOException {
            if (requestInitializer != null) {
              requestInitializer.initialize(request);
            }
            final HttpExecuteInterceptor interceptor = request.getInterceptor();
            request.setInterceptor(new HttpExecuteInterceptor() {
              public void intercept(HttpRequest request) throws IOException {
                if (interceptor != null) {
                  interceptor.intercept(request);
                }
                if (clientAuthentication != null) {
                  clientAuthentication.intercept(request);
                }
              }
            });
          }
        });
    // make request
    HttpRequest request =
        requestFactory.buildPostRequest(tokenServerUrl, new UrlEncodedContent(this));
    request.setParser(new JsonObjectParser(jsonFactory));
    request.setThrowExceptionOnExecuteError(false);
    HttpResponse response = request.execute();
    if (response.isSuccessStatusCode()) {
      return response;
    }
    throw TokenResponseException.from(jsonFactory, response);
  }

  /**
   * Executes request for an access token, and returns the parsed access token response.
   *
   * <p>
   * To execute but parse the response in an alternate way, use {@link #executeUnparsed()}.
   * </p>
   *
   * <p>
   * Default implementation calls {@link #executeUnparsed()} and then parses using
   * {@link TokenResponse}. Subclasses may override to change the return type, but must still call
   * {@link #executeUnparsed()}.
   * </p>
   *
   * @return parsed successful access token response
   * @throws TokenResponseException for an error response
   */
  public TokenResponse execute() throws IOException {
    return executeUnparsed().parseAs(TokenResponse.class);
  }

  @Override
  public TokenRequest set(String fieldName, Object value) {
    return (TokenRequest) super.set(fieldName, value);
  }
}
