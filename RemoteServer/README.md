RESTful SikuliX client-server
======

Implemented via maven, grizzly-http-server, sikulixapi and apache commons utils.

Main usage: remote Sikulix server provides useful services for common sikulixapi actions, bi-directional file transferring and command line execution.

Source code provides the following content:

 - Grizzly http server with custom configuration.
 - Services for processing Sikulix, IO and Cmd http requests.
 - CommandLine utility class, that uses commons-exec library for flexible cmd control.
 - RemoteDesktop - Sikulix wrapper for common click / type / exists APIs. Uses observers mechanism to allow flexible elements' waiting.

To build remote server use the following command: `mvn clean install`.
Note that it depends on `sikulixapi`, so you must build appropriate dependencies first or provide your own version reference.
 
Sample sikulix, command line and file transfer services look like the following:

```java

    @POST
    @Path("/click")
    public Response click(final Image image, @QueryParam("timeout") final int timeout) {
        return Response.status(new RemoteDesktop().click(image, timeout) ?
                Response.Status.OK : Response.Status.NOT_FOUND).build();
    }

    @Path("/download")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput downloadFile(@QueryParam("fromPath") final String fromPath) {
        FILE_TRANSFER_LOGGER.info("Sending " + fromPath);

        return new StreamingOutput() {
            @Override
            public void write(final OutputStream outputStream) {
                try (final FileInputStream inputStream = new FileInputStream(new File(fromPath))) {
                    IOUtils.copy(inputStream, outputStream);
                    outputStream.flush();

                    FILE_TRANSFER_LOGGER.info("File " + fromPath + " has been sent.");
                } catch (NullPointerException | IOException e) {
                    FILE_TRANSFER_LOGGER.severe("An error occurred while stream copying: " + e.getMessage());
                }
            }
        };
    }

    @POST
    @Path("/execute")
    public Response execute(final Command command) {
        return Response.status(CommandLineUtils.executeCommandLine(command) != -1 ?
                Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR)
                .build();
    }
```