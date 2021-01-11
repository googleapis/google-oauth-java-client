/*
 * Copyright 2020 Google Inc.
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

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.testing.http.HttpTesting;
import com.google.api.client.testing.http.MockHttpTransport;
import java.util.Map;
import junit.framework.TestCase;
import static org.junit.Assert.assertThrows;
import org.junit.function.ThrowingRunnable;

/**
 * Tests {@link JWTAuthentication}.
 *
 * @author Jun Ying
 */
public class JWTAuthenticationTest extends TestCase {

  private static final String JWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzM4NCJ9";

  public void test() throws Exception {
    TokenRequest request =
            new ClientCredentialsTokenRequest(new MockHttpTransport(), new JacksonFactory(),
                    new GenericUrl(HttpTesting.SIMPLE_GENERIC_URL.toString()));

    JWTAuthentication auth =
            new JWTAuthentication(JWT);

    assertEquals(JWT, auth.getJWT());

    request.setGrantType(JWTAuthentication.GRANT_TYPE_CLIENT_CREDENTIALS);

    request.setClientAuthentication(auth);

    HttpRequest httpRequest = request.executeUnparsed().getRequest();
    auth.intercept(httpRequest);
    UrlEncodedContent content = (UrlEncodedContent) httpRequest.getContent();
    @SuppressWarnings("unchecked")
    Map<String, ?> data = (Map<String, ?>) content.getData();
    assertEquals(JWT, data.get("client_assertion"));
    assertEquals(JWTAuthentication.GRANT_TYPE_CLIENT_CREDENTIALS, data.get("grant_type"));
  }

  public void testNoGrantType() throws Exception {
    HttpRequest request =
        new MockHttpTransport()
            .createRequestFactory()
            .buildGetRequest(HttpTesting.SIMPLE_GENERIC_URL);
    JWTAuthentication auth =
        new JWTAuthentication(JWT);
    assertEquals(JWT, auth.getJWT());
    auth.intercept(request);
    UrlEncodedContent content = (UrlEncodedContent) request.getContent();
    @SuppressWarnings("unchecked")
    Map<String, ?> data = (Map<String, ?>) content.getData();
    assertEquals(JWT, data.get("client_assertion"));
    assertEquals(JWTAuthentication.GRANT_TYPE_CLIENT_CREDENTIALS, data.get("grant_type"));
  }

  public void testInvalidGrantType() {
    final TokenRequest request =
            new ClientCredentialsTokenRequest(new MockHttpTransport(), new JacksonFactory(),
                    new GenericUrl(HttpTesting.SIMPLE_GENERIC_URL.toString()));

    JWTAuthentication auth =
            new JWTAuthentication(JWT);

    assertEquals(JWT, auth.getJWT());

    request.setGrantType("invalid");

    request.setClientAuthentication(auth);


    assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
      @Override
      public void run() throws Throwable {
        request.executeUnparsed();
      }
    });
  }

  public void test_noJWT() {
    assertThrows(RuntimeException.class, new ThrowingRunnable() {
      @Override
      public void run() {
        JWTAuthentication auth = new JWTAuthentication(null);
      }
    });
  }
}
