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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialStore;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe file implementation of a credential store.
 * 
 * @since 1.11
 * @author Rafael Naufal
 */
public class FileCredentialStore implements CredentialStore {

  /** Json factory for serializing user credentials. */
  private final JsonFactory jsonFactory;

  /** Lock on access to the store. */
  private final Lock lock = new ReentrantLock();

  /** User credentials repository. */
  private FilePersistedCredentials credentials = new FilePersistedCredentials();

  /** File to store user credentials. */
  private final File file;

  /**
   * 
   * @param file
   *          File to store user credentials
   * @param jsonFactory
   *          JSON factory to serialize user credentials
   */
  public FileCredentialStore(File file, JsonFactory jsonFactory) throws IOException {
    this.file = Preconditions.checkNotNull(file, "missing file for credentials");
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory, "missing json factory");
    if (file.exists()) {
      loadCredentials(file);
    } else {
      file.setReadable(false, false);
      Preconditions.checkState(file.setWritable(false, false),
          "Error when setting permissions for %s", file);
      file.setExecutable(false, false);
      file.setReadable(true);
      file.setWritable(true);
    }
  }

  @Override
  public void store(String userId, Credential credential) throws IOException {
    lock.lock();
    try {
      credentials.store(userId, credential);
      writeCredentials(userId, credential);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void delete(String userId, Credential credential) throws IOException {
    lock.lock();
    try {
      credentials.delete(userId);
      writeCredentials(userId, credential);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean load(String userId, Credential credential) {
    lock.lock();
    try {
      return credentials.load(userId, credential);
    } finally {
      lock.unlock();
    }
  }

  private void loadCredentials(File file) throws IOException {
    FileInputStream is = new FileInputStream(file);
    try {
      this.credentials = this.jsonFactory.fromInputStream(is, FilePersistedCredentials.class);
    } finally {
      is.close();
    }
  }

  private void writeCredentials(String userId, Credential credential) throws IOException {
    FileOutputStream fos = new FileOutputStream(file);
    try {
      JsonGenerator generator = jsonFactory.createJsonGenerator(fos, Charsets.UTF_8);
      generator.serialize(credentials);
      generator.close();
    } finally {
      fos.close();
    }
  }
}
