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
import com.google.api.client.util.Beta;
import com.google.api.client.util.Clock;
import com.google.api.client.util.Joiner;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

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
  @Beta
  @Deprecated
  private final CredentialStore credentialStore;

  /** Stored credential data store or {@code null} for none. */
  @Beta
  private final DataStore<StoredCredential> credentialDataStore;

  /** HTTP request initializer or {@code null} for none. */
  private final HttpRequestInitializer requestInitializer;

  /** Clock passed along to Credential. */
  private final Clock clock;

  /** Collection of scopes. */
  private final Collection<String> scopes;

  /** Credential created listener or {@code null} for none. */
  private final CredentialCreatedListener credentialCreatedListener;

  /** Refresh listeners provided by the client. */
  private final Collection<CredentialRefreshListener> refreshListeners;

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
    credentialDataStore = builder.credentialDataStore;
    scopes = Collections.unmodifiableCollection(builder.scopes);
    clock = Preconditions.checkNotNull(builder.clock);
    credentialCreatedListener = builder.credentialCreatedListener;
    refreshListeners = Collections.unmodifiableCollection(builder.refreshListeners);
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
  @SuppressWarnings("deprecation")
  public Credential createAndStoreCredential(TokenResponse response, String userId)
      throws IOException {
    Credential credential = newCredential(userId).setFromTokenResponse(response);
    if (credentialStore != null) {
      credentialStore.store(userId, credential);
    }
    if (credentialDataStore != null) {
      credentialDataStore.set(userId, new StoredCredential(credential));
    }
    if (credentialCreatedListener != null) {
      credentialCreatedListener.onCredentialCreated(credential, response);
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
  @SuppressWarnings("deprecation")
  public Credential loadCredential(String userId) throws IOException {
    if (credentialDataStore == null && credentialStore == null) {
      return null;
    }
    Credential credential = newCredential(userId);
    if (credentialDataStore != null) {
      StoredCredential stored = credentialDataStore.get(userId);
      if (stored == null) {
        return null;
      }
      credential.setAccessToken(stored.getAccessToken());
      credential.setRefreshToken(stored.getRefreshToken());
      credential.setExpirationTimeMilliseconds(stored.getExpirationTimeMilliseconds());
    } else if (!credentialStore.load(userId, credential)) {
      return null;
    }
    return credential;
  }

  /**
   * Returns a new credential instance based on the given user ID.
   *
   * @param userId user ID or {@code null} if not using a persisted credential store
   */
  @SuppressWarnings("deprecation")
  private Credential newCredential(String userId) {
    Credential.Builder builder = new Credential.Builder(method).setTransport(transport)
        .setJsonFactory(jsonFactory)
        .setTokenServerEncodedUrl(tokenServerEncodedUrl)
        .setClientAuthentication(clientAuthentication)
        .setRequestInitializer(requestInitializer)
        .setClock(clock);
    if (credentialDataStore != null) {
      builder.addRefreshListener(
          new DataStoreCredentialRefreshListener(userId, credentialDataStore));
    } else if (credentialStore != null) {
      builder.addRefreshListener(new CredentialStoreRefreshListener(userId, credentialStore));
    }
    builder.getRefreshListeners().addAll(refreshListeners);
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

  /**
   * {@link Beta} <br/>
   * Returns the credential persistence store or {@code null} for none.
   * @deprecated (to be removed in the future) Use {@link #getCredentialDataStore()} instead.
   */
  @Beta
  @Deprecated
  public final CredentialStore getCredentialStore() {
    return credentialStore;
  }

  /**
   * {@link Beta} <br/>
   * Returns the stored credential data store or {@code null} for none.
   *
   * @since 1.16
   */
  @Beta
  public final DataStore<StoredCredential> getCredentialDataStore() {
    return credentialDataStore;
  }

  /** Returns the HTTP request initializer or {@code null} for none. */
  public final HttpRequestInitializer getRequestInitializer() {
    return requestInitializer;
  }

  /**
   * Returns the space-separated list of scopes.
   *
   * @since 1.15
   */
  public final String getScopesAsString() {
    return Joiner.on(' ').join(scopes);
  }

  /** Returns the a collection of scopes. */
  public final Collection<String> getScopes() {
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
   * Returns the unmodifiable list of listeners for refresh token results.
   *
   * @since 1.15
   */
  public final Collection<CredentialRefreshListener> getRefreshListeners() {
    return refreshListeners;
  }

  /**
   * Listener for a created credential after a successful token response in
   * {@link #createAndStoreCredential}.
   *
   * @since 1.14
   */
  public interface CredentialCreatedListener {

    /**
     * Notifies of a created credential after a successful token response in
     * {@link #createAndStoreCredential}.
     *
     * <p>
     * Typical use is to parse additional fields from the credential created, such as an ID token.
     * </p>
     *
     * @param credential created credential
     * @param tokenResponse successful token response
     */
    void onCredentialCreated(Credential credential, TokenResponse tokenResponse) throws IOException;
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
    @Deprecated
    @Beta
    CredentialStore credentialStore;

    /** Stored credential data store or {@code null} for none. */
    @Beta
    DataStore<StoredCredential> credentialDataStore;

    /** HTTP request initializer or {@code null} for none. */
    HttpRequestInitializer requestInitializer;

    /** Collection of scopes. */
    Collection<String> scopes = Lists.newArrayList();

    /** Clock passed along to the Credential. */
    Clock clock = Clock.SYSTEM;

    /** Credential created listener or {@code null} for none. */
    CredentialCreatedListener credentialCreatedListener;

    /** Refresh listeners provided by the client. */
    Collection<CredentialRefreshListener> refreshListeners = Lists.newArrayList();

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

    /**
     * {@link Beta} <br/>
     * Returns the credential persistence store or {@code null} for none.
     * @deprecated (to be removed in the future) Use {@link #getCredentialDataStore()} instead.
     */
    @Beta
    @Deprecated
    public final CredentialStore getCredentialStore() {
      return credentialStore;
    }

    /**
     * {@link Beta} <br/>
     * Returns the stored credential data store or {@code null} for none.
     *
     * @since 1.16
     */
    @Beta
    public final DataStore<StoredCredential> getCredentialDataStore() {
      return credentialDataStore;
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
     * {@link Beta} <br/>
     * Sets the credential persistence store or {@code null} for none.
     *
     * <p>
     * Warning: not compatible with {@link #setDataStoreFactory} or {@link #setCredentialDataStore},
     * and if either of those is called before this method is called, this method will throw an
     * {@link IllegalArgumentException}.
     * </p>
     *
     * </p>
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @deprecated (to be removed in the future) Use
     *             {@link #setDataStoreFactory(DataStoreFactory)} or
     *             {@link #setCredentialDataStore(DataStore)} instead.
     */
    @Beta
    @Deprecated
    public Builder setCredentialStore(CredentialStore credentialStore) {
      Preconditions.checkArgument(credentialDataStore == null);
      this.credentialStore = credentialStore;
      return this;
    }

    /**
     * {@link Beta} <br/>
     * Sets the data store factory or {@code null} for none.
     *
     * <p>
     * Warning: not compatible with {@link #setCredentialStore}, and if it is called before this
     * method is called, this method will throw an {@link IllegalArgumentException}.
     * </p>
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @since 1.16
     */
    @Beta
    public Builder setDataStoreFactory(DataStoreFactory dataStoreFactory) throws IOException {
      return setCredentialDataStore(StoredCredential.getDefaultDataStore(dataStoreFactory));
    }

    /**
     * {@link Beta} <br/>
     * Sets the stored credential data store or {@code null} for none.
     *
     * <p>
     * Warning: not compatible with {@link #setCredentialStore}, and if it is called before this
     * method is called, this method will throw an {@link IllegalArgumentException}.
     * </p>
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @since 1.16
     */
    @Beta
    public Builder setCredentialDataStore(DataStore<StoredCredential> credentialDataStore) {
      Preconditions.checkArgument(credentialStore == null);
      this.credentialDataStore = credentialDataStore;
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
     * Sets the collection of scopes.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @param scopes collection of scopes
     * @since 1.15
     */
    public Builder setScopes(Collection<String> scopes) {
      this.scopes = Preconditions.checkNotNull(scopes);
      return this;
    }

    /** Returns a collection of scopes. */
    public final Collection<String> getScopes() {
      return scopes;
    }

    /**
     * Sets the credential created listener or {@code null} for none.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @since 1.14
     */
    public Builder setCredentialCreatedListener(
        CredentialCreatedListener credentialCreatedListener) {
      this.credentialCreatedListener = credentialCreatedListener;
      return this;
    }

    /**
     * Adds a listener for refresh token results.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @param refreshListener refresh listener
     * @since 1.15
     */
    public Builder addRefreshListener(CredentialRefreshListener refreshListener) {
      refreshListeners.add(Preconditions.checkNotNull(refreshListener));
      return this;
    }

    /**
     * Returns the listeners for refresh token results.
     *
     * @since 1.15
     */
    public final Collection<CredentialRefreshListener> getRefreshListeners() {
      return refreshListeners;
    }

    /**
     * Sets the listeners for refresh token results.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @since 1.15
     */
    public Builder setRefreshListeners(Collection<CredentialRefreshListener> refreshListeners) {
      this.refreshListeners = Preconditions.checkNotNull(refreshListeners);
      return this;
    }

    /**
     * Returns the credential created listener or {@code null} for none.
     *
     * @since 1.14
     */
    public final CredentialCreatedListener getCredentialCreatedListener() {
      return credentialCreatedListener;
    }
  }
}
