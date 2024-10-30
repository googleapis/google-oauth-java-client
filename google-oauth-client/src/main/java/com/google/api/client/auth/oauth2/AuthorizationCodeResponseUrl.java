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

import com.google.api.client.http.GenericUrl;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;

/**
 * OAuth 2.0 URL parser for the redirect URL after end user grants or denies authorization as
 * specified in <a href="http://tools.ietf.org/html/rfc6749#section-4.1.2">Authorization
 * Response</a>.
 *
 * <p>Check if {@link #getError()} is {@code null} to check if the end-user granted authorization.
 *
 * <p>Sample usage:
 *
 * <pre>{@code
 * public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
 * StringBuffer fullUrlBuf = request.getRequestURL();
 * if (request.getQueryString() != null) {
 * fullUrlBuf.append('?').append(request.getQueryString());
 * }
 * AuthorizationCodeResponseUrl authResponse =
 * new AuthorizationCodeResponseUrl(fullUrlBuf.toString());
 * // check for user-denied error
 * if (authResponse.getError() != null) {
 * // authorization denied...
 * } else {
 * // request access token using authResponse.getCode()...
 * }
 * }
 * }</pre>
 *
 * <p>Implementation is not thread-safe.
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class AuthorizationCodeResponseUrl extends GenericUrl {

  /** Authorization code generated by the authorization server or {@code null} for none. */
  @Key private String code;

  /**
   * State parameter matching the state parameter in the authorization request or {@code null} for
   * none.
   */
  @Key private String state;

  /**
   * Error code ({@code "invalid_request"}, {@code "unauthorized_client"}, {@code "access_denied"},
   * {@code "unsupported_response_type"}, {@code "invalid_scope"}, {@code "server_error"}, {@code
   * "temporarily_unavailable"}, or an extension error code as specified in <a
   * href="http://tools.ietf.org/html/rfc6749#section-8.5">Defining Additional Error Codes</a>) or
   * {@code null} for none.
   */
  @Key private String error;

  /**
   * Human-readable text providing additional information used to assist the client developer in
   * understanding the error that occurred or {@code null} for none.
   */
  @Key("error_description")
  private String errorDescription;

  /**
   * URI identifying a human-readable web page with information about the error used to provide the
   * client developer with additional information about the error or {@code null} for none.
   */
  @Key("error_uri")
  private String errorUri;

  /** @param encodedResponseUrl encoded authorization code response URL */
  public AuthorizationCodeResponseUrl(String encodedResponseUrl) {
    super(encodedResponseUrl);
    // either error or code but not both
    Preconditions.checkArgument((code == null) != (error == null));
  }

  /**
   * Returns the authorization code generated by the authorization server or {@code null} for none.
   */
  public final String getCode() {
    return code;
  }

  /**
   * Sets the authorization code generated by the authorization server or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public AuthorizationCodeResponseUrl setCode(String code) {
    this.code = code;
    return this;
  }

  /**
   * Returns the state parameter matching the state parameter in the authorization request or {@code
   * null} for none.
   */
  public final String getState() {
    return state;
  }

  /**
   * Sets the state parameter matching the state parameter in the authorization request or {@code
   * null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public AuthorizationCodeResponseUrl setState(String state) {
    this.state = state;
    return this;
  }

  /**
   * Returns the error code ({@code "invalid_request"}, {@code "unauthorized_client"}, {@code
   * "access_denied"}, {@code "unsupported_response_type"}, {@code "invalid_scope"}, {@code
   * "server_error"}, {@code "temporarily_unavailable"}, or an extension error code as specified in
   * <a href="http://tools.ietf.org/html/rfc6749#section-8.5">Defining Additional Error Codes</a>)
   * or {@code null} for none.
   */
  public final String getError() {
    return error;
  }

  /**
   * Sets the error code ({@code "invalid_request"}, {@code "unauthorized_client"}, {@code
   * "access_denied"}, {@code "unsupported_response_type"}, {@code "invalid_scope"}, {@code
   * "server_error"}, {@code "temporarily_unavailable"}, or an extension error code as specified in
   * <a href="http://tools.ietf.org/html/rfc6749#section-8.5">Defining Additional Error Codes</a>)
   * or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public AuthorizationCodeResponseUrl setError(String error) {
    this.error = error;
    return this;
  }

  /**
   * Returns the human-readable text providing additional information used to assist the client
   * developer in understanding the error that occurred or {@code null} for none.
   */
  public final String getErrorDescription() {
    return errorDescription;
  }

  /**
   * Sets the human-readable text providing additional information used to assist the client
   * developer in understanding the error that occurred or {@code null} for none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public AuthorizationCodeResponseUrl setErrorDescription(String errorDescription) {
    this.errorDescription = errorDescription;
    return this;
  }

  /**
   * Returns the URI identifying a human-readable web page with information about the error used to
   * provide the client developer with additional information about the error or {@code null} for
   * none.
   */
  public final String getErrorUri() {
    return errorUri;
  }

  /**
   * Sets the URI identifying a human-readable web page with information about the error used to
   * provide the client developer with additional information about the error or {@code null} for
   * none.
   *
   * <p>Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  public AuthorizationCodeResponseUrl setErrorUri(String errorUri) {
    this.errorUri = errorUri;
    return this;
  }

  @Override
  public AuthorizationCodeResponseUrl set(String fieldName, Object value) {
    return (AuthorizationCodeResponseUrl) super.set(fieldName, value);
  }

  @Override
  public AuthorizationCodeResponseUrl clone() {
    return (AuthorizationCodeResponseUrl) super.clone();
  }
}
