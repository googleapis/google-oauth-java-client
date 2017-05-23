/*
 * Copyright (c) 2017 Google Inc.
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

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;

import com.google.api.client.json.jackson.JacksonFactory;
import org.junit.Test;

/**
 * Unit tests for class {@link IdTokenResponse}.
 *
 * @author Michael Hausegger, hausegger.michael@googlemail.com
 **/
public class IdTokenResponseTest {


	@Test(expected = NullPointerException.class)
	public void testSetTokenTypeThrowsNullPointerException() throws Exception {

		IdTokenResponse idTokenResponse = new IdTokenResponse();

		idTokenResponse.setTokenType(null);
	}


	@Test(expected = NullPointerException.class)
	public void testSetIdTokenThrowsNullPointerException() throws Exception {

		IdTokenResponse idTokenResponse = new IdTokenResponse();

		idTokenResponse.setIdToken(null);
	}


	@Test(expected = NullPointerException.class)
	public void testSetAccessTokenThrowsNullPointerException() throws Exception {

		IdTokenResponse idTokenResponse = new IdTokenResponse();

		idTokenResponse.setAccessToken(null);
	}


	@Test(expected = IllegalArgumentException.class)
	public void testParseIdTokenThrowsIllegalArgumentException() throws Exception {

		IdTokenResponse idTokenResponse = new IdTokenResponse();
		JacksonFactory jacksonFactory = new JacksonFactory();
		idTokenResponse.setFactory(jacksonFactory);
		IdTokenResponse idTokenResponseTwo = idTokenResponse.setIdToken("");

		idTokenResponseTwo.parseIdToken();
	}


	@Test(expected = NullPointerException.class)
	public void testExecuteThrowsNullPointerExceptionAndExecuteWithNull() throws Exception {

		IdTokenResponse.execute(null);
	}


	@Test
	public void testSetGetIdToken() throws Exception {

		IdTokenResponse idTokenResponseOne = new IdTokenResponse();
		IdTokenResponse idTokenResponseTwo = idTokenResponseOne.setIdToken("a");

		assertNull(idTokenResponseOne.getScope());
		assertEquals("a", idTokenResponseOne.getIdToken());

		assertNull(idTokenResponseOne.getTokenType());
		assertNull(idTokenResponseOne.getAccessToken());

		assertNull(idTokenResponseOne.getRefreshToken());
		assertEquals("a", idTokenResponseTwo.getIdToken());

		assertNull(idTokenResponseTwo.getAccessToken());
		assertNull(idTokenResponseTwo.getRefreshToken());

		assertNull(idTokenResponseTwo.getScope());
		assertNull(idTokenResponseTwo.getTokenType());

		assertSame(idTokenResponseOne, idTokenResponseTwo);
		assertSame(idTokenResponseTwo, idTokenResponseOne);

		String idTokenString = idTokenResponseOne.getIdToken();

		assertEquals("a", idTokenString);
		assertNull(idTokenResponseOne.getScope());

		assertEquals("a", idTokenResponseOne.getIdToken());
		assertNull(idTokenResponseOne.getTokenType());

		assertNull(idTokenResponseOne.getAccessToken());
		assertNull(idTokenResponseOne.getRefreshToken());

		assertNotNull(idTokenString);
		assertSame(idTokenResponseOne, idTokenResponseTwo);
	}
}