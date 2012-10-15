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
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpResponseException;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Thread-safe OAuth 2.0 authorization code flow HTTP servlet that manages and persists end-user
 * credentials.
 *
 * <p>
 * This is designed to simplify the flow in which an end-user authorizes your web application to
 * access their protected data. Your application then has access to their data based on an access
 * token and a refresh token to refresh that access token when it expires. Your main servlet class
 * should extend {@link AbstractAuthorizationCodeServlet} and implement the abstract methods. To get
 * the persisted credential associated with the current request, call {@link #getCredential()}. It
 * is assumed that the end-user is authenticated by some external means by which a user ID is
 * obtained. This user ID is used as the primary key for persisting the end-user credentials, and
 * passed in via {@link #getUserId(HttpServletRequest)}. The first time an end-user arrives at your
 * servlet, they will be redirected in the browser to an authorization page. Next, they will be
 * redirected back to your site at the redirect URI selected in
 * {@link #getRedirectUri(HttpServletRequest)}. The servlet to process that should extend
 * {@link AbstractAuthorizationCodeCallbackServlet}, which should redirect back to this servlet on
 * success.
 * </p>
 *
 * <p>
 * Although this implementation is thread-safe, it can only process one request at a time. For a
 * more performance-critical multi-threaded web application, instead use
 * {@link AuthorizationCodeFlow} directly.
 * </p>
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
public class ServletSample extends AbstractAuthorizationCodeServlet {

  &#64;Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // do stuff
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
public abstract class AbstractAuthorizationCodeServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  /** Lock on the flow and credential. */
  private final Lock lock = new ReentrantLock();

  /** Persisted credential associated with the current request or {@code null} for none. */
  private Credential credential;

  /**
   * Authorization code flow to be used across all HTTP servlet requests or {@code null} before
   * initialized in {@link #initializeFlow()}.
   */
  private AuthorizationCodeFlow flow;

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    lock.lock();
    try {
      // load credential from persistence store
      String userId = getUserId(req);
      if (flow == null) {
        flow = initializeFlow();
      }
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
      onAuthorization(req, resp, authorizationUrl);
      credential = null;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Loads the authorization code flow to be used across all HTTP servlet requests (only called
   * during the first HTTP servlet request).
   */
  protected abstract AuthorizationCodeFlow initializeFlow() throws ServletException, IOException;

  /** Returns the redirect URI for the given HTTP servlet request. */
  protected abstract String getRedirectUri(HttpServletRequest req)
      throws ServletException, IOException;

  /** Returns the user ID for the given HTTP servlet request. */
  protected abstract String getUserId(HttpServletRequest req) throws ServletException, IOException;

  /**
   * Return the persisted credential associated with the current request or {@code null} for none.
   */
  protected final Credential getCredential() {
    return credential;
  }

  /**
   * Handles user authorization by redirecting to the OAuth 2.0 authorization server.
   *
   * <p>
   * Default implementation is to call {@code resp.sendRedirect(authorizationUrl.build())}.
   * Subclasses may override to provide optional parameters such as the recommended state parameter.
   * Sample implementation:
   * </p>
   *
   * <pre>
  &#64;Override
  protected void onAuthorization(HttpServletRequest req, HttpServletResponse resp,
      AuthorizationCodeRequestUrl authorizationUrl) throws ServletException, IOException {
    authorizationUrl.setState("xyz");
    super.onAuthorization(req, resp, authorizationUrl);
  }
   * </pre>
   *
   * @param authorizationUrl authorization code request URL
   * @param req HTTP servlet request
   * @throws ServletException servlet exception
   * @since 1.11
   */
  protected void onAuthorization(HttpServletRequest req, HttpServletResponse resp,
      AuthorizationCodeRequestUrl authorizationUrl) throws ServletException, IOException {
    resp.sendRedirect(authorizationUrl.build());
  }
}
