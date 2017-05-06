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
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Clock;
import com.google.api.client.util.Lists;
import com.google.api.client.util.Objects;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStoreFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread-safe OAuth 2.0 helper for accessing protected resources using an access token, as well as
 * optionally refreshing the access token when it expires using a refresh token.
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
  public static Credential createCredentialWithAccessTokenOnly(
      HttpTransport transport, JsonFactory jsonFactory, TokenResponse tokenResponse) {
    return new Credential(BearerToken.authorizationHeaderAccessMethod()).setFromTokenResponse(
        tokenResponse);
  }

  public static Credential createCredentialWithRefreshToken(
      HttpTransport transport, JsonFactory jsonFactory, TokenResponse tokenResponse) {
    return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod()).setTransport(
        transport)
        .setJsonFactory(jsonFactory)
        .setTokenServerUrl(
            new GenericUrl("https://server.example.com/token"))
        .setClientAuthentication(new BasicAuthentication("s6BhdRkqt3", "7Fjfp0ZBr1KtDRbnfVdmIw"))
        .build()
        .setFromTokenResponse(tokenResponse);
  }
 * </pre>
 *
 * <p>
 * If you need to persist the access token in a data store, use {@link DataStoreFactory} and
 * {@link Builder#addRefreshListener(CredentialRefreshListener)} with
 * {@link DataStoreCredentialRefreshListener}.
 * </p>
 *
 * <p>
 * If you have a custom request initializer, request execute interceptor, or unsuccessful response
 * handler, take a look at the sample usage for {@link HttpExecuteInterceptor} and
 * {@link HttpUnsuccessfulResponseHandler}, which are interfaces that this class also implements.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class Credential
    implements
      HttpExecuteInterceptor,
      HttpRequestInitializer,
      HttpUnsuccessfulResponseHandler {

  static final Logger LOGGER = Logger.getLogger(Credential.class.getName());

  /**
   * Method of presenting the access token to the resource server as specified in <a
   * href="http://tools.ietf.org/html/rfc6749#section-7">Accessing Protected Resources</a>.
   */
  public interface AccessMethod {

    /**
     * Intercept the HTTP request during {@link Credential#intercept(HttpRequest)} right before the
     * HTTP request executes by providing the access token.
     *
     * @param request HTTP request
     * @param accessToken access token
     */
    void intercept(HttpRequest request, String accessToken) throws IOException;

    /**
     * Retrieve the original access token in the HTTP request, as provided in
     * {@link #intercept(HttpRequest, String)}.
     *
     * @param request HTTP request
     * @return original access token or {@code null} for none
     */
    String getAccessTokenFromRequest(HttpRequest request);
  }

  /** Lock on the token response information. */
  private final Lock lock = new ReentrantLock();

  /**
   * Method of presenting the access token to the resource server (for example
   * {@link BearerToken.AuthorizationHeaderAccessMethod}).
   */
  private final AccessMethod method;

  /** Clock used to provide the currentMillis. */
  private final Clock clock;

  /** Access token issued by the authorization server. */
  private String accessToken;

  /**
   * Expected expiration time in milliseconds based on {@link #setExpiresInSeconds} or {@code null}
   * for none.
   */
  private Long expirationTimeMilliseconds;

  /**
   * Refresh token which can be used to obtain new access tokens using the same authorization grant
   * or {@code null} for none.
   */
  private String refreshToken;

  /** HTTP transport for executing refresh token request or {@code null} for none. */
  private final HttpTransport transport;

  /** Client authentication or {@code null} for none. */
  private final HttpExecuteInterceptor clientAuthentication;

  /**
   * JSON factory to use for parsing response for refresh token request or {@code null} for none.
   */
  private final JsonFactory jsonFactory;

  /** Encoded token server URL or {@code null} for none. */
  private final String tokenServerEncodedUrl;

  /** Unmodifiable collection of listeners for refresh token results. */
  private final Collection<CredentialRefreshListener> refreshListeners;

  /**
   * HTTP request initializer for refresh token requests to the token server or {@code null} for
   * none.
   */
  private final HttpRequestInitializer requestInitializer;

  /**
   * Constructor with the ability to access protected resources, but not refresh tokens.
   *
   * <p>
   * To use with the ability to refresh tokens, use {@link Builder}.
   * </p>
   *
   * @param method method of presenting the access token to the resource server (for example
   *        {@link BearerToken.AuthorizationHeaderAccessMethod})
   */
  public Credential(AccessMethod method) {
    this(new Builder(method));
  }

  /**
   * @param builder credential builder
   *
   * @since 1.14
   */
  protected Credential(Builder builder) {
    method = Preconditions.checkNotNull(builder.method);
    transport = builder.transport;
    jsonFactory = builder.jsonFactory;
    tokenServerEncodedUrl = builder.tokenServerUrl == null ? null : builder.tokenServerUrl.build();
    clientAuthentication = builder.clientAuthentication;
    requestInitializer = builder.requestInitializer;
    refreshListeners = Collections.unmodifiableCollection(builder.refreshListeners);
    clock = Preconditions.checkNotNull(builder.clock);
  }

  /**
   * {@inheritDoc}
   * <p>
   * Default implementation is to try to refresh the access token if there is no access token or if
   * we are 1 minute away from expiration. If token server is unavailable, it will try to use the
   * access token even if has expired. If a 4xx error is encountered while refreshing the token,
   * {@link TokenResponseException} is thrown. If successful, it will call {@link #getMethod()} and
   * {@link AccessMethod#intercept}.
   * </p>
   *
   * <p>
   * Subclasses may override.
   * </p>
   */
  public void intercept(HttpRequest request) throws IOException {
    lock.lock();
    try {
      Long expiresIn = getExpiresInSeconds();
      // check if token will expire in a minute
      if (accessToken == null || expiresIn != null && expiresIn <= 60) {
        refreshToken();
        if (accessToken == null) {
          // nothing we can do without an access token
          return;
        }
      }
      method.intercept(request, accessToken);
    } finally {
      lock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * Default implementation checks if {@code WWW-Authenticate} exists and contains a "Bearer" value
   * (see <a href="http://tools.ietf.org/html/rfc6750#section-3.1">rfc6750 section 3.1</a> for more
   * details). If so, it calls {@link #refreshToken} in case the error code contains
   * {@code invalid_token}. If there is no "Bearer" in {@code WWW-Authenticate} and the status code
   * is {@link HttpStatusCodes#STATUS_CODE_UNAUTHORIZED} it calls {@link #refreshToken}. If
   * {@link #executeRefreshToken()} throws an I/O exception, this implementation will log the
   * exception and return {@code false}. Subclasses may override.
   * </p>
   */
  public boolean handleResponse(HttpRequest request, HttpResponse response, boolean supportsRetry) {
    boolean refreshToken = false;
    boolean bearer = false;

    List<String> authenticateList = response.getHeaders().getAuthenticateAsList();

    // TODO(peleyal): this logic should be implemented as a pluggable interface, in the same way we
    // implement different AccessMethods

    // if authenticate list is not null we will check if one of the entries contains "Bearer"
    if (authenticateList != null) {
      for (String authenticate : authenticateList) {
        if (authenticate.startsWith(BearerToken.AuthorizationHeaderAccessMethod.HEADER_PREFIX)) {
          // mark that we found a "Bearer" value, and check if there is a invalid_token error
          bearer = true;
          refreshToken = BearerToken.INVALID_TOKEN_ERROR.matcher(authenticate).find();
          break;
        }
      }
    }

    // if "Bearer" wasn't found, we will refresh the token, if we got 401
    if (!bearer) {
      refreshToken = response.getStatusCode() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED;
    }

    if (refreshToken) {
      try {
        lock.lock();
        try {
          // need to check if another thread has already refreshed the token
          return !Objects.equal(accessToken, method.getAccessTokenFromRequest(request))
              || refreshToken();
        } finally {
          lock.unlock();
        }
      } catch (IOException exception) {
        LOGGER.log(Level.SEVERE, "unable to refresh token", exception);
      }
    }
    return false;
  }

  public void initialize(HttpRequest request) throws IOException {
    request.setInterceptor(this);
    request.setUnsuccessfulResponseHandler(this);
  }

  /** Returns the access token or {@code null} for none. */
  public final String getAccessToken() {
    lock.lock();
    try {
      return accessToken;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Sets the access token.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   *
   * @param accessToken access token or {@code null} for none
   */
  public Credential setAccessToken(String accessToken) {
    lock.lock();
    try {
      this.accessToken = accessToken;
    } finally {
      lock.unlock();
    }
    return this;
  }

  /**
   * Return the method of presenting the access token to the resource server (for example
   * {@link BearerToken.AuthorizationHeaderAccessMethod}).
   */
  public final AccessMethod getMethod() {
    return method;
  }

  /**
   * Returns the clock used for expiration checks by this Credential. Mostly used for unit-testing.
   * @since 1.9
   */
  public final Clock getClock() {
    return clock;
  }

  /** Return the HTTP transport for executing refresh token request or {@code null} for none. */
  public final HttpTransport getTransport() {
    return transport;
  }

  /**
   * Returns the JSON factory to use for parsing response for refresh token request or {@code null}
   * for none.
   */
  public final JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /** Returns the encoded authorization server URL or {@code null} for none. */
  public final String getTokenServerEncodedUrl() {
    return tokenServerEncodedUrl;
  }

  /**
   * Returns the refresh token associated with the access token to be refreshed or {@code null} for
   * none.
   */
  public final String getRefreshToken() {
    lock.lock();
    try {
      return refreshToken;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Sets the refresh token.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   *
   * @param refreshToken refresh token or {@code null} for none
   */
  public Credential setRefreshToken(String refreshToken) {
    lock.lock();
    try {
      if (refreshToken != null) {
        Preconditions.checkArgument(jsonFactory != null && transport != null
            && clientAuthentication != null && tokenServerEncodedUrl != null,
            "Please use the Builder and call setJsonFactory, setTransport, setClientAuthentication"
            + " and setTokenServerUrl/setTokenServerEncodedUrl");
      }
      this.refreshToken = refreshToken;
    } finally {
      lock.unlock();
    }
    return this;
  }

  /** Expected expiration time in milliseconds or {@code null} for none. */
  public final Long getExpirationTimeMilliseconds() {
    lock.lock();
    try {
      return expirationTimeMilliseconds;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Sets the expected expiration time in milliseconds or {@code null} for none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public Credential setExpirationTimeMilliseconds(Long expirationTimeMilliseconds) {
    lock.lock();
    try {
      this.expirationTimeMilliseconds = expirationTimeMilliseconds;
    } finally {
      lock.unlock();
    }
    return this;
  }

  /**
   * Returns the remaining lifetime in seconds of the access token (for example 3600 for an hour, or
   * -3600 if expired an hour ago) or {@code null} if unknown.
   */
  public final Long getExpiresInSeconds() {
    lock.lock();
    try {
      if (expirationTimeMilliseconds == null) {
        return null;
      }
      return (expirationTimeMilliseconds - clock.currentTimeMillis()) / 1000;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Sets the lifetime in seconds of the access token (for example 3600 for an hour) or {@code null}
   * for none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   *
   * @param expiresIn lifetime in seconds of the access token (for example 3600 for an hour) or
   *        {@code null} for none
   */
  public Credential setExpiresInSeconds(Long expiresIn) {
    return setExpirationTimeMilliseconds(
        expiresIn == null ? null : clock.currentTimeMillis() + expiresIn * 1000);
  }

  /** Returns the client authentication or {@code null} for none. */
  public final HttpExecuteInterceptor getClientAuthentication() {
    return clientAuthentication;
  }

  /**
   * Returns the HTTP request initializer for refresh token requests to the token server or
   * {@code null} for none.
   */
  public final HttpRequestInitializer getRequestInitializer() {
    return requestInitializer;
  }

  /**
   * Request a new access token from the authorization endpoint.
   *
   * <p>
   * On success, it will call {@link #setFromTokenResponse(TokenResponse)}, call
   * {@link CredentialRefreshListener#onTokenResponse} with the token response, and return
   * {@code true}. On error, it will call {@link #setAccessToken(String)} and
   * {@link #setExpiresInSeconds(Long)} with {@code null}, call
   * {@link CredentialRefreshListener#onTokenErrorResponse} with the token error response, and
   * return {@code false}. If a 4xx error is encountered while refreshing the token,
   * {@link TokenResponseException} is thrown.
   * </p>
   *
   * <p>
   * If there is no refresh token, it will quietly return {@code false}.
   * </p>
   *
   * @return whether a new access token was successfully retrieved
   */
  public final boolean refreshToken() throws IOException {
    lock.lock();
    try {
      try {
        TokenResponse tokenResponse = executeRefreshToken();
        if (tokenResponse != null) {
          setFromTokenResponse(tokenResponse);
          for (CredentialRefreshListener refreshListener : refreshListeners) {
            refreshListener.onTokenResponse(this, tokenResponse);
          }
          return true;
        }
      } catch (TokenResponseException e) {
        boolean statusCode4xx = 400 <= e.getStatusCode() && e.getStatusCode() < 500;
        // check if it is a normal error response
        if (e.getDetails() != null && statusCode4xx) {
          // We were unable to get a new access token (e.g. it may have been revoked), we must now
          // indicate that our current token is invalid.
          setAccessToken(null);
          setExpiresInSeconds(null);
        }
        for (CredentialRefreshListener refreshListener : refreshListeners) {
          refreshListener.onTokenErrorResponse(this, e.getDetails());
        }
        if (statusCode4xx) {
          throw e;
        }
      }
      return false;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Sets the {@link #setAccessToken access token}, {@link #setRefreshToken refresh token} (if
   * available), and {@link #setExpiresInSeconds expires-in time} based on the values from the token
   * response.
   *
   * <p>
   * It does not call the refresh listeners.
   * </p>
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   *
   * @param tokenResponse successful token response
   */
  public Credential setFromTokenResponse(TokenResponse tokenResponse) {
    setAccessToken(tokenResponse.getAccessToken());
    // handle case of having a refresh token previous, but no refresh token in current
    // response
    if (tokenResponse.getRefreshToken() != null) {
      setRefreshToken(tokenResponse.getRefreshToken());
    }
    setExpiresInSeconds(tokenResponse.getExpiresInSeconds());
    return this;
  }

  /**
   * Executes a request for new credentials from the token server.
   *
   * <p>
   * The default implementation calls {@link RefreshTokenRequest#execute()} using the
   * {@link #getTransport()}, {@link #getJsonFactory()}, {@link #getRequestInitializer()},
   * {@link #getTokenServerEncodedUrl()}, {@link #getRefreshToken()}, and the
   * {@link #getClientAuthentication()}. If {@link #getRefreshToken()} is {@code null}, it instead
   * returns {@code null}.
   * </p>
   *
   * <p>
   * Subclasses may override for a different implementation. Implementations can assume proper
   * thread synchronization is already taken care of inside {@link #refreshToken()}.
   * </p>
   *
   * @return successful response from the token server or {@code null} if it is not possible to
   *         refresh the access token
   * @throws TokenResponseException if an error response was received from the token server
   */
  protected TokenResponse executeRefreshToken() throws IOException {
    if (refreshToken == null) {
      return null;
    }
    return new RefreshTokenRequest(transport, jsonFactory, new GenericUrl(tokenServerEncodedUrl),
        refreshToken).setClientAuthentication(clientAuthentication)
        .setRequestInitializer(requestInitializer).execute();
  }

  /** Returns the unmodifiable collection of listeners for refresh token results. */
  public final Collection<CredentialRefreshListener> getRefreshListeners() {
    return refreshListeners;
  }

  /**
   * Credential builder.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   */
  public static class Builder {

    /**
     * Method of presenting the access token to the resource server (for example
     * {@link BearerToken.AuthorizationHeaderAccessMethod}).
     */
    final AccessMethod method;

    /**
     * HTTP transport for executing refresh token request or {@code null} if not refreshing tokens.
     */
    HttpTransport transport;

    /**
     * JSON factory to use for parsing response for refresh token request or {@code null} if not
     * refreshing tokens.
     */
    JsonFactory jsonFactory;

    /** Token server URL or {@code null} if not refreshing tokens. */
    GenericUrl tokenServerUrl;

    /** Clock used for expiration checks. */
    Clock clock = Clock.SYSTEM;

    /**
     * Client authentication or {@code null} for none (see
     * {@link TokenRequest#setClientAuthentication(HttpExecuteInterceptor)}).
     */
    HttpExecuteInterceptor clientAuthentication;

    /**
     * HTTP request initializer for refresh token requests to the token server or {@code null} for
     * none.
     */
    HttpRequestInitializer requestInitializer;

    /** Listeners for refresh token results. */
    Collection<CredentialRefreshListener> refreshListeners = Lists.newArrayList();

    /**
     * @param method method of presenting the access token to the resource server (for example
     *        {@link BearerToken.AuthorizationHeaderAccessMethod})
     */
    public Builder(AccessMethod method) {
      this.method = Preconditions.checkNotNull(method);
    }

    /** Returns a new credential instance. */
    public Credential build() {
      return new Credential(this);
    }

    /**
     * Returns the method of presenting the access token to the resource server (for example
     * {@link BearerToken.AuthorizationHeaderAccessMethod}).
     */
    public final AccessMethod getMethod() {
      return method;
    }

    /**
     * Returns the HTTP transport for executing refresh token request or {@code null} if not
     * refreshing tokens.
     */
    public final HttpTransport getTransport() {
      return transport;
    }

    /**
     * Sets the HTTP transport for executing refresh token request or {@code null} if not refreshing
     * tokens.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Builder setTransport(HttpTransport transport) {
      this.transport = transport;
      return this;
    }

    /**
     * Returns the clock to use for expiration checks or {@link Clock#SYSTEM} as default.
     * @since 1.9
     */
    public final Clock getClock() {
      return clock;
    }

    /**
     * Sets the clock to use for expiration checks.
     *
     * <p>
     * The default value is Clock.SYSTEM.
     * </p>
     *
     * @since 1.9
     */
    public Builder setClock(Clock clock) {
      this.clock = Preconditions.checkNotNull(clock);
      return this;
    }

    /**
     * Returns the JSON factory to use for parsing response for refresh token request or
     * {@code null} if not refreshing tokens.
     */
    public final JsonFactory getJsonFactory() {
      return jsonFactory;
    }

    /**
     * Sets the JSON factory to use for parsing response for refresh token request or {@code null}
     * if not refreshing tokens.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Builder setJsonFactory(JsonFactory jsonFactory) {
      this.jsonFactory = jsonFactory;
      return this;
    }

    /** Returns the token server URL or {@code null} if not refreshing tokens. */
    public final GenericUrl getTokenServerUrl() {
      return tokenServerUrl;
    }

    /**
     * Sets the token server URL or {@code null} if not refreshing tokens.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Builder setTokenServerUrl(GenericUrl tokenServerUrl) {
      this.tokenServerUrl = tokenServerUrl;
      return this;
    }

    /**
     * Sets the encoded token server URL or {@code null} if not refreshing tokens.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     */
    public Builder setTokenServerEncodedUrl(String tokenServerEncodedUrl) {
      this.tokenServerUrl =
          tokenServerEncodedUrl == null ? null : new GenericUrl(tokenServerEncodedUrl);
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
     */
    public Builder setClientAuthentication(HttpExecuteInterceptor clientAuthentication) {
      this.clientAuthentication = clientAuthentication;
      return this;
    }

    /**
     * Returns the HTTP request initializer for refresh token requests to the token server or
     * {@code null} for none.
     */
    public final HttpRequestInitializer getRequestInitializer() {
      return requestInitializer;
    }

    /**
     * Sets the HTTP request initializer for refresh token requests to the token server or
     * {@code null} for none.
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
     * Adds a listener for refresh token results.
     *
     * <p>
     * Overriding is only supported for the purpose of calling the super implementation and changing
     * the return type, but nothing else.
     * </p>
     *
     * @param refreshListener refresh listener
     */
    public Builder addRefreshListener(CredentialRefreshListener refreshListener) {
      refreshListeners.add(Preconditions.checkNotNull(refreshListener));
      return this;
    }

    /** Returns the listeners for refresh token results. */
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
     */
    public Builder setRefreshListeners(Collection<CredentialRefreshListener> refreshListeners) {
      this.refreshListeners = Preconditions.checkNotNull(refreshListeners);
      return this;
    }
  }
}
