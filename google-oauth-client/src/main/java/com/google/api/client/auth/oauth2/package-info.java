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

/**
 * Implementation of the <a href="http://tools.ietf.org/html/rfc6749">OAuth 2.0 Authorization
 * Framework</a>.
 *
 * <p>
 * Before using this library, you will typically need to register your application with the
 * authorization server to receive a client ID and client secret. See <a
 * href="http://tools.ietf.org/html/rfc6749#section-2">Client Registration</a>.
 * </p>
 *
 * <p>
 * These are the typical steps of the web server flow based on an authorization code, as specified
 * in <a href="http://tools.ietf.org/html/rfc6749#section-4.1">Authorization Code Grant</a>:
 * <ul>
 * <li>Redirect the end user in the browser to the authorization page using
 * {@link com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl} to grant your application
 * access to the end user's protected data.</li>
 * <li>Process the authorization response using
 * {@link com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl} to parse the authorization
 * code.</li>
 * <li>Request an access token and possibly a refresh token using
 * {@link com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest}.</li>
 * <li>Access protected resources using {@link com.google.api.client.auth.oauth2.Credential}.
 * Expired access tokens will automatically be refreshed using the refresh token (if applicable).
 * </li>
 * </ul>
 * </p>
 *
 * <p>
 * These are the typical steps of the the browser-based client flow specified in <a
 * href="http://tools.ietf.org/html/rfc6749#section-4.2">Implicit Grant</a>:
 * <ul>
 * <li>Redirect the end user in the browser to the authorization page using
 * {@link com.google.api.client.auth.oauth2.BrowserClientRequestUrl} to grant your browser
 * application access to the end user's protected data.</li>
 * <li>Use a JavaScript application to process the access token found in the URL fragment at the
 * redirect URI registered with the authorization server.</li>
 * </ul>
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */

package com.google.api.client.auth.oauth2;

