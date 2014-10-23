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
import java.util.List;
import java.util.logging.Logger;

import static org.apache.commons.lang.StringEscapeUtils.escapeJava;

/**
 * Author: Sergey Kuts
 */
public class Client implements Sikulix {

    private javax.ws.rs.client.Client client;
    private WebTarget service;

    private String ip;

    private static final Logger CLIENT_LOGGER = Logger.getLogger(Client.class.getName());

    public Client(final String ip, final int port) {
        this.ip = ip;
        this.client = ClientBuilder.newBuilder()
                .register(ObjectMapperProvider.class)
                .register(JacksonFeature.class)
                .register(MultiPartFeature.class)
                .build();

        this.service = this.client.target("http://" + this.ip + ":" + port + "/sikuli");
    }

    public void executeCommandLine(final Command command) {
        final Response response = service.path("cmd")
                .path("execute")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(command));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            CLIENT_LOGGER.info("The following process has been finished on " + ip + ": " + command);
        } else {
            CLIENT_LOGGER.severe("Unable to finish the following process on " + ip + ": " + command);
        }

        response.close();
    }

    public boolean exists(final List<String> paths) {
        final Response response = service.path("file")
                .path("exists")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(paths, MediaType.APPLICATION_JSON_TYPE));

        final boolean exists = response.getStatus() == Response.Status.OK.getStatusCode();

        if (exists) {
            CLIENT_LOGGER.info("The following file(-s) or folder(-s) exists on " + ip + ": " + paths);
        } else {
            CLIENT_LOGGER.severe("The following file(-s) or folder(-s) doesn't exist on " + ip + ": " + paths);
        }

        response.close();

        return exists;
    }

    public void delete(final String path) {
        final Response response = service.path("file")
                .path("delete")
                .queryParam("path", path)
                .request(MediaType.APPLICATION_JSON)
                .post(null);

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            CLIENT_LOGGER.info("The following file or folder has been deleted from " + ip + ": " + path);
        } else {
            CLIENT_LOGGER.severe("Unable to delete " + path + " from " + ip);
        }

        response.close();
    }

    public void createFolder(final String path) {
        final Response response = service.path("file")
                .path("createFolder")
                .queryParam("path", path)
                .request(MediaType.APPLICATION_JSON)
                .post(null);

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            CLIENT_LOGGER.info(path + " has been created on " + ip);
        } else {
            CLIENT_LOGGER.severe("Unable to create " + path + " on " + ip);
        }

        response.close();
    }

    public void uploadFile(final List<String> filesPath, final String saveToPath) {
        final MultiPart multiPart = new MultiPart(MediaType.MULTIPART_FORM_DATA_TYPE);

        for (String path : filesPath) {
            multiPart.bodyPart(new FileDataBodyPart("file", new File(path), MediaType.APPLICATION_OCTET_STREAM_TYPE));
        }

        final Response response = service.path("file")
                .path("upload")
                .queryParam("saveTo", saveToPath)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(multiPart, multiPart.getMediaType()));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            CLIENT_LOGGER.info("File(-s) " + filesPath + " has been saved to " + saveToPath + " on " + ip);
        } else {
            CLIENT_LOGGER.severe("Unable to save file(-s) " + filesPath + " to " + saveToPath + " on " + ip);
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

            CLIENT_LOGGER.info("File " + downloadFilePath + " has been saved to " + saveToPath + " on " + ip);
        } catch (NullPointerException | IOException e) {
            CLIENT_LOGGER.severe("Unable to save a file " + downloadFilePath + " from " + ip +
                    " to " + saveToPath + " on local VM: " + e.getMessage());
        }
    }

    public void click(final Image image, final int timeout) {
        final Response response = service.path("image")
                .path("click")
                .queryParam("timeout", timeout)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(image));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            CLIENT_LOGGER.info("Image " + image + " has been clicked on " + ip);
        } else {
            CLIENT_LOGGER.severe("Unable to click image " + image +  " on " + ip);
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
            CLIENT_LOGGER.info("Image " + image + " exists on " + ip);
        } else {
            CLIENT_LOGGER.severe("Unable to find image " + image + " on " + ip);
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
            CLIENT_LOGGER.info("Text '" + escapeJava(text) + "' has been set to " + image + " on " + ip);
        } else {
            CLIENT_LOGGER.severe("Unable to set text '" + escapeJava(text) + "' to " + image + " on " + ip);
        }

        response.close();
    }

    public void close() {
        client.close();
    }
}