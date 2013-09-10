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

package com.google.api.client.extensions.servlet.auth.oauth2;

import com.google.api.client.auth.oauth2.Credential;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Nick Miceli
 * @author Eyal Peled
 * @since 1.18
 *
 */
public interface AuthServletCallback {

  /**
   * Handles a successfully granted authorization.
   *
   * @param req HTTP servlet request
   * @param resp HTTP servlet response
   * @param credential credential
   */
  void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential);

  /**
   * Handles an error to the authorization, such as when an end user denies authorization.
   *
   * @param req HTTP servlet request
   * @param resp HTTP servlet response
   * @param error the error response
   */
  void onError(
      HttpServletRequest req, HttpServletResponse resp, String error);
}
