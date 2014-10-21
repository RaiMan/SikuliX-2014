package com.sikulix.tests;

import com.sikulix.entities.CommandLineBox;
import com.sikulix.remoteserver.client.Client;
import com.sikulix.remoteserver.interfaces.common.Sikulix;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import static org.testng.Assert.assertTrue;

/**
 * Author: Sergey Kuts
 */
public class CommandLineTests {

    // You should set real IP, instead of localhost
    private static final String SIKULIX_SERVER_IP = "127.0.0.1";
    private static final int SIKULIX_SERVER_PORT = 4041;
    private static final String RESOURCE_FILE = "runner.bat";
    private static final String EMPTY_FILE = "EmptyFile.txt";
    private static final int WAIT_TIMEOUT = 5;

    private File filePath;
    private File emptyFile;

    private Sikulix sikuliXClient;

    @BeforeClass
    public void init() throws URISyntaxException, IOException {
        sikuliXClient = new Client(SIKULIX_SERVER_IP, SIKULIX_SERVER_PORT);
        filePath = new File(ClassLoader.getSystemResource(RESOURCE_FILE).toURI());
        emptyFile = new File(filePath.getParent() + "\\" + EMPTY_FILE);

        if (emptyFile.exists()) {
            FileUtils.forceDelete(emptyFile);
        }
    }

    @Test
    public void runBatchFromCommandLine() {
        sikuliXClient.executeCommandLine(new CommandLineBox(filePath.getPath(), Arrays.asList("arg1", "arg2", "arg3"), WAIT_TIMEOUT));
        assertTrue(emptyFile.exists());
    }

    @AfterClass
    public void disposeClient() {
        if (sikuliXClient != null) {
            sikuliXClient.close();
        }
    }
}
