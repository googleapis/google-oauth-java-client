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

import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.util.Data;
import com.google.api.client.util.Preconditions;

import java.io.IOException;
import java.util.Map;

/**
 * Client credentials specified as URL-encoded parameters in the HTTP request body as specified in
 * <a href="http://tools.ietf.org/html/rfc6749#section-2.3.1">Client Password</a>
 *
 * <p>
 * This implementation assumes that the {@link HttpRequest#getContent()} is {@code null} or an
 * instance of {@link UrlEncodedContent}. This is used as the client authentication in
 * {@link TokenRequest#setClientAuthentication(HttpExecuteInterceptor)}.
 * </p>
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
  static void requestAccessToken() throws IOException {
    try {
      TokenResponse response = new AuthorizationCodeTokenRequest(new NetHttpTransport(),
          new JacksonFactory(), new GenericUrl("https://server.example.com/token"),
          "SplxlOBeZQQYbYS6WxSbIA").setRedirectUri("https://client.example.com/rd")
          .setClientAuthentication(
              new ClientParametersAuthentication("s6BhdRkqt3", "7Fjfp0ZBr1KtDRbnfVdmIw")).execute();
      System.out.println("Access token: " + response.getAccessToken());
    } catch (TokenResponseException e) {
      if (e.getDetails() != null) {
        System.err.println("Error: " + e.getDetails().getError());
        if (e.getDetails().getErrorDescription() != null) {
          System.err.println(e.getDetails().getErrorDescription());
        }
        if (e.getDetails().getErrorUri() != null) {
          System.err.println(e.getDetails().getErrorUri());
        }
      } else {
        System.err.println(e.getMessage());
      }
    }
  }
 * </pre>
 *
 * <p>
 * Implementation is immutable and thread-safe.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class ClientParametersAuthentication
    implements
      HttpRequestInitializer,
      HttpExecuteInterceptor {

  /** Client identifier issued to the client during the registration process. */
  private final String clientId;

  /** Client secret or {@code null} for none. */
  private final String clientSecret;

  /**
   * @param clientId client identifier issued to the client during the registration process
   * @param clientSecret client secret or {@code null} for none
   */
  public ClientParametersAuthentication(String clientId, String clientSecret) {
    this.clientId = Preconditions.checkNotNull(clientId);
    this.clientSecret = clientSecret;
  }

  public void initialize(HttpRequest request) throws IOException {
    request.setInterceptor(this);
  }

  public void intercept(HttpRequest request) throws IOException {
    Map<String, Object> data = Data.mapOf(UrlEncodedContent.getContent(request).getData());
    data.put("client_id", clientId);
    if (clientSecret != null) {
      data.put("client_secret", clientSecret);
    }
  }

  /** Returns the client identifier issued to the client during the registration process. */
  public final String getClientId() {
    return clientId;
  }

  /** Returns the client secret or {@code null} for none. */
  public final String getClientSecret() {
    return clientSecret;
  }
}
