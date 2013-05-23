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

package com.google.api.client.extensions.java6.auth.oauth2;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.util.Preconditions;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OAuth 2.0 authorization code flow for an installed Java application that persists end-user
 * credentials.
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * @since 1.11
 * @author Yaniv Inbar
 */
public class AuthorizationCodeInstalledApp {

  /** Authorization code flow. */
  private final AuthorizationCodeFlow flow;

  /** Verification code receiver. */
  private final VerificationCodeReceiver receiver;

  private static final Logger LOGGER =
      Logger.getLogger(AuthorizationCodeInstalledApp.class.getName());

  /**
   * @param flow authorization code flow
   * @param receiver verification code receiver
   */
  public AuthorizationCodeInstalledApp(
      AuthorizationCodeFlow flow, VerificationCodeReceiver receiver) {
    this.flow = Preconditions.checkNotNull(flow);
    this.receiver = Preconditions.checkNotNull(receiver);
  }

  /**
   * Authorizes the installed application to access user's protected data.
   *
   * @param userId user ID or {@code null} if not using a persisted credential store
   * @return credential
   */
  public Credential authorize(String userId) throws IOException {
    try {
      Credential credential = flow.loadCredential(userId);
      if (credential != null
          && (credential.getRefreshToken() != null || credential.getExpiresInSeconds() > 60)) {
        return credential;
      }
      // open in browser
      String redirectUri = receiver.getRedirectUri();
      AuthorizationCodeRequestUrl authorizationUrl =
          flow.newAuthorizationUrl().setRedirectUri(redirectUri);
      onAuthorization(authorizationUrl);
      // receive authorization code and exchange it for an access token
      String code = receiver.waitForCode();
      TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
      // store credential and return it
      return flow.createAndStoreCredential(response, userId);
    } finally {
      receiver.stop();
    }
  }

  /**
   * Handles user authorization by redirecting to the OAuth 2.0 authorization server.
   *
   * <p>
   * Default implementation is to call {@code browse(authorizationUrl.build())}. Subclasses may
   * override to provide optional parameters such as the recommended state parameter. Sample
   * implementation:
   * </p>
   *
   * <pre>
  &#64;Override
  protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) throws IOException {
    authorizationUrl.setState("xyz");
    super.onAuthorization(authorizationUrl);
  }
   * </pre>
   *
   * @param authorizationUrl authorization URL
   * @throws IOException I/O exception
   */
  protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) throws IOException {
    browse(authorizationUrl.build());
  }

  /**
   * Open a browser at the given URL using {@link Desktop} if available, or alternatively output the
   * URL to {@link System#out} for command-line applications.
   *
   * @param url URL to browse
   */
  public static void browse(String url) {
    Preconditions.checkNotNull(url);
    // Ask user to open in their browser using copy-paste
    System.out.println("Please open the following address in your browser:");
    System.out.println("  " + url);
    // Attempt to open it in the browser
    try {
      if (Desktop.isDesktopSupported()) {
        Desktop desktop = Desktop.getDesktop();
        if (desktop.isSupported(Action.BROWSE)) {
          System.out.println("Attempting to open that address in the default browser now...");
          desktop.browse(URI.create(url));
        }
      }
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Unable to open browser", e);
    } catch (InternalError e) {
      // A bug in a JRE can cause Desktop.isDesktopSupported() to throw an
      // InternalError rather than returning false. The error reads,
      // "Can't connect to X11 window server using ':0.0' as the value of the
      // DISPLAY variable." The exact error message may vary slightly.
      LOGGER.log(Level.WARNING, "Unable to open browser", e);
    }
  }

  /** Returns the authorization code flow. */
  public final AuthorizationCodeFlow getFlow() {
    return flow;
  }

  /** Returns the verification code receiver. */
  public final VerificationCodeReceiver getReceiver() {
    return receiver;
  }
}
