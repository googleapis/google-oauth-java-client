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

import com.google.api.client.http.json.HttpJsonContext;
import com.google.api.client.util.store.DataStoreFactory;

import java.io.IOException;
import java.util.Collection;

/**
 * A collection of data about the authorizing application. It extends the {@link HttpJsonContext} and
 * add useful oauth properties like scopes, authorization code flow and data store factory.
 *
 * @author Nick Miceli
 * @author Eyal Peled
 */
public interface OAuthContext extends HttpJsonContext {

  /** Returns the scopes used by the OAuth2 flow. */
  Collection<String> getScopes();

  /** Returns the authorization code flow. */
  AuthorizationCodeFlow getFlow() throws IOException;

  /** Returns the data store factory. */
  DataStoreFactory getDataStoreFactory();
}
