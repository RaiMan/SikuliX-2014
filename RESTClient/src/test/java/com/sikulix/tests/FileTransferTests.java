package com.sikulix.tests;

import com.sikulix.entities.BaseTest;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * Author: Sergey Kuts
 */
public class FileTransferTests extends BaseTest {

    private File button;
    private File input;
    private File label;

    private List<String> localPaths;
    private List<String> remotePaths;

    @BeforeClass
    public void init() throws IOException {
        button = getResource(RESOURCE_BUTTON_IMAGE);
        input = getResource(RESOURCE_INPUT_IMAGE);
        label = getResource(RESOURCE_LABEL_IMAGE);

        localPaths = Arrays.asList(button.getPath(), input.getPath(), label.getPath());
        remotePaths = Arrays.asList(IMAGES_PATH + "\\" + button.getName(),
                IMAGES_PATH + "\\" + input.getName(), IMAGES_PATH + "\\" + label.getName());

        if (getClient().exists(Arrays.asList(IMAGES_PATH))) {
            getClient().delete(IMAGES_PATH);
        }

        getClient().createFolder(IMAGES_PATH);

        final File localFolder = new File(IMAGES_PATH);
        if (localFolder.exists()) {
            FileUtils.forceDelete(localFolder);
        }

        FileUtils.forceMkdir(localFolder);
    }

    @Test(priority = 1)
    public void uploadFiles() {
        getClient().uploadFile(localPaths, IMAGES_PATH);
        assertTrue(getClient().exists(remotePaths));
    }

    @Test(priority = 2)
    public void downloadFile() {
        getClient().downloadFile(remotePaths.get(0), IMAGES_PATH);
        assertTrue(new File(IMAGES_PATH + "\\" + button.getName()).exists());
    }
}
