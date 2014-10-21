package com.sikulix.remoteserver.service;

import com.sikulix.remoteserver.interfaces.entities.Command;
import com.sikulix.remoteserver.utils.CommandLineUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Author: Sergey Kuts
 */
@Path("/cmd")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommandLineService {

    @POST
    @Path("/execute")
    public Response execute(final Command command) {
        return Response.status(CommandLineUtils.executeCommandLine(command) != -1 ?
                Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR)
                .build();
    }
}
