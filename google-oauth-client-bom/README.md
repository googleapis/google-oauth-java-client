# Google OAuth Client Library Bill of Materials

The `google-oauth-client-bom` module is a pom that can be used to import consistent 
versions of `google-oauth-client` components.

To use it in Maven, add the following to your `pom.xml`:

[//]: # ({x-version-update-start:google-oauth-client:released})
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.google.oauth-client</groupId>
      <artifactId>google-oauth-client-bom</artifactId>
      <version>1.30.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```
[//]: # ({x-version-update-end})

## License

Apache 2.0 - See [LICENSE] for more information.

[LICENSE]: https://github.com/googleapis/google-oauth-java-client/blob/master/LICENSE
