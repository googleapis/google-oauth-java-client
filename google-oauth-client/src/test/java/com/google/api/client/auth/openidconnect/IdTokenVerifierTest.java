/*
 * Copyright (c) 2012 Google Inc.
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

import com.google.api.client.auth.openidconnect.IdToken.Payload;
import com.google.api.client.json.webtoken.JsonWebSignature.Header;
import com.google.api.client.util.Clock;
import com.google.api.client.util.Lists;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests {@link IdTokenVerifier}.
 *
 * @author Yaniv Inbar
 */
public class IdTokenVerifierTest extends TestCase {

  private static final String CLIENT_ID = "myclientid";
  private static final String CLIENT_ID2 = CLIENT_ID + "2";

  private static final List<String> TRUSTED_CLIENT_IDS = Arrays.asList(CLIENT_ID, CLIENT_ID2);

  private static final String ISSUER = "issuer.example.com";
  private static final String ISSUER2 = ISSUER + "2";
  private static final String ISSUER3 = ISSUER + "3";

  private static IdToken newIdToken(String issuer, String audience) {
    Payload payload = new Payload();
    payload.setIssuer(issuer);
    payload.setAudience(audience);
    payload.setExpirationTimeSeconds(2000L);
    payload.setIssuedAtTimeSeconds(1000L);
    return new IdToken(new Header(), payload, new byte[0], new byte[0]);
  }

  public void testBuilder() throws Exception {
    IdTokenVerifier.Builder builder =
        new IdTokenVerifier.Builder().setIssuer(ISSUER).setAudience(TRUSTED_CLIENT_IDS);
    assertEquals(Clock.SYSTEM, builder.getClock());
    assertEquals(ISSUER, builder.getIssuer());
    assertEquals(Collections.singleton(ISSUER), builder.getIssuers());
    assertTrue(TRUSTED_CLIENT_IDS.equals(builder.getAudience()));
    Clock clock = new MyClock();
    builder.setClock(clock);
    assertEquals(clock, builder.getClock());
    IdTokenVerifier verifier = builder.build();
    assertEquals(clock, verifier.getClock());
    assertEquals(ISSUER, verifier.getIssuer());
    assertEquals(Collections.singleton(ISSUER), builder.getIssuers());
    assertEquals(TRUSTED_CLIENT_IDS, Lists.newArrayList(verifier.getAudience()));
  }

  static class MyClock implements Clock {

    long timeMillis;

    public long currentTimeMillis() {
      return timeMillis;
    }
  }

  public void testVerify() throws Exception {
    MyClock clock = new MyClock();
    IdTokenVerifier verifier = new IdTokenVerifier.Builder()
        .setIssuers(Arrays.asList(ISSUER, ISSUER3))
        .setAudience(Arrays.asList(CLIENT_ID)).setClock(clock).build();
    // verifier flexible doesn't check issuer and audience
    IdTokenVerifier verifierFlexible = new IdTokenVerifier.Builder().setClock(clock).build();
    // issuer
    clock.timeMillis = 1500000L;
    IdToken idToken = newIdToken(ISSUER, CLIENT_ID);
    assertTrue(verifier.verify(idToken));
    assertTrue(verifierFlexible.verify(newIdToken(ISSUER2, CLIENT_ID)));
    assertFalse(verifier.verify(newIdToken(ISSUER2, CLIENT_ID)));
    assertTrue(verifier.verify(newIdToken(ISSUER3, CLIENT_ID)));
    // audience
    assertTrue(verifierFlexible.verify(newIdToken(ISSUER, CLIENT_ID2)));
    assertFalse(verifier.verify(newIdToken(ISSUER, CLIENT_ID2)));
    // time
    clock.timeMillis = 700000L;
    assertTrue(verifier.verify(idToken));
    clock.timeMillis = 2300000L;
    assertTrue(verifier.verify(idToken));
    clock.timeMillis = 699999L;
    assertFalse(verifier.verify(idToken));
    clock.timeMillis = 2300001L;
    assertFalse(verifier.verify(idToken));
  }

  public void testEmptyIssuersFails() throws Exception {
    IdTokenVerifier.Builder builder = new IdTokenVerifier.Builder();
    try {
      builder.setIssuers(Collections.<String>emptyList());
      fail("Exception expected");
    } catch (IllegalArgumentException ex) {
      // Expected
    }
  }

  public void testBuilderSetNullIssuers() throws Exception {
    IdTokenVerifier.Builder builder = new IdTokenVerifier.Builder();
    IdTokenVerifier verifier = builder.build();
    assertNull(builder.getIssuers());
    assertNull(builder.getIssuer());
    assertNull(verifier.getIssuers());
    assertNull(verifier.getIssuer());

    builder.setIssuers(null);
    verifier = builder.build();
    assertNull(builder.getIssuers());
    assertNull(builder.getIssuer());
    assertNull(verifier.getIssuers());
    assertNull(verifier.getIssuer());

    builder.setIssuer(null);
    verifier = builder.build();
    assertNull(builder.getIssuers());
    assertNull(builder.getIssuer());
    assertNull(verifier.getIssuers());
    assertNull(verifier.getIssuer());
  }
}
