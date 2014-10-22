package com.sikulix.tests;

import com.sikulix.entities.BaseTest;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.testng.Assert.assertTrue;

/**
 * Author: Sergey Kuts
 */
public class FileTransferTests extends BaseTest {

    private File filePath;
    private File copyToServerPath;
    private File copyToClientPath;
    private File serverFilePath;
    private File clientFilePath;

    @BeforeClass
    public void init() throws URISyntaxException, IOException {

        filePath = getResource(RESOURCE_BUTTON_IMAGE);
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
        getClient().uploadFile(filePath.getPath(), copyToServerPath.getPath());
        assertTrue(serverFilePath.exists());
    }

    @Test(priority = 2)
    public void downloadFile() {
        getClient().downloadFile(serverFilePath.getPath(), copyToClientPath.getPath());
        assertTrue(clientFilePath.exists());
    }
}
