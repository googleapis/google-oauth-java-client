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

package com.google.api.services.samples.keycloak.cmdline;

import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.util.Data;
import com.google.api.client.util.Preconditions;

import java.io.IOException;
import java.util.Map;

public class PKCEAuthentication
        implements
        HttpRequestInitializer,
        HttpExecuteInterceptor {

    private final String clientId;

    private final PKCE pkce;

    public PKCEAuthentication(String clientId, PKCE pkce) {
        this.clientId = Preconditions.checkNotNull(clientId);
        this.pkce = pkce;
    }

    public void initialize(HttpRequest request) throws IOException {
        request.setInterceptor(this);
    }

    public void intercept(HttpRequest request) throws IOException {
        Map<String, Object> data = Data.mapOf(UrlEncodedContent.getContent(request).getData());
        data.put("client_id", clientId);
        if (pkce != null) {
            data.put("code_challenge", pkce.getChallenge());
            data.put("code_challenge_method", pkce.getChallengeMethod());
        }
    }
}
