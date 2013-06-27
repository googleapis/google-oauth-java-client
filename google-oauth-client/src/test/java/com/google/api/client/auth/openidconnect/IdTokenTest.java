/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.api.client.auth.openidconnect;

import junit.framework.TestCase;

import java.util.Arrays;


/**
 * Tests {@link IdToken}.
 *
 * @author Yaniv Inbar
 */
public class IdTokenTest extends TestCase {

  public void testValidateIssuer() {
    IdToken token =
        new IdToken(new IdToken.Header(), new IdToken.Payload(), new byte[0], new byte[0]);
    token.getPayload().setIssuer("issuer");
    assertTrue(token.verifyIssuer("issuer"));
    assertFalse(token.verifyIssuer("not"));
  }

  public void testValidateIssuedAtTime() {
    IdToken token =
        new IdToken(new IdToken.Header(), new IdToken.Payload(), new byte[0], new byte[0]);
    token.getPayload().setIssuedAtTimeSeconds(123L);
    assertTrue(token.verifyIssuedAtTime(123000, 1));
    assertTrue(token.verifyIssuedAtTime(122000, 1));
    assertFalse(token.verifyIssuedAtTime(121999, 1));
  }

  public void testValidateExpirationTime() {
    IdToken token =
        new IdToken(new IdToken.Header(), new IdToken.Payload(), new byte[0], new byte[0]);
    token.getPayload().setExpirationTimeSeconds(123L);
    assertTrue(token.verifyExpirationTime(123000, 1));
    assertTrue(token.verifyExpirationTime(124000, 1));
    assertFalse(token.verifyExpirationTime(124001, 1));
  }

  public void testValidateAudience() {
    IdToken token =
        new IdToken(new IdToken.Header(), new IdToken.Payload(), new byte[0], new byte[0]);
    token.getPayload().setAudience("cid");
    assertTrue(token.verifyAudience(Arrays.asList("cid")));
    assertFalse(token.verifyAudience(Arrays.asList("cid2")));
    token.getPayload().setAudience(Arrays.asList("cid"));
    assertTrue(token.verifyAudience(Arrays.asList("cid")));
    assertFalse(token.verifyAudience(Arrays.asList("cid2")));
    token.getPayload().setAudience(Arrays.asList("cid", "cid2"));
    assertTrue(token.verifyAudience(Arrays.asList("cid", "cid2")));
    assertFalse(token.verifyAudience(Arrays.asList("cid")));
    assertFalse(token.verifyAudience(Arrays.asList("cid2")));
    assertFalse(token.verifyAudience(Arrays.asList("cid", "cid3")));
  }

  public void testValidateTime() {
    IdToken token =
        new IdToken(new IdToken.Header(), new IdToken.Payload(), new byte[0], new byte[0]);
    token.getPayload().setExpirationTimeSeconds(123L).setIssuedAtTimeSeconds(123L);
    assertTrue(token.verifyTime(123000, 1));
    assertTrue(token.verifyTime(122000, 1));
    assertFalse(token.verifyTime(121999, 1));
    assertTrue(token.verifyTime(123000, 1));
    assertTrue(token.verifyTime(124000, 1));
    assertFalse(token.verifyTime(124001, 1));
  }

  public void testConstructor() {
    try {
      new IdToken(new IdToken.Header(), new IdToken.Payload(), null, new byte[0]);
      fail("expected " + NullPointerException.class);
    } catch (NullPointerException e) {
      // expected
    }
    try {
      new IdToken(new IdToken.Header(), new IdToken.Payload(), new byte[0], null);
      fail("expected " + NullPointerException.class);
    } catch (NullPointerException e) {
      // expected
    }
  }
}
