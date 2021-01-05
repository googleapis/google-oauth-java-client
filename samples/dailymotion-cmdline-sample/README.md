# Instructions for the Daily Motion OAuth2 Command-Line Sample

## Browse Online

[Browse Source][browse-source], or main file [DailyMotionSample.java][main-source].

## Command-Line Instructions

**Prerequisites:** install [Java 7 or higher][install-java], [git][install-git], and
[Maven][install-maven]. You may need to set your `JAVA_HOME`.

1. Check out the sample code:

    ```bash
    git clone https://github.com/google/google-oauth-java-client.git
    cd google-oauth-java-client/samples/dailymotion-cmdline-sample
    ```

2. Edit the `OAuth2ClientCredentials.java` file with your credentials.  To acquire credentials, go
   to the [Dailymotion Developer Profile][dailymotion-developer-profile], click "Create New API Key"
   and specify "http://127.0.0.1:8080/Callback" as the "Callback URL".

3. Run the sample:

    ```bash
    mvn compile
    mvn -q exec:java
    ```


[browse-source]: https://github.com/google/google-oauth-java-client/tree/dev/samples/dailymotion-cmdline-sample
[main-source]: https://github.com/google/google-oauth-java-client/blob/dev/samples/dailymotion-cmdline-sample/src/main/java/com/google/api/services/samples/dailymotion/cmdline/DailyMotionSample.java
[install-java]: https://java.com/
[install-git]: https://git-scm.com
[install-maven]: https://maven.apache.org
[dailymotion-developer-profile]: https://www.dailymotion.com/profile/developer
