package com.sikulix.remoteserver.service;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.logging.Logger;

/**
 * Author: Sergey Kuts
 */
@Path("/file")
public class FileTransferService {

    private static final Logger FILE_TRANSFER_LOGGER = Logger.getLogger(FileTransferService.class.getName());

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(final FormDataMultiPart form, @QueryParam("saveTo") final String saveToPath) {

        Response.Status responseStatus;

        final FormDataBodyPart filePart = form.getField("file");
        final ContentDisposition headerOfFilePart = filePart.getContentDisposition();
        final String filePath = (saveToPath.endsWith("\\") ? saveToPath : saveToPath.concat("\\")) +
                headerOfFilePart.getFileName();

        FILE_TRANSFER_LOGGER.info("Saving " + filePath);

        try (final InputStream inputStream = filePart.getValueAs(InputStream.class);
             final FileOutputStream outputStream = new FileOutputStream(new File(filePath))) {
            IOUtils.copy(inputStream, outputStream);
            outputStream.flush();

            FILE_TRANSFER_LOGGER.info("File " + filePath + " has been saved.");
            responseStatus = Response.Status.OK;
        } catch (NullPointerException | IOException e) {
            FILE_TRANSFER_LOGGER.severe("Unable to save file " + filePath + ": " + e.getMessage());
            responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
        }

        return Response.status(responseStatus).build();
    }

    @Path("/download")
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public StreamingOutput downloadFile(@QueryParam("fromPath") final String fromPath) {
        FILE_TRANSFER_LOGGER.info("Sending " + fromPath);

        return outputStream -> {
            try (final FileInputStream inputStream = new FileInputStream(new File(fromPath))) {
                IOUtils.copy(inputStream, outputStream);
                outputStream.flush();

                FILE_TRANSFER_LOGGER.info("File " + fromPath + " has been sent.");
            } catch (NullPointerException | IOException e) {
                FILE_TRANSFER_LOGGER.severe("An error occurred while stream copying: " + e.getMessage());
            }
        };
    }
}
