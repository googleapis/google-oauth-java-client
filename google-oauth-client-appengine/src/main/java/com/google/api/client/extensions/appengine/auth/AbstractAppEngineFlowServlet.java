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

package com.google.api.client.extensions.appengine.auth;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.extensions.servlet.auth.AbstractFlowUserServlet;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Beta;

/**
 * {@link Beta} <br/>
 * This class fills in some of the unknowns of the generic {@link AbstractFlowUserServlet} with
 * reasonable defaults for App Engine. This servlet requires that the App Engine user must be logged
 * in to work correctly.
 *
 * <p>
 * Warning: starting with version 1.7, usage of this for OAuth 2.0 is deprecated. Instead use {@link
 *com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet}.
 * </p>
 *
 * <p>
 * Upgrade warning: in version 1.15 there was an implementation of {@link #newJsonFactoryInstance()}
 * that used {@code com.google.api.client.json.jackson.JacksonFactory}, but starting with version
 * 1.16 there is no such implementation.
 * </p>
 *
 * @author moshenko@google.com (Jacob Moshenko)
 * @since 1.4
 */
@Beta
public abstract class AbstractAppEngineFlowServlet extends AbstractFlowUserServlet {

  private static final long serialVersionUID = 1L;

  /**
   * Return the user ID of the user that is logged in.
   *
   * @throws IllegalStateException Thrown when no user is logged in.
   */
  @Override
  protected String getUserId() {
    return AppEngineServletUtils.getUserId();
  }

  @Override
  protected HttpTransport newHttpTransportInstance() {
    return new UrlFetchTransport();
  }
}
