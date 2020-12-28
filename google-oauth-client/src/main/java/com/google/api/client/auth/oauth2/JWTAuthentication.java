/*
 * Copyright (c) 2021 Jun Ying.
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

package com.google.api.client.auth.oauth2;

import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.util.Data;
import com.google.api.client.util.Preconditions;

import java.io.IOException;
import java.util.Map;

/**
 * Client credentials specified as URL-encoded parameters in the HTTP request body as specified in
 * <a href="https://tools.ietf.org/html/rfc7523">JSON Web Token (JWT) Profile
 *       for OAuth 2.0 Client Authentication and Authorization Grants</a>
 *
 * <p>This implementation assumes that the {@link HttpRequest#getContent()} is {@code null} or an
 * instance of {@link UrlEncodedContent}. This is used as the client authentication in {@link
 * TokenRequest#setClientAuthentication(HttpExecuteInterceptor)}.
 *
 * <p>
 *     To use JWT authentication, grant_type must be "client_credentials".
 *     If AuthorizationCodeTokenRequest.setGrantType() is called, set it to
 *     JWTAuthentication.GRANT_TYPE_CLIENT_CREDENTIALS. It can also be left
 *     uncalled. Setting it to any other value will cause an IllegalArgumentException.
 * </p>
 *
 * <p>Sample usage:
 *
 * <pre>
 * static void requestAccessToken() throws IOException {
 * try {
 * TokenResponse response = new AuthorizationCodeTokenRequest(new NetHttpTransport(),
 * new JacksonFactory(), new GenericUrl("https://server.example.com/token")
 * .setGrantType(JWTAuthentication.GRANT_TYPE_CLIENT_CREDENTIALS)
 * .setClientAuthentication(
 * new JWTAuthentication("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzM4NCJ9...")).execute();
 * System.out.println("Access token: " + response.getAccessToken());
 * } catch (TokenResponseException e) {
 * if (e.getDetails() != null) {
 * System.err.println("Error: " + e.getDetails().getError());
 * if (e.getDetails().getErrorDescription() != null) {
 * System.err.println(e.getDetails().getErrorDescription());
 * }
 * if (e.getDetails().getErrorUri() != null) {
 * System.err.println(e.getDetails().getErrorUri());
 * }
 * } else {
 * System.err.println(e.getMessage());
 * }
 * }
 * }
 * </pre>
 *
 * <p>Implementation is immutable and thread-safe.
 *
 * @author Jun Ying
 */

public class JWTAuthentication
    implements HttpRequestInitializer, HttpExecuteInterceptor {

    public static final String GRANT_TYPE_KEY = "grant_type";

    /** Predefined value for grant_type when using JWT **/
    public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

    public static final String CLIENT_ASSERTION_TYPE_KEY = "client_assertion_type";

    /** Predefined value for client_assertion_type when using JWT **/
    public static final String CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    public static final String CLIENT_ASSERTION_KEY = "client_assertion";

    /** JWT for authentication. */
    private final String jwt;

    /**
     * @param jwt JWT used for authentication
     */
    public JWTAuthentication(String jwt) {
        this.jwt = Preconditions.checkNotNull(jwt);
    }

    public void initialize(HttpRequest request) throws IOException {
        request.setInterceptor(this);
    }

    public void intercept(HttpRequest request) {
        Map<String, Object> data = Data.mapOf(UrlEncodedContent.getContent(request).getData());
        if (!data.containsKey(GRANT_TYPE_KEY)) {
            data.put(GRANT_TYPE_KEY, GRANT_TYPE_CLIENT_CREDENTIALS);
        } else {
            String grantType = (String) data.get(GRANT_TYPE_KEY);
            if (!grantType.equals(GRANT_TYPE_CLIENT_CREDENTIALS)) {
                throw new IllegalArgumentException(GRANT_TYPE_KEY
                        + " must be "
                        + GRANT_TYPE_CLIENT_CREDENTIALS
                        + ", not "
                        + grantType
                        + ".");
            }
        }
        data.put(CLIENT_ASSERTION_TYPE_KEY, CLIENT_ASSERTION_TYPE);
        data.put(CLIENT_ASSERTION_KEY, jwt);
    }

    /** Returns the JWT. */
    public final String getJWT() {
        return jwt;
    }
}
