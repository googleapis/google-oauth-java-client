---
title: Component Modules
---

# Component Modules

This libraries is composed of several modules:

## google-oauth-client

Google OAuth Client Library for Java (`google-oauth-client`) is designed to be
compatible with all supported Java platforms, including Android.

## google-oauth-client-appengine

Google App Engine extensions to the Google OAuth Client Library for Java
(`google-oauth-client-appengine`) support Java Google App Engine applications.
This module depends on `google-oauth-client` and `google-oauth-client-servlet`.

## google-oauth-client-java6

Java 6 (and higher) extensions to the Google OAuth Client Library for Java
(`google-oauth-client-java6`) support Java6+ applications. This module depends
on `google-oauth-client`.

## google-oauth-client-jetty

Jetty extensions to the Google OAuth Client Library for Java
(`google-oauth-client-jetty`) support authorization code flow for installed
applications. This module depends on `google-oauth-client-java6`.

## google-oauth-client-servlet

Servlet and JDO extensions to the Google OAuth Client Library for Java
(`google-oauth-client-servlet`) support Java servlet web applications. This
module depends on `google-oauth-client`.
