package com.sikulix.tests;

import com.sikulix.entities.BaseTest;
import com.sikulix.entities.ImageBox;
import org.sikuli.script.Key;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Author: Serhii Kuts
 * Date: 10/23/2014
 * Time: 12:32 PM
 */
public class IntegrationTests extends BaseTest {

    private List<String> localPaths;
    private List<String> remotePaths;

    @BeforeClass
    public void init() {
        final File button = getResource(RESOURCE_BUTTON_IMAGE);
        final File input = getResource(RESOURCE_INPUT_IMAGE);
        final File label = getResource(RESOURCE_LABEL_IMAGE);

        localPaths = Arrays.asList(button.getPath(), input.getPath(), label.getPath());
        remotePaths = Arrays.asList(IMAGES_PATH + "\\" + button.getName(),
                IMAGES_PATH + "\\" + input.getName(), IMAGES_PATH + "\\" + label.getName());

        if (getClient().exists(Arrays.asList(IMAGES_PATH))) {
            getClient().delete(IMAGES_PATH);
        }

        getClient().createFolder(IMAGES_PATH);
    }

    @Test
    public void uploadFilesAndExecuteSikuliScript() throws InterruptedException {
        getClient().uploadFile(localPaths, IMAGES_PATH);
        if (getClient().exists(remotePaths) && remotePaths.size() >= 3) {
            getClient().click(new ImageBox(remotePaths.get(0), SIMILARITY), WAIT_TIMEOUT);
            getClient().setText(new ImageBox(remotePaths.get(1), SIMILARITY), "cmd" + Key.ENTER, WAIT_TIMEOUT);
            assertTrue(getClient().exists(new ImageBox(remotePaths.get(2), SIMILARITY), WAIT_TIMEOUT));
        } else {
            fail();
        }
    }
}
