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
import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * OAuth 2.0 verification code receiver that runs a Jetty server on a free port, waiting for a
 * redirect with the verification code.
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * @author Yaniv Inbar
 * @since 1.11
 */
public final class LocalServerReceiver implements VerificationCodeReceiver {

    private static final String LOCALHOST = "localhost";

    private static final String CALLBACK_PATH = "/Callback";

    /**
     * Server or {@code null} before {@link #getRedirectUri()}.
     */
    private HttpServer server;

    /**
     * Verification code or {@code null} for none.
     */
    String code;

    /**
     * Error code or {@code null} for none.
     */
    String error;

    /**
     * To block until receiving an authorization response or stop() is called.
     */
    final Semaphore waitUnlessSignaled = new Semaphore(0 /* initially zero permit */);

    /**
     * Port to use or {@code -1} to select an unused port in {@link #getRedirectUri()}.
     */
    private int port;

    /**
     * Host name to use.
     */
    private final String host;

    /**
     * Callback path of redirect_uri.
     */
    private final String callbackPath;

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
        this(LOCALHOST, -1, CALLBACK_PATH, null, null);
    }

    /**
     * Constructor.
     *
     * @param host Host name to use
     * @param port Port to use or {@code -1} to select an unused port
     */
    LocalServerReceiver(String host, int port,
                        String successLandingPageUrl, String failureLandingPageUrl) {
        this(host, port, CALLBACK_PATH, successLandingPageUrl, failureLandingPageUrl);
    }

    /**
     * Constructor.
     *
     * @param host Host name to use
     * @param port Port to use or {@code -1} to select an unused port
     */
    LocalServerReceiver(String host, int port, String callbackPath,
                        String successLandingPageUrl, String failureLandingPageUrl) {
        this.host = host;
        this.port = port;
        this.callbackPath = callbackPath;
        this.successLandingPageUrl = successLandingPageUrl;
        this.failureLandingPageUrl = failureLandingPageUrl;
    }

    @Override
    public String getRedirectUri() throws IOException {

        server = HttpServer.create(new InetSocketAddress(port != -1 ? port : findOpenPort()), 0);
        HttpContext context = server.createContext(callbackPath, new CallbackHandler());
        server.setExecutor(null);
/*
    server = new Server(port != -1 ? port : 0);
    ServerConnector connector = new ServerConnector(server);
    connector.setHost(host);
    server.setConnectors(new Connector[] { connector } );
    server.setHandler(new CallbackHandler());
*/
        try {
            server.start();
            port = server.getAddress().getPort();
        } catch (Exception e) {
            Throwables.propagateIfPossible(e);
            throw new IOException(e);
        }
        return "http://" + this.getHost() + ":" + port + callbackPath;
    }

    /*
     *Copied from Jetty findFreePort() as referenced by: https://gist.github.com/vorburger/3429822
     */

    private int findOpenPort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            socket.setReuseAddress(true);
            int port = socket.getLocalPort();
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore IOException on close()
            }
            return port;
        } catch (IOException e) {
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        throw new IllegalStateException("No free TCP/IP port to start embedded HTTP Server on");
    }

    /**
     * Blocks until the server receives a login result, or the server is stopped
     * by {@link #stop()}, to return an authorization code.
     *
     * @return authorization code if login succeeds; may return {@code null} if the server
     * is stopped by {@link #stop()}
     * @throws IOException if the server receives an error code (through an HTTP request
     *                     parameter {@code error})
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
                server.stop(0);
            } catch (Exception e) {
                Throwables.propagateIfPossible(e);
                throw new IOException(e);
            }
            server = null;
        }
    }

    /**
     * Returns the host name to use.
     */
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
     * Returns callback path used in redirect_uri.
     */
    public String getCallbackPath() {
        return callbackPath;
    }

    /**
     * Builder.
     *
     * <p>
     * Implementation is not thread-safe.
     * </p>
     */
    public static final class Builder {

        /**
         * Host name to use.
         */
        private String host = LOCALHOST;

        /**
         * Port to use or {@code -1} to select an unused port.
         */
        private int port = -1;

        private String successLandingPageUrl;
        private String failureLandingPageUrl;

        private String callbackPath = CALLBACK_PATH;

        /**
         * Builds the {@link LocalServerReceiver}.
         */
        public LocalServerReceiver build() {
            return new LocalServerReceiver(host, port, callbackPath,
                    successLandingPageUrl, failureLandingPageUrl);
        }

        /**
         * Returns the host name to use.
         */
        public String getHost() {
            return host;
        }

        /**
         * Sets the host name to use.
         */
        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        /**
         * Returns the port to use or {@code -1} to select an unused port.
         */
        public int getPort() {
            return port;
        }

        /**
         * Sets the port to use or {@code -1} to select an unused port.
         */
        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        /**
         * Returns the callback path of redirect_uri.
         */
        public String getCallbackPath() {
            return callbackPath;
        }

        /**
         * Set the callback path of redirect_uri.
         */
        public Builder setCallbackPath(String callbackPath) {
            this.callbackPath = callbackPath;
            return this;
        }

        public Builder setLandingPages(String successLandingPageUrl, String failureLandingPageUrl) {
            this.successLandingPageUrl = successLandingPageUrl;
            this.failureLandingPageUrl = failureLandingPageUrl;
            return this;
        }
    }

    /**
     * HttpServer handler that takes the verifier token passed over
     * from the OAuth provider and stashes it
     * where {@link #waitForCode} will find it.
     */
    class CallbackHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            if (!callbackPath.equals(httpExchange.getRequestURI().getPath())) {
                return;
            }

            StringBuilder body = new StringBuilder();

            try {
                Map<String, String> parms =
                        this.queryToMap(httpExchange.getRequestURI().getQuery());
                error = parms.get("error");
                code = parms.get("code");

                Headers respHeaders = httpExchange.getResponseHeaders();
                if (error == null && successLandingPageUrl != null) {
                    respHeaders.add("Location", successLandingPageUrl);
                    httpExchange.sendResponseHeaders(302, -1);
                } else if (error != null && failureLandingPageUrl != null) {
                    respHeaders.add("Location", failureLandingPageUrl);
                    httpExchange.sendResponseHeaders(302, -1);
                } else {
                    writeLandingHtml(httpExchange, respHeaders);
                }
                httpExchange.close();
            } finally {
                waitUnlessSignaled.release();
            }
        }

        private Map<String, String> queryToMap(String query) {
            Map<String, String> result = new HashMap<String, String>();
            if (query != null) {
                for (String param : query.split("&")) {
                    String pair[] = param.split("=");
                    if (pair.length > 1) {
                        result.put(pair[0], pair[1]);
                    } else {
                        result.put(pair[0], "");
                    }
                }
            }
            return result;
        }

        private void writeLandingHtml(HttpExchange exchange, Headers headers) throws IOException {
            OutputStream os = exchange.getResponseBody();
            exchange.sendResponseHeaders(200, 0);
            headers.add("ContentType", "text/html");

            PrintWriter doc = new PrintWriter(os);
            doc.println("<html>");
            doc.println("<head><title>OAuth 2.0 Authentication Token Received</title></head>");
            doc.println("<body>");
            doc.println("Received verification code. You may now close this window.");
            doc.println("</body>");
            doc.println("</html>");
            doc.flush();
            os.close();
        }

    }

}
