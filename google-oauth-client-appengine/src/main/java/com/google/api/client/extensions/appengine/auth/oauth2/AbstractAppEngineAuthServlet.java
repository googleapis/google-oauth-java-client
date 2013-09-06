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

package com.google.api.client.extensions.appengine.auth.oauth2;

import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthServlet;
import com.google.api.client.extensions.servlet.auth.oauth2.AuthorizedServlet;
import com.google.api.client.extensions.servlet.auth.oauth2.ServletOAuthApplicationContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Simple extension of {@link AbstractAuthServlet} that uses the currently logged-in Google
 * Account user, as directed in <a
 * href="http://code.google.com/appengine/docs/java/config/webxml.html#Security_and_Authentication">
 * Security and Authentication</a>.
 *
 * <p>
 * Your servlet must be annotated with {@link AuthorizedServlet}, or it will throw a
 * {@link NullPointerException} on instantiation.
 * </p>
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
  @AuthorizedServlet(redirect = "/myauthorizedurl", flowDataClass=MyFlowData.class)
  public class MyServlet extends AbstractAppEngineAuthorizationServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    // If you are using a Google API:
    Plus plus = ServiceFactory.createService(Plus.class, new MyFlowData(), MY_APPLICATION_NAME);

    // Otherwise, you can access the credential like so, and then use it in your service:
    Credential credential = new MyFlowData().getFlow()
      .loadCredential(UserServiceFactory.getUserService().getCurrentUser().getUserId());
 * </pre>

 * @since 1.17
 * @author ngmiceli@google.com (Nick Miceli)
 * @author peleyal@google.com (Eyal Peled)
 */
public abstract class AbstractAppEngineAuthServlet extends AbstractAuthServlet {

  protected AbstractAppEngineAuthServlet(ServletOAuthApplicationContext context) {
    super(context);
  }

  private static final long serialVersionUID = 1L;
}
