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

package com.google.api.client.extensions.appengine.auth.oauth2;

import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.extensions.servlet.auth.oauth2.ServletOAuthContext;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Beta;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.appengine.api.users.UserServiceFactory;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

/**
 * {@link Beta} <br/>
 * Google App Engine OAuth context which extends {@link ServletOAuthContext} and defines
 * {@link AppEngineDataStoreFactory#getDefaultInstance()} as the default data store factory,
 * {@link UrlFetchTransport#getDefaultInstance} as the transport layer and
 * {@link JacksonFactory#getDefaultInstance} as the JSON factory.
 *
 * @author Nick Miceli
 * @author Eyal Peled
 * @since 1.18
 */
@Beta
public abstract class AppEngineOAuthContext extends ServletOAuthContext {

  /**
   * Constructs a new App Engine OAuth context
   *
   * @param redirectUri Redirect URI
   * @param scopes Scopes
   * @param applicationName application name
   */
  public AppEngineOAuthContext(
      String redirectUri, Collection<String> scopes, String applicationName) {
    super(redirectUri, scopes, applicationName);
  }

  /** Returns the HTTP transport. The default is {@link UrlFetchTransport#getDefaultInstance}. */
  @Override
  public HttpTransport getTransport() {
    return UrlFetchTransport.getDefaultInstance();
  }

  /** Returns the JSON factory. The default is {@link JacksonFactory#getDefaultInstance}. */
  @Override
  public JsonFactory getJsonFactory() {
    return JacksonFactory.getDefaultInstance();
  }

  /**
   * Returns the data store factory. Default value is
   * {@link AppEngineDataStoreFactory#getDefaultInstance()}.
   */
  @Override
  public DataStoreFactory getDataStoreFactory() {
    return AppEngineDataStoreFactory.getDefaultInstance();
  }

  /**
   * Returns the user ID for the given HTTP servlet request.
   *
   * <p>
   * It uses {@link com.google.appengine.api.users.UserService#getCurrentUser()} to get the current
   * user, and then {@link com.google.appengine.api.users.User#getUserId} to get the ID.
   * </p>
   */
  @Override
  public String getUserId(HttpServletRequest request) {
    return UserServiceFactory.getUserService().getCurrentUser().getUserId();
  }
}
