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

import com.google.api.client.auth.oauth2.Credential.AccessMethod;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Clock;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.Arrays;

/**
 * Thread-safe OAuth 2.0 authorization code flow that manages and persists end-user credentials.
 *
 * <p>
 * This is designed to simplify the flow in which an end-user authorizes the application to access
 * their protected data, and then the application has access to their data based on an access token
 * and a refresh token to refresh that access token when it expires.
 * </p>
 *
 * <p>
 * The first step is to call {@link #loadCredential(String)} based on the known user ID to check if
 * the end-user's credentials are already known. If not, call {@link #newAuthorizationUrl()} and
 * direct the end-user's browser to an authorization page. The web browser will then redirect to the
 * redirect URL with a {@code "code"} query parameter which can then be used to request an access
 * token using {@link #newTokenRequest(String)}. Finally, use
 * {@link #createAndStoreCredential(TokenResponse, String)} to store and obtain a credential for
 * accessing protected resources.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class AuthorizationCodeFlow {

  /**
   * Method of presenting the access token to the resource server (for example
   * {@link BearerToken#authorizationHeaderAccessMethod}).
   */
  private final AccessMethod method;

  /** HTTP transport. */
  private final HttpTransport transport;

  /** JSON factory. */
  private final JsonFactory jsonFactory;

  /** Token server encoded URL. */
  private final String tokenServerEncodedUrl;

  /**
   * Client authentication or {@code null} for none (see
   * {@link TokenRequest#setClientAuthentication(HttpExecuteInterceptor)}).
   */
  private final HttpExecuteInterceptor clientAuthentication;

  /** Client identifier. */
  private final String clientId;

  /** Authorization server encoded URL. */
  private final String authorizationServerEncodedUrl;

  /** Credential persistence store or {@code null} for none. */
  private final CredentialStore credentialStore;

  /** HTTP request initializer or {@code null} for none. */
  private final HttpRequestInitializer requestInitializer;

  /** Clock passed along to Credential. */
  private final Clock clock;

  /** Space-separated list of scopes or {@code null} for none. */
  private String scopes;

  /**
   * @param method method of presenting the access token to the resource server (for example
   *        {@link BearerToken#authorizationHeaderAccessMethod})
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param tokenServerUrl token server URL
   * @param clientAuthentication client authentication or {@code null} for none (see
   *        {@link TokenRequest#setClientAuthentication(HttpExecuteInterceptor)})
   * @param clientId client identifier
   * @param authorizationServerEncodedUrl authorization server encoded URL
   *
   * @since 1.14
   */
  public AuthorizationCodeFlow(AccessMethod method,
      HttpTransport transport,
      JsonFactory jsonFactory,
      GenericUrl tokenServerUrl,
      HttpExecuteInterceptor clientAuthentication,
      String clientId,
      String authorizationServerEncodedUrl) {
    this(new Builder(method,
        transport,
        jsonFactory,
        tokenServerUrl,
        clientAuthentication,
        clientId,
        authorizationServerEncodedUrl));
  }

  /**
   * @param builder authorization code flow builder
   *
   * @since 1.14
   */
  protected AuthorizationCodeFlow(Builder builder) {
    method = Preconditions.checkNotNull(builder.method);
    transport = Preconditions.checkNotNull(builder.transport);
    jsonFactory = Preconditions.checkNotNull(builder.jsonFactory);
    tokenServerEncodedUrl = Preconditions.checkNotNull(builder.tokenServerUrl).build();
    clientAuthentication = builder.clientAuthentication;
    clientId = Preconditions.checkNotNull(builder.clientId);
    authorizationServerEncodedUrl =
        Preconditions.checkNotNull(builder.authorizationServerEncodedUrl);
    requestInitializer = builder.requestInitializer;
    credentialStore = builder.credentialStore;
    scopes = builder.scopes;
    clock = Preconditions.checkNotNull(builder.clock);
  }

  /**
   * @param method method of presenting the access token to the resource server (for example
   *        {@link BearerToken#authorizationHeaderAccessMethod})
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param tokenServerUrl token server URL
   * @param clientAuthentication client authentication or {@code null} for none (see
   *        {@link TokenRequest#setClientAuthentication(HttpExecuteInterceptor)})
   * @param clientId client identifier
   * @param authorizationServerEncodedUrl authorization server encoded URL
   * @param credentialStore credential persistence store or {@code null} for none
   * @param requestInitializer HTTP request initializer or {@code null} for none
   * @param scopes space-separated list of scopes or {@code null} for none
   * @deprecated (scheduled to be removed in 1.15) Use {@link #AuthorizationCodeFlow(Builder)}
   */
  @Deprecated
  protected AuthorizationCodeFlow(AccessMethod method,
      HttpTransport transport,
      JsonFactory jsonFactory,
      GenericUrl tokenServerUrl,
      HttpExecuteInterceptor clientAuthentication,
      String clientId,
      String authorizationServerEncodedUrl,
      CredentialStore credentialStore,
      HttpRequestInitializer requestInitializer,
      String scopes) {
    this(method,
        transport,
        jsonFactory,
        tokenServerUrl,
        clientAuthentication,
        clientId,
        authorizationServerEncodedUrl,
        credentialStore,
        requestInitializer,
        scopes,
        Clock.SYSTEM);
  }

  /**
   * @param method method of presenting the access token to the resource server (for example
   *        {@link BearerToken#authorizationHeaderAccessMethod})
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param tokenServerUrl token server URL
   * @param clientAuthentication client authentication or {@code null} for none (see
   *        {@link TokenRequest#setClientAuthentication(HttpExecuteInterceptor)})
   * @param clientId client identifier
   * @param authorizationServerEncodedUrl authorization server encoded URL
   * @param credentialStore credential persistence store or {@code null} for none
   * @param requestInitializer HTTP request initializer or {@code null} for none
   * @param scopes space-separated list of scopes or {@code null} for none
   * @param clock Clock used for Credential expiration
   * @since 1.9
   * @deprecated (scheduled to be removed in 1.15) Use {@link #AuthorizationCodeFlow(Builder)}
   */
  @Deprecated
  protected AuthorizationCodeFlow(AccessMethod method,
      HttpTransport transport,
      JsonFactory jsonFactory,
      GenericUrl tokenServerUrl,
      HttpExecuteInterceptor clientAuthentication,
      String clientId,
      String authorizationServerEncodedUrl,
      CredentialStore credentialStore,
      HttpRequestInitializer requestInitializer,
      String scopes,
      Clock clock) {
    this.method = Preconditions.checkNotNull(method);
    this.transport = Preconditions.checkNotNull(transport);
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    this.tokenServerEncodedUrl = Preconditions.checkNotNull(tokenServerUrl).build();
    this.clientAuthentication = clientAuthentication;
    this.clientId = Preconditions.checkNotNull(clientId);
    this.authorizationServerEncodedUrl = Preconditions.checkNotNull(authorizationServerEncodedUrl);
    this.requestInitializer = requestInitializer;
    this.credentialStore = credentialStore;
    this.scopes = scopes;
    this.clock = Preconditions.checkNotNull(clock);
  }

  /**
   * Returns a new instance of an authorization code request URL.
   *
   * <p>
   * This is a builder for an authorization web page to allow the end user to authorize the
   * application to access their protected resources and that returns an authorization code. It uses
   * the {@link #getAuthorizationServerEncodedUrl()}, {@link #getClientId()}, and
   * {@link #getScopes()}. Sample usage:
   * </p>
   *
   * <pre>
  private AuthorizationCodeFlow flow;

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String url = flow.newAuthorizationUrl().setState("xyz")
        .setRedirectUri("https://client.example.com/rd").build();
    response.sendRedirect(url);
  }
   * </pre>
   */
  public AuthorizationCodeRequestUrl newAuthorizationUrl() {
    return new AuthorizationCodeRequestUrl(authorizationServerEncodedUrl, clientId).setScopes(
        scopes);
  }

  /**
   * Returns a new instance of an authorization code token request based on the given authorization
   * code.
   *
   * <p>
   * This is used to make a request for an access token using the authorization code. It uses
   * {@link #getTransport()}, {@link #getJsonFactory()}, {@link #getTokenServerEncodedUrl()},
   * {@link #getClientAuthentication()}, {@link #getRequestInitializer()}, and {@link #getScopes()}.
   * </p>
   *
   * <pre>
  static TokenResponse requestAccessToken(AuthorizationCodeFlow flow, String code)
      throws IOException, TokenResponseException {
    return flow.newTokenRequest(code).setRedirectUri("https://client.example.com/rd").execute();
  }
   * </pre>
   *
   * @param authorizationCode authorization code.
   */
  public AuthorizationCodeTokenRequest newTokenRequest(String authorizationCode) {
    return new AuthorizationCodeTokenRequest(transport, jsonFactory,
        new GenericUrl(tokenServerEncodedUrl), authorizationCode).setClientAuthentication(
        clientAuthentication).setRequestInitializer(requestInitializer).setScopes(scopes);
  }

  /**
   * Creates a new credential for the given user ID based on the given token response and store in
   * the credential store.
   *
   * @param response token response
   * @param userId user ID or {@code null} if not using a persisted credential store
   * @return newly created credential
   */
  public Credential createAndStoreCredential(TokenResponse response, String userId)
      throws IOException {
    Credential credential = newCredential(userId).setFromTokenResponse(response);
    if (credentialStore != null) {
      credentialStore.store(userId, credential);
    }
    return credential;
  }

  /**
   * Loads the credential of the given user ID from the credential store.
   *
   * @param userId user ID or {@code null} if not using a persisted credential store
   * @return credential found in the credential store of the given user ID or {@code null} for none
   *         found
   */
  public Credential loadCredential(String userId) throws IOException {
    if (credentialStore == null) {
      return null;
    }
    Credential credential = newCredential(userId);
    if (!credentialStore.load(userId, credential)) {
      return null;
    }
    return credential;
  }

  /**
   * Returns a new credential instance based on the given user ID.
   *
   * @param userId user ID or {@code null} if not using a persisted credential store
   */
  private Credential newCredential(String userId) {
    Credential.Builder builder = new Credential.Builder(method).setTransport(transport)
        .setJsonFactory(jsonFactory)
        .setTokenServerEncodedUrl(tokenServerEncodedUrl)
        .setClientAuthentication(clientAuthentication)
        .setRequestInitializer(requestInitializer)
        .setClock(clock);
    if (credentialStore != null) {
      builder.addRefreshListener(new CredentialStoreRefreshListener(userId, credentialStore));
    }
    return builder.build();
  }

  /**
   * Returns the method of presenting the access token to the resource server (for example
   * {@link BearerToken#authorizationHeaderAccessMethod}).
   */
  public final AccessMethod getMethod() {
    return method;
  }

  /** Returns the HTTP transport. */
  public final HttpTransport getTransport() {
    return transport;
  }

  /** Returns the JSON factory. */
  public final JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /** Returns the token server encoded URL. */
  public final String getTokenServerEncodedUrl() {
    return tokenServerEncodedUrl;
  }

  /**
   * Returns the client authentication or {@code null} for none (see
   * {@link TokenRequest#setClientAuthentication(HttpExecuteInterceptor)}).
   */
  public final HttpExecuteInterceptor getClientAuthentication() {
    return clientAuthentication;
  }

  /** Returns the client identifier. */
  public final String getClientId() {
    return clientId;
  }

  /** Returns the authorization server encoded URL. */
  public final String getAuthorizationServerEncodedUrl() {
    return authorizationServerEncodedUrl;
  }

  /** Returns the credential persistence store or {@code null} for none. */
  public final CredentialStore getCredentialStore() {
    return credentialStore;
  }

  /** Returns the HTTP request initializer or {@code null} for none. */
  public final HttpRequestInitializer getRequestInitializer() {
    return requestInitializer;
  }

  /** Returns the space-separated list of scopes or {@code null} for none. */
  public final String getScopes() {
    return scopes;
  }

  /**
   * Returns the clock which will be passed along to the Credential.
   * @since 1.9
   */
  public final Clock getClock() {
    return clock;
  }

  /**
   * Authorization code flow builder.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   */
  public static class Builder {

    /**
     * Method of presenting the access token to the resource server (for example
     * {@link BearerToken#authorizationHeaderAccessMethod}).
     */
    AccessMethod method;

    /** HTTP transport. */
    HttpTransport transport;

    /** JSON factory. */
    JsonFactory jsonFactory;

    /** Token server URL. */
    GenericUrl tokenServerUrl;

    /**
     * Client authentication or {@code null} for none (see
     * {@link TokenRequest#setClientAuthentication(HttpExecuteInterceptor)}).
     */
    HttpExecuteInterceptor clientAuthentication;

    /** Client identifier. */
    String clientId;

    /** Authorization server encoded URL. */
    String authorizationServerEncodedUrl;

    /** Credential persistence store or {@code null} for none. */
    CredentialStore credentialStore;

    /** HTTP request initializer or {@code null} for none. */
    HttpRequestInitializer requestInitializer;

    /** Space-separated list of scopes or {@code null} for none. */
    String scopes;

    /** Clock passed along to the Credential. */
    Clock clock = Clock.SYSTEM;

    /**
     * @param method method of presenting the access token to the resource server (for example
     *        {@link BearerToken#authorizationHeaderAccessMethod})
     * @param transport HTTP transport
     * @param jsonFactory JSON factory
     * @param tokenServerUrl token server URL
     * @param clientAuthentication client authentication or {@code null} for none (see
     *        {@link TokenRequest#setClientAuthentication(HttpExecuteInterceptor)})
     * @param clientId client identifier
     * @param authorizationServerEncodedUrl authorization server encoded URL
     */
    public Builder(AccessMethod method,
        HttpTransport transport,
        JsonFactory jsonFactory,
        GenericUrl tokenServerUrl,
        HttpExecuteInterceptor clientAuthentication,
        String clientId,
        String authorizationServerEncodedUrl) {
      setMethod(method);
      setTransport(transport);
      setJsonFactory(jsonFactory);
      setTokenServerUrl(tokenServerUrl);
      setClientAuthentication(clientAuthentication);
      setClientId(clientId);
      setAuthorizationServerEncodedUrl(authorizationServerEncodedUrl);
    }

    /** Returns a new instance of an authorization code flow based on this builder. */
    public AuthorizationCodeFlow build() {
      return new AuthorizationCodeFlow(this);
    }

    /**
     * Returns the method of presenting the access token to the resource server (for example
     * {@link BearerToken#authorizationHeaderAccessMethod}).
     */
    public final AccessMethod getMethod() {
      return method;
    }

    /**
     * Sets the method of presenting the access token to the resource server (for example
     * {@link BearerToken#authorizationHeaderAccessMethod}).
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     * @since 1.11
     */
    public Builder setMethod(AccessMethod method) {
      this.method = Preconditions.checkNotNull(method);
      return this;
    }

    /** Returns the HTTP transport. */
    public final HttpTransport getTransport() {
      return transport;
    }

    /**
     * Sets the HTTP transport.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     * @since 1.11
     */
    public Builder setTransport(HttpTransport transport) {
      this.transport = Preconditions.checkNotNull(transport);
      return this;
    }

    /** Returns the JSON factory. */
    public final JsonFactory getJsonFactory() {
      return jsonFactory;
    }

    /**
     * Sets the JSON factory.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     * @since 1.11
     */
    public Builder setJsonFactory(JsonFactory jsonFactory) {
      this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
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
     * @since 1.11
     */
    public Builder setTokenServerUrl(GenericUrl tokenServerUrl) {
      this.tokenServerUrl = Preconditions.checkNotNull(tokenServerUrl);
      return this;
    }

    /**
     * Returns the client authentication or {@code null} for none (see
     * {@link TokenRequest#setClientAuthentication(HttpExecuteInterceptor)}).
     */
    public final HttpExecuteInterceptor getClientAuthentication() {
      return clientAuthentication;
    }

    /**
     * Sets the client authentication or {@code null} for none (see
     * {@link TokenRequest#setClientAuthentication(HttpExecuteInterceptor)}).
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     * @since 1.11
     */
    public Builder setClientAuthentication(HttpExecuteInterceptor clientAuthentication) {
      this.clientAuthentication = clientAuthentication;
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
     * @since 1.11
     */
    public Builder setClientId(String clientId) {
      this.clientId = Preconditions.checkNotNull(clientId);
      return this;
    }

    /** Returns the authorization server encoded URL. */
    public final String getAuthorizationServerEncodedUrl() {
      return authorizationServerEncodedUrl;
    }

    /**
     * Sets the authorization server encoded URL.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     * @since 1.11
     */
    public Builder setAuthorizationServerEncodedUrl(String authorizationServerEncodedUrl) {
      this.authorizationServerEncodedUrl =
          Preconditions.checkNotNull(authorizationServerEncodedUrl);
      return this;
    }

    /** Returns the credential persistence store or {@code null} for none. */
    public final CredentialStore getCredentialStore() {
      return credentialStore;
    }

    /**
     * Returns the clock passed along to the Credential or {@link Clock#SYSTEM} when system default
     * is used.
     * @since 1.9
     */
    public final Clock getClock() {
      return clock;
    }

    /**
     * Sets the clock to pass to the Credential.
     *
     * <p>
     * The default value for this parameter is {@link Clock#SYSTEM}
     * </p>
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     * @since 1.9
     */
    public Builder setClock(Clock clock) {
      this.clock = Preconditions.checkNotNull(clock);
      return this;
    }

    /**
     * Sets the credential persistence store or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Builder setCredentialStore(CredentialStore credentialStore) {
      this.credentialStore = credentialStore;
      return this;
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
    public Builder setRequestInitializer(HttpRequestInitializer requestInitializer) {
      this.requestInitializer = requestInitializer;
      return this;
    }

    /**
     * Sets the list of scopes or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @param scopes list of scopes to be joined by a space separator (or a single value containing
     *        multiple space-separated scopes)
     */
    public Builder setScopes(Iterable<String> scopes) {
      this.scopes = scopes == null ? null : Joiner.on(' ').join(scopes);
      return this;
    }

    /**
     * Sets the list of scopes or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @param scopes list of scopes to be joined by a space separator (or a single value containing
     *        multiple space-separated scopes)
     */
    public Builder setScopes(String... scopes) {
      return setScopes(scopes == null ? null : Arrays.asList(scopes));
    }

    /** Returns the space-separated list of scopes or {@code null} for none. */
    public final String getScopes() {
      return scopes;
    }
  }
}
