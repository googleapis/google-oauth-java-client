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

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.google.api.client.util.Preconditions;

/**
 * OAuth 2.0 parser for an error access token response as specified in <a
 * href="http://tools.ietf.org/html/rfc6749#section-5.2">Error Response</a>.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.7
 * @author Yaniv Inbar
 */
public class TokenErrorResponse extends GenericJson {

  /**
   * Error code ({@code "invalid_request"}, {@code "invalid_client"}, {@code "invalid_grant"},
   * {@code "unauthorized_client"}, {@code "unsupported_grant_type"}, {@code "invalid_scope"}, or an
   * extension error code as specified in <a
   * href="http://tools.ietf.org/html/rfc6749#section-8.5">Defining Additional Error Codes</a>).
   */
  @Key
  private String error;

  /**
   * Human-readable text providing additional information, used to assist the client developer in
   * understanding the error that occurred or {@code null} for none.
   */
  @Key("error_description")
  private String errorDescription;

  /**
   * URI identifying a human-readable web page with information about the error, used to provide the
   * client developer with additional information about the error or {@code null} for none.
   */
  @Key("error_uri")
  private String errorUri;

  /**
   * Returns the error code ({@code "invalid_request"}, {@code "invalid_client"},
   * {@code "invalid_grant"}, {@code "unauthorized_client"}, {@code "unsupported_grant_type"},
   * {@code "invalid_scope"}, or an extension error code as specified in <a
   * href="http://tools.ietf.org/html/rfc6749#section-8.5">Defining Additional Error Codes</a>).
   */
  public final String getError() {
    return error;
  }

  /**
   * Sets the error code ({@code "invalid_request"}, {@code "invalid_client"},
   * {@code "invalid_grant"}, {@code "unauthorized_client"}, {@code "unsupported_grant_type"},
   * {@code "invalid_scope"}, or an extension error code as specified in <a
   * href="http://tools.ietf.org/html/rfc6749#section-8.5">Defining Additional Error Codes</a>).
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public TokenErrorResponse setError(String error) {
    this.error = Preconditions.checkNotNull(error);
    return this;
  }

  /**
   * Returns the human-readable text providing additional information, used to assist the client
   * developer in understanding the error that occurred or {@code null} for none.
   */
  public final String getErrorDescription() {
    return errorDescription;
  }

  /**
   * Sets the human-readable text providing additional information, used to assist the client
   * developer in understanding the error that occurred or {@code null} for none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public TokenErrorResponse setErrorDescription(String errorDescription) {
    this.errorDescription = errorDescription;
    return this;
  }

  /**
   * Returns the URI identifying a human-readable web page with information about the error, used to
   * provide the client developer with additional information about the error or {@code null} for
   * none.
   */
  public final String getErrorUri() {
    return errorUri;
  }

  /**
   * Sets the URI identifying a human-readable web page with information about the error, used to
   * provide the client developer with additional information about the error or {@code null} for
   * none.
   *
   * <p>
   * Overriding is only supported for the purpose of calling the super implementation and changing
   * the return type, but nothing else.
   * </p>
   */
  public TokenErrorResponse setErrorUri(String errorUri) {
    this.errorUri = errorUri;
    return this;
  }

  @Override
  public TokenErrorResponse set(String fieldName, Object value) {
    return (TokenErrorResponse) super.set(fieldName, value);
  }

  @Override
  public TokenErrorResponse clone() {
    return (TokenErrorResponse) super.clone();
  }
}
