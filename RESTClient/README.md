REST SikuliX client
======

Implemented via maven, jersey client, sikulixapi, apache commons utils and testng.

Main usage: RESTful SikuliX remote server control.

Source code provides the following content:

 - REST Jersey 2.x client, that implements common interfaces for further sending http requests to remote server.
 - Common entity containers implementation.
 - BaseTest wrapper for encapsulating preparation activities.
 - Sample tests, that use embedded resources for bi-directional file transferring, batch / sh scripts execution and
 common Sikuli click / setText / exists verifications.

Important notes:

 - Both client and server uses `sikulixapi`, so you must build appropriate dependencies first or include your version manually into `pom.xml`.
 - `RemoteServer` should be built before test execution: `mvn clean install`.
 - Tests are `disabled by default` to avoid unexpected failures during project building. Set `skipTests=false` in `pom.xml` to enable them.
 - Tests were created both for Windows and Linux OS. Feel free to add your own for Mac.
 - Explore `resources folder` before test execution: you will probably need to replace images with your own.
 - `RemoteServer` should be started before test execution: `java -jar sikulixremoteserver-1.1.0-jar-with-dependencies.jar port`. You can skip port to use default one - 4041.
 - `SIKULI_SERVER_IP` / `SIKULIX_SERVER_PORT` should be changed according to your server's configuration.
 
Common functionality is covered by the following tests:

```java

    @Test
    public void uploadFileToServer() {
        getClient().uploadFile(Arrays.asList(BATCH_RUNNER_SCRIPT.getPath(), BATCH_SAMPLE_SCRIPT.getPath()), SERVER_PATH.getPath());
        assertTrue(getClient().exists(Arrays.asList(SERVER_PATH.getPath() + "/" + BATCH_RUNNER_SCRIPT.getName(),
                SERVER_PATH.getPath() + "/" + BATCH_SAMPLE_SCRIPT.getName())));
    }

    @Test
    public void executeScript() {
        getClient().executeCommandLine(new CommandLineBox(SERVER_PATH.getPath() + "/" + BATCH_RUNNER_SCRIPT.getName(),
                Arrays.asList("arg1", "arg2", "arg3"), WAIT_TIMEOUT));
        assertTrue(getClient().exists(Arrays.asList(SERVER_PATH.getPath() + "/" + EMPTY_FILE)));
    }

    @Test
    public void downloadFileFromServer() {
        getClient().downloadFile(SERVER_PATH.getPath() + "/" + EMPTY_FILE, CLIENT_PATH.getPath());
        assertTrue(new File(CLIENT_PATH.getPath() + "/" + EMPTY_FILE).exists());
    }

    @Test
    public void callCommandLineFromStartMenu() {
        getClient().click(new ImageBox(getResource(RESOURCE_BUTTON_START_IMAGE).getPath(), SIMILARITY), WAIT_TIMEOUT);
        getClient().setText(new ImageBox(getResource(RESOURCE_INPUT_FIND_FILES_IMAGE).getPath(), SIMILARITY),
                "cmd" + Key.ENTER, WAIT_TIMEOUT);
        assertTrue(getClient().exists(new ImageBox(getResource(RESOURCE_INPUT_CMD_IMAGE).getPath(), SIMILARITY), WAIT_TIMEOUT));
    }
```