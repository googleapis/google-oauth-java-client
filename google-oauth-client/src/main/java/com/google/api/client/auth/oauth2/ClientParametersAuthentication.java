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
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.Map;

/**
 * Client credentials specified as URL-encoded parameters in the HTTP request body as specified in
 * <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-2.3.1">Client Password</a>
 * 
 * <p>
 * This implementation assumes that the {@link HttpRequest#getContent()} is {@code null} or
 * {@link UrlEncodedContent}.
 * </p>
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

  private final String clientId;
  private final String clientSecret;

  /**
   * @param clientId client identifier issued to the client during the registration process
   * @param clientSecret client secret
   */
  public ClientParametersAuthentication(String clientId, String clientSecret) {
    this.clientId = Preconditions.checkNotNull(clientId);
    this.clientSecret = Preconditions.checkNotNull(clientSecret);
  }

  public void initialize(HttpRequest request) throws IOException {
    request.setInterceptor(this);
  }

  public void intercept(HttpRequest request) throws IOException {
    Map<String, Object> data = Data.mapOf(UrlEncodedContent.getContent(request).getData());
    data.put("client_id", clientId);
    data.put("client_secret", clientSecret);
  }
}
