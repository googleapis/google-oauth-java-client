---
title: OAuth 2.0
---

# OAuth 2.0 and the Google OAuth Client Library for Java

## Overview

**Purpose:** This document describes the generic OAuth 2.0 functions offered by
the Google OAuth Client Library for Java. You can use these functions for
authentication and authorization for any Internet services.

For instructions on using `GoogleCredential` to do OAuth 2.0 authorization with
Google services, see
[Using OAuth 2.0 with the Google API Client Library for Java][google-api-client-oauth2].

**Summary:** [OAuth 2.0][oauth2] is a standard specification for allowing end
users to securely authorize a client application to access protected server-side
resources. In addition, the [OAuth 2.0 bearer token][bearer-token] specification
explains how to access those protected resources using an access token granted
during the end-user authorization process.

For details, see the Javadoc documentation for the following packages:

* [`com.google.api.client.auth.oauth2`][oauth2-package]
  (from `google-oauth-client`)
* [`com.google.api.client.extensions.servlet.auth.oauth2`][oauth2-servlet-package]
  (from `google-oauth-client-servlet`
* [`com.google.api.client.extensions.appengine.auth.oauth2`][oauth2-appengine-package]
  (from `google-oauth-client-appengine`)

## Client registration

Before using the Google OAuth Client Library for Java, you probably need to
register your application with an authorization server to receive a client ID
and client secret. (For general information about this process, see the
[Client Registration specification][client-registration].

## Credential and credential store

[`Credential`][credential] is a thread-safe OAuth 2.0 helper class for accessing
protected resources using an access token. When using a refresh token,
`Credential` also refreshes the access token when the access token expires using
the refresh token. For example, if you already have an access token, you can
make a request in the following way:

```java
public static HttpResponse executeGet(
    HttpTransport transport, JsonFactory jsonFactory, String accessToken, GenericUrl url)
    throws IOException {
  Credential credential =
      new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(accessToken);
  HttpRequestFactory requestFactory = transport.createRequestFactory(credential);
  return requestFactory.buildGetRequest(url).execute();
}
```

Most applications need to persist the credential's access token and refresh
token in order to avoid a future redirect to the authorization page in the
browser. The [`CredentialStore`][credential-store] implementation in this
library is deprecated and to be removed in future releases. The alternative is
to use the [`DataStoreFactory`][datastore-factory] and [`DataStore`][datastore]
interfaces with [`StoredCredential`][stored-credential], which are provided by
the [Google HTTP Client Library for Java][google-http-client].

You can use one of the following implementations provided by the library:

* [`AppEngineDataStoreFactory`][appengine-datastore-factory] persists the
  credential using the Google App Engine Data Store API.
* [`MemoryDataStoreFactory`][memory-datastore-factory] "persists" the credential
  in memory, which is only useful as a short-term storage for the lifetime of
  the process.
* [`FileDataStoreFactory`] persists the credential in a file.

### Google App Engine users

[`AppEngineCredentialStore`][appengine-credential-store] is deprecated and is being removed.

We recommend that you use
[`AppEngineDataStoreFactory`][appengine-datastore-factory] with
[`StoredCredential`][stored-credential]. If you have credentials stored in the
old way, you can use the added helper methods
[`migrateTo(AppEngineDataStoreFactory)`][appengine-migrate] or
[`migrateTo(DataStore)`][datastore-migrate] to migrate.

Use [`DataStoreCredentialRefreshListener`][datastore-credential-listener] and
set it for the credential using
[`GoogleCredential.Builder.addRefreshListener(CredentialRefreshListener)`][add-refresh-listener].

## Authorization code flow

Use the authorization code flow to allow the end user to grant your application
access to their protected data. The protocol for this flow is specified in the
[Authorization Code Grant specification][authorization-code-grant].

This flow is implemented using [`AuthorizationCodeFlow`][authorization-code-flow].
The steps are:

* An end user logs in to your application. You need to associate that user with
  a user ID that is unique for your application.
* Call [`AuthorizationCodeFlow.loadCredential(String)`][auth-code-flow-load],
  based on the user ID, to check if the user's credentials are already known.
  If so, you're done.
* If not, call [`AuthorizationCodeFlow.newAuthorizationUrl()`][auth-code-flow-new]
  and direct the end user's browser to an authorization page where they can grant
  your application access to their protected data.
* The web browser then redirects to the redirect URL with a "code" query
  parameter that can then be used to request an access token using
  [`AuthorizationCodeFlow.newTokenRequest(String)`][token-request].
* Use
  [`AuthorizationCodeFlow.createAndStoreCredential(TokenResponse, String)`][create-and-store]
  to store and obtain a credential for accessing protected resources.

Alternatively, if you are not using
[`AuthorizationCodeFlow`][authorization-code-flow], you may use the lower-level
classes:

* Use [`DataStore.get(String)`][datastore-get] to load the credential from the
  store, based on the user ID.
* Use [`AuthorizationCodeRequestUrl`][auth-code-request-url] to direct the
  browser to the authorization page.
* Use [`AuthorizationCodeResponseUrl`][auth-code-response-url] to process the
  authorization response and parse the authorization code.
* Use [`AuthorizationCodeTokenRequest`][auth-code-token-request] to request an
  access token and possibly a refresh token.
* Create a new [`Credential`][credential] and store it using
  [DataStore.set(String, V)][datastore-set].
* Access protected resources using the [Credential][credential]. Expired access
  tokens are automatically refreshed using the refresh token, if applicable.
  Make sure to use
  [`DataStoreCredentialRefreshListener`][datastore-credential-listener] and set
  it for the credential using
  [`Credential.Builder.addRefreshListener(CredentialRefreshListener)`][add-refresh-listener].

### Servlet authorization code flow

This library provides servlet helper classes to significantly simplify the
authorization code flow for basic use cases. You just provide concrete subclasses
of [`AbstractAuthorizationCodeServlet`][abstract-code-servlet]
and [`AbstractAuthorizationCodeCallbackServlet`][abstract-code-callback-servlet]
(from `google-oauth-client-servlet`) and add them to your `web.xml` file. Note
that you still need to take care of user login for your web application and
extract a user ID.

Sample code:

```java
public class ServletSample extends AbstractAuthorizationCodeServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // do stuff
  }

  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    url.setRawPath("/oauth2callback");
    return url.build();
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
        new NetHttpTransport(),
        new GsonFactory(),
        new GenericUrl("https://server.example.com/token"),
        new BasicAuthentication("s6BhdRkqt3", "7Fjfp0ZBr1KtDRbnfVdmIw"),
        "s6BhdRkqt3",
        "https://server.example.com/authorize").setCredentialDataStore(
            StoredCredential.getDefaultDataStore(
                new FileDataStoreFactory(new File("datastoredir"))))
        .build();
  }

  @Override
  protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
    // return user ID
  }
}

public class ServletCallbackSample extends AbstractAuthorizationCodeCallbackServlet {

  @Override
  protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
      throws ServletException, IOException {
    resp.sendRedirect("/");
  }

  @Override
  protected void onError(
      HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
      throws ServletException, IOException {
    // handle error
  }

  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    url.setRawPath("/oauth2callback");
    return url.build();
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
        new NetHttpTransport(),
        new GsonFactory(),
        new GenericUrl("https://server.example.com/token"),
        new BasicAuthentication("s6BhdRkqt3", "7Fjfp0ZBr1KtDRbnfVdmIw"),
        "s6BhdRkqt3",
        "https://server.example.com/authorize").setCredentialDataStore(
            StoredCredential.getDefaultDataStore(
                new FileDataStoreFactory(new File("datastoredir"))))
        .build();
  }

  @Override
  protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
    // return user ID
  }
}
```

### Google App Engine authorization code flow

The authorization code flow on App Engine is almost identical to the servlet
authorization code flow, except that we can leverage Google App Engine's
[Users Java API][users-api]. The user needs to be logged in for the Users Java
API to be enabled; for information about redirecting users to a login page if
they are not already logged in, see
[Security and Authentication][security-authentication] (in `web.xml`).

The primary difference from the servlet case is that you provide concrete
subclasses of [`AbstractAppEngineAuthorizationCodeServlet`][abstract-gae-code-servlet]
and [`AbstractAppEngineAuthorizationCodeCallbackServlet`][abstract-gae-code-callback-servlet]
(from `google-oauth-client-appengine`). They extend the abstract servlet classes
and implement the `getUserId` method for you using the Users Java API. [`AppEngineDataStoreFactory`][appengine-datastore-factory] (from
[Google HTTP Client Library for Java][google-http-client]) is a good option for
persisting the credential using the Google App Engine Data Store API.

Sample code:

```java
public class AppEngineSample extends AbstractAppEngineAuthorizationCodeServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // do stuff
  }

  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    url.setRawPath("/oauth2callback");
    return url.build();
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
        new UrlFetchTransport(),
        new GsonFactory(),
        new GenericUrl("https://server.example.com/token"),
        new BasicAuthentication("s6BhdRkqt3", "7Fjfp0ZBr1KtDRbnfVdmIw"),
        "s6BhdRkqt3",
        "https://server.example.com/authorize").setCredentialStore(
            StoredCredential.getDefaultDataStore(AppEngineDataStoreFactory.getDefaultInstance()))
        .build();
  }
}

public class AppEngineCallbackSample extends AbstractAppEngineAuthorizationCodeCallbackServlet {

  @Override
  protected void onSuccess(HttpServletRequest req, HttpServletResponse resp, Credential credential)
      throws ServletException, IOException {
    resp.sendRedirect("/");
  }

  @Override
  protected void onError(
      HttpServletRequest req, HttpServletResponse resp, AuthorizationCodeResponseUrl errorResponse)
      throws ServletException, IOException {
    // handle error
  }

  @Override
  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
    url.setRawPath("/oauth2callback");
    return url.build();
  }

  @Override
  protected AuthorizationCodeFlow initializeFlow() throws IOException {
    return new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
        new UrlFetchTransport(),
        new GsonFactory(),
        new GenericUrl("https://server.example.com/token"),
        new BasicAuthentication("s6BhdRkqt3", "7Fjfp0ZBr1KtDRbnfVdmIw"),
        "s6BhdRkqt3",
        "https://server.example.com/authorize").setCredentialStore(
            StoredCredential.getDefaultDataStore(AppEngineDataStoreFactory.getDefaultInstance()))
        .build();
  }
}
```

### Command-line authorization code flow

Simplified example code taken from
[dailymotion-cmdline-sample][dailymotion-cmdline-sample]:

```java
/** Authorizes the installed application to access user's protected data. */
private static Credential authorize() throws Exception {
  OAuth2ClientCredentials.errorIfNotSpecified();
  // set up authorization code flow
  AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(BearerToken
      .authorizationHeaderAccessMethod(),
      HTTP_TRANSPORT,
      JSON_FACTORY,
      new GenericUrl(TOKEN_SERVER_URL),
      new ClientParametersAuthentication(
          OAuth2ClientCredentials.API_KEY, OAuth2ClientCredentials.API_SECRET),
      OAuth2ClientCredentials.API_KEY,
      AUTHORIZATION_SERVER_URL).setScopes(Arrays.asList(SCOPE))
      .setDataStoreFactory(DATA_STORE_FACTORY).build();
  // authorize
  LocalServerReceiver receiver = new LocalServerReceiver.Builder().setHost(
      OAuth2ClientCredentials.DOMAIN).setPort(OAuth2ClientCredentials.PORT).build();
  return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
}

private static void run(HttpRequestFactory requestFactory) throws IOException {
  DailyMotionUrl url = new DailyMotionUrl("https://api.dailymotion.com/videos/favorites");
  url.setFields("id,tags,title,url");

  HttpRequest request = requestFactory.buildGetRequest(url);
  VideoFeed videoFeed = request.execute().parseAs(VideoFeed.class);
  ...
}

public static void main(String[] args) {
  ...
  DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
  final Credential credential = authorize();
  HttpRequestFactory requestFactory =
      HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
        @Override
        public void initialize(HttpRequest request) throws IOException {
          credential.initialize(request);
          request.setParser(new JsonObjectParser(JSON_FACTORY));
        }
      });
  run(requestFactory);
  ...
}
```

### Browser-based client flow

These are the typical steps of the the browser-based client flow specified in the
[Implicit Grant specification][implicit-grant]:

* Using [`BrowserClientRequestUrl`][browser-client-request], redirect the end
  user's browser to the authorization page where the end user can grant your
  application access to their protected data.
* Use a JavaScript application to process the access token found in the URL
  fragment at the redirect URI that is registered with the authorization server.

Sample usage for a web application:

```java
public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
  String url = new BrowserClientRequestUrl(
      "https://server.example.com/authorize", "s6BhdRkqt3").setState("xyz")
      .setRedirectUri("https://client.example.com/cb").build();
  response.sendRedirect(url);
}
```

## Detecting an expired access token

According to the [OAuth 2.0 bearer specification][bearer-spec], when the server
is called to access a protected resource with an expired access token, the
server typically responds with an HTTP `401 Unauthorized` status code such as
the following:

```txt
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Bearer realm="example",
                  error="invalid_token",
                  error_description="The access token expired"
```

However, there appears to be a lot of flexibility in the specification. For
details, check the documentation of the OAuth 2.0 provider.

An alternative approach is to check the `expires_in` parameter in the
[access token response][access-token-response]. This specifies the lifetime in
seconds of the granted access token, which is typically an hour.  However, the
access token might not actually expire at the end of that period, and the server
might continue to allow access. That's why we typically recommend waiting for a
`401 Unauthorized` status code, rather than assuming the token has expired based
on the elapsed time.  Alternatively, you can try to refresh an access token
shortly before it expires, and if the token server is unavailable, continue to
use the access token until you receive a `401`. This is the strategy used by
default in [`Credential`][credential].

Another option is to grab a new access token before every request, but that
requires an extra HTTP request to the token server every time, so it is likely a
poor choice in terms of speed and network usage. Ideally, store the access token
in secure, persistent storage to minimize an application's requests for new
access tokens. (But for installed applications, secure storage is a difficult
problem.)

Note that an access token may become invalid for reasons other than expiration,
for example if the user has explicitly revoked the token, so be sure your
error-handling code is robust. Once you've detected that a token is no longer
valid, for example if it has expired or been revoked, you must remove the access
token from your storage.  On Android, for example, you must call
[`AccountManager.invalidateAuthToken`][invalidate-auth-token].

[google-api-client-oauth2]: https://github.com/googleapis/google-api-java-client/wiki/OAuth2
[oauth2]: https://tools.ietf.org/html/rfc6749
[bearer-token]: https://tools.ietf.org/html/rfc6750
[oauth2-package]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/auth/oauth2/package-summary.html
[oauth2-servlet-package]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/extensions/servlet/auth/oauth2/package-summary.html
[oauth2-appengine-package]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/extensions/appengine/auth/oauth2/package-summary.html
[client-registration]: https://tools.ietf.org/html/rfc6749#section-2
[credential]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/auth/oauth2/Credential.html
[credential-store]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/auth/oauth2/CredentialStore.html
[datastore-factory]: https://googleapis.dev/java/google-http-client/latest/com/google/api/client/util/store/DataStoreFactory.html
[datastore]: https://googleapis.dev/java/google-http-client/latest/com/google/api/client/util/store/DataStore.html
[stored-credential]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/auth/oauth2/StoredCredential.html
[google-http-client]: https://github.com/googleapis/google-http-java-client
[appengine-datastore-factory]: https://googleapis.dev/java/google-http-client/latest/com/google/api/client/extensions/appengine/datastore/AppEngineDataStoreFactory.html
[memory-datastore-factory]: https://googleapis.dev/java/google-http-client/latest/com/google/api/client/util/store/MemoryDataStoreFactory.html
[file-datastore-factory]: https://googleapis.dev/java/google-http-client/latest/com/google/api/client/util/store/FileDataStoreFactory.html
[appengine-credential-store]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/extensions/appengine/auth/oauth2/AppEngineCredentialStore.html
[appengine-migrate]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/extensions/appengine/auth/oauth2/AppEngineCredentialStore.html#migrateTo-com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory-
[datastore-migrate]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/extensions/appengine/auth/oauth2/AppEngineCredentialStore.html#migrateTo-com.google.api.client.util.store.DataStore-
[datastore-credential-listener]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/auth/oauth2/DataStoreCredentialRefreshListener.html
[add-refresh-listener]: https://googleapis.dev/java/google-api-client/latest/com/google/api/client/googleapis/auth/oauth2/GoogleCredential.Builder.html#addRefreshListener-com.google.api.client.auth.oauth2.CredentialRefreshListener-
[authorization-code-grant]: https://tools.ietf.org/html/rfc6749#section-4.1
[authorization-code-flow]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/auth/oauth2/AuthorizationCodeFlow.html
[auth-code-flow-load]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/auth/oauth2/AuthorizationCodeFlow.html#loadCredential-java.lang.String-
[auth-code-flow-new]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/auth/oauth2/AuthorizationCodeFlow.html#newAuthorizationUrl--
[token-request]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/auth/oauth2/AuthorizationCodeFlow.html#newTokenRequest-java.lang.String-
[create-and-store]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/auth/oauth2/AuthorizationCodeFlow.html#createAndStoreCredential-com.google.api.client.auth.oauth2.TokenResponse-java.lang.String-
[datastore-get]: https://googleapis.dev/java/google-http-client/latest/com/google/api/client/util/store/DataStore.html#get-java.lang.String-
[auth-code-request-url]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/auth/oauth2/AuthorizationCodeRequestUrl.html
[auth-code-response-url]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/auth/oauth2/AuthorizationCodeResponseUrl.html
[auth-code-token-request]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/auth/oauth2/AuthorizationCodeTokenRequest.html
[datastore-set]: https://googleapis.dev/java/google-http-client/latest/com/google/api/client/util/store/DataStore.html#set(java.lang.String,%20V)
[abstract-code-servlet]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/extensions/servlet/auth/oauth2/AbstractAuthorizationCodeServlet.html
[abstract-code-callback-servlet]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/extensions/servlet/auth/oauth2/AbstractAuthorizationCodeCallbackServlet.html
[users-api]: https://cloud.google.com/appengine/docs/java/users/
[security-authentication]: https://cloud.google.com/appengine/docs/java/config/webxml#Security_and_Authentication
[abstract-gae-code-servlet]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/extensions/appengine/auth/oauth2/AbstractAppEngineAuthorizationCodeServlet.html
[abstract-gae-code-callback-servlet]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/extensions/appengine/auth/oauth2/AbstractAppEngineAuthorizationCodeCallbackServlet.html
[dailymotion-cmdline-sample]: https://github.com/googleapis/google-oauth-java-client/tree/master/samples/dailymotion-cmdline-sample
[implicit-grant]: https://tools.ietf.org/html/rfc6749#section-4.2
[browser-client-request]: https://googleapis.dev/java/google-oauth-client/latest/com/google/api/client/auth/oauth2/BrowserClientRequestUrl.html
[bearer-spec]: https://tools.ietf.org/html/rfc6750#section-2.4
[access-token-response]: https://tools.ietf.org/html/rfc6749#section-4.2.2
[invalidate-auth-token]: http://developer.android.com/reference/android/accounts/AccountManager.html#invalidateAuthToken(java.lang.String,%20java.lang.String)
