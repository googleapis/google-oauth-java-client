# Instructions for the Daily Motion OAuth2 Command-Line Sample

## Browse Online

[Browse Source][browse-source], or main file [DailyMotionSample.java][main-source].

## Command-Line Instructions

**Prerequisites:** install [Java 7 or higher][install-java], [git][install-git], and
[Maven][install-maven]. You may need to set your `JAVA_HOME`.

1. Check out the sample code:

    ```bash
    git clone https://github.com/google/google-oauth-java-client.git
    cd google-oauth-java-client/samples/dailymotion/cmdline-sample
    ```

2. Edit the `OAuth2ClientCredentials.java` file with your credentials.  To acquire credentials, go
   to the [Dailymotion Developer Profile][dailymotion-developer-profile], click "Create New API Key"
   and specify "http://127.0.0.1:8080/Callback" as the "Callback URL".

3. Run the sample:

    ```bash
    mvn compile
    mvn -q exec: java
    ```

## Setup Project in Eclipse 3.5/3.6

**Prerequisites:** install [Eclipse][install-eclipse], the [git plugin][install-git-plugin], and
[Maven plugin][install-maven-plugin]. You may need to set your `JAVA_HOME`.

1. Setup Eclipse Preferences
  * Window > Preferences... (or on Mac, Eclipse > Preferences...)
  * Select Maven
    * check on "Download Artifact Sources"
    * check on "Download Artifact JavaDoc"
2. Import `dailymotion-cmdline-sample` project
  * File > Import...
  * Select "General > Existing Project into Workspace" and click "Next"
  * Click "Browse" next to "Select root directory", find
    *someDirectory*/google-oauth-java-client-samples/dailymotion-cmdline-sample and click "Next"
  * Click "Finish"
3. Edit the `OAuth2ClientCredentials.java` file with your credentials.  To acquire credentials, go
   to the [Dailymotion Developer Profile][dailymotion-developer-profile], click "Create New API Key"
   and specify "http://127.0.0.1:8080/Callback" as the "Callback URL".
4. Run
  * Right-click on project dailymotion-cmdline-sample
  * Run As > Java Application
  * If asked, type "DailyMotionSample" and click OK
  * To enabled logging:
    * Run > Run Configurations...
    * Click on "Java Application > DailyMotionSample"
    * VM arguments: `-Djava.util.logging.config.file=${project_loc}/logging.properties`
    * Click "Run"

[browse-source]: https://github.com/google/google-oauth-java-client/tree/dev/samples/dailymotion-cmdline-sample
[main-source]: https://github.com/google/google-oauth-java-client/blob/dev/samples/dailymotion-cmdline-sample/src/main/java/com/google/api/services/samples/dailymotion/cmdline/DailyMotionSample.java
[install-java]: https://java.com/
[install-git]: https://git-scm.com
[install-maven]: https://maven.apache.org
[dailymotion-developer-profile]: https://www.dailymotion.com/profile/developer
[install-eclipse]: https://www.eclipse.org/downloads/
[install-git-plugin]: https://eclipse.org/egit/
[install-maven-plugin]: https://eclipse.org/m2e/
