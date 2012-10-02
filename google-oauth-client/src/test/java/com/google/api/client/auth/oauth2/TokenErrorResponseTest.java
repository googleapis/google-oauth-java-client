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

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;

import junit.framework.TestCase;

/**
 * Tests {@link TokenErrorResponse}.
 *
 * @author Yaniv Inbar
 */
public class TokenErrorResponseTest extends TestCase {

  private static final String JSON = "{\"error\":\"invalid_request\","
      + "\"error_uri\":\"http://www.example.com/error\","
      + "\"error_description\":\"error description\"}";

  public void test() throws Exception {
    JsonFactory jsonFactory = new JacksonFactory();
    TokenErrorResponse response = jsonFactory.fromString(JSON, TokenErrorResponse.class);
    assertEquals("invalid_request", response.getError());
    assertEquals("http://www.example.com/error", response.getErrorUri());
    assertEquals("error description", response.getErrorDescription());
  }
}
