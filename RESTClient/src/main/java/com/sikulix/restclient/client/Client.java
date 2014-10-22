package com.sikulix.restclient.client;

import org.sikuli.remoteinterfaces.common.Sikulix;
import org.sikuli.remoteinterfaces.entities.Command;
import org.sikuli.remoteinterfaces.entities.Image;
import com.sikulix.restclient.utils.ObjectMapperProvider;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.*;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static org.apache.commons.lang.StringEscapeUtils.escapeJava;
/**
 * Author: Sergey Kuts
 */
public class Client implements Sikulix {

    private javax.ws.rs.client.Client client;
    private WebTarget service;

    private static final Logger CLIENT_LOGGER = Logger.getLogger(Client.class.getName());

    public Client(final String ip, final int port) {
        client = ClientBuilder.newBuilder()
                .register(ObjectMapperProvider.class)
                .register(JacksonFeature.class)
                .register(MultiPartFeature.class)
                .build();

        service = client.target("http://" + ip + ":" + port + "/sikuli");
    }

    public void executeCommandLine(final Command command) {
        final Response response = service.path("cmd")
                .path("execute")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(command));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            CLIENT_LOGGER.info("The following process has been finished: " + command);
        } else {
            CLIENT_LOGGER.severe("Unable to finish the following process: " + command);
        }

        CLIENT_LOGGER.info("Status: " + response.getStatus());

        response.close();
    }

    public void uploadFile(final String filePath, final String saveToPath) {
        final MultiPart multiPart = new MultiPart(MediaType.MULTIPART_FORM_DATA_TYPE)
                .bodyPart(new FileDataBodyPart("file", new File(filePath), MediaType.APPLICATION_OCTET_STREAM_TYPE));

        final Response response = service.path("file")
                .path("upload")
                .queryParam("saveTo", saveToPath)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(multiPart, multiPart.getMediaType()));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            CLIENT_LOGGER.info("File " + filePath + " has been saved to " + saveToPath);
        } else {
            CLIENT_LOGGER.severe("Unable to save a file " + filePath + " to " + saveToPath);
        }

        response.close();
    }

    public void downloadFile(final String downloadFilePath, final String saveToPath) {
        final String filePath = (saveToPath.endsWith("\\") ? saveToPath : saveToPath.concat("\\")) +
                Paths.get(downloadFilePath).getFileName();

        try (final InputStream inputStream = service.path("file")
                .path("download")
                .queryParam("fromPath", downloadFilePath)
                .request()
                .get(InputStream.class);
             final FileOutputStream fileOutputStream = new FileOutputStream(new File(filePath))) {

            IOUtils.copy(inputStream, fileOutputStream);
            fileOutputStream.flush();

            CLIENT_LOGGER.info("File " + downloadFilePath + " has been saved to " + saveToPath);
        } catch (NullPointerException | IOException e) {
            CLIENT_LOGGER.severe("Unable to save a file " + downloadFilePath + " to " + saveToPath + ": " + e.getMessage());
        }
    }

    public void click(final Image image, final int timeout) {
        final Response response = service.path("image")
                .path("click")
                .queryParam("timeout", timeout)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(image));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            CLIENT_LOGGER.info("Image " + image + " has been clicked.");
        } else {
            CLIENT_LOGGER.severe("Unable to click image " + image);
        }

        response.close();
    }

    public boolean exists(final Image image, final int timeout) {
        final Response response = service.path("image")
                .path("exists")
                .queryParam("timeout", timeout)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(image));

        final boolean exists = response.getStatus() == Response.Status.OK.getStatusCode();
        response.close();

        if (exists) {
            CLIENT_LOGGER.info("Image " + image + " exists.");
        } else {
            CLIENT_LOGGER.severe("Unable to find image " + image);
        }

        return exists;
    }

    public void setText(final Image image, final String text, final int timeout) {
        final Response response = service.path("image")
                .path("setText")
                .queryParam("text", text)
                .queryParam("timeout", timeout)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(image));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            CLIENT_LOGGER.info("Text '" + escapeJava(text) + "' has been set to " + image);
        } else {
            CLIENT_LOGGER.severe("Unable to set text '" + escapeJava(text) + "' to " + image);
        }

        response.close();
    }

    public void close() {
        client.close();
    }
}