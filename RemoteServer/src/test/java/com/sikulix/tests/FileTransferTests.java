package com.sikulix.tests;

import com.sikulix.remoteserver.client.Client;
import com.sikulix.remoteserver.interfaces.common.Sikulix;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.testng.Assert.assertTrue;

/**
 * Author: Sergey Kuts
 */
public class FileTransferTests {

    // You should set real IP, instead of localhost
    private static final String SIKULIX_SERVER_IP = "127.0.0.1";
    private static final int SIKULIX_SERVER_PORT = 4041;
    private static final String RESOURCE_IMAGE = "buttonStart.png";

    private File filePath;
    private File copyToServerPath;
    private File copyToClientPath;
    private File serverFilePath;
    private File clientFilePath;

    private Sikulix sikuliXClient;

    @BeforeClass
    public void init() throws URISyntaxException, IOException {
        sikuliXClient = new Client(SIKULIX_SERVER_IP, SIKULIX_SERVER_PORT);

        filePath = new File(ClassLoader.getSystemResource(RESOURCE_IMAGE).toURI());
        copyToServerPath = new File(filePath.getParent() + "\\server");
        copyToClientPath = new File(filePath.getParent() + "\\client");
        serverFilePath = new File(copyToServerPath.getPath() + "\\" + filePath.getName());
        clientFilePath = new File(copyToClientPath.getPath() + "\\" + filePath.getName());

        if (copyToServerPath.exists()) {
            FileUtils.deleteDirectory(copyToServerPath);
        }

        if (copyToClientPath.exists()) {
            FileUtils.deleteDirectory(copyToClientPath);
        }

        copyToServerPath.mkdir();
        copyToClientPath.mkdir();
    }

    @Test(priority = 1)
    public void uploadFile() {
        sikuliXClient.uploadFile(filePath.getPath(), copyToServerPath.getPath());
        assertTrue(serverFilePath.exists());
    }

    @Test(priority = 2)
    public void downloadFile() {
        sikuliXClient.downloadFile(serverFilePath.getPath(), copyToClientPath.getPath());
        assertTrue(clientFilePath.exists());
    }

    @AfterClass
    public void disposeClient() {
        if (sikuliXClient != null) {
            sikuliXClient.close();
        }
    }
}
