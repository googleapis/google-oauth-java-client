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
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStore;

import java.io.IOException;
import java.util.Random;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * This utility class is designed to simplify the flow in which an end-user authorizes your web
 * application to access their protected data. Your application then has access to their data based
 * on an access token and a refresh token to refresh that access token when it expires. A sample
 * usage is in {@link AbstractAuthServlet} class. If you prefer not to inherit from
 * {@link AbstractAuthServlet} you can use the {@link #service} implementation in your
 * {@link HttpServlet#service} method.
 * @since 1.17
 *
 * @author Nick Miceli
 * @author Eyal Peled
 *
 * @since 1.18
 */
public class ServletAuthUtility {

  // TODO(peleyal): expose it as a property and find a better name
  private static boolean addRandomNumber = true;

  /**
   * This method is designed to simplify the authorization code flow.
   *
   * <p>
   * Your servlet {@link HttpServlet#service} method should call this implementation. To get the
   * persisted credential associated with this flow, call
   * {@link ServletRequestContext#getCredential()}. It is assumed that the end-user is authenticated
   * by some external means by which a user ID is obtained. This user ID is used as the primary key
   * for persisting the end-user credentials, and passed in via
   * {@link ServletOAuthApplicationContext#getUserId(ServletRequest)}. The first time an end-user
   * arrives at your servlet, they will be redirected in the browser to an authorization page. Next,
   * they will be redirected back to your site at the redirect URI selected in
   * {@link #getRedirectUri}. Then the final step is to return the user back to the point he
   * started, using the state parameter.
   * </p>
   *
   * @param serviceRequest The service request object which contains the servlet request and
   *        response, the credential and the application context
   * @return {@code true} if the user was already authenticated and has a valid access token
   * @throws IOException
   */
  public static boolean service(ServletRequestContext serviceRequest) throws IOException {
    HttpServletRequest req = Preconditions.checkNotNull(serviceRequest.getRequest());
    ServletOAuthApplicationContext oauthContext =
        Preconditions.checkNotNull(serviceRequest.getOauthContext());

    // check if there is already credential in the store for this user
    AuthorizationCodeFlow authorizationCodeFlow = oauthContext.getFlow();
    String userId = serviceRequest.getOauthContext().getUserId(req);
    Credential credential = authorizationCodeFlow.loadCredential(userId);
    if (credential != null && credential.getAccessToken() != null) {
      serviceRequest.setCredential(credential);
      return true;
    }

    // check if the query parameter contains code
    String code = req.getParameter("code");
    String error = req.getParameter("error");
    if (code != null) {
      handleAuthorizationCode(serviceRequest, code);
      if (serviceRequest.getCallback() != null) {
        serviceRequest.getCallback()
            .onSuccess(req, serviceRequest.getResponse(), serviceRequest.getCredential());
      }
      return false;
    } else if (error != null) {
      if (serviceRequest.getCallback() != null) {
        serviceRequest.getCallback().onError(req, serviceRequest.getResponse(), error);
      }
    }

    // redirect to the authorization flow and set the state parameter so we will back to the
    // original request.
    AuthorizationCodeRequestUrl authorizationUrl = authorizationCodeFlow.newAuthorizationUrl();
    StringBuffer requestBuf = req.getRequestURL();
    if (req.getQueryString() != null) {
      requestBuf.append('?').append(req.getQueryString());
    }

    if (addRandomNumber) {
      DataStore<String> store =
          serviceRequest.getOauthContext().getDataStoreFactory().getDataStore("authState");
      // store a random string in the session for verifying the responses in our OAuth2 flow.
      Random rnd = new Random();
      StringBuffer authState = new StringBuffer(10);
      for (int i = 0; i < 10; ++i) {
        authState.append(new Integer(rnd.nextInt(10)).toString());
      }
      store.set(userId, authState.toString());
      requestBuf.append(authState);
    }

    authorizationUrl.setState(requestBuf.toString());
    authorizationUrl.setRedirectUri(getRedirectUri(serviceRequest));
    serviceRequest.getResponse().sendRedirect(authorizationUrl.build());
    serviceRequest.setCredential(null);

    // TODO(NOW): Again, do we want to provide this hook at all? It was for if the user wants to
    // set the state, but obviously we don't support that in this specific call (in the current
    // implementation at least). It wouldn't be hard here to call the hook first, then check if
    // the user set the state and do some clever "multi-state" value that is parsed later.
    // onAuthorization(req, resp, authorizationUrl);
    return false;
  }

  private static void handleAuthorizationCode(ServletRequestContext serviceRequest, String code)
      throws IOException {
    HttpServletRequest req = serviceRequest.getRequest();
    AuthorizationCodeFlow flow = serviceRequest.getOauthContext().getFlow();
    String redirectUri = getRedirectUri(serviceRequest);

    // get the token
    AuthorizationCodeTokenRequest tokenReq = flow.newTokenRequest(code);
    TokenResponse token = tokenReq.setRedirectUri(redirectUri).execute();
    String userId = serviceRequest.getOauthContext().getUserId(req);
    flow.createAndStoreCredential(token, userId);
    String state = req.getParameter("state");

    if (addRandomNumber) {
      // check state against the session state
      if (state.length() > 10) {
        String redirect = state.substring(0, state.length() - 10);
        String currentAuthState = state.substring(state.length() - 10);
        DataStore<String> store =
            serviceRequest.getOauthContext().getDataStoreFactory().getDataStore("authState");
        String storedAuthState = store.get(userId);
        if (storedAuthState != null && storedAuthState.equals(currentAuthState)) {
          GenericUrl url = new GenericUrl(redirect);
          serviceRequest.getResponse().sendRedirect(url.toString());
          return;
        }
      }
    } else {
      GenericUrl url = new GenericUrl(state);
      serviceRequest.getResponse().sendRedirect(url.toString());
      return;
    }

    // TODO(peleyal): throw illegal. check if we prefer to throw a different exception for each case
    // here
  }

  private static String getRedirectUri(ServletRequestContext flowContext) {
    GenericUrl url = new GenericUrl(flowContext.getRequest().getRequestURL().toString());
    url.setRawPath(flowContext.getOauthContext().getRedirectUri());
    return url.toString();
  }
}
