# Instructions for the Keycloak OAuth2 with PKCE Command-Line Sample

## Browse Online

[Browse Source][browse-source], or main file [PKCESample.java][main-source].

## Command-Line Instructions

**Prerequisites:** install [Java 7 or higher][install-java], [git][install-git], and
[Maven][install-maven]. You may need to set your `JAVA_HOME`. 
You'll also need [Docker][install-docker].

1. Check out the sample code:

    ```bash
    git clone https://github.com/google/google-oauth-java-client.git
    cd google-oauth-java-client
    ```

2. Run keycloak in a docker container:

   ```
   docker run -p 8080:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin quay.io/keycloak/keycloak:10.0.1 
   ```

3. Run the sample:

    ```bash
    mvn install
    mvn exec:java -pl samples/keycloak-pkce-cmdline-sample
    ```
   
   This will open up the Keycloak login page where you can log in with the username/password specified
   when running the Keycloak docker container above (`admin / admin`). Once you log in, the application
   will print out a message that it successfully obtained an access token.

[browse-source]: https://github.com/google/google-oauth-java-client/tree/dev/samples/keycloak-pkce-cmdline-sample
[main-source]: https://github.com/google/google-oauth-java-client/blob/dev/samples/keycloak-pkce-cmdline-sample/src/main/java/com/google/api/services/samples/keycloak/cmdline/PKCESample.java
[install-java]: https://java.com/
[install-git]: https://git-scm.com
[install-maven]: https://maven.apache.org
[install-docker]: https://docs.docker.com/get-docker/