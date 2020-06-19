/*
 * Copyright (c) 2020 Google Inc.
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

package com.google.api.services.samples.keycloak.cmdline;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * A sample application that demonstrates how the Google OAuth2 library can be used to authenticate
 * against a locally running Keycloak server with a registered public client where using
 * <a href="https://tools.ietf.org/html/rfc7636">PKCE</a> is required.
 *
 * Please note that before running this sample application, a local Keycloak server must be running
 * and a PKCE enabled client must have been defined. Please see
 * <code>samples/keycloak-pkce-cmdline-sample/scripts/initialize-keycloak.sh</code> for further
 * information.
 *
 * @author Stefan Freyr Stefansson
 */
public class PKCESample {
    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
     * globally shared instance across your application.
     */
    private static DataStoreFactory DATA_STORE_FACTORY;

    /** OAuth 2 scope. */
    private static final String SCOPE = "email";

    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private static final String TOKEN_SERVER_URL = "http://127.0.0.1:8080/auth/realms/master/protocol/openid-connect/token";
    private static final String AUTHORIZATION_SERVER_URL = "http://127.0.0.1:8080/auth/realms/master/protocol/openid-connect/auth";

    /** Authorizes the installed application to access user's protected data. */
    private static Credential authorize() throws Exception {
        // set up authorization code flow
        String clientId = "pkce-test-client";
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                HTTP_TRANSPORT,
                JSON_FACTORY,
                new GenericUrl(TOKEN_SERVER_URL),
                new ClientParametersAuthentication(clientId, null),
                clientId,
                AUTHORIZATION_SERVER_URL)
                .setScopes(Arrays.asList(SCOPE))
                .enablePKCE()
                .setDataStoreFactory(DATA_STORE_FACTORY).build();
        // authorize
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setHost("127.0.0.1").build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String[] args) {
        try {
            DATA_STORE_FACTORY = new MemoryDataStoreFactory();
            final Credential credential = authorize();
            System.out.println("Successfully obtained credential from Keycloak running on localhost.");
            final String accessToken = credential.getAccessToken();
            System.out.println("Retrieved an access token of length " + accessToken.length());
            return;
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.exit(1);
    }
}
