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
 * A servlet request context which contains the servlet request and response, the user's credential
 * and the oauth application context.
 *
 * @author Nick Miceli
 * @author Eyal Peled
 *
 * @since 1.18
 */

public class ServletRequestContext {

  private HttpServletRequest request;
  private HttpServletResponse response;
  private Credential credential;
  private ServletOAuthApplicationContext oauthContext;

  /**
   * @return the request
   */
  public HttpServletRequest getRequest() {
    return request;
  }

  /**
   * @param request the request to set
   */
  public ServletRequestContext setRequest(HttpServletRequest request) {
    this.request = request;
    return this;
  }

  /**
   * @return the response
   */
  public HttpServletResponse getResponse() {
    return response;
  }

  /**
   * @param response the response to set
   */
  public ServletRequestContext setResponse(HttpServletResponse response) {
    this.response = response;
    return this;
  }

  /**
   * @return the credential
   */
  public Credential getCredential() {
    return credential;
  }

  /**
   * @param credential the credential to set
   */
  public ServletRequestContext setCredential(Credential credential) {
    this.credential = credential;
    return this;
  }

  /**
   * @return the oauthContext
   */
  public ServletOAuthApplicationContext getOauthContext() {
    return oauthContext;
  }

  /**
   * @param oauthContext the oauthContext to set
   */
  public ServletRequestContext setOauthContext(ServletOAuthApplicationContext oauthContext) {
    this.oauthContext = oauthContext;
    return this;
  }
}
