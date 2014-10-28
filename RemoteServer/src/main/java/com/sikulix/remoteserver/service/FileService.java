package com.sikulix.remoteserver.service;

import com.sikulix.remoteserver.utils.FileUtility;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.apache.commons.io.FilenameUtils.separatorsToSystem;

/**
 * Author: Sergey Kuts
 */
@Path("/file")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FileService {

    private static final Logger IO_LOGGER = Logger.getLogger(FileService.class.getName());

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(final FormDataMultiPart form, @QueryParam("saveTo") final String saveToPath) {

        final List<Response.Status> responses = new ArrayList<>();

        for (FormDataBodyPart filePart : form.getFields("file")) {
            final String filePath = (saveToPath.endsWith("\\") ? saveToPath : saveToPath.concat("\\")) +
                    filePart.getContentDisposition().getFileName();

            try (final InputStream inputStream = filePart.getValueAs(InputStream.class);
                 final FileOutputStream outputStream = new FileOutputStream(new File(separatorsToSystem(filePath)))) {
                IOUtils.copy(inputStream, outputStream);
                outputStream.flush();

                IO_LOGGER.info("File " + separatorsToSystem(filePath) + " has been saved.");
                responses.add(Response.Status.OK);
            } catch (NullPointerException | IOException e) {
                IO_LOGGER.severe("Unable to save file " + separatorsToSystem(filePath) + ": " + e.getMessage());
                responses.add(Response.Status.INTERNAL_SERVER_ERROR);
            }
        }

        return Response.status(responses.contains(Response.Status.INTERNAL_SERVER_ERROR) ?
                Response.Status.INTERNAL_SERVER_ERROR : Response.Status.OK).build();
    }

    @Path("/download")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput downloadFile(@QueryParam("fromPath") final String fromPath) {
        return new StreamingOutput() {
            @Override
            public void write(final OutputStream outputStream) {
                try (final FileInputStream inputStream = new FileInputStream(new File(separatorsToSystem(fromPath)))) {
                    IOUtils.copy(inputStream, outputStream);
                    outputStream.flush();

                    IO_LOGGER.info("File " + separatorsToSystem(fromPath) + " has been sent.");
                } catch (NullPointerException | IOException e) {
                    IO_LOGGER.severe("An error occurred while stream copying: " + e.getMessage());
                }
            }
        };
    }
    
    @POST
    @Path("/delete")
    public Response delete(@QueryParam("path") final String path) {
        return Response.status(FileUtility.delete(path) ? Response.Status.OK : Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/exists")
    public Response exists(final List<String> paths) {
        return Response.status(FileUtility.exists(paths) ? Response.Status.OK : Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/createFolder")
    public Response createFolder(@QueryParam("path") final String path) {
        return Response.status(FileUtility.createFolder(path) ? Response.Status.OK : Response.Status.NOT_FOUND).build();
    }
}
