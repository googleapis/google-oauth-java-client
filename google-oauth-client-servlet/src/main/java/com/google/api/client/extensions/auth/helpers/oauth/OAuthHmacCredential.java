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

package com.google.api.client.extensions.auth.helpers.oauth;

import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.extensions.auth.helpers.Credential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpStatusCodes;
import com.google.api.client.util.Beta;

import java.io.IOException;

import javax.jdo.InstanceCallbacks;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * {@link Beta} <br/>
 * OAuth 1 credential which uses the {@link OAuthHmacSigner} to sign requests.
 *
 * This class is both immutable and thread safe.
 *
 * @author moshenko@google.com (Jacob Moshenko)
 * @since 1.5
 */
@PersistenceCapable
@Beta
public final class OAuthHmacCredential implements Credential, InstanceCallbacks {

  /**
   * Primary key that will be used to store and retrieve this credential. Usually the user id of the
   * logged in user.
   */
  // We need this annotation because the userId is used by JDO but not the end user code
  @SuppressWarnings("unused")
  @PrimaryKey
  private String userId;

  /**
   * Key that identifies the server to the service provider.
   */
  @Persistent
  private String consumerKey;

  /**
   * Secret that the server shares with the service provider.
   */
  @Persistent
  private String sharedSecret;

  /**
   * Token secret that server uses to authenticate the requests.
   */
  @Persistent
  private String tokenSharedSecret;

  /**
   * Token that has been authorized by the end user to allow the server to access the resources or
   * {@code null} for none.
   */
  @Persistent
  private String token;

  /**
   * Authorizer instance used to sign requests.
   */
  @NotPersistent
  private OAuthParameters authorizer;

  /**
   * Create an OAuth 1 credential object from information obtained from the server.
   *
   * @param userId User ID key that can be used to associate this credential with a user.
   * @param consumerKey Key that identifies the server to the service provider.
   * @param sharedSecret Secret that the server shares with the service provider.
   * @param tokenSharedSecret Token secret that server uses to authenticate the requests.
   * @param token Token that has been authorized by the end user to allow the server to access the
   *        resources or {@code null} for none
   */
  public OAuthHmacCredential(String userId, String consumerKey, String sharedSecret,
      String tokenSharedSecret, String token) {
    this.userId = userId;
    this.consumerKey = consumerKey;
    this.sharedSecret = sharedSecret;
    this.tokenSharedSecret = tokenSharedSecret;
    this.token = token;

    postConstruct();
  }

  private void postConstruct() {
    OAuthHmacSigner signer = new OAuthHmacSigner();
    signer.clientSharedSecret = sharedSecret;
    signer.tokenSharedSecret = tokenSharedSecret;

    authorizer = new OAuthParameters();
    authorizer.consumerKey = consumerKey;
    authorizer.signer = signer;
    authorizer.token = token;
  }

  public void initialize(HttpRequest request) throws IOException {
    authorizer.initialize(request);
    request.setUnsuccessfulResponseHandler(this);
  }

  public void intercept(HttpRequest request) throws IOException {
    authorizer.intercept(request);
  }

  public boolean handleResponse(
      HttpRequest request, HttpResponse response, boolean retrySupported) {
    if (response.getStatusCode() == HttpStatusCodes.STATUS_CODE_UNAUTHORIZED) {
      // If the token was revoked, we must mark our credential as invalid
      token = null;
    }

    // We didn't do anything to fix the problem
    return false;
  }

  public void jdoPreClear() {
    // Intentionally blank
  }

  public void jdoPreDelete() {
    // Intentionally blank
  }

  public void jdoPostLoad() {
    postConstruct();
  }

  public void jdoPreStore() {
    // Intentionally blank
  }

  public boolean isInvalid() {
    return token == null;
  }
}
