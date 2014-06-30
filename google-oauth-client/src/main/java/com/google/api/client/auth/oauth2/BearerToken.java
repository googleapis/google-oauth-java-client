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

package com.google.api.client.auth.oauth2;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.util.Data;
import com.google.api.client.util.Preconditions;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * OAuth 2.0 helper for accessing protected resources using the <a
 * href="http://tools.ietf.org/html/rfc6750">Bearer Token specification</a>.
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class BearerToken {

  /** Query and form-encoded parameter name. */
  static final String PARAM_NAME = "access_token";

  /**
   * In case an abnormal HTTP response is received with {@code WWW-Authenticate} header, and its
   * value contains this error pattern, we will try to refresh the token.
   */
  static final Pattern INVALID_TOKEN_ERROR =
      Pattern.compile("\\s*error\\s*=\\s*\"?invalid_token\"?");

  /**
   * Immutable and thread-safe OAuth 2.0 method for accessing protected resources using the <a
   * href="http://tools.ietf.org/html/rfc6750#section-2.1">Authorization Request Header Field</a>.
   *
   * <p>
   * According to the specification, this method MUST be supported by resource servers.
   * </p>
   */
  static final class AuthorizationHeaderAccessMethod implements Credential.AccessMethod {

    /** Authorization header prefix. */
    static final String HEADER_PREFIX = "Bearer ";

    AuthorizationHeaderAccessMethod() {
    }

    public void intercept(HttpRequest request, String accessToken) throws IOException {
      request.getHeaders().setAuthorization(HEADER_PREFIX + accessToken);
    }

    public String getAccessTokenFromRequest(HttpRequest request) {
      List<String> authorizationAsList = request.getHeaders().getAuthorizationAsList();
      if (authorizationAsList != null) {
        for (String header : authorizationAsList) {
          if (header.startsWith(HEADER_PREFIX)) {
            return header.substring(HEADER_PREFIX.length());
          }
        }
      }
      return null;
    }
  }

  /**
   * Immutable and thread-safe OAuth 2.0 method for accessing protected resources using the <a
   * href="http://tools.ietf.org/html/rfc6750#section-2.2">Form-Encoded Body Parameter</a>.
   */
  static final class FormEncodedBodyAccessMethod implements Credential.AccessMethod {

    FormEncodedBodyAccessMethod() {
    }

    public void intercept(HttpRequest request, String accessToken) throws IOException {
      Preconditions.checkArgument(
          !HttpMethods.GET.equals(request.getRequestMethod()), "HTTP GET method is not supported");
      getData(request).put(PARAM_NAME, accessToken);
    }

    public String getAccessTokenFromRequest(HttpRequest request) {
      Object bodyParam = getData(request).get(PARAM_NAME);
      return bodyParam == null ? null : bodyParam.toString();
    }

    private static Map<String, Object> getData(HttpRequest request) {
      return Data.mapOf(UrlEncodedContent.getContent(request).getData());
    }
  }

  /**
   * Immutable and thread-safe OAuth 2.0 method for accessing protected resources using the <a
   * href="http://tools.ietf.org/html/rfc6750#section-2.3">URI Query Parameter</a>.
   */
  static final class QueryParameterAccessMethod implements Credential.AccessMethod {

    QueryParameterAccessMethod() {
    }

    public void intercept(HttpRequest request, String accessToken) throws IOException {
      request.getUrl().set(PARAM_NAME, accessToken);
    }

    public String getAccessTokenFromRequest(HttpRequest request) {
      Object param = request.getUrl().get(PARAM_NAME);
      return param == null ? null : param.toString();
    }
  }

  /**
   * Returns a new instance of an immutable and thread-safe OAuth 2.0 method for accessing protected
   * resources using the <a href="http://tools.ietf.org/html/rfc6750#section-2.1">Authorization
   * Request Header Field</a>.
   *
   * <p>
   * According to the specification, this method MUST be supported by resource servers.
   * </p>
   */
  public static Credential.AccessMethod authorizationHeaderAccessMethod() {
    return new AuthorizationHeaderAccessMethod();
  }

  /**
   * Returns a new instance of an immutable and thread-safe OAuth 2.0 method for accessing protected
   * resources using the <a href="http://tools.ietf.org/html/rfc6750#section-2.2">Form-Encoded Body
   * Parameter</a>.
   */
  public static Credential.AccessMethod formEncodedBodyAccessMethod() {
    return new FormEncodedBodyAccessMethod();
  }

  /**
   * Returns a new instance of an immutable and thread-safe OAuth 2.0 method for accessing protected
   * resources using the <a href="http://tools.ietf.org/html/rfc6750#section-2.3">URI Query
   * Parameter</a>.
   */
  public static Credential.AccessMethod queryParameterAccessMethod() {
    return new QueryParameterAccessMethod();
  }
}
