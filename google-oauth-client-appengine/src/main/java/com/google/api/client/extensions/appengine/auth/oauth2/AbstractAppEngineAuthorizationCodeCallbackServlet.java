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

package com.google.api.client.extensions.appengine.auth.oauth2;

import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeCallbackServlet;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Simple extension of {@link AbstractAuthorizationCodeCallbackServlet} that uses the currently
 * logged-in Google Account user, as directed in <a
 * href="https://cloud.google.com/appengine/docs/standard/java/config/webxml#security-auth">Security
 * and Authentication</a>.
 *
 * <p>Note that if there is no currently logged-in user, {@link #getUserId(HttpServletRequest)} will
 * throw a {@link NullPointerException}. Example to require login for all pages:
 *
 * <pre>
 * &lt;security-constraint&gt;
 * &lt;web-resource-collection&gt;
 * &lt;web-resource-name&gt;any&lt;/web-resource-name&gt;
 * &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 * &lt;/web-resource-collection&gt;
 * &lt;auth-constraint&gt;
 * &lt;role-name&gt;*&lt;/role-name&gt;
 * &lt;/auth-constraint&gt;
 * &lt;/security-constraint&gt;
 * </pre>
 *
 * <p>Sample usage:
 *
 * <pre>{@code
 * public class ServletCallbackSample extends AbstractAppEngineAuthorizationCodeCallbackServlet {
 *
 * &#64;Override
 * protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
 * throws ServletException, IOException {
 * resp.sendRedirect("/");
 * }
 *
 * &#64;Override
 * protected void onError(
 * HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
 * throws ServletException, IOException {
 * // handle error
 * }
 *
 * &#64;Override
 * protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
 * GenericUrl url = new GenericUrl(req.getRequestURL().toString());
 * url.setRawPath("/oauth2callback");
 * return url.build();
 * }
 *
 * &#64;Override
 * protected AuthorizationCodeFlow initializeFlow() throws IOException {
 * return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
 * new UrlFetchTransport(),
 * new GsonFactory(),
 * new GenericUrl("https://server.example.com/token"),
 * new BasicAuthentication("s6BhdRkqt3", "7Fjfp0ZBr1KtDRbnfVdmIw"),
 * "s6BhdRkqt3",
 * "https://server.example.com/authorize").setCredentialStore(new AppEngineCredentialStore())
 * .build();
 * }
 * }</pre>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public abstract class AbstractAppEngineAuthorizationCodeCallbackServlet
    extends AbstractAuthorizationCodeCallbackServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
    // Use GAE Standard's users service to fetch the current user of the application.
    return UserServiceFactory.getUserService().getCurrentUser().getUserId();
  }
}
