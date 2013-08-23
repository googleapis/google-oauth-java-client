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

package com.google.api.client.extensions.servlet.auth.oauth2;

import com.google.api.client.auth.oauth2.OAuthApplicationContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Provides information for a class that extends {@link AbstractAuthServlet}.
 *
 * @since 1.17
 * @author Nick Miceli
 * @author Eyal Peled
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthorizedServlet {

  /**
   * The redirect URI for the application. This must be given a relative URI that maps to a servlet
   * which extends {@link AbstractAuthServlet}. A servlet with this annotation may have the
   * {@code redirect} field reference itself. Most importantly, the servlet must be a registered
   * OAuth redirect URI. The advantage of this is all authorized servlets in your application can
   * reference the same servlet, allowing you to only register one URI for the whole application.
   */
  String redirect();

  /**
   * The flowData class that extends {@link OAuthApplicationContext} for your application.
   */
  Class<? extends OAuthApplicationContext> flowDataClass();

}
