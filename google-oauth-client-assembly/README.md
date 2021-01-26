# Google OAuth Client Library for Java

## Overview

High-level details about this library can be found at
https://github.com/google/google-oauth-java-client

* [Release Notes][release-notes]
* [JavaDoc][javadoc]
* [Developer's Guide][developers-guide]
* [Support][support]

## Dependencies and Licenses

The license can be found [here](LICENSE.txt).

Dependent jars can be found in the [libs](libs) folder and the corresponding source jars can be
found in the [libs-sources](libs-sources) folder.

The dependency structure and licenses for the different libraries can be found here:

* [google-oauth-client](dependencies/google-oauth-client-dependencies.html)
* [google-oauth-client-appengine](dependencies/google-oauth-client-appengine-dependencies.html)
* [google-oauth-client-servlet](dependencies/google-oauth-client-servlet-dependencies.html)
* [google-oauth-client-java6](dependencies/google-oauth-client-java6-dependencies.html)
* [google-oauth-client-jetty](dependencies/google-oauth-client-jetty-dependencies.html)
* [google-http-client](dependencies/google-http-client-dependencies.html)
* [google-http-client-android](dependencies/google-http-client-android-dependencies.html)
* [google-http-client-appengine](dependencies/google-http-client-appengine-dependencies.html)
* [google-http-client-gson](dependencies/google-http-client-gson-dependencies.html)

## Maven Usage

For information on how to add these libraries to your Maven project please see
[Setup Maven][setup-maven].

## Eclipse

A `.classpath` file snippet that can be included in your project's `.classpath` has been provided
[here](.classpath). Please only use the `classpathentry`s you actually need (see below for more
details).

## ProGuard

A ProGuard configuration file [proguard-google-oauth-client.txt](proguard-google-oauth-client.txt)
is included for common settings for using the library. On Android projects, you may want to add a
reference to `proguard-google-oauth-client.txt` in the `project.properties` file under the
`proguard.config` property.

Please read [Setup ProGuard][proguard-setup] for more details.

## Dependencies for all Platforms

The following are the jars from the [libs](libs) folder needed for applications on all platform:

* google-oauth-client-${project.version}.jar
* google-http-client-${project.http.version}.jar
* jsr305-${project.jsr305.version}.jar
* google-http-client-gson-${project.http.version}.jar (when using GSON)
  * gson-${project.gson.version}.jar
* google-http-client-xml-${project.version}.jar (when using XML)
  * xpp3-${project.xpp3.version}.jar (when NOT on Android)

## Android Dependencies

The following are the jars from the [libs](libs) folder required for Android applications:

* google-http-client-android-${project.http.version}.jar

The libs folder also contains properties files that specify the location of source jars for Android
projects in Eclipse.

Please see the [Setup Android][android-setup] for more details.

## Google App Engine Dependencies

The following are the jars from the [libs](libs) folder required for Google App Engine applications
or a newer compatible version:

* google-oauth-client-appengine-${project.version}.jar
* google-oauth-client-servlet-${project.version}.jar
* google-http-client-appengine-${project.http.version}.jar
* jdo2-api-${project.jdo2-api.version}.jar
* transaction-api-${project.transaction-api.version}.jar

Please see the [Setup Google App Engine][appengine-setup] for more details.

## Servlet Dependencies

The following are the jars from the [libs](libs) folder required for Servlet applications or a newer
compatible version:

* google-oauth-client-servlet-${project.version}.jar
* commons-logging-${project.commons-logging.version}.jar
* httpclient-${project.httpclient.version}.jar
* httpcore-${project.httpcore.version}.jar
* jdo2-api-${project.jdo2-api.version}.jar
* transaction-api-${project.transaction-api.version}.jar

## General Purpose Java 6 Environment Dependencies

The following are the jars from the [libs](libs) folder required for general purpose Java 6
applications or a newer compatible version:

* google-oauth-client-java6-${project.version}.jar (for JDK >= 6)
  * google-oauth-client-jetty-${project.version}.jar (for Jetty 6)
    * jetty-${project.jetty.version}.jar
    * jetty-util-${project.jetty.version}.jar
* commons-logging-${project.commons-logging.version}.jar
* httpclient-${project.httpclient.version}.jar
* httpcore-${project.httpcore.version}.jar

[release-notes]: https://github.com/google/google-oauth-java-client/releases/tag/${project.version}
[javadoc]: https://google.github.io/google-oauth-java-client/releases/${project.version}/javadoc/index.html
[developers-guide]: https://developers.google.com/api-client-library/java/google-oauth-java-client/
[support]: https://developers.google.com/api-client-library/java/google-oauth-java-client/support
[setup-maven]: https://developers.google.com/api-client-library/java/google-oauth-java-client/setup#maven
[proguard-setup]: https://developers.google.com/api-client-library/java/google-http-java-client/setup#proguard
[android-setup]: https://developers.google.com/api-client-library/java/google-http-java-client/setup#android
[appengine-setup]: https://developers.google.com/api-client-library/java/google-oauth-java-client/setup#google_app_engine
