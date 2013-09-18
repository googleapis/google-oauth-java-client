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
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStore;

import java.io.IOException;
import java.util.Random;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link Beta} <br/>
 * A servlet request context which contains the servlet request and response, the user's credential,
 * etc. It important {@link #execute} method is responsible for the user authorization.
 *
 * <p>
 * This request context is designed to simplify the flow in which an end-user authorizes your web
 * application to access their protected data. Your application then has access to their data based
 * on an access token and a refresh token to refresh that access token when it expires. A sample
 * usage is in {@link AbstractAuthServlet} class. If you prefer not to inherit from
 * {@link AbstractAuthServlet} you can use the {@link #execute} implementation in your
 * {@link HttpServlet#service} method.
 * </p>
 *
 * @author Nick Miceli
 * @author Eyal Peled
 * @since 1.18
 */
@Beta
public class ServletRequestContext {

  /** Servlet request. */
  private HttpServletRequest request;

  /** Servlet response. */
  private HttpServletResponse response;

  /** Credential or {@code null} for none. */
  private Credential credential;

  /** Servlet OAuth context instance. */
  private ServletOAuthContext oauthContext;

  /** Returns the callback which will be invoked on authorization code success. */
  private AuthServletCallback callback;

  /** Indicates whether nonce will be added to the state parameter to make it unpredictable. */
  private boolean addStateNonce = true;

  /** Returns the servlet request. */
  public HttpServletRequest getRequest() {
    return request;
  }

  /** Sets the servlet request. */
  public ServletRequestContext setRequest(HttpServletRequest request) {
    this.request = request;
    return this;
  }

  /** Returns the servlet response. */
  public HttpServletResponse getResponse() {
    return response;
  }

  /** Sets the servlet response. */
  public ServletRequestContext setResponse(HttpServletResponse response) {
    this.response = Preconditions.checkNotNull(response);
    return this;
  }

  /** Returns the credential or {@code null} for none. */
  public Credential getCredential() {
    return credential;
  }

  /** Sets the credential or {@code null} for none. */
  public ServletRequestContext setCredential(Credential credential) {
    this.credential = credential;
    return this;
  }

  /** Returns the servlet OAuth context instance. */
  public ServletOAuthContext getOauthContext() {
    return oauthContext;
  }

  /** Sets the servlet OAuth context instance. */
  public ServletRequestContext setOauthContext(ServletOAuthContext oauthContext) {
    this.oauthContext = Preconditions.checkNotNull(oauthContext);
    return this;
  }

  /**
   * Returns the callback which will be invoked on authorization code success or failure or
   * {@code null} for none.
   */
  public AuthServletCallback getCallback() {
    return callback;
  }

  /**
   * Sets callback which will be invoked on authorization code success or failure or {@code null}
   * for none.
   */
  public ServletRequestContext setCallback(AuthServletCallback callback) {
    this.callback = callback;
    return this;
  }

  /**
   * Returns whether nonce will be added to the state parameter to make it unpredictable.
   */
  public boolean isAddStateNonce() {
    return addStateNonce;
  }

  /**
   * Sets whether nonce will be added to the state parameter to make it unpredictable.
   *
   * <p>
   * The default value is {@code true}.
   * </p>
   *
   */
  public ServletRequestContext setAddStateNonce(boolean addStateNonce) {
    this.addStateNonce = addStateNonce;
    return this;
  }

  /**
   * This method is designed to simplify the authorization code flow.
   *
   * <p>
   * Your servlet {@link HttpServlet#service} method should call this implementation. To get the
   * persisted credential associated with this flow, call
   * {@link ServletRequestContext#getCredential()}. It is assumed that the end-user is authenticated
   * by some external means by which a user ID is obtained. This user ID is used as the primary key
   * for persisting the end-user credentials, and passed in via
   * {@link ServletOAuthContext#getUserId(HttpServletRequest)}. The first time an end-user arrives
   * at your servlet, they will be redirected in the browser to an authorization page. Next, they
   * will be redirected back to your site at the redirect URI selected in {@link #getRedirectUri}.
   * Then the final step is to return the user back to the point he started, using the state
   * parameter.
   * </p>
   *
   * @return {@code true} if the user was already authenticated and has a valid access token
   *
   * @throws IOException
   */
  public boolean execute() throws IOException {
    HttpServletRequest req = getRequest();
    // check if there is already credential in the store for this user
    AuthorizationCodeFlow codeFlow = oauthContext.getFlow();
    String userId = getOauthContext().getUserId(getRequest());
    Credential credential = codeFlow.loadCredential(userId);

    // the user already has credential, so no need to start the authorization code flow
    if (credential != null && credential.getAccessToken() != null) {
      setCredential(credential);
      return true;
    }

    // check if the query parameter contains code
    String code = req.getParameter("code");
    String error = req.getParameter("error");
    if (code != null) {
      handleAuthorizationCode(code);
      if (getCallback() != null) {
        getCallback().onSuccess(req, getResponse(), getCredential());
      }
      return false;
    } else if (error != null) {
      if (getCallback() != null) {
        getCallback().onError(req, getResponse(), error);
      }
      return false;
    }

    // we need to start the authorization code flow. Redirect the user to the authorization flow
    // page and set the state parameter so we will back to the original request.
    AuthorizationCodeRequestUrl authorizationUrl = codeFlow.newAuthorizationUrl();
    StringBuffer requestBuf = req.getRequestURL();
    if (req.getQueryString() != null) {
      requestBuf.append('?').append(req.getQueryString());
    }

    if (addStateNonce) {
      DataStore<String> store = getOauthContext().getDataStoreFactory().getDataStore("authState");
      // store a random string in the session for verifying the responses in our OAuth2 flow.
      Random rnd = new Random();
      StringBuffer authState = new StringBuffer(10);
      for (int i = 0; i < 10; ++i) {
        authState.append(rnd.nextInt(10));
      }
      store.set(userId, authState.toString());
      requestBuf.append(authState);
    }

    authorizationUrl.setState(requestBuf.toString());
    authorizationUrl.setRedirectUri(getRedirectUri());
    getResponse().sendRedirect(authorizationUrl.build());
    setCredential(null);

    return false;
  }

  /**
   * Handles authorization code by redirecting to the servlet OAuth callback.
   *
   * <p>
   * The OAuth callback is defined in the state parameter.
   * </p>
   *
   * @param code authorization code
   */
  private void handleAuthorizationCode(String code) throws IOException {
    HttpServletRequest req = getRequest();
    AuthorizationCodeFlow flow = getOauthContext().getFlow();
    String redirectUri = getRedirectUri();

    // get the token
    AuthorizationCodeTokenRequest tokenReq = flow.newTokenRequest(code);
    TokenResponse token = tokenReq.setRedirectUri(redirectUri).execute();
    String userId = getOauthContext().getUserId(req);
    flow.createAndStoreCredential(token, userId);
    String state = req.getParameter("state");

    if (addStateNonce) {
      // check state against the session state
      if (state.length() > 10) {
        String redirect = state.substring(0, state.length() - 10);
        String currentAuthState = state.substring(state.length() - 10);
        DataStore<String> store = getOauthContext().getDataStoreFactory().getDataStore("authState");
        String storedAuthState = store.get(userId);
        if (storedAuthState != null && storedAuthState.equals(currentAuthState)) {
          GenericUrl url = new GenericUrl(redirect);
          getResponse().sendRedirect(url.toString());
          store.delete(userId);
          return;
        }
      }
    } else {
      GenericUrl url = new GenericUrl(state);
      getResponse().sendRedirect(url.toString());
      return;
    }

    // TODO(peleyal): throw illegal. check if we prefer to throw a different exception for each case
    // here
  }

  private String getRedirectUri() {
    GenericUrl url = new GenericUrl(getRequest().getRequestURL().toString());
    url.setRawPath(getOauthContext().getRedirectUri());
    return url.toString();
  }
}
