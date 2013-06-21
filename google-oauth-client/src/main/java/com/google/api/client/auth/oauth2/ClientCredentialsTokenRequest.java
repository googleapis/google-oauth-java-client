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

import java.util.Collection;

/**
 * OAuth 2.0 request for an access token using only its client credentials as specified in <a
 * href="http://tools.ietf.org/html/rfc6749#section-4.4">Client Credentials Grant</a>.
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
          new ClientCredentialsTokenRequest(new NetHttpTransport(), new JacksonFactory(),
              new GenericUrl("https://server.example.com/token"))
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
public class ClientCredentialsTokenRequest extends TokenRequest {

  /**
   * @param transport HTTP transport
   * @param jsonFactory JSON factory
   * @param tokenServerUrl token server URL
   */
  public ClientCredentialsTokenRequest(
      HttpTransport transport, JsonFactory jsonFactory, GenericUrl tokenServerUrl) {
    super(transport, jsonFactory, tokenServerUrl, "client_credentials");
  }

  @Override
  public ClientCredentialsTokenRequest setRequestInitializer(
      HttpRequestInitializer requestInitializer) {
    return (ClientCredentialsTokenRequest) super.setRequestInitializer(requestInitializer);
  }

  @Override
  public ClientCredentialsTokenRequest setTokenServerUrl(GenericUrl tokenServerUrl) {
    return (ClientCredentialsTokenRequest) super.setTokenServerUrl(tokenServerUrl);
  }

  @Override
  public ClientCredentialsTokenRequest setScopes(Collection<String> scopes) {
    return (ClientCredentialsTokenRequest) super.setScopes(scopes);
  }

  @Override
  public ClientCredentialsTokenRequest setGrantType(String grantType) {
    return (ClientCredentialsTokenRequest) super.setGrantType(grantType);
  }

  @Override
  public ClientCredentialsTokenRequest setClientAuthentication(
      HttpExecuteInterceptor clientAuthentication) {
    return (ClientCredentialsTokenRequest) super.setClientAuthentication(clientAuthentication);
  }

  @Override
  public ClientCredentialsTokenRequest set(String fieldName, Object value) {
    return (ClientCredentialsTokenRequest) super.set(fieldName, value);
  }
}
