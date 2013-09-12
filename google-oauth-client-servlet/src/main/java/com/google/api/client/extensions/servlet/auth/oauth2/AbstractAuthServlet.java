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
 * @author Nick Miceli
 * @author Eyal Peled
 *
 * @since 1.18
 */
public abstract class AbstractAuthServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  protected AbstractAuthServlet(ServletOAuthApplicationContext context) {
    this.oauthContext = Preconditions.checkNotNull(context);
  }

  /** Lock on the flow and credential. */
  private final Lock lock = new ReentrantLock();

  /** Persisted credential associated with the current request or {@code null} for none. */
  private Credential credential;

  /** The OAuth application context instance. */
  private ServletOAuthApplicationContext oauthContext;

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    // TODO(peleyal): I think that we should remove the lock from here.
    lock.lock();
    try {
      ServletRequestContext requestContext = new ServletRequestContext().setCredential(credential)
          .setOauthContext(oauthContext)
          .setRequest(req)
          .setResponse(resp)
          .setCallback(getCallback());
      boolean authorized = ServletAuthUtility.service(requestContext);
      credential = requestContext.getCredential();
      if (authorized) {
        super.service(req, resp);
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Return the persisted credential associated with the current request or {@code null} for none.
   */
  protected final Credential getCredential() {
    return credential;
  }

  /** Returns the OAuth2 application context. */
  protected ServletOAuthApplicationContext getOAuthApplicationContext() {
    return oauthContext;
  }

  /**
   * Returns the auth servlet callback.
   *
   * <p>
   * Override this method to provide a callback for successful or unsuccessful response to the
   * authorization code process.
   * </p>
   *
   */
  public AuthServletCallback getCallback() {
    return null;
  }
}
