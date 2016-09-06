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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocalServerReceiverTest {

  @Test
  public void testActualPort() throws IOException {
    LocalServerReceiver receiver = new LocalServerReceiver();

    try {
      receiver.getRedirectUri();
      assertTrue(receiver.getPort() != 0);
      assertTrue(receiver.getPort() != -1);
    } finally {
      receiver.stop();
    }
  }

  @Test
    public void testRedirectUri() throws IOException {
    LocalServerReceiver receiver = new LocalServerReceiver("localhost", -1, null, null);

    try {
      String localEndpoint = receiver.getRedirectUri();
      assertEquals("http://localhost:" + receiver.getPort() + "/Callback", localEndpoint);
    } finally {
      receiver.stop();
    }
  }

  boolean forkTermianted;
  String authCode;
  String error;

  @Test
  public void testPrematureStopCancelsWaitForCode() throws IOException, InterruptedException {
    final LocalServerReceiver receiver = new LocalServerReceiver();

    try {
      receiver.getRedirectUri();

      receiver.stop();
      Thread fork = runWaitForCodeThread(receiver);

      verifyForkTermination(fork);
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void testStopCancelsWaitForCode() throws IOException, InterruptedException {
    final LocalServerReceiver receiver = new LocalServerReceiver();

    try {
      receiver.getRedirectUri();

      Thread fork = runWaitForCodeThread(receiver);
      Thread.sleep(100 /* ms */);  // Sleep for a while to make fork run into waitForCode().
      receiver.stop();

      verifyForkTermination(fork);
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void testPrematureLoginCancelsWaitForCode() throws IOException, InterruptedException {
    final LocalServerReceiver receiver = new LocalServerReceiver();

    try {
      String localEndpoint = receiver.getRedirectUri();

      sendSuccessLoginResult(localEndpoint);
      Thread fork = runWaitForCodeThread(receiver);

      verifyForkTermination(fork);
      verifyLoginSuccess();
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void testLoginCancelsWaitForCode() throws IOException, InterruptedException {
    final LocalServerReceiver receiver = new LocalServerReceiver();

    try {
      String localEndpoint = receiver.getRedirectUri();  // Start the server.

      Thread fork = runWaitForCodeThread(receiver);
      Thread.sleep(100 /* ms */);  // Sleep for a while to make fork run into waitForCode().
      sendSuccessLoginResult(localEndpoint);

      verifyForkTermination(fork);
      verifyLoginSuccess();
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void testPrematureLoginErrorCancelsWaitForCode() throws IOException, InterruptedException {
    final LocalServerReceiver receiver = new LocalServerReceiver();

    try {
      String localEndpoint = receiver.getRedirectUri();

      sendFailureLoginResult(localEndpoint);
      Thread fork = runWaitForCodeThread(receiver);

      verifyForkTermination(fork);
      verifyLoginFailure();
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void testLoginErrorCancelsWaitForCode() throws IOException, InterruptedException {
    final LocalServerReceiver receiver = new LocalServerReceiver();

    try {
      String localEndpoint = receiver.getRedirectUri();  // Start the server.

      Thread fork = runWaitForCodeThread(receiver);
      Thread.sleep(100 /* ms */);  // Sleep for a while to make fork run into waitForCode().
      sendFailureLoginResult(localEndpoint);

      verifyForkTermination(fork);
      verifyLoginFailure();
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void testWaitForCodeIsBlocked() throws IOException, InterruptedException {
    final LocalServerReceiver receiver = new LocalServerReceiver();

    try {
      receiver.getRedirectUri();

      runWaitForCodeThread(receiver);
      Thread.sleep(200);
      assertFalse(forkTermianted);
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void testStopDoesNotChangeAuthCode() throws IOException, InterruptedException {
    final LocalServerReceiver receiver = new LocalServerReceiver();

    try {
      String localEndpoint = receiver.getRedirectUri();  // Start the server.

      Thread fork = runWaitForCodeThread(receiver);
      Thread.sleep(100 /* ms */);  // Sleep for a while to make fork run into waitForCode().
      sendSuccessLoginResult(localEndpoint);
      receiver.stop();

      verifyForkTermination(fork);
      verifyLoginSuccess();
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void testStopDoesNotChangeErrorCode() throws IOException, InterruptedException {
    final LocalServerReceiver receiver = new LocalServerReceiver();

    try {
      String localEndpoint = receiver.getRedirectUri();

      Thread fork = runWaitForCodeThread(receiver);
      Thread.sleep(100 /* ms */);  // Sleep for a while to make fork run into waitForCode().
      sendFailureLoginResult(localEndpoint);
      receiver.stop();

      verifyForkTermination(fork);
      verifyLoginFailure();
    } finally {
      receiver.stop();
    }
  }

  private Thread runWaitForCodeThread(final LocalServerReceiver receiver) {
    Thread fork = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          authCode = receiver.waitForCode();
        } catch (IOException ioe) {
          error = ioe.getMessage();
        }
        finally {
          forkTermianted = true;
        }
      }
    });
    fork.start();
    return fork;
  }

  private void verifyForkTermination(Thread fork) throws InterruptedException {
    fork.join(3000 /* ms */);  // Test should pass right away. Don't wait too long.
    assertTrue(forkTermianted);
  }

  private void verifyLoginSuccess() {
    assertEquals(authCode, "some-authorization-code");
    assertNull(error);
  }

  private void verifyLoginFailure() {
    assertEquals(authCode, null);
    assertTrue(error.contains("some-error"));
  }

  private int responseCode;
  private StringBuilder responseOutput = new StringBuilder();
  private String redirectedLandingPageUrl;

  @Test
  public void testSuccessLandingPage() throws IOException, InterruptedException {
    String successLandingPageUrl = "https://www.example.com/my-success-landing-page";
    LocalServerReceiver receiver =
        new LocalServerReceiver.Builder().setLandingPages(successLandingPageUrl, null).build();

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
    LocalServerReceiver receiver =
        new LocalServerReceiver.Builder().setLandingPages(null, failureLandingPageUrl).build();

    try {
      sendFailureLoginResult(receiver.getRedirectUri());
      verifyRedirectedLandingPageUrl(failureLandingPageUrl);
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void testDefaultSuccessLandingPage() throws IOException {
    LocalServerReceiver receiver =
        new LocalServerReceiver.Builder().setLandingPages(null, null).build();

    try {
      sendSuccessLoginResult(receiver.getRedirectUri());
      verifyDefaultLandingPage();
    } finally {
      receiver.stop();
    }
  }

  @Test
  public void testDefaultFailureLandingPage() throws IOException {
    LocalServerReceiver receiver =
        new LocalServerReceiver.Builder().setLandingPages(null, null).build();

    try {
      sendFailureLoginResult(receiver.getRedirectUri());
      verifyDefaultLandingPage();
    } finally {
      receiver.stop();
    }
  }

  private void verifyRedirectedLandingPageUrl(String landingPageUrlMatch) {
    assertEquals(302, responseCode);
    assertEquals(landingPageUrlMatch, redirectedLandingPageUrl);
    assertTrue(responseOutput.toString().isEmpty());
  }

  private void verifyDefaultLandingPage() {
    assertEquals(200, responseCode);
    assertNull(redirectedLandingPageUrl);
    assertTrue(responseOutput.toString().contains("<html>"));
    assertTrue(responseOutput.toString().contains("</html>"));
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
