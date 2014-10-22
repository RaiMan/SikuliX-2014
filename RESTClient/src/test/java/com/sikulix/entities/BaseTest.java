package com.sikulix.entities;

import com.sikulix.restclient.client.Client;
import org.sikuli.remoteinterfaces.common.Sikulix;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * Author: Sergey Kuts
 */
public class BaseTest {
    
    // You should set real IP, instead of localhost
    public static final String SIKULIX_SERVER_IP = "127.0.0.1";
    public static final int SIKULIX_SERVER_PORT = 4041;
    // Don't forget to change resource batches' extension from .txt to .bat
    public static final String RESOURCE_FILE = "batches/runner.bat";
    public static final String RESOURCE_BUTTON_IMAGE = "images/buttonStart.png";
    public static final String RESOURCE_LABEL_IMAGE = "images/labelAdministrator.png";
    public static final String RESOURCE_INPUT_IMAGE = "images/inputFindFiles.png";
    public static final String EMPTY_FILE = "EmptyFile.txt";
    public static final int WAIT_TIMEOUT = 5;
    public static final float SIMILARITY = 0.9f;

    private static final Logger BASE_TEST_LOGGER = Logger.getLogger(BaseTest.class.getName());

    // In case of tests scalability, you should take care about Sikulix thread-safety!
    private static Sikulix sikuliXClient;

    @BeforeSuite
    public void initClient() {
        if (sikuliXClient == null) {
            sikuliXClient = new Client(SIKULIX_SERVER_IP, SIKULIX_SERVER_PORT);
        }
    }

    @AfterSuite
    public void disposeClient() {
        if (sikuliXClient != null) {
            sikuliXClient.close();
        }
    }

    public static Sikulix getClient() {
        return sikuliXClient;
    }

    public File getResource(final String resourceName) {
        try {
            return new File(ClassLoader.getSystemResource(resourceName).toURI());
        } catch (URISyntaxException e) {
            BASE_TEST_LOGGER.severe("Unable to get URI from resource " + resourceName + ": " + e.getMessage());
            return null;
        }
    }

    public String getResourcePath(final String resourceName) {
        return ClassLoader.getSystemResource(resourceName).getPath();
    }
}
