/*
 * Copyright (c) 2013 Google Inc.
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
 * OAuth 2.0 request for an access token using the user's username and password as specified in <a
 * href="http://tools.ietf.org/html/rfc6749#section-4.3">Resource Owner Password Credentials
 * Grant</a>.
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
  static void requestAccessToken() throws IOException {
    try {
      TokenResponse response =
          new PasswordTokenRequest(new NetHttpTransport(), new JacksonFactory(),
              new GenericUrl("https://server.example.com/token"), "johndoe", "A3ddj3w")
              .setRedirectUri("https://client.example.com/rd")
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
 * @since 1.14
 * @author Yaniv Inbar
 */
public class PasswordTokenRequest extends TokenRequest {

  /** Resource owner username. */
  @Key
  private String username;

  /** Resource owner password. */
  @Key
  private String password;

  /**
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param tokenServerUrl token server URL
   * @param username resource owner username
   * @param password resource owner password
   */
  public PasswordTokenRequest(HttpTransport transport, JsonFactory jsonFactory,
      GenericUrl tokenServerUrl, String username, String password) {
    super(transport, jsonFactory, tokenServerUrl, "password");
    setUsername(username);
    setPassword(password);
  }

  @Override
  public PasswordTokenRequest setRequestInitializer(HttpRequestInitializer requestInitializer) {
    return (PasswordTokenRequest) super.setRequestInitializer(requestInitializer);
  }

  @Override
  public PasswordTokenRequest setTokenServerUrl(GenericUrl tokenServerUrl) {
    return (PasswordTokenRequest) super.setTokenServerUrl(tokenServerUrl);
  }

  @Override
  public PasswordTokenRequest setScopes(Collection<String> scopes) {
    return (PasswordTokenRequest) super.setScopes(scopes);
  }

  @Override
  public PasswordTokenRequest setGrantType(String grantType) {
    return (PasswordTokenRequest) super.setGrantType(grantType);
  }

  @Override
  public PasswordTokenRequest setClientAuthentication(HttpExecuteInterceptor clientAuthentication) {
    return (PasswordTokenRequest) super.setClientAuthentication(clientAuthentication);
  }

  /** Returns the resource owner username. */
  public final String getUsername() {
    return username;
  }

  /**
   * Sets the resource owner username.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public PasswordTokenRequest setUsername(String username) {
    this.username = Preconditions.checkNotNull(username);
    return this;
  }

  /** Returns the resource owner password. */
  public final String getPassword() {
    return password;
  }

  /**
   * Sets the resource owner password.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public PasswordTokenRequest setPassword(String password) {
    this.password = Preconditions.checkNotNull(password);
    return this;
  }

  @Override
  public PasswordTokenRequest set(String fieldName, Object value) {
    return (PasswordTokenRequest) super.set(fieldName, value);
  }
}
