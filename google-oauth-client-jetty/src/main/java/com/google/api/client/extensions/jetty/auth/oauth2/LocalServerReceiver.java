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

package com.google.api.client.extensions.jetty.auth.oauth2;

import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.util.Throwables;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Semaphore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * OAuth 2.0 verification code receiver that runs a Jetty server on a free port, waiting for a
 * redirect with the verification code.
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * @since 1.11
 * @author Yaniv Inbar
 */
public final class LocalServerReceiver implements VerificationCodeReceiver {

  private static final String LOCALHOST = "localhost";

  private static final String CALLBACK_PATH = "/Callback";

  /** Server or {@code null} before {@link #getRedirectUri()}. */
  private Server server;

  /** Verification code or {@code null} for none. */
  String code;

  /** Error code or {@code null} for none. */
  String error;

  /** To block until receiving an authorization response or stop() is called. */
  final Semaphore waitUnlessSignaled = new Semaphore(0 /* initially zero permit */);

  /** Port to use or {@code -1} to select an unused port in {@link #getRedirectUri()}. */
  private int port;

  /** Host name to use. */
  private final String host;

  /**
   * URL to an HTML page to be shown (via redirect) after successful login. If null, a canned
   * default landing page will be shown (via direct response).
   */
  private String successLandingPageUrl;

  /**
   * URL to an HTML page to be shown (via redirect) after failed login. If null, a canned
   * default landing page will be shown (via direct response).
   */
  private String failureLandingPageUrl;

  /**
   * Constructor that starts the server on {@link #LOCALHOST} and an unused port.
   *
   * <p>
   * Use {@link Builder} if you need to specify any of the optional parameters.
   * </p>
   */
  public LocalServerReceiver() {
    this(LOCALHOST, -1, null, null);
  }

  /**
   * Constructor.
   *
   * @param host Host name to use
   * @param port Port to use or {@code -1} to select an unused port
   */
  LocalServerReceiver(String host, int port,
                      String successLandingPageUrl, String failureLandingPageUrl) {
    this.host = host;
    this.port = port;
    this.successLandingPageUrl = successLandingPageUrl;
    this.failureLandingPageUrl = failureLandingPageUrl;
  }

  @Override
  public String getRedirectUri() throws IOException {
    server = new Server(port != -1 ? port : 0);
    Connector connector = server.getConnectors()[0];
    connector.setHost(host);
    server.addHandler(new CallbackHandler());
    try {
      server.start();
      port = connector.getLocalPort();
    } catch (Exception e) {
      Throwables.propagateIfPossible(e);
      throw new IOException(e);
    }
    return "http://" + host + ":" + port + CALLBACK_PATH;
  }

  /**
   * Blocks until the server receives a login result, or the server is stopped
   * by {@link #stop()}, to return an authorization code.
   *
   * @return authorization code if login succeeds; may return {@code null} if the server
   *    is stopped by {@link #stop()}
   * @throws IOException if the server receives an error code (through an HTTP request
   *    parameter {@code error})
   */
  @Override
  public String waitForCode() throws IOException {
    waitUnlessSignaled.acquireUninterruptibly();
    if (error != null) {
      throw new IOException("User authorization failed (" + error + ")");
    }
    return code;
  }

  @Override
  public void stop() throws IOException {
    waitUnlessSignaled.release();
    if (server != null) {
      try {
        server.stop();
      } catch (Exception e) {
        Throwables.propagateIfPossible(e);
        throw new IOException(e);
      }
      server = null;
    }
  }

  /** Returns the host name to use. */
  public String getHost() {
    return host;
  }

  /**
   * Returns the port to use or {@code -1} to select an unused port in {@link #getRedirectUri()}.
   */
  public int getPort() {
    return port;
  }

  /**
   * Builder.
   *
   * <p>
   * Implementation is not thread-safe.
   * </p>
   */
  public static final class Builder {

    /** Host name to use. */
    private String host = LOCALHOST;

    /** Port to use or {@code -1} to select an unused port. */
    private int port = -1;

    private String successLandingPageUrl;
    private String failureLandingPageUrl;

    /** Builds the {@link LocalServerReceiver}. */
    public LocalServerReceiver build() {
      return new LocalServerReceiver(host, port, successLandingPageUrl, failureLandingPageUrl);
    }

    /** Returns the host name to use. */
    public String getHost() {
      return host;
    }

    /** Sets the host name to use. */
    public Builder setHost(String host) {
      this.host = host;
      return this;
    }

    /** Returns the port to use or {@code -1} to select an unused port. */
    public int getPort() {
      return port;
    }

    /** Sets the port to use or {@code -1} to select an unused port. */
    public Builder setPort(int port) {
      this.port = port;
      return this;
    }

    public Builder setLandingPages(String successLandingPageUrl, String failureLandingPageUrl) {
      this.successLandingPageUrl = successLandingPageUrl;
      this.failureLandingPageUrl = failureLandingPageUrl;
      return this;
    }
  }

  /**
   * Jetty handler that takes the verifier token passed over from the OAuth provider and stashes it
   * where {@link #waitForCode} will find it.
   */
  class CallbackHandler extends AbstractHandler {

    @Override
    public void handle(
        String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
        throws IOException {
      if (!CALLBACK_PATH.equals(target)) {
        return;
      }

      try {
        ((Request) request).setHandled(true);
        error = request.getParameter("error");
        code = request.getParameter("code");

        if (error == null && successLandingPageUrl != null) {
          response.sendRedirect(successLandingPageUrl);
        } else if (error != null && failureLandingPageUrl != null) {
          response.sendRedirect(failureLandingPageUrl);
        } else {
          writeLandingHtml(response);
        }
        response.flushBuffer();
      }
      finally {
        waitUnlessSignaled.release();
      }
    }

    private void writeLandingHtml(HttpServletResponse response) throws IOException {
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType("text/html");

      PrintWriter doc = response.getWriter();
      doc.println("<html>");
      doc.println("<head><title>OAuth 2.0 Authentication Token Received</title></head>");
      doc.println("<body>");
      doc.println("Received verification code. You may now close this window.");
      doc.println("</body>");
      doc.println("</html>");
      doc.flush();
    }
  }
}
