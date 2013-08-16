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
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Charsets;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * {@link Beta} <br/>
 * Thread-safe file implementation of a credential store.
 *
 * @since 1.11
 * @author Rafael Naufal
 * @deprecated (to be removed in the future) Use {@link FileDataStoreFactory} with
 *             {@link StoredCredential} instead, optionally using
 *             {@link #migrateTo(FileDataStoreFactory)} or {@link #migrateTo(DataStore)} to
 *             migrating an existing {@link FileCredentialStore}.
 */
@Deprecated
@Beta
public class FileCredentialStore implements CredentialStore {

  private static final Logger LOGGER = Logger.getLogger(FileCredentialStore.class.getName());

  /** Json factory for serializing user credentials. */
  private final JsonFactory jsonFactory;

  /** Lock on access to the store. */
  private final Lock lock = new ReentrantLock();

  /** User credentials repository. */
  private FilePersistedCredentials credentials = new FilePersistedCredentials();

  /** File to store user credentials. */
  private final File file;

  private static final boolean IS_WINDOWS = File.separatorChar == '\\';

  /**
   * @param file File to store user credentials
   * @param jsonFactory JSON factory to serialize user credentials
   */
  public FileCredentialStore(File file, JsonFactory jsonFactory) throws IOException {
    this.file = Preconditions.checkNotNull(file);
    this.jsonFactory = Preconditions.checkNotNull(jsonFactory);
    // create parent directory (if necessary)
    File parentDir = file.getCanonicalFile().getParentFile();
    if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
      throw new IOException("unable to create parent directory: " + parentDir);
    }
    // error if it is a symbolic link
    if (isSymbolicLink(file)) {
      throw new IOException("unable to use a symbolic link: " + file);
    }
    // create new file (if necessary)
    if (!file.createNewFile()) {
      // load credentials from existing file
      loadCredentials(file);
    } else {
      // disable access by other users if O/S allows it
      if (!file.setReadable(false, false) || !file.setWritable(false, false)
          || !file.setExecutable(false, false)) {
        LOGGER.warning("unable to change file permissions for everybody: " + file);
      }
      // set file permissions to readable and writable by user
      if (!file.setReadable(true) || !file.setWritable(true)) {
        throw new IOException("unable to set file permissions: " + file);
      }
      // save the credentials to create a new file
      save();
    }
  }

  /**
   * Returns whether the given file is a symbolic link.
   *
   * @since 1.13
   */
  protected boolean isSymbolicLink(File file) throws IOException {
    if (IS_WINDOWS) {
      return false;
    }

    File canonical = file;
    if (file.getParent() != null) {
      canonical = new File(file.getParentFile().getCanonicalFile(), file.getName());
    }
    return !canonical.getCanonicalFile().equals(canonical.getAbsoluteFile());
  }

  @Override
  public void store(String userId, Credential credential) throws IOException {
    lock.lock();
    try {
      credentials.store(userId, credential);
      save();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void delete(String userId, Credential credential) throws IOException {
    lock.lock();
    try {
      credentials.delete(userId);
      save();
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

  private void save() throws IOException {
    FileOutputStream fos = new FileOutputStream(file);
    try {
      JsonGenerator generator = jsonFactory.createJsonGenerator(fos, Charsets.UTF_8);
      generator.serialize(credentials);
      generator.close();
    } finally {
      fos.close();
    }
  }

  /**
   * Migrates to the new {@link FileDataStoreFactory} format.
   *
   * <p>
   * Sample usage:
   * </p>
   *
   * <pre>
  public static FileDataStore migrate(FileCredentialStore credentialStore, File dataDirectory)
      throws IOException {
    FileDataStore dataStore = new FileDataStore(dataDirectory);
    credentialStore.migrateTo(dataStore);
    return dataStore;
  }
   * </pre>
   * @param dataStoreFactory file data store factory
   * @since 1.16
   */
  public final void migrateTo(FileDataStoreFactory dataStoreFactory) throws IOException {
    migrateTo(StoredCredential.getDefaultDataStore(dataStoreFactory));
  }

  /**
   * Migrates to the new format using {@link DataStore} of {@link StoredCredential}.
   *
   * @param credentialDataStore credential data store
   * @since 1.16
   */
  public final void migrateTo(DataStore<StoredCredential> credentialDataStore) throws IOException {
    credentials.migrateTo(credentialDataStore);
  }
}
