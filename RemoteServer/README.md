RESTful SikuliX client-server
======

Implemented via java 8, maven, grizzly-http-server, jersey client, sikulixapi, apache commons utils and testng.

Main usage: remote SikuliX control, bi-directional file transferring and command line execution.

Sources contain the following content: 
 
 - Common interfaces for Sikuli, IO and Cmd + Image and Command entities.
 - Grizzly http server with custom configuration.
 - Services for processing Sikuli, IO and Cmd http requests.
 - CommandLine utility class, that uses commons-exec library for flexible cmd control.
 - RemoteDesktop - SikuliX wrapper for common click / type / exists APIs. Uses observers mechanism to allow flexible elements' waiting.
 - Embedded REST Jersey 2.x client, that implements common interfaces for further sending http requests to remote server.
 - Sample tests, that use embedded resources for bi-directional file transferring, batch files execution and common sikuli actions checking.

Note: batch files' extension in resources should be changed from 'txt' to 'bat' (updated due to security reasons). 
 
Sample REST request for typing text, and appropriate service looks like the following:
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
            CLIENT_LOGGER.severe("Unable to set text '" + text + "' to " + image.getValues());
        }

        response.close();
    }

    @POST
    @Path("/setText")
    public Response setText(final Image image, @QueryParam("text") final String text,
                            @QueryParam("timeout") final int timeout) {
        return Response.status(new RemoteDesktop().setText(image, text, timeout) ?
                Response.Status.OK : Response.Status.NOT_FOUND).build();
    }	
```