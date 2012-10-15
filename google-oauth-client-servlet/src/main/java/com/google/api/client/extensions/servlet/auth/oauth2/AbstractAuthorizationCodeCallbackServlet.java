/*
 * Copyright (c) 2012 Google Inc.
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
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Thread-safe OAuth 2.0 authorization code callback servlet to process the authorization code or
 * error response from authorization page redirect.
 *
 * <p>
 * This is designed to simplify the flow in which an end-user authorizes your web application to
 * access their protected data. The main servlet class extends
 * {@link AbstractAuthorizationCodeServlet} which if the end-user credentials are not found, will
 * redirect the end-user to an authorization page. If the end-user grants authorization, they will
 * be redirected to this servlet that extends {@link AbstractAuthorizationCodeCallbackServlet} and
 * the {@link #onSuccess} will be called. Similarly, if the end-user grants authorization, they will
 * be redirected to this servlet and {@link #onError} will be called.
 * </p>
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
public class ServletCallbackSample extends AbstractAuthorizationCodeCallbackServlet {

  &#64;Override
  protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
      throws ServletException, IOException {
    resp.sendRedirect("/");
  }

  &#64;Override
  protected void onError(
      HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
      throws ServletException, IOException {
    // handle error
  }

  &#64;Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    url.setRawPath("/oauth2callback");
    return url.build();
  }

  &#64;Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
        new NetHttpTransport(),
        new JacksonFactory(),
        new GenericUrl("https://server.example.com/token"),
        new BasicAuthentication("s6BhdRkqt3", "7Fjfp0ZBr1KtDRbnfVdmIw"),
        "s6BhdRkqt3",
        "https://server.example.com/authorize").setCredentialStore(
        new JdoCredentialStore(JDOHelper.getPersistenceManagerFactory("transactions-optional")))
        .build();
  }

  &#64;Override
  protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
    // return user ID
  }
}
 * </pre>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public abstract class AbstractAuthorizationCodeCallbackServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  /** Lock on the flow. */
  private final Lock lock = new ReentrantLock();

  /**
   * Authorization code flow to be used across all HTTP servlet requests or {@code null} before
   * initialized in {@link #initializeFlow()}.
   */
  private AuthorizationCodeFlow flow;

  @Override
  protected final void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    StringBuffer buf = req.getRequestURL();
    if (req.getQueryString() != null) {
      buf.append('?').append(req.getQueryString());
    }
    AuthorizationCodeResponseUrl responseUrl = new AuthorizationCodeResponseUrl(buf.toString());
    String code = responseUrl.getCode();
    if (responseUrl.getError() != null) {
      onError(req, resp, responseUrl);
    } else if (code == null) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().print("Missing authorization code");
    } else {
      lock.lock();
      try {
        if (flow == null) {
          flow = initializeFlow();
        }
        String redirectUri = getRedirectUri(req);
        TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
        String userId = getUserId(req);
        Credential credential = flow.createAndStoreCredential(response, userId);
        onSuccess(req, resp, credential);
      } finally {
        lock.unlock();
      }
    }
  }

  /**
   * Loads the authorization code flow to be used across all HTTP servlet requests (only called
   * during the first HTTP servlet request with an authorization code).
   */
  protected abstract AuthorizationCodeFlow initializeFlow() throws ServletException, IOException;

  /** Returns the redirect URI for the given HTTP servlet request. */
  protected abstract String getRedirectUri(HttpServletRequest req)
      throws ServletException, IOException;

  /** Returns the user ID for the given HTTP servlet request. */
  protected abstract String getUserId(HttpServletRequest req) throws ServletException, IOException;

  /**
   * Handles a successfully granted authorization.
   *
   * <p>
   * Default implementation is to do nothing, but subclasses should override and implement. Sample
   * implementation:
   * </p>
   *
   * <pre>
      resp.sendRedirect("/granted");
   * </pre>
   *
   * @param req HTTP servlet request
   * @param resp HTTP servlet response
   * @param credential credential
   * @throws ServletException HTTP servlet exception
   * @throws IOException some I/O exception
   */
  protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
      throws ServletException, IOException {
  }

  /**
   * Handles an error to the authorization, such as when an end user denies authorization.
   *
   * <p>
   * Default implementation is to do nothing, but subclasses should override and implement. Sample
   * implementation:
   * </p>
   *
   * <pre>
      resp.sendRedirect("/denied");
   * </pre>
   *
   * @param req HTTP servlet request
   * @param resp HTTP servlet response
   * @param errorResponse error response ({@link AuthorizationCodeResponseUrl#getError()} is not
   *        {@code null})
   * @throws ServletException HTTP servlet exception
   * @throws IOException some I/O exception
   */
  protected void onError(
      HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
      throws ServletException, IOException {
  }
}
