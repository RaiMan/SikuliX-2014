REST SikuliX client
======

Implemented via maven, jersey client, sikulixapi, apache commons utils and testng.

Main usage: RESTful SikuliX remote server control.

Source code provides the following content:

 - REST Jersey 2.x client, that implements common interfaces for further sending http requests to remote server.
 - Common entity containers implementation.
 - BaseTest wrapper for encapsulating preparation activities.
 - Sample tests, that use embedded resources for bi-directional file transferring, 
 remote windows batch files execution and integration test that sends screenshots to remote VM and interacts with them
 using common sikulix actions.

Important notes:

 - Both client and server uses `sikulixapi`, so you must build appropriate dependencies first or include your version manually into `pom.xml`.
 - `RemoteServer` should be built before test execution: `mvn clean install`.
 - Tests are `disabled by default` to avoid unexpected failures during project building. Set `skipTests=false` in `pom.xml` to enable them.
 - Tests were created `only for Windows OS`. Feel free to add your own for Unix and Mac.
 - Explore `resources folder` before test execution: batch files' extension should be changed from `txt` to `bat`; you will probably need to replace images with your own.
 - `RemoteServer` should be started before test execution: `java -jar sikulixremoteserver-1.1.0-jar-with-dependencies.jar port`. You can skip port to use default one - 4041.
 - `SIKULI_SERVER_IP` / `SIKULIX_SERVER_PORT` should be changed according to your server's configuration.
 
Integration test looks like the following:

```java

    @Test
    public void uploadFilesAndExecuteSikuliScript() throws InterruptedException {
        getClient().uploadFile(localPaths, IMAGES_PATH);
        if (getClient().exists(remotePaths) && remotePaths.size() >= 3) {
            getClient().click(new ImageBox(remotePaths.get(0), SIMILARITY), WAIT_TIMEOUT);
            getClient().setText(new ImageBox(remotePaths.get(1), SIMILARITY), "cmd" + Key.ENTER, WAIT_TIMEOUT);
            assertTrue(getClient().exists(new ImageBox(remotePaths.get(2), SIMILARITY), WAIT_TIMEOUT));
        } else {
            fail();
        }
    }
```