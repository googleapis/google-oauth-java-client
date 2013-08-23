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

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.OAuthApplicationContext;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.util.Preconditions;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 *
 * @since 1.17
 * @author Nick Miceli
 * @author Eyal Peled
 */
public abstract class AbstractAuthServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  protected AbstractAuthServlet(ServletOAuthApplicationContext context) {
    this.oAuthContext = Preconditions.checkNotNull(context);
  }

  /** Lock on the flow and credential. */
  private final Lock lock = new ReentrantLock();

  /** Persisted credential associated with the current request or {@code null} for none. */
  private Credential credential;

  private ServletOAuthApplicationContext oAuthContext;

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    lock.lock();
    try {
      // TODO(NOW): Make check for code prettier
      StringBuffer buf = req.getRequestURL();
      if (req.getQueryString() != null) {
        buf.append('?').append(req.getQueryString());
      }
      // TODO(NOW): buf.toString() == null or empty?
      Object code = req.getParameter("code");
      if (code != null) {
        handleAuthorizationCode(req, resp, code.toString());
        return;
      }

      // load credential from persistence store
      String userId = getUserId(req);
      AuthorizationCodeFlow flow = oAuthContext.getFlow();
      Preconditions.checkNotNull(flow);
      credential = flow.loadCredential(userId);
      // if credential found with an access token, invoke the user code
      if (credential != null && credential.getAccessToken() != null) {
        try {
          super.service(req, resp);
          return;
        } catch (HttpResponseException e) {
          // if access token is null, assume it is because auth failed and we need to re-authorize
          // but if access token is not null, it is some other problem
          if (credential.getAccessToken() != null) {
            throw e;
          }
        }
      }

      // redirect to the authorization flow
      AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl();
      authorizationUrl.setRedirectUri(getRedirectUri(req));
      authorizationUrl.setState(req.getRequestURI());
      resp.sendRedirect(authorizationUrl.build());
      // TODO(NOW): Again, do we want to provide this hook at all? It was for if the user wants to
      // set the state, but obviously we don't support that in this specific call (in the current
      // implementation at least). It wouldn't be hard here to call the hook first, then check if
      // the user set the state and do some clever "multi-state" value that is parsed later.
      // onAuthorization(req, resp, authorizationUrl);
      credential = null;
    } finally {
      lock.unlock();
    }
  }

  private void handleAuthorizationCode(
      HttpServletRequest req, HttpServletResponse resp, String code)
      throws ServletException, IOException {
    lock.lock();
    try {
      AuthorizationCodeFlow flow = oAuthContext.getFlow();
      String redirectUri = getRedirectUri(req);
      AuthorizationCodeTokenRequest req2 = flow.newTokenRequest(code);
      TokenResponse response = req2.setRedirectUri(redirectUri).execute();
      String userId = getUserId(req);
      flow.createAndStoreCredential(response, userId);
      String state = req.getParameter("state");
      GenericUrl url = new GenericUrl(req.getRequestURL().toString());
      // TODO(NOW): Specify behavior if state is null?
      if (state != null) {
        url.setRawPath(state);
      }
      resp.sendRedirect(url.toString());
      // TODO(NOW): Do we want to give the user this hook? If so, how?
      // onSuccess(req, resp, credential);
    } finally {
      lock.unlock();
    }
  }

  private String getRedirectUri(HttpServletRequest req) {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    url.setRawPath(oAuthContext.getRedirectUri());
    return url.toString();
  }

  /**
   * Returns the user ID for the given HTTP servlet request.
   * @param req
   */
  protected abstract String getUserId(HttpServletRequest req) throws ServletException, IOException;

  /**
   * Return the persisted credential associated with the current request or {@code null} for none.
   */
  protected final Credential getCredential() {
    return credential;
  }

  protected OAuthApplicationContext getOAuthApplicationContext() {
    return oAuthContext;
  }
}
