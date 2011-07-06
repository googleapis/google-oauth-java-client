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
import com.google.api.client.extensions.auth.helpers.TwoLeggedFlow;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This specialization of {@link HttpServlet} allows accessing OAuth resources using a credential
 * that can be created without user intervention. Subclasses should call getCredential in their
 * handlers when they want access to protected resources.
 *
 * @author moshenko@google.com (Jacob Moshenko)
 *
 * @since 1.5
 */
public abstract class AbstractTwoLeggedFlowServlet extends HttpServlet {

  /**
   * Reserved request context identifier used to store the persistence manager used to interact with
   * JDO manager credential objects in an authorized servlet.
   */
  private static final String AUTH_PERSISTENCE_MANAGER =
      "com.google.api.client.extensions.servlet.auth.persistence_manager";

  private static final long serialVersionUID = 1L;

  private final HttpTransport httpTransport;
  private final JsonFactory jsonFactory;

  /**
   * Create an instance of the servlet.
   */
  public AbstractTwoLeggedFlowServlet() {
    httpTransport = newHttpTransportInstance();
    jsonFactory = newJsonFactoryInstance();
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
    req.setAttribute(AUTH_PERSISTENCE_MANAGER, pm);

    try {
      // Invoke the user code
      super.service(req, resp);
    } catch (HttpResponseException e) {
      // After this catch block, control flow would be returned to the servlet container, therefore
      // Google APIs client requests will have their content consumed here to make it available for
      // logging.
      e.getResponse().ignore();
      throw e;
    } finally {
      pm.close();
    }
  }

  /**
   * Fetch a credential associated with this request.
   *
   * @param req Request object to use as context for fetching the credential.
   * @return Credential object for this request and user.
   */
  protected Credential getCredential(HttpServletRequest req) throws IOException {
    PersistenceManager pm = (PersistenceManager) req.getAttribute(AUTH_PERSISTENCE_MANAGER);
    String userId = getUserId();
    TwoLeggedFlow oauthFlow = newFlow(userId);
    Credential cred = oauthFlow.loadOrCreateCredential(pm);
    return cred;
  }

  /**
   * Create a two legged flow that can be used to create credentials for accessing protected
   * resources using OAuth.
   *
   * @param userId Identifier used to associate a flow or credential object with a specific user.
   * @return Flow object that the servlet can use to create a credential object.
   */
  protected abstract TwoLeggedFlow newFlow(String userId);

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
