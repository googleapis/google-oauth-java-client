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

package com.google.api.client.extensions.appengine.auth.oauth2;

import com.google.api.client.auth.oauth2.OAuthApplicationContext;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.extensions.servlet.auth.oauth2.ServletOAuthApplicationContext;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;

import java.util.Collection;

/**
 * Google App Engine wrapper around {@link OAuthApplicationContext}.
 *
 * @since 1.17
 * @author Nick Miceli
 */
public abstract class AppEngineOAuthApplicationContext extends ServletOAuthApplicationContext {

  public AppEngineOAuthApplicationContext(
      String redirectUri, Collection<String> scopes, String applicationName) {
    super(redirectUri, scopes, applicationName);
  }

  @Override
  public HttpTransport getTransport() {
    return UrlFetchTransport.getDefaultInstance();
  }

  @Override
  public JsonFactory getJsonFactory() {
    return JacksonFactory.getDefaultInstance();
  }

  @Override
  public DataStoreFactory getDataStoreFactory() {
    return AppEngineDataStoreFactory.getDefaultInstance();
  }
}
