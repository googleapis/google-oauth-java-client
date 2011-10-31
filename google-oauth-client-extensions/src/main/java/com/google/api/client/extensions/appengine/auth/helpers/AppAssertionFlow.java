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

package com.google.api.client.extensions.appengine.auth.helpers;

import com.google.api.client.extensions.auth.helpers.Credential;
import com.google.api.client.extensions.auth.helpers.TwoLeggedFlow;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.PersistenceAware;

/**
 * Class which will allow us to create or load instances of {@link AppAssertionCredential}.
 *
 * @author moshenko@google.com (Jake Moshenko)
 *
 * @since 1.6
 */
@PersistenceAware
public class AppAssertionFlow implements TwoLeggedFlow {

  static final Logger LOGGER = Logger.getLogger(AppAssertionFlow.class.getName());

  private final String robotName;
  private final String authorizationServerUrl;
  private final String scope;
  private final String audience;
  private final HttpTransport transport;
  private final JsonFactory jsonFactory;

  /**
   * Create an instance.
   *
   * @param robotName Identifier that will eventually become the primary key for the credential
   *        object created by this flow. This is usually the application's identifier.
   * @param authorizationServerUrl Server with which we will exchange our assertion for an access
   *        token.
   * @param scope Scope (or list of scopes) for which we require access.
   * @param audience Audience that will be used when creating the assertion.
   * @param transport {@link HttpTransport} instance that will be used for network communication.
   * @param jsonFactory {@link JsonFactory} instance whtat will be used to serialize and deserialize
   *        auth server communications.
   */
  public AppAssertionFlow(String robotName,
      String authorizationServerUrl,
      String scope,
      String audience,
      HttpTransport transport,
      JsonFactory jsonFactory) {
    this.robotName = robotName;
    this.authorizationServerUrl = authorizationServerUrl;
    this.scope = scope;
    this.audience = audience;
    this.transport = transport;
    this.jsonFactory = jsonFactory;
  }

  public Credential loadOrCreateCredential(PersistenceManager pm) throws IOException {
    AppAssertionCredential cred;
    try {
      cred = pm.getObjectById(AppAssertionCredential.class, robotName);
    } catch (JDOObjectNotFoundException e) {
      LOGGER.fine("Creating new credential instance");
      cred = new AppAssertionCredential(robotName, authorizationServerUrl, scope, audience);
      pm.makePersistent(cred);
    }

    cred.postConstruct(transport, jsonFactory);
    return cred;
  }
}
