package com.sikulix.tests;

import com.sikulix.entities.BaseTest;
import com.sikulix.entities.CommandLineBox;
import com.sikulix.entities.ImageBox;
import org.apache.commons.io.FileUtils;
import org.sikuli.script.Key;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.testng.Assert.*;

public class LinuxTests extends BaseTest {

    private static final File SERVER_PATH = new File(System.getProperty("user.home") + "/server");
    private static final File CLIENT_PATH = new File(System.getProperty("user.home") + "/client");
    private static final File SH_SCRIPT = getResource(RESOURCE_SH_SCRIPT);

    @Test(priority = 1)
    public void recreateFolders() throws IOException {
        if (getClient().exists(Arrays.asList(SERVER_PATH.getPath()))) {
            getClient().delete(SERVER_PATH.getPath());
        }

        if (CLIENT_PATH.exists()) {
            FileUtils.forceDelete(CLIENT_PATH);
        }

        assertFalse(getClient().exists(Arrays.asList(SERVER_PATH.getPath())));
        assertFalse(CLIENT_PATH.exists());

        getClient().createFolder(SERVER_PATH.getPath());
        FileUtils.forceMkdir(CLIENT_PATH);

        assertTrue(getClient().exists(Arrays.asList(SERVER_PATH.getPath())));
        assertTrue(CLIENT_PATH.exists());
    }

    @Test(priority = 2)
    public void uploadFileToServer() {
        getClient().uploadFile(Arrays.asList(SH_SCRIPT.getPath()), SERVER_PATH.getPath());
        assertTrue(getClient().exists(Arrays.asList(SERVER_PATH.getPath() + "/" + SH_SCRIPT.getName())));
    }

    @Test(priority = 3)
    public void executeScript() {
        getClient().executeCommandLine(
                new CommandLineBox("sh", Arrays.asList(SERVER_PATH.getPath() + "/" + SH_SCRIPT.getName()), WAIT_TIMEOUT));
        assertTrue(getClient().exists(Arrays.asList(SERVER_PATH.getPath() + "/" + EMPTY_FILE)));
    }

    @Test(priority = 4)
    public void downloadFileFromServer() {
        getClient().downloadFile(SERVER_PATH.getPath() + "/" + EMPTY_FILE, CLIENT_PATH.getPath());
        assertTrue(new File(CLIENT_PATH.getPath() + "/" + EMPTY_FILE).exists());
    }

    @Test(priority = 5)
    public void callFirefoxFromTerminal() {
        getClient().click(new ImageBox(getResource(RESOURCE_TERMINAL_IMAGE).getPath(), SIMILARITY), 3);
        getClient().setText(new ImageBox(getResource(RESOURCE_INPUT_TERMINAL_IMAGE).getPath(), SIMILARITY),
                "firefox" + Key.ENTER, 3);
        getClient().exists(new ImageBox(getResource(RESOURCE_LABEL_FF_IMAGE).getPath(), SIMILARITY), 5);
    }
}
