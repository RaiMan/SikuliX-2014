package com.sikulix.tests;

import com.sikulix.entities.BaseTest;
import com.sikulix.entities.CommandLineBox;
import org.apache.commons.io.FileUtils;
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
public class CommandLineTests extends BaseTest {

    private File filePath;
    private File emptyFile;

    @BeforeClass
    public void init() throws URISyntaxException, IOException {
        filePath = getResource(RESOURCE_FILE);
        emptyFile = new File(filePath.getParent() + "\\" + EMPTY_FILE);

        if (emptyFile.exists()) {
            FileUtils.forceDelete(emptyFile);
        }
    }

    // Don't forget to change resource batches' extension from .txt to .bat
    @Test
    public void runBatchFromCommandLine() {
        getClient().executeCommandLine(new CommandLineBox(filePath.getPath(), Arrays.asList("arg1", "arg2", "arg3"), WAIT_TIMEOUT));
        assertTrue(emptyFile.exists());
    }
}
