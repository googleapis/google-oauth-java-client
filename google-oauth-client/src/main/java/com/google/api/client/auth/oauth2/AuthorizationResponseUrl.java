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

/**
 * OAuth 2.0 URL parser for the redirect URL after end user grants or denies authorization as
 * specified in <a
 * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-4.1.2">Authorization Response</a>
 * and <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-4.2.2">Access Token
 * Response</a>.
 * 
 * <p>
 * Implementation is not thread-safe.
 * </p>
 * 
 * @since 1.7
 * @author Yaniv Inbar
 */
public class AuthorizationResponseUrl extends GenericUrl {

  /**
   * State parameter matching the state parameter in the authorization request or {@code null} for
   * none.
   */
  @Key
  private String state;

  /**
   * Error code ({@code "invalid_request"}, {@code "unauthorized_client"}, {@code "access_denied"},
   * {@code "unsupported_response_type"}, {@code "invalid_scope"}, {@code "server_error"},
   * {@code "temporarily_unavailable"}, or an extension error code as specified in <a
   * href="http://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-8.5">Defining Additional Error
   * Codes</a>) or {@code null} for none.
   */
  @Key
  private String error;

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

  /**
   * @param encodedResponseUrl encoded response URL
   */
  public AuthorizationResponseUrl(String encodedResponseUrl) {
    super(encodedResponseUrl);
  }

  /**
   * Returns the state parameter matching the state parameter in the authorization request or
   * {@code null} for none.
   */
  public final String getState() {
    return state;
  }

  /**
   * Sets the state parameter matching the state parameter in the authorization request or
   * {@code null} for none.
   * 
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AuthorizationResponseUrl setState(String state) {
    this.state = state;
    return this;
  }

  /**
   * Returns the error code ({@code "invalid_request"}, {@code "unauthorized_client"},
   * {@code "access_denied"}, {@code "unsupported_response_type"}, {@code "invalid_scope"},
   * {@code "server_error"}, {@code "temporarily_unavailable"}, or an extension error code as
   * specified in <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-8.5">Defining
   * Additional Error Codes</a>) or {@code null} for none.
   */
  public final String getError() {
    return error;
  }

  /**
   * Sets the error code ({@code "invalid_request"}, {@code "unauthorized_client"},
   * {@code "access_denied"}, {@code "unsupported_response_type"}, {@code "invalid_scope"},
   * {@code "server_error"}, {@code "temporarily_unavailable"}, or an extension error code as
   * specified in <a href="http://tools.ietf.org/html/draft-ietf-oauth-v2-22#section-8.5">Defining
   * Additional Error Codes</a>) or {@error null} for none.
   * 
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AuthorizationResponseUrl setError(String error) {
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
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AuthorizationResponseUrl setErrorDescription(String errorDescription) {
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
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public AuthorizationResponseUrl setErrorUri(String errorUri) {
    this.errorUri = errorUri;
    return this;
  }
}
