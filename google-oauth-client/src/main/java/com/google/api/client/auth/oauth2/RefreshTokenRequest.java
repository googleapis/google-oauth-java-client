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
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;

import java.util.Collection;

/**
 * OAuth 2.0 request to refresh an access token using a refresh token as specified in <a
 * href="http://tools.ietf.org/html/rfc6749#section-6">Refreshing an Access Token</a>.
 *
 * <p>
 * Use {@link Credential} to access protected resources from the resource server using the
 * {@link TokenResponse} returned by {@link #execute()}. On error, it will instead throw
 * {@link TokenResponseException}.
 * </p>
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
  static void refreshAccessToken() throws IOException {
    try {
      TokenResponse response =
          new RefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(), new GenericUrl(
              "https://server.example.com/token"), "tGzv3JOkF0XG5Qx2TlKWIA")
              .setClientAuthentication(
                  new BasicAuthentication("s6BhdRkqt3", "7Fjfp0ZBr1KtDRbnfVdmIw")).execute();
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
 * Some OAuth 2.0 providers don't support {@link BasicAuthentication} but instead support
 * {@link ClientParametersAuthentication}. In the above sample code, simply replace the class name
 * and it will work the same way.
 * </p>
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class RefreshTokenRequest extends TokenRequest {

  /** Refresh token issued to the client. */
  @Key("refresh_token")
  private String refreshToken;

  /**
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param tokenServerUrl token server URL
   * @param refreshToken refresh token issued to the client
   */
  public RefreshTokenRequest(HttpTransport transport, JsonFactory jsonFactory,
      GenericUrl tokenServerUrl, String refreshToken) {
    super(transport, jsonFactory, tokenServerUrl, "refresh_token");
    setRefreshToken(refreshToken);
  }

  @Override
  public RefreshTokenRequest setRequestInitializer(HttpRequestInitializer requestInitializer) {
    return (RefreshTokenRequest) super.setRequestInitializer(requestInitializer);
  }

  @Override
  public RefreshTokenRequest setTokenServerUrl(GenericUrl tokenServerUrl) {
    return (RefreshTokenRequest) super.setTokenServerUrl(tokenServerUrl);
  }

  @Override
  public RefreshTokenRequest setScopes(Collection<String> scopes) {
    return (RefreshTokenRequest) super.setScopes(scopes);
  }

  @Override
  public RefreshTokenRequest setGrantType(String grantType) {
    return (RefreshTokenRequest) super.setGrantType(grantType);
  }

  @Override
  public RefreshTokenRequest setClientAuthentication(HttpExecuteInterceptor clientAuthentication) {
    return (RefreshTokenRequest) super.setClientAuthentication(clientAuthentication);
  }

  /** Returns the refresh token issued to the client. */
  public final String getRefreshToken() {
    return refreshToken;
  }

  /**
   * Sets the refresh token issued to the client.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public RefreshTokenRequest setRefreshToken(String refreshToken) {
    this.refreshToken = Preconditions.checkNotNull(refreshToken);
    return this;
  }

  @Override
  public RefreshTokenRequest set(String fieldName, Object value) {
    return (RefreshTokenRequest) super.set(fieldName, value);
  }
}
