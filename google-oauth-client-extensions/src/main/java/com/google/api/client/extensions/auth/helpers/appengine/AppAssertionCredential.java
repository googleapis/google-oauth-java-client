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

package com.google.api.client.extensions.auth.helpers.appengine;

import com.google.api.client.auth.oauth2.draft10.AccessProtectedResource.Method;
import com.google.api.client.extensions.auth.helpers.Credential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Implementation of credentials that is used to communicate with resources managed on behalf of an
 * App Engine application.
 *
 * @author moshenko@google.com (Jake Moshenko)
 *
 * @since 1.5
 */
@PersistenceCapable
public final class AppAssertionCredential implements Credential {

  static final Logger LOGGER = Logger.getLogger(AppAssertionCredential.class.getName());

  private static final Method AUTHORIZATION_METHOD = Method.AUTHORIZATION_HEADER;

  /**
   * Primary key for this data object, this is usually the name of the app engine application.
   */
  @SuppressWarnings("unused")
  @PrimaryKey
  private String applicationName;

  /**
   * Access token to use for OAuth2 authenticated requests or {@code null} for none.
   */
  @Persistent
  private String accessToken;

  /**
   * Server with which you can exchange an assertion for an access token.
   */
  @Persistent
  private String authorizationServerUrl;

  /**
   * Scope(s) for which this credential provides access.
   */
  @Persistent
  private String scope;

  /**
   * Audience to be used in the json web token. For Google services this is always the
   * authorizationServerUrl.
   */
  @Persistent
  private String audience;

  @NotPersistent
  private AccessAppResource authInterceptor;

  /**
   * Create an instance of this class. This will only set up on the object, and a call to
   * {@link #postConstruct} is required before it can be used.
   *
   * @param applicationName Primary key for this data object in the data store. It is usually the
   *        robot name.
   * @param authorizationServerUrl Server with which we can exchange an assertion for an access
   *        token.
   * @param scope Scope(s) for which this credential will request access.
   * @param audience Audience to be used in the JSON web token.
   */
  public AppAssertionCredential(
      String applicationName, String authorizationServerUrl, String scope, String audience) {
    this.applicationName = applicationName;
    this.authorizationServerUrl = authorizationServerUrl;
    this.scope = scope;
    this.audience = audience;
  }

  /**
   * Set the access token to a new value.
   *
   * @param accessToken New access token or {@code null} for none
   */
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  /**
   * Finish building the object. This should be called whether the object is loaded using JDO or
   * created using the constructor.
   *
   * @param transport {@link HttpTransport} instance to use to access the network.
   * @param jsonFactory {@link JsonFactory} instance to use to serialize and deserialize auth server
   *        communications.
   * @throws IOException Thrown when we are unable to set up access token communications.
   */
  public void postConstruct(HttpTransport transport, JsonFactory jsonFactory) throws IOException {
    authInterceptor = new AccessAppResource(accessToken,
        AUTHORIZATION_METHOD,
        transport,
        jsonFactory,
        authorizationServerUrl,
        scope,
        audience) {

      @Override
      protected void onAccessToken(String accessToken) {
        AppAssertionCredential.this.setAccessToken(accessToken);
      }
    };
  }

  public void initialize(HttpRequest request) throws IOException {
    checkIntializationStatus();

    request.setInterceptor(authInterceptor);
    request.setUnsuccessfulResponseHandler(authInterceptor);
  }

  public void intercept(HttpRequest request) throws IOException {
    checkIntializationStatus();

    authInterceptor.intercept(request);
  }

  public boolean handleResponse(HttpRequest request, HttpResponse response, boolean retrySupported)
      throws IOException {
    checkIntializationStatus();

    return authInterceptor.handleResponse(request, response, retrySupported);
  }

  private void checkIntializationStatus() {
    Preconditions.checkNotNull(authInterceptor, "Please call postConstruct before using.");
  }

  public boolean isInvalid() {
    return accessToken == null;
  }
}
