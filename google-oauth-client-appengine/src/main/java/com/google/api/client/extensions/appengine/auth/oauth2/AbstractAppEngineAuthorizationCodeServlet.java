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

import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Simple extension of {@link AbstractAuthorizationCodeServlet} that uses the currently logged-in
 * Google Account user, as directed in <a
 * href="http://code.google.com/appengine/docs/java/config/webxml.html#Security_and_Authentication">
 * Security and Authentication</a>.
 *
 * <p>
 * Note that if there is no currently logged-in user, {@link #getUserId(HttpServletRequest)} will
 * throw a {@link NullPointerException}. Example to require login for all pages:
 * </p>
 *
 * <pre>
  &lt;security-constraint&gt;
    &lt;web-resource-collection&gt;
      &lt;web-resource-name&gt;any&lt;/web-resource-name&gt;
      &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
    &lt;/web-resource-collection&gt;
    &lt;auth-constraint&gt;
      &lt;role-name&gt;*&lt;/role-name&gt;
    &lt;/auth-constraint&gt;
  &lt;/security-constraint&gt;
 * </pre>
 *
 * <p>
 * Sample usage:
 * </p>
 *
 * <pre>
public class ServletSample extends AbstractAppEngineAuthorizationCodeServlet {

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
        new UrlFetchTransport(),
        new JacksonFactory(),
        new GenericUrl("https://server.example.com/token"),
        new BasicAuthentication("s6BhdRkqt3", "7Fjfp0ZBr1KtDRbnfVdmIw"),
        "s6BhdRkqt3",
        "https://server.example.com/authorize").setCredentialStore(new AppEngineCredentialStore())
        .build();
  }
}
 * </pre>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public abstract class AbstractAppEngineAuthorizationCodeServlet
    extends AbstractAuthorizationCodeServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
    return UserServiceFactory.getUserService().getCurrentUser().getUserId();
  }
}
