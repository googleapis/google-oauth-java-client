/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.api.client.auth.oauth;

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Data;
import com.google.api.client.util.escape.PercentEscaper;
import com.google.common.collect.Multiset;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Map;

/**
 * {@link Beta} <br/>
 * OAuth 1.0a parameter manager.
 * <p>
 * The only required non-computed fields are {@link #signer} and {@link #consumerKey}. Use
 * {@link #token} to specify token or temporary credentials.
 *
 * <p>
 * Sample usage, taking advantage that this class implements {@link HttpRequestInitializer}:
 * </p>
 *
 * <pre>
  public static HttpRequestFactory createRequestFactory(HttpTransport transport) {
    OAuthParameters parameters = new OAuthParameters();
    // ...
    return transport.createRequestFactory(parameters);
  }
 * </pre>
 *
 * <p>
 * If you have a custom request initializer, take a look at the sample usage for
 * {@link HttpExecuteInterceptor}, which this class also implements.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
@Beta
public final class OAuthParameters implements HttpExecuteInterceptor, HttpRequestInitializer
{

  /** Secure random number generator to sign requests. */
  private static final SecureRandom RANDOM = new SecureRandom();

  /** Required OAuth signature algorithm. */
  public OAuthSigner signer;

  /**
   * Absolute URI back to which the server will redirect the resource owner when the Resource Owner
   * Authorization step is completed.
   */
  public String callback;

  /**
   * Required identifier portion of the client credentials (equivalent to a username).
   */
  public String consumerKey;

  /** Required nonce value. Should be computed using {@link #computeNonce()}. */
  public String nonce;

  /** Realm. */
  public String realm;

  /** Signature. Required but normally computed using {@link #computeSignature}. */
  public String signature;

  /**
   * Name of the signature method used by the client to sign the request. Required, but normally
   * computed using {@link #computeSignature}.
   */
  public String signatureMethod;

  /**
   * Required timestamp value. Should be computed using {@link #computeTimestamp()}.
   */
  public String timestamp;

  /**
   * Token value used to associate the request with the resource owner or {@code null} if the
   * request is not associated with a resource owner.
   */
  public String token;

  /** The verification code received from the server. */
  public String verifier;

  /**
   * Must either be "1.0" or {@code null} to skip. Provides the version of the authentication
   * process as defined in this specification.
   */
  public String version;

  private static final PercentEscaper ESCAPER = new PercentEscaper("-_.~");

  /**
   * Computes a nonce based on the hex string of a random non-negative long, setting the value of
   * the {@link #nonce} field.
   */
  public void computeNonce() {
    nonce = Long.toHexString(Math.abs(RANDOM.nextLong()));
  }

  /**
   * Computes a timestamp based on the current system time, setting the value of the
   * {@link #timestamp} field.
   */
  public void computeTimestamp() {
    timestamp = Long.toString(System.currentTimeMillis() / 1000);
  }

  /**
   * This class is used as the Entry for the SortedMultiset. Parameters are sorted lexically first
   * by key, then by value.
   */
  private static class Parameter implements Comparable<Parameter> {
    private final String key;
    private final String value;

    public Parameter(String key, String value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }

    @Override
    public int compareTo(Parameter p) {
      // Compare lexically by key, then value on ties
      int result = key.compareTo(p.key);
      return result == 0 ? value.compareTo(p.value) : result;
    }
  }

  /**
   * Computes a new signature based on the fields and the given request method and URL, setting the
   * values of the {@link #signature} and {@link #signatureMethod} fields.
   *
   * @throws GeneralSecurityException general security exception
   */
  public void computeSignature(String requestMethod, GenericUrl requestUrl)
      throws GeneralSecurityException {
    OAuthSigner signer = this.signer;
    String signatureMethod = this.signatureMethod = signer.getSignatureMethod();
    // oauth_* parameters (except oauth_signature)
    SortedMultiset<Parameter> parameters = TreeMultiset.create();
    putParameterIfValueNotNull(parameters, "oauth_callback", callback);
    putParameterIfValueNotNull(parameters, "oauth_consumer_key", consumerKey);
    putParameterIfValueNotNull(parameters, "oauth_nonce", nonce);
    putParameterIfValueNotNull(parameters, "oauth_signature_method", signatureMethod);
    putParameterIfValueNotNull(parameters, "oauth_timestamp", timestamp);
    putParameterIfValueNotNull(parameters, "oauth_token", token);
    putParameterIfValueNotNull(parameters, "oauth_verifier", verifier);
    putParameterIfValueNotNull(parameters, "oauth_version", version);
    // parse request URL for query parameters
    for (Map.Entry<String, Object> fieldEntry : requestUrl.entrySet()) {
      Object value = fieldEntry.getValue();
      if (value != null) {
        String name = fieldEntry.getKey();
        if (value instanceof Collection<?>) {
          for (Object repeatedValue : (Collection<?>) value) {
            putParameter(parameters, name, repeatedValue);
          }
        } else {
          putParameter(parameters, name, value);
        }
      }
    }
    // normalize parameters
    StringBuilder parametersBuf = new StringBuilder();
    boolean first = true;
    for (Parameter parameter : parameters.elementSet()) {
      if (first) {
        first = false;
      } else {
        parametersBuf.append('&');
      }
      parametersBuf.append(parameter.getKey());
      String value = parameter.getValue();
      if (value != null) {
        parametersBuf.append('=').append(value);
      }
    }
    String normalizedParameters = parametersBuf.toString();
    // normalize URL, removing any query parameters and possibly port
    GenericUrl normalized = new GenericUrl();
    String scheme = requestUrl.getScheme();
    normalized.setScheme(scheme);
    normalized.setHost(requestUrl.getHost());
    normalized.setPathParts(requestUrl.getPathParts());
    int port = requestUrl.getPort();
    if ("http".equals(scheme) && port == 80 || "https".equals(scheme) && port == 443) {
      port = -1;
    }
    normalized.setPort(port);
    String normalizedPath = normalized.build();
    // signature base string
    StringBuilder buf = new StringBuilder();
    buf.append(escape(requestMethod)).append('&');
    buf.append(escape(normalizedPath)).append('&');
    buf.append(escape(normalizedParameters));
    String signatureBaseString = buf.toString();
    signature = signer.computeSignature(signatureBaseString);
  }

  /**
   * Returns the {@code Authorization} header value to use with the OAuth parameter values found in
   * the fields.
   */
  public String getAuthorizationHeader() {
    StringBuilder buf = new StringBuilder("OAuth");
    appendParameter(buf, "realm", realm);
    appendParameter(buf, "oauth_callback", callback);
    appendParameter(buf, "oauth_consumer_key", consumerKey);
    appendParameter(buf, "oauth_nonce", nonce);
    appendParameter(buf, "oauth_signature", signature);
    appendParameter(buf, "oauth_signature_method", signatureMethod);
    appendParameter(buf, "oauth_timestamp", timestamp);
    appendParameter(buf, "oauth_token", token);
    appendParameter(buf, "oauth_verifier", verifier);
    appendParameter(buf, "oauth_version", version);
    // hack: we have to remove the extra ',' at the end
    return buf.substring(0, buf.length() - 1);
  }

  private void appendParameter(StringBuilder buf, String name, String value) {
    if (value != null) {
      buf.append(' ').append(escape(name)).append("=\"").append(escape(value)).append("\",");
    }
  }

  private void putParameterIfValueNotNull(
      Multiset<Parameter> parameters, String key, String value) {
    if (value != null) {
      putParameter(parameters, key, value);
    }
  }

  private void putParameter(Multiset<Parameter> parameters, String key, Object value) {
    parameters.add(new Parameter(escape(key), value == null ? null : escape(value.toString())));
  }

  /** Returns the escaped form of the given value using OAuth escaping rules. */
  public static String escape(String value) {
    return ESCAPER.escape(value);
  }

  public void initialize(HttpRequest request) throws IOException {
    request.setInterceptor(this);
  }

  public void intercept(HttpRequest request) throws IOException {
    computeNonce();
    computeTimestamp();
    try {
      GenericUrl url = request.getUrl();
      HttpContent content = request.getContent();
      Map<String, Object> urlEncodedParams = null;
      if (content instanceof UrlEncodedContent) {
        urlEncodedParams = Data.mapOf(((UrlEncodedContent) content).getData());
        url.putAll(urlEncodedParams);
      }
      computeSignature(request.getRequestMethod(), url);
      if (urlEncodedParams != null) {
        for (Map.Entry<String, Object> entry : urlEncodedParams.entrySet()) {
          url.remove(entry.getKey());
        }
      }
    } catch (GeneralSecurityException e) {
      IOException io = new IOException();
      io.initCause(e);
      throw io;
    }
    request.getHeaders().setAuthorization(getAuthorizationHeader());
  }
}
