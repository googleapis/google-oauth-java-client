/*
 * Copyright (c) 2011 Google Inc.
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

package com.google.api.client.extensions.servlet.auth;

import com.google.api.client.extensions.auth.helpers.Credential;
import com.google.api.client.extensions.auth.helpers.ThreeLeggedFlow;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.Beta;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link Beta} <br/>
 * Servlet that can be used to invoke and manage a {@link ThreeLeggedFlow} object in the App Engine
 * container. Developers should subclass this to provide the necessary information for their
 * specific use case.
 *
 * <p>
 * Warning: starting with version 1.7, usage of this for OAuth 2.0 is deprecated. Instead use {@link
 *    com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet}.
 * </p>
 *
 * @author moshenko@google.com (Jacob Moshenko)
 * @since 1.4
 */
@Beta
public abstract class AbstractFlowUserServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private final HttpTransport httpTransport;
  private final JsonFactory jsonFactory;

  /**
   * Reserved request context identifier used to store the credential instance in an authorized
   * servlet.
   */
  private static final String AUTH_CREDENTIAL =
      "com.google.api.client.extensions.servlet.auth.credential";

  public AbstractFlowUserServlet() {
    httpTransport = newHttpTransportInstance();
    jsonFactory = newJsonFactoryInstance();
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();

    String userId = getUserId();

    ThreeLeggedFlow oauthFlow = newFlow(userId);
    oauthFlow.setJsonFactory(getJsonFactory());
    oauthFlow.setHttpTransport(getHttpTransport());

    try {
      Credential cred = oauthFlow.loadCredential(pm);

      if (cred != null && cred.isInvalid()) {
        pm.deletePersistent(cred);
        cred = null;
      }

      if (cred != null) {
        req.setAttribute(AUTH_CREDENTIAL, cred);
        try {
          // Invoke the user code
          super.service(req, resp);
        } catch (IOException e) {
          // Determine if we failed due to auth, or just failed
          if (cred.isInvalid()) {
            pm.deletePersistent(cred);
            startAuthFlow(resp, pm, oauthFlow);
          } else {
            throw e;
          }
        }
      } else {
        startAuthFlow(resp, pm, oauthFlow);
      }
    } finally {
      pm.close();
    }
  }

  /**
   * Start the auth flow. Don't run any code after this method that will change the response object.
   */
  private void startAuthFlow(
      HttpServletResponse resp, PersistenceManager pm, ThreeLeggedFlow oauthFlow)
      throws IOException {
    pm.makePersistent(oauthFlow);
    String authorizationUrl = oauthFlow.getAuthorizationUrl();
    resp.sendRedirect(authorizationUrl);
  }

  /**
   * Fetch a credential associated with this request.
   *
   * @param req Request object to use as context for fetching the credential.
   * @return Credential object for this request and user.
   *
   * @since 1.5
   */
  protected Credential getCredential(HttpServletRequest req) {
    Credential cred = (Credential) req.getAttribute(AUTH_CREDENTIAL);
    return cred;
  }

  /**
   * Return the {@link JsonFactory} instance for this servlet.
   */
  protected final JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  /**
   * Return the {@link HttpTransport} instance for this servlet.
   */
  protected final HttpTransport getHttpTransport() {
    return httpTransport;
  }

  /**
   * Obtain a PersistenceManagerFactory for working with the datastore.
   *
   * @return PersistenceManagerFactory instance.
   */
  protected abstract PersistenceManagerFactory getPersistenceManagerFactory();

  /**
   * Create a flow object which will be used to obtain credentials.
   *
   * @param userId User id to be passed to the constructor of the flow object
   * @return Flow object used to obtain credentials
   */
  protected abstract ThreeLeggedFlow newFlow(String userId) throws IOException;

  /**
   * Create a new {@link HttpTransport} instance. Implementations can create any type of applicable
   * transport and should be as simple as:
   *
   * <pre>
  new NetHttpTransport();
   * </pre>
   *
   * @return {@link HttpTransport} instance for your particular environment
   */
  protected abstract HttpTransport newHttpTransportInstance();

  /**
   * Create a new {@link JsonFactory} instance. Implementations can create any type of applicable
   * json factory and should be as simple as:
   *
   * <pre>
  new JacksonFactory();
   * </pre>
   *
   * @return {@link JsonFactory} instance for your particular environment
   */
  protected abstract JsonFactory newJsonFactoryInstance();

  /**
   * @return Get a string representation of a userId that can be used to associate credentials and
   *         flows with a specific user.
   */
  protected abstract String getUserId();
}
