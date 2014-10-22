package com.sikulix.remoteserver.service;

import org.sikuli.remoteinterfaces.entities.Image;
import com.sikulix.remoteserver.wrapper.RemoteDesktop;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Author: Sergey Kuts
 */
@Path("/image")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ImageService {

    @POST
    @Path("/click")
    public Response click(final Image image, @QueryParam("timeout") final int timeout) {
        return Response.status(new RemoteDesktop().click(image, timeout) ?
                Response.Status.OK : Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/setText")
    public Response setText(final Image image, @QueryParam("text") final String text,
                            @QueryParam("timeout") final int timeout) {
        return Response.status(new RemoteDesktop().setText(image, text, timeout) ?
                Response.Status.OK : Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/exists")
    public Response exists(final Image image, @QueryParam("timeout") final int timeout) {
        return Response.status(new RemoteDesktop().exists(image, timeout) ?
                Response.Status.OK : Response.Status.NOT_FOUND).build();
    }
}
