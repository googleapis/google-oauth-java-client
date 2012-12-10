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

package com.google.api.client.extensions.java7.auth.oauth2;

import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
import com.google.api.client.json.JsonFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


/**
 * Thread-safe file implementation of a credential store based on Java 7.
 *
 * @since 1.13
 * @author Yaniv Inbar
 */
public class FileCredentialStoreJava7 extends FileCredentialStore {

  /**
   * @param file File to store user credentials
   * @param jsonFactory JSON factory to serialize user credentials
   */
  public FileCredentialStoreJava7(File file, JsonFactory jsonFactory) throws IOException {
    super(file, jsonFactory);
  }

  @Override
  protected boolean isSymbolicLink(File file) throws IOException {
    return Files.isSymbolicLink(file.toPath());
  }
}
