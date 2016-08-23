/*
 * Copyright (c) 2016 Google Inc.
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

package com.google.api.client.extensions.jetty.auth.oauth2;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocalServerReceiverTest {

  private int responseCode;
  private StringBuilder responseOutput = new StringBuilder();
  private String redirectedLandingPageUrl;

  @Test
  public void testNumericLocalhost() {
    Assert.assertEquals("127.0.0.1", new LocalServerReceiver().getHost());
  }

  @Test
  public void testSuccessLandingPage() throws IOException, InterruptedException {
    String successLandingPageUrl = "https://www.example.com/my-success-landing-page";
    LocalServerReceiver receiver = new LocalServerReceiver(successLandingPageUrl, null);

    try {
      sendSuccessLoginResult(receiver.getRedirectUri());
      verifyRedirectedLandingPageUrl(successLandingPageUrl);
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void testFailureLandingPage() throws IOException {
    String failureLandingPageUrl = "https://www.example.com/my-failure-landing-page";
    LocalServerReceiver receiver = new LocalServerReceiver(null, failureLandingPageUrl);

    try {
      sendFailureLoginResult(receiver.getRedirectUri());
      verifyRedirectedLandingPageUrl(failureLandingPageUrl);
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void testDefaultSuccessLandingPage() throws IOException {
    LocalServerReceiver receiver = new LocalServerReceiver(null, null);

    try {
      sendSuccessLoginResult(receiver.getRedirectUri());
      verifyDefaultLandingPage();
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void testDefaultFailureLandingPage() throws IOException {
    LocalServerReceiver receiver = new LocalServerReceiver(null, null);

    try {
      sendFailureLoginResult(receiver.getRedirectUri());
      verifyDefaultLandingPage();
    } finally {
      receiver.stop();
    }
  }

  private void verifyRedirectedLandingPageUrl(String landingPageUrlMatch) {
    Assert.assertEquals(302, responseCode);
    Assert.assertEquals(landingPageUrlMatch, redirectedLandingPageUrl);
    Assert.assertTrue(responseOutput.toString().isEmpty());
  }

  private void verifyDefaultLandingPage() {
    Assert.assertEquals(200, responseCode);
    Assert.assertNull(redirectedLandingPageUrl);
    Assert.assertTrue(responseOutput.toString().contains("<html>"));
    Assert.assertTrue(responseOutput.toString().contains("</html>"));
  }

  private void sendSuccessLoginResult(String serverEndpoint) throws IOException {
    sendLoginResult(serverEndpoint, "?code=some-authorization-code");
  }

  private void sendFailureLoginResult(String serverEndpoint) throws IOException {
    sendLoginResult(serverEndpoint, "?error=some-error");
  }

  private void sendLoginResult(final String serverEndpoint, final String parameters)
      throws IOException {
    HttpURLConnection connection = null;

    try {
      URL url = new URL(serverEndpoint + parameters);
      connection = (HttpURLConnection) url.openConnection();
      connection.setConnectTimeout(2000 /* ms */);
      connection.setReadTimeout(2000 /* ms */);
      responseCode = connection.getResponseCode();
      redirectedLandingPageUrl = connection.getHeaderField("Location");

      InputStreamReader reader = new InputStreamReader(connection.getInputStream(), "UTF-8");
      for (int ch = reader.read(); ch != -1; ch = reader.read()) {
        responseOutput.append((char) ch);
      }
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }
}
