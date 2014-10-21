package com.sikulix.tests;

import com.sikulix.entities.ImageBox;
import com.sikulix.remoteserver.client.Client;
import com.sikulix.remoteserver.interfaces.common.Sikulix;
import org.sikuli.script.Key;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

/**
 * Author: Sergey Kuts
 */
public class SikuliXTests {

    // You should set real IP, instead of localhost
    private static final String SIKULIX_SERVER_IP = "127.0.0.1";
    private static final int SIKULIX_SERVER_PORT = 4041;
    private static final int WAIT_TIMEOUT = 5;
    private static final float SIMILARITY = 0.9f;

    private Sikulix sikuliXClient;

    @BeforeClass
    public void initClient() {
        sikuliXClient = new Client(SIKULIX_SERVER_IP, SIKULIX_SERVER_PORT);
    }

    @Test
    public void clickStartAndOpenCmd() {
        sikuliXClient.click(new ImageBox(ClassLoader.getSystemResource("buttonStart.png").getPath(), SIMILARITY), WAIT_TIMEOUT);
        sikuliXClient.setText(new ImageBox(ClassLoader.getSystemResource("inputFindFiles.png").getPath(), SIMILARITY),
                "cmd" + Key.ENTER, WAIT_TIMEOUT);

        assertTrue(sikuliXClient.exists(
                new ImageBox(ClassLoader.getSystemResource("labelAdministrator.png").getPath(), SIMILARITY),
                WAIT_TIMEOUT));
    }

    @AfterClass
    public void disposeClient() {
        if (sikuliXClient != null) {
            sikuliXClient.close();
        }
    }
}
