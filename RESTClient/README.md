REST SikuliX client
======

Implemented via maven, jersey client, sikulixapi, apache commons utils and testng.

Main usage: RESTful SikuliX remote server control.

Source code provides the following content:

 - REST Jersey 2.x client, that implements common interfaces for further sending http requests to remote server.
 - Common entity containers implementation.
 - BaseTest wrapper for encapsulating preparation activities.
 - Sample tests, that use embedded resources for bi-directional file transferring, windows batch files execution and common sikuli actions checking.

Important notes:

 - Both client and server uses `sikulixapi`, so you must build appropriate dependencies first or include your version manually into `pom.xml`.
 - `RemoteServer` should be built before test execution: `mvn clean install`.
 - Tests are `disabled by default` to avoid unexpected failures during project building. Set `skipTests=false` in `pom.xml` to enable them.
 - Tests were created `only for Windows OS`. Feel free to add your own for Unix and Mac.
 - Explore `resources folder` before test execution: batch files' extension should be changed from `txt` to `bat`; you will probably need to replace images with your own.
 - `RemoteServer` should be started before test execution: `java -jar sikulixremoteserver-1.1.0-jar-with-dependencies.jar port`. You can skip port to use default one - 4041.
 - `SIKULI_SERVER_IP` / `SIKULIX_SERVER_PORT` should be changed according to your server's configuration.
 
REST call example for setting some text into particular image element:

```java

    public void setText(final Image image, final String text, final int timeout) {
        final Response response = service.path("image")
                .path("setText")
                .queryParam("text", text)
                .queryParam("timeout", timeout)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(image));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            CLIENT_LOGGER.info("Text '" + escapeJava(text) + "' has been set to " + image.getValues());
        } else {
            CLIENT_LOGGER.severe("Unable to set text '" + escapeJava(text) + "' to " + image.getValues());
        }

        response.close();
    }
```

And appropriate test to click `Start` button, type `cmd` into search field, press `Enter` and verify that command line is opened.

```java

    @Test
    public void clickStartAndOpenCmd() {
        getClient().click(new ImageBox(getResourcePath(RESOURCE_BUTTON_IMAGE), SIMILARITY), WAIT_TIMEOUT);
        getClient().setText(new ImageBox(getResourcePath(RESOURCE_INPUT_IMAGE), SIMILARITY), "cmd" + Key.ENTER, WAIT_TIMEOUT);
        assertTrue(getClient().exists(new ImageBox(getResourcePath(RESOURCE_LABEL_IMAGE), SIMILARITY), WAIT_TIMEOUT));
    }
```

Remote command line execution example:

```java

    @Test
    public void runBatchFromCommandLine() {
        getClient().executeCommandLine(new CommandLineBox(filePath.getPath(), Arrays.asList("arg1", "arg2", "arg3"), WAIT_TIMEOUT));
        assertTrue(emptyFile.exists());
    }
```

In this particular case we run batch file from resources, that calls an other batch, that waits for user input.
This is the most tricky case, as normally if child process stucked, main thread will wait forever.
Current implementation provides user to set wait timeout to release main thread.

The last example is related to file transferring between client and server. Currently, you can send only a single file in both directions.

```java

    @Test(priority = 1)
    public void uploadFile() {
        getClient().uploadFile(filePath.getPath(), copyToServerPath.getPath());
        assertTrue(serverFilePath.exists());
    }

    @Test(priority = 2)
    public void downloadFile() {
        getClient().downloadFile(serverFilePath.getPath(), copyToClientPath.getPath());
        assertTrue(clientFilePath.exists());
    }
```

These examples use internal resources and folders. Provide a valid local / remote path to test this feature on your own files.