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

package com.google.api.client.extensions.java6.auth.oauth2;

import java.util.Scanner;

/**
 * OAuth 2.0 abstract verification code receiver that prompts user to paste the code copied from the
 * browser.
 *
 * <p>
 * Implementation is thread-safe.
 * </p>
 *
 * @since 1.11
 * @author Yaniv Inbar
 */
public abstract class AbstractPromptReceiver implements VerificationCodeReceiver {

  @SuppressWarnings("resource")
  @Override
  public String waitForCode() {
    String code;
    do {
      System.out.print("Please enter code: ");
      code = new Scanner(System.in).nextLine();
    } while (code.isEmpty());
    return code;
  }

  @Override
  public void stop() {
  }
}
